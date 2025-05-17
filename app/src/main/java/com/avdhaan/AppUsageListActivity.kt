package com.avdhaan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.avdhaan.db.AppDatabase
import com.avdhaan.db.AppUsageSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppUsageListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = AppDatabase.getInstance(this).appUsageDao()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val coroutineScope = rememberCoroutineScope()
                    var usageSummaries by remember { mutableStateOf(listOf<AppUsageSummary>()) }

                    LaunchedEffect(Unit) {
                        coroutineScope.launch(Dispatchers.IO) {
                            usageSummaries = dao.getTotalUsageSummary()
                        }
                    }

                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(usageSummaries) { summary ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(text = summary.packageName, style = MaterialTheme.typography.titleMedium)
                                Text(text = formatDuration(summary.totalDuration), style = MaterialTheme.typography.bodySmall)
                                summary.startOfPeriod?.let {
                                    Text(text = "Period: $it", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun formatDuration(durationMillis: Long): String {
        val seconds = durationMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return "${hours}h ${minutes % 60}m ${seconds % 60}s"
    }
}