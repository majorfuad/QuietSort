package com.fuad.quietsort

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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
import com.fuad.quietsort.data.local.EncryptedDatabaseVerifier
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var verifier: EncryptedDatabaseVerifier
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { QuietSortHome(verifier) }
    }
}

@Composable
private fun QuietSortHome(verifier: EncryptedDatabaseVerifier) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var storageResult by remember { mutableStateOf("Not checked") }
    var accessGranted by remember { mutableStateOf(NotificationAccess.isGranted(context)) }
    val diagnostics by ListenerDiagnostics.state.collectAsState()

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
                modifier = Modifier.fillMaxSize().padding(24.dp),
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
                    "QuietSort works offline. No notification content is saved during this setup phase.",
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
                Text("Listener: " + when {
                    !accessGranted -> "Access disabled"
                    diagnostics.connected -> "Connected"
                    else -> "Not connected"
                }, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Events observed this session: posted ${diagnostics.postedEvents}, removed ${diagnostics.removedEvents}, ranking updates ${diagnostics.rankingUpdates}",
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
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
                Text("Real notification history is not being stored yet.",
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
    }
}
