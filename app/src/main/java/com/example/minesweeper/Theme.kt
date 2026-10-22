package com.example.minesweeper

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MinesweeperColors = darkColorScheme(
    primary = Color(0xFFFFC857),
    onPrimary = Color(0xFF151A26),
    secondary = Color(0xFF64B5F6),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFF5F7FB),
    surface = Color(0xFF151C2C),
    onSurface = Color(0xFFE6EAF2)
)

@Composable
fun MinesweeperTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinesweeperColors,
        content = content
    )
}
