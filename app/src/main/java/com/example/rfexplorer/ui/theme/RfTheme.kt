package com.example.rfexplorer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CyberObsidian = Color(0xFF0A0E17)
val CyberSurface = Color(0xFF131926)
val CyberCard = Color(0xFF1C2538)
val NeonCyan = Color(0xFF00E5FF)
val ElectricPurple = Color(0xFFBB86FC)
val EmeraldSuccess = Color(0xFF00E676)
val AmberWarning = Color(0xFFFF9100)
val CrimsonAlert = Color(0xFFFF1744)
val SlateText = Color(0xFFB0BEC5)

private val RfDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = NeonCyan,
    secondary = ElectricPurple,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF381E72),
    onSecondaryContainer = ElectricPurple,
    tertiary = EmeraldSuccess,
    background = CyberObsidian,
    onBackground = Color.White,
    surface = CyberSurface,
    onSurface = Color.White,
    surfaceVariant = CyberCard,
    onSurfaceVariant = SlateText,
    error = CrimsonAlert,
    onError = Color.White
)

@Composable
fun RfExplorerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RfDarkColorScheme,
        content = content
    )
}
