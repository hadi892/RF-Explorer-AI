package com.example.rfexplorer.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rfexplorer.data.model.ScanReportEntity
import com.example.rfexplorer.ui.components.CyberHeaderCard
import com.example.rfexplorer.ui.components.StatusBadge
import com.example.rfexplorer.ui.theme.*
import com.example.rfexplorer.ui.viewmodel.ExplorerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(viewModel: ExplorerViewModel) {
    val reports by viewModel.savedReports.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedReport by remember { mutableStateOf<ScanReportEntity?>(null) }
    var showRawTextDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        CyberHeaderCard(
            title = "Scan Reports & Audit Archive",
            subtitle = "Locally persisted system evaluations stored in Room Database",
            chipText = "${reports.size} ARCHIVED",
            icon = Icons.Default.Description
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    val fullText = viewModel.exportFullReportText()
                    copyToClipboard(context, fullText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("EXPORT CURRENT MD/HTML", fontWeight = FontWeight.Bold)
            }

            if (reports.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearAllReports() }) {
                    Text("CLEAR ALL", color = CrimsonAlert, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No scan reports archived yet. Run a system scan from the dashboard.", color = SlateText)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(reports) { rep ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                Text(sdf.format(Date(rep.timestampMs)), style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                                StatusBadge("${rep.fmProbabilityPercent}% FM", if (rep.fmProbabilityPercent > 70) EmeraldSuccess else AmberWarning)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Target: ${rep.targetDevice} | Libs: ${rep.totalLibrariesScanned} | SELinux: ${rep.selinuxStatus}", style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = {
                                    selectedReport = rep
                                }) {
                                    Text("VIEW JSON", color = NeonCyan)
                                }
                                IconButton(onClick = { viewModel.deleteReport(rep.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonAlert)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedReport != null) {
        AlertDialog(
            onDismissRequest = { selectedReport = null },
            containerColor = CyberSurface,
            title = { Text("Audit Summary JSON #$${selectedReport!!.id}", color = Color.White, fontFamily = FontFamily.Monospace) },
            text = {
                Card(colors = CardDefaults.cardColors(containerColor = CyberObsidian), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        selectedReport!!.jsonSummary,
                        style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan, fontFamily = FontFamily.Monospace),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    copyToClipboard(context, selectedReport!!.jsonSummary)
                    selectedReport = null
                }) {
                    Text("COPY & CLOSE", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("RF Explorer Report", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
}
