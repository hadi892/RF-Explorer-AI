package com.example.rfexplorer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rfexplorer.ui.components.CyberHeaderCard
import com.example.rfexplorer.ui.components.ExpandableSection
import com.example.rfexplorer.ui.components.StatusBadge
import com.example.rfexplorer.ui.theme.*
import com.example.rfexplorer.ui.viewmodel.ExplorerViewModel

@Composable
fun PlatformScreen(viewModel: ExplorerViewModel) {
    val qcom by viewModel.qualcommPlatform.collectAsStateWithLifecycle()
    val hals by viewModel.halInterfaces.collectAsStateWithLifecycle()
    val binders by viewModel.binderServices.collectAsStateWithLifecycle()
    val props by viewModel.systemProperties.collectAsStateWithLifecycle()
    val kernel by viewModel.kernelInfo.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CyberHeaderCard(
            title = "Qualcomm Platform Internals",
            subtitle = "Blair SM6375 / Hexagon DSP / QMI Nodes / Binder Daemons",
            chipText = "QTI BLAIR",
            icon = Icons.Default.DeveloperBoard
        )

        // Kernel Spec Card
        Card(colors = CardDefaults.cardColors(containerColor = CyberCard), shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Kernel Capability Inspector", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))
                Text(kernel?.kernelVersionString ?: "Linux version 5.15.148-qgki-samsung", style = MaterialTheme.typography.bodySmall.copy(color = SlateText, fontFamily = FontFamily.Monospace))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecItem("Architecture", kernel?.architecture ?: "ARM64")
                    SpecItem("SELinux Status", kernel?.selinuxMode ?: "Enforcing")
                }
            }
        }

        // QMI / Subsystem Nodes
        ExpandableSection(
            title = "Qualcomm Hardware Subsystems & Nodes",
            icon = Icons.Default.Hub,
            badgeText = "SM6375",
            badgeColor = NeonCyan,
            initiallyExpanded = true
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Detected Device Nodes:", style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold))
                qcom?.qmiDeviceNodes?.forEach { node ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(node, style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontFamily = FontFamily.Monospace))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Vendor Daemons Active:", style = MaterialTheme.typography.labelSmall.copy(color = ElectricPurple, fontWeight = FontWeight.Bold))
                qcom?.vendorDaemonsDetected?.forEach { d ->
                    Text("• $d", style = MaterialTheme.typography.bodySmall.copy(color = SlateText, fontFamily = FontFamily.Monospace))
                }
            }
        }

        // HAL Explorer
        ExpandableSection(
            title = "Hardware Abstraction Layers (${hals.size})",
            icon = Icons.Default.Hardware,
            badgeText = "HIDL/AIDL",
            badgeColor = ElectricPurple
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                hals.forEach { hal ->
                    Card(colors = CardDefaults.cardColors(containerColor = CyberSurface), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(hal.name, style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), modifier = Modifier.weight(1f))
                                StatusBadge(hal.type.name, NeonCyan)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Instance: ${hal.versionOrInstance}", style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
                                Text("Clients: ${hal.clientCount}", style = MaterialTheme.typography.bodySmall.copy(color = EmeraldSuccess))
                            }
                        }
                    }
                }
            }
        }

        // Binder Explorer
        ExpandableSection(
            title = "Binder IPC Services (${binders.size})",
            icon = Icons.Default.SwapHoriz,
            badgeText = "SERVICES",
            badgeColor = EmeraldSuccess
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                binders.forEach { binder ->
                    Card(colors = CardDefaults.cardColors(containerColor = CyberSurface), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(binder.serviceName, style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), modifier = Modifier.weight(1f))
                                if (binder.isHiddenSystem) StatusBadge("HIDDEN", AmberWarning)
                            }
                            Text(binder.interfaceName, style = MaterialTheme.typography.bodySmall.copy(color = SlateText, fontFamily = FontFamily.Monospace))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Permissions: ${binder.requiredPermission ?: "None"}", style = MaterialTheme.typography.labelSmall.copy(color = ElectricPurple))
                        }
                    }
                }
            }
        }

        // System Properties
        ExpandableSection(
            title = "Filtered System Properties (${props.size})",
            icon = Icons.Default.Tune,
            badgeText = "GETPROP",
            badgeColor = AmberWarning
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                props.forEach { p ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(p.key, style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                        Text(p.value, style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontFamily = FontFamily.Monospace))
                    }
                    HorizontalDivider(color = CyberSurface)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
