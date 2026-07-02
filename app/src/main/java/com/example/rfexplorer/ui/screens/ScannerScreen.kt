package com.example.rfexplorer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rfexplorer.data.model.ElfBinaryInfo
import com.example.rfexplorer.data.model.LibraryCategory
import com.example.rfexplorer.data.model.ScannedLibrary
import com.example.rfexplorer.ui.components.StatusBadge
import com.example.rfexplorer.ui.theme.*
import com.example.rfexplorer.ui.viewmodel.ExplorerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(viewModel: ExplorerViewModel) {
    val filteredLibs by viewModel.filteredLibraries.collectAsStateWithLifecycle()
    val selectedCat by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val elfInfo by viewModel.selectedElfInfo.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("ELF Hardware Library Scanner", style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
        Text("Enumerating /system, /vendor, and /apex native libraries", style = MaterialTheme.typography.bodyMedium.copy(color = SlateText))
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by library name or path (.e.g., libfmjni, radio, qmi)...", color = SlateText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { viewModel.setSearchQuery("") }) { Icon(Icons.Default.Clear, contentDescription = null, tint = SlateText) } }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CyberCard,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = (selectedCat == null),
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("ALL (${filteredLibs.size})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan, selectedLabelColor = Color.Black)
                )
            }
            items(LibraryCategory.values()) { cat ->
                FilterChip(
                    selected = (selectedCat == cat),
                    onClick = { viewModel.setCategoryFilter(if (selectedCat == cat) null else cat) },
                    label = { Text(cat.displayName) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan, selectedLabelColor = Color.Black)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Library Table
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredLibs) { lib ->
                LibraryRowCard(lib = lib, onClick = { viewModel.inspectElfBinary(lib) })
            }
        }
    }

    // ELF Binary Inspection Modal
    if (elfInfo != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedElfInfo() },
            containerColor = CyberSurface
        ) {
            ElfAnalysisDialogContent(info = elfInfo!!)
        }
    }
}

@Composable
fun LibraryRowCard(lib: ScannedLibrary, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(NeonCyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(lib.name, style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                Text(lib.path, style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(lib.architecture, ElectricPurple)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${lib.sizeBytes / 1024} KB", style = MaterialTheme.typography.labelSmall.copy(color = SlateText))
            }
        }
    }
}

@Composable
fun ElfAnalysisDialogContent(info: ElfBinaryInfo) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Analytics, contentDescription = null, tint = NeonCyan)
            Spacer(modifier = Modifier.width(10.dp))
            Text(info.fileName, style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
        }
        Text(info.filePath, style = MaterialTheme.typography.bodySmall.copy(color = SlateText))
        HorizontalDivider(color = CyberCard)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SpecItem("SONAME", info.soname)
            SpecItem("Architecture", info.architecture)
            SpecItem("Hidden Symbols", "${info.hiddenSymbolsCount}")
        }

        Text("Exported JNI & Public Symbols (${info.exportedSymbols.size})", style = MaterialTheme.typography.titleSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold))
        Card(colors = CardDefaults.cardColors(containerColor = CyberCard), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
                info.exportedSymbols.take(8).forEach { sym ->
                    Text(sym, style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontFamily = FontFamily.Monospace))
                }
            }
        }

        Text("DT_NEEDED Shared Library Dependencies (${info.neededDependencies.size})", style = MaterialTheme.typography.titleSmall.copy(color = ElectricPurple, fontWeight = FontWeight.Bold))
        Card(colors = CardDefaults.cardColors(containerColor = CyberCard), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
                info.neededDependencies.forEach { dep ->
                    Text("• $dep", style = MaterialTheme.typography.bodySmall.copy(color = SlateText, fontFamily = FontFamily.Monospace))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
