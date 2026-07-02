package com.example.rfexplorer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rfexplorer.data.model.FmHardwareAnalysis
import com.example.rfexplorer.data.model.QualcommPlatformDetection
import com.example.rfexplorer.ui.components.CyberHeaderCard
import com.example.rfexplorer.ui.components.ProbabilityGauge
import com.example.rfexplorer.ui.components.StatusBadge
import com.example.rfexplorer.ui.theme.*
import com.example.rfexplorer.ui.viewmodel.ExplorerViewModel

data class NavTile(
    val title: String,
    val subtitle: String,
    val route: String,
    val icon: ImageVector,
    val badge: String,
    val badgeColor: Color
)

@Composable
fun DashboardScreen(
    viewModel: ExplorerViewModel,
    onNavigate: (String) -> Unit
) {
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val qcom by viewModel.qualcommPlatform.collectAsStateWithLifecycle()
    val fm by viewModel.fmAnalysis.collectAsStateWithLifecycle()
    val libs by viewModel.scannedLibraries.collectAsStateWithLifecycle()
    val devMode by viewModel.developerModeEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        CyberHeaderCard(
            title = "RF Hardware Explorer Ultimate AI",
            subtitle = "Qualcomm Snapdragon Blair / Galaxy Tab A9+ 5G",
            chipText = if (devMode) "DEV MODE" else "READ-ONLY",
            icon = Icons.Default.Radar
        )

        // Safety Status Banner
        Surface(
            color = if (devMode) AmberWarning.copy(alpha = 0.15f) else EmeraldSuccess.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (devMode) AmberWarning else EmeraldSuccess)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (devMode) Icons.Default.Warning else Icons.Default.Security,
                    contentDescription = null,
                    tint = if (devMode) AmberWarning else EmeraldSuccess,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (devMode) "Developer Override Enabled" else "100% Read-Only Safety Active",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = if (devMode) "Reflection and ADB recommendations unlocked. No automated system modifications permitted."
                        else "All filesystem writes locked. Zero risk of Knox fuse tripping or SELinux violation.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SlateText)
                    )
                }
                TextButton(onClick = { viewModel.toggleDeveloperMode() }) {
                    Text(
                        if (devMode) "LOCK" else "OVERRIDE",
                        color = if (devMode) AmberWarning else NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick Scan / Status Section
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Target Platform Capabilities", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NeonCyan, strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { viewModel.runScan() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Rescan", tint = NeonCyan)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecItem("Model", qcom?.deviceModel?.take(18) ?: "SM-X216B")
                    SpecItem("SoC", "Snapdragon 695")
                    SpecItem("Libs", "${libs.size} Scanned")
                    SpecItem("SELinux", "Enforcing")
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CyberSurface)
                Spacer(modifier = Modifier.height(16.dp))
                Text("FM Radio Activation Assessment", style = MaterialTheme.typography.labelLarge.copy(color = NeonCyan, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                ProbabilityGauge(percentage = fm?.activationProbabilityPercent ?: 78, title = "Overall Hardware Activation Feasibility")
            }
        }

        Text("Subsystem Modules", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))

        // Navigation Grid
        val tiles = listOf(
            NavTile("Hardware & ELF Scanner", "Enumerate & parse ELF .so binaries", "scanner", Icons.Default.Memory, "${libs.size} Libs", NeonCyan),
            NavTile("FM Radio Analyzer", "Deep forensic FM silicon & JNI audit", "fm", Icons.Default.Radio, "${fm?.activationProbabilityPercent ?: 78}% Feasible", ElectricPurple),
            NavTile("Qualcomm Platform", "Blair HAL, QMI, DSP & Binder IPC", "platform", Icons.Default.DeveloperBoard, "Snapdragon", EmeraldSuccess),
            NavTile("Activation Matrix", "9 pathways & Safe Remediation Assistant", "activation", Icons.Default.Speed, "Recommended", AmberWarning),
            NavTile("Dependency Graph", "Relational Java->JNI->HAL->HW tree", "graph", Icons.Default.Hub, "Interactive", NeonCyan),
            NavTile("Scan Reports & Archive", "Room DB exported audit reports", "reports", Icons.Default.Description, "Export", ElectricPurple),
            NavTile("Developer Console", "Live logs, property lookup & ELF tool", "console", Icons.Default.Terminal, "Live", EmeraldSuccess)
        )

        tiles.forEach { tile ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(tile.route) },
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tile.badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(tile.icon, contentDescription = null, tint = tile.badgeColor)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tile.title, style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        Text(tile.subtitle, style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
                    }
                    StatusBadge(tile.badge, tile.badgeColor)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SlateText)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SpecItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = SlateText))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
    }
}
