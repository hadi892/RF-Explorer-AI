package com.example.rfexplorer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rfexplorer.data.model.DependencyGraphEdge
import com.example.rfexplorer.data.model.DependencyGraphNode
import com.example.rfexplorer.ui.components.CyberHeaderCard
import com.example.rfexplorer.ui.components.StatusBadge
import com.example.rfexplorer.ui.theme.*
import com.example.rfexplorer.ui.viewmodel.ExplorerViewModel

@Composable
fun DependencyGraphScreen(viewModel: ExplorerViewModel) {
    val nodes by viewModel.dependencyNodes.collectAsStateWithLifecycle()
    val edges by viewModel.dependencyEdges.collectAsStateWithLifecycle()

    var selectedNodeId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberHeaderCard(
            title = "Hardware Dependency Graph",
            subtitle = "Interactive multi-tier software to silicon stack mapping",
            chipText = "${nodes.size} NODES",
            icon = Icons.Default.Hub
        )

        Text("Select layer node to trace dependencies across JNI & HAL:", style = MaterialTheme.typography.bodyMedium.copy(color = SlateText))

        // Interactive Visual Stack
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Layer 1: Java Framework
                StackTierHeader("LAYER 1: JAVA APPLICATION & FRAMEWORK SERVICE", NeonCyan)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    nodes.filter { it.type == "JNI" }.forEach { n ->
                        GraphNodeBox(n, selectedNodeId == n.id) { selectedNodeId = n.id }
                    }
                }

                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = NeonCyan, modifier = Modifier.align(Alignment.CenterHorizontally))

                // Layer 2: Native JNI Libraries
                StackTierHeader("LAYER 2: NATIVE JNI BRIDGES (/system & /vendor .so)", ElectricPurple)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    nodes.filter { it.type == "LIB" }.forEach { n ->
                        GraphNodeBox(n, selectedNodeId == n.id) { selectedNodeId = n.id }
                    }
                }

                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = ElectricPurple, modifier = Modifier.align(Alignment.CenterHorizontally))

                // Layer 3: Hardware Abstraction Layer
                StackTierHeader("LAYER 3: HARDWARE ABSTRACTION LAYER (HIDL/AIDL)", EmeraldSuccess)
                nodes.filter { it.type == "HAL" }.forEach { n ->
                    GraphNodeBox(n, selectedNodeId == n.id) { selectedNodeId = n.id }
                }

                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.align(Alignment.CenterHorizontally))

                // Layer 4: Physical Silicon / Device Nodes
                StackTierHeader("LAYER 4: KERNEL DRIVERS & PHYSICAL SOC NODES (/dev)", AmberWarning)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    nodes.filter { it.type == "HW" }.forEach { n ->
                        GraphNodeBox(n, selectedNodeId == n.id) { selectedNodeId = n.id }
                    }
                }
            }
        }

        // Details Panel for Selected Node
        if (selectedNodeId != null) {
            val node = nodes.find { it.id == selectedNodeId }
            val connectedEdges = edges.filter { it.fromId == selectedNodeId || it.toId == selectedNodeId }

            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(node?.label ?: "", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), modifier = Modifier.weight(1f))
                        StatusBadge(node?.type ?: "", NeonCyan)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Inter-Process Relationships (${connectedEdges.size}):", style = MaterialTheme.typography.labelSmall.copy(color = SlateText))
                    connectedEdges.forEach { e ->
                        val target = if (e.fromId == selectedNodeId) e.toId else e.fromId
                        val dir = if (e.fromId == selectedNodeId) "Calls Down To →" else "Called By ←"
                        Text("• $dir $target (${e.relationship})", style = MaterialTheme.typography.bodySmall.copy(color = ElectricPurple, fontFamily = FontFamily.Monospace))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StackTierHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    )
}

@Composable
fun GraphNodeBox(node: DependencyGraphNode, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) NeonCyan.copy(alpha = 0.25f) else CyberSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) NeonCyan else CyberCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (node.type) {
                    "HW" -> Icons.Default.Memory
                    "HAL" -> Icons.Default.Hardware
                    "LIB" -> Icons.Default.Code
                    else -> Icons.Default.Apps
                },
                contentDescription = null,
                tint = if (isSelected) NeonCyan else SlateText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(node.label, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
        }
    }
}
