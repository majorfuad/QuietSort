package com.fuad.quietsort

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fuad.quietsort.feature.onboarding.NotificationAccess
import com.fuad.quietsort.capture.ListenerDiagnostics
import com.fuad.quietsort.capture.CaptureHealthEvaluator
import java.text.DateFormat
import java.util.Date
import com.fuad.quietsort.data.local.EncryptedDatabaseVerifier
import com.fuad.quietsort.data.preferences.PrivacyPreferences
import com.fuad.quietsort.data.repository.NotificationHistoryRepository
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var verifier: EncryptedDatabaseVerifier
    @Inject lateinit var preferences: PrivacyPreferences
    @Inject lateinit var history: NotificationHistoryRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { QuietSortHome(verifier, preferences, history) }
    }
}

@Composable
private fun QuietSortHome(
    verifier: EncryptedDatabaseVerifier,
    preferences: PrivacyPreferences,
    history: NotificationHistoryRepository
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var storageResult by remember { mutableStateOf("Not checked") }
    var accessGranted by remember { mutableStateOf(NotificationAccess.isGranted(context)) }
    val diagnostics by ListenerDiagnostics.state.collectAsState()
    val captureEnabled by preferences.isCaptureEnabled.collectAsState(initial = false)
    val savedCount by history.capturedCount.collectAsState(initial = 0)
    val savedEvents by history.eventCount.collectAsState(initial = 0)
    val health = CaptureHealthEvaluator.evaluate(accessGranted, captureEnabled, diagnostics)
    fun formatted(timestamp: Long?): String = timestamp?.let {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date(it))
    } ?: "Not recorded this session"

    DisposableEffect(owner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessGranted = NotificationAccess.isGranted(context)
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("QuietSort", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                Text("Your notifications. Your device. No cloud.",
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(24.dp))
                Text(if (accessGranted) "Notification access granted"
                     else "Notification access not granted",
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Android notification access lets QuietSort read notifications from other apps. " +
                    "This can include personal messages, one-time codes and financial alerts. " +
                    "QuietSort works offline. Saving is optional, and records are encrypted on this device.",
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = {
                    val settingsIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    try {
                        context.startActivity(settingsIntent)
                    } catch (_: ActivityNotFoundException) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                }) {
                    Text(if (accessGranted) "Manage notification access" else "Grant notification access")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = {
                    accessGranted = NotificationAccess.isGranted(context)
                }) { Text("Refresh access status") }
                Spacer(Modifier.height(24.dp))
                Text("Capture health: ${health.headline}", style = MaterialTheme.typography.titleMedium)
                Text(health.detail, style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("Listener: " + when {
                    !accessGranted -> "Access disabled"
                    diagnostics.connected -> "Connected"
                    else -> "Not connected"
                }, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Events observed this session: posted ${diagnostics.postedEvents}, removed ${diagnostics.removedEvents}, ranking updates ${diagnostics.rankingUpdates}",
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Save notifications locally", modifier = Modifier.weight(1f))
                    Switch(
                        checked = captureEnabled,
                        enabled = accessGranted,
                        onCheckedChange = { enabled -> scope.launch { preferences.setCaptureEnabled(enabled) } }
                    )
                }
                Text("Local capture is optional. Stored records are encrypted on this device.",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Saved notifications: $savedCount  •  history events: $savedEvents",
                    style = MaterialTheme.typography.bodySmall)
                Text("Storage errors: ${diagnostics.persistenceFailures}, queue drops: ${diagnostics.droppedFromQueue}",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Connected at: ${formatted(diagnostics.lastConnectedAtMs)}",
                    style = MaterialTheme.typography.bodySmall)
                Text("Disconnected at: ${formatted(diagnostics.lastDisconnectedAtMs)}",
                    style = MaterialTheme.typography.bodySmall)
                Text("Last notification event: ${formatted(diagnostics.lastObservedAtMs)}",
                    style = MaterialTheme.typography.bodySmall)
                Text("Last saved event: ${formatted(diagnostics.lastSavedAtMs)}",
                    style = MaterialTheme.typography.bodySmall)
                Text("Successful processing operations this session: ${diagnostics.savedEvents}",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Troubleshooting: check Notification access in Settings; verify local capture is ON; trigger a test notification from another app. Android may delay or suppress some callbacks.",
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = {
                    scope.launch {
                        storageResult = "Checking encrypted storage..."
                        storageResult = try { verifier.verify() }
                        catch (error: Exception) { "Storage test failed: " + (error.message ?: "Unknown error") }
                    }
                }) { Text("Verify encrypted storage") }
                Text(storageResult, style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text("Only notifications received while local capture is enabled can be saved.",
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
    }
}
