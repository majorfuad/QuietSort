package com.fuad.quietsort

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { QuietSortHome() }
    }
}

@Composable
private fun QuietSortHome() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("QuietSort", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                Text("Offline Notification Organizer", textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(24.dp))
                Text("Privacy-first notification intelligence. Capture is not enabled yet.",
                    textAlign = TextAlign.Center)
            }
        }
    }
}
