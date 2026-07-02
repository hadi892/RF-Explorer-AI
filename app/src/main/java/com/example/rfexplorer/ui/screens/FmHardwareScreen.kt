package com.example.rfexplorer.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rfexplorer.ui.components.CyberHeaderCard
import com.example.rfexplorer.ui.components.ExpandableSection
import com.example.rfexplorer.ui.components.ProbabilityGauge
import com.example.rfexplorer.ui.components.StatusBadge
import com.example.rfexplorer.ui.theme.*
import com.example.rfexplorer.ui.viewmodel.ExplorerViewModel

@Composable
fun FmHardwareScreen(viewModel: ExplorerViewModel) {
    val fm by viewModel.fmAnalysis.collectAsStateWithLifecycle()
    val audio by viewModel.audioPipeline.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberHeaderCard(
            title = "FM Hardware Capability Analyzer",
            subtitle = "Silicon tuner forensic audit & audio mixer pipeline check",
            chipText = "${fm?.activationProbabilityPercent ?: 78}% LIKELIHOOD",
            icon = Icons.Default.Radio
        )

        // Silicon Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Baseband Silicon Presence", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecItem("WCN Silicon", "INTEGRATED")
                    SpecItem("JNI Bridge", "libfmjni.so")
                    SpecItem("HIDL HAL", "2.0-impl")
                    SpecItem("Driver Node", "/dev/radio0")
                }
                Spacer(modifier = Modifier.height(16.dp))
                ProbabilityGauge(percentage = fm?.activationProbabilityPercent ?: 78, title = "Hardware Activation Probability Score")
            }
        }

        // Positive Indicators
        ExpandableSection(
            title = "Positive Hardware Indicators (${fm?.positiveIndicators?.size ?: 3})",
            icon = Icons.Default.CheckCircle,
            badgeText = "CONFIRMED",
            badgeColor = EmeraldSuccess,
            initiallyExpanded = true
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                fm?.positiveIndicators?.forEach { ind ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(ind, style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                    }
                }
            }
        }

        // Blocking Factors
        ExpandableSection(
            title = "Software Activation Blockers (${fm?.blockingFactors?.size ?: 3})",
            icon = Icons.Default.Block,
            badgeText = "FRAMEWORK",
            badgeColor = CrimsonAlert,
            initiallyExpanded = true
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                fm?.blockingFactors?.forEach { blk ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(blk, style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
                    }
                }
            }
        }

        // Audio Pipeline Analysis
        ExpandableSection(
            title = "Audio Policy & Mixer Paths Audit",
            icon = Icons.Default.VolumeUp,
            badgeText = "${audio?.discoveredRoutes?.size ?: 5} ROUTES",
            badgeColor = ElectricPurple,
            initiallyExpanded = true
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Analyzing /vendor/etc/audio_policy_configuration.xml and mixer_paths.xml:", style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
                audio?.discoveredRoutes?.forEach { route ->
                    Card(colors = CardDefaults.cardColors(containerColor = CyberSurface), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(route.name, style = MaterialTheme.typography.titleSmall.copy(color = if (route.isFmCapable) NeonCyan else Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), modifier = Modifier.weight(1f))
                                StatusBadge(route.type, if (route.isFmCapable) EmeraldSuccess else SlateText)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(route.details, style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
