package com.example.rfexplorer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rfexplorer.ui.components.CyberHeaderCard
import com.example.rfexplorer.ui.components.StatusBadge
import com.example.rfexplorer.ui.theme.*
import com.example.rfexplorer.ui.viewmodel.ExplorerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperConsoleScreen(viewModel: ExplorerViewModel) {
    val logs by viewModel.developerLogs.collectAsStateWithLifecycle()
    val props by viewModel.systemProperties.collectAsStateWithLifecycle()
    val devMode by viewModel.developerModeEnabled.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0 = Live Logs, 1 = Property Searcher
    var propQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        CyberHeaderCard(
            title = "Engineering Diagnostic Console",
            subtitle = "Live scanner telemetry stream & system property lookup",
            chipText = if (devMode) "OVERRIDE ON" else "LOCKED",
            icon = Icons.Default.Terminal
        )
        Spacer(modifier = Modifier.height(14.dp))

        // Tab selection
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = CyberCard,
            contentColor = NeonCyan
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("LIVE TELEMETRY LOGS (${logs.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("SYSTEM PROPERTY SEARCH", fontWeight = FontWeight.Bold) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (activeTab == 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberObsidian),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { logLine ->
                        val logColor = when {
                            logLine.contains("[SUCCESS]") -> EmeraldSuccess
                            logLine.contains("[SEC]") || logLine.contains("[WARN]") -> AmberWarning
                            logLine.contains("[ERR]") -> CrimsonAlert
                            logLine.contains("[DEV]") -> ElectricPurple
                            else -> SlateText
                        }
                        Text(logLine, style = MaterialTheme.typography.bodySmall.copy(color = logColor, fontFamily = FontFamily.Monospace))
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = propQuery,
                    onValueChange = { propQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Filter properties (.e.g., ro.vendor, fm, radio)...", color = SlateText) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                val filteredProps = props.filter {
                    propQuery.isBlank() || it.key.contains(propQuery, ignoreCase = true) || it.value.contains(propQuery, ignoreCase = true)
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredProps) { p ->
                        Card(colors = CardDefaults.cardColors(containerColor = CyberCard), shape = RoundedCornerShape(8.dp)) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(p.key, style = MaterialTheme.typography.titleSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), modifier = Modifier.weight(1f))
                                    StatusBadge(p.sourceCategory, ElectricPurple)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(p.value, style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontFamily = FontFamily.Monospace))
                            }
                        }
                    }
                }
            }
        }
    }
}
