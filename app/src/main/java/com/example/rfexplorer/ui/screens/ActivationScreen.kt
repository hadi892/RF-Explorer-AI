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
import com.example.rfexplorer.data.model.ActivationEvaluation
import com.example.rfexplorer.data.model.SafeRecommendationStep
import com.example.rfexplorer.ui.components.CyberHeaderCard
import com.example.rfexplorer.ui.components.ExpandableSection
import com.example.rfexplorer.ui.components.ProbabilityGauge
import com.example.rfexplorer.ui.components.StatusBadge
import com.example.rfexplorer.ui.theme.*
import com.example.rfexplorer.ui.viewmodel.ExplorerViewModel

@Composable
fun ActivationScreen(viewModel: ExplorerViewModel) {
    val evals by viewModel.activationMethods.collectAsStateWithLifecycle()
    val recs by viewModel.safeRecommendations.collectAsStateWithLifecycle()
    val risk by viewModel.riskReport.collectAsStateWithLifecycle()
    val devMode by viewModel.developerModeEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberHeaderCard(
            title = "Activation Probability Matrix",
            subtitle = "Forensic assessment across 9 architectural access layers",
            chipText = if (devMode) "UNLOCKED" else "SAFE AUDIT",
            icon = Icons.Default.Speed
        )

        // Activation Pathways
        Text("Detailed Pathway Evaluation", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))

        evals.sortedByDescending { it.probabilityPercent }.forEach { eval ->
            Card(colors = CardDefaults.cardColors(containerColor = CyberCard), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(eval.displayName, style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                        StatusBadge(eval.feasibilityStatus, if (eval.probabilityPercent > 70) EmeraldSuccess else if (eval.probabilityPercent > 30) AmberWarning else CrimsonAlert)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ProbabilityGauge(percentage = eval.probabilityPercent, title = "Likelihood of Success")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(eval.technicalRationale, style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
                    Spacer(modifier = Modifier.height(8.dp))
                    if (eval.blockers.isNotEmpty()) {
                        Text("Technical Blockers:", style = MaterialTheme.typography.labelSmall.copy(color = CrimsonAlert, fontWeight = FontWeight.Bold))
                        eval.blockers.forEach { b ->
                            Text("• $b", style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
                        }
                    }
                }
            }
        }

        // Safe Activation Assistant
        ExpandableSection(
            title = "Safe Activation Remediation Steps (${recs.size})",
            icon = Icons.Default.Assistant,
            badgeText = "RECOMMENDED",
            badgeColor = EmeraldSuccess,
            initiallyExpanded = true
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recs.forEach { step ->
                    Card(colors = CardDefaults.cardColors(containerColor = CyberSurface), shape = RoundedCornerShape(10.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(NeonCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${step.stepNumber}", style = MaterialTheme.typography.titleSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(step.title, style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                                    StatusBadge(step.riskLevel, if (step.riskLevel.contains("SAFE")) EmeraldSuccess else if (step.riskLevel.contains("LOW")) NeonCyan else AmberWarning)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(step.description, style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Required Privilege: ${step.privilegeRequired}", style = MaterialTheme.typography.labelSmall.copy(color = ElectricPurple, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // Risk Assessment
        ExpandableSection(
            title = "Multi-Dimensional Risk Matrix",
            icon = Icons.Default.Warning,
            badgeText = "AUDIT",
            badgeColor = AmberWarning
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecItem("Security Risk", "${risk?.securityRiskScore ?: 15}/100 (Safe)")
                    SpecItem("Warranty Impact", "Zero (Read-Only)")
                    SpecItem("Recovery", "Trivial")
                }
                HorizontalDivider(color = CyberCard)
                risk?.detailedRisks?.forEach { r ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${r.category}: ${r.title}", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                            StatusBadge(r.severity, if (r.severity == "CRITICAL") CrimsonAlert else if (r.severity == "MODERATE") AmberWarning else EmeraldSuccess)
                        }
                        Text(r.description, style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
                        Text("Mitigation: ${r.mitigation}", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldSuccess))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
