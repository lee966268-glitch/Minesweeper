package com.example.minesweeper

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MinesweeperApp(
    viewModel: MinesweeperViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0B1020)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GameTitle()
            DifficultySelector(
                currentDifficulty = state.difficulty,
                onDifficultySelected = viewModel::changeDifficulty
            )
            GameHeader(
                state = state,
                onRestart = viewModel::restart
            )
            StatusBanner(state = state)
            MineGrid(
                state = state,
                onTap = viewModel::onCellTap,
                onLongPress = viewModel::onCellLongPress,
                onDoubleTap = viewModel::onCellDoubleTap
            )
            Text(
                text = "玩法：点击翻开 · 长按插旗 · 双击数字快速展开",
                color = Color(0xFF8C9AB5),
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GameTitle() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "扫雷",
                color = Color(0xFFF5F7FB),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "MINESWEEPER",
                color = Color(0xFF71809B),
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
        }
        Text(
            text = "💣",
            fontSize = 40.sp
        )
    }
}

@Composable
private fun DifficultySelector(
    currentDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Difficulty.entries.forEach { difficulty ->
            val selected = difficulty == currentDifficulty
            Surface(
                onClick = {
                    onDifficultySelected(difficulty)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (selected) {
                    Color(0xFFFFC857)
                } else {
                    Color(0xFF1B2233)
                }
            ) {
                Text(
                    text = difficulty.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    color = if (selected) {
                        Color(0xFF151A26)
                    } else {
                        Color(0xFFB7C2D6)
                    },
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun GameHeader(
    state: GameState,
    onRestart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1B2233),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CounterBox(
            title = "剩余地雷",
            value = formatCounter(state.minesRemaining)
        )
        Button(
            onClick = onRestart,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFC857),
                contentColor = Color(0xFF151A26)
            )
        ) {
            Text(
                text = "重新开始",
                fontWeight = FontWeight.Bold
            )
        }
        CounterBox(
            title = "用时",
            value = formatCounter(state.elapsedSeconds)
        )
    }
}

@Composable
private fun CounterBox(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color(0xFF7F8BA5),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color(0xFFFF5C7A),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusBanner(state: GameState) {
    val (message, color) = when (state.status) {
        GameStatus.READY -> {
            "点击任意格子开始（首次点击及周围 3×3 不会有雷）" to
                Color(0xFF80CBC4)
        }
        GameStatus.PLAYING -> {
            "游戏进行中" to Color(0xFFFFC857)
        }
        GameStatus.WON -> {
            "🎉 恭喜通关！用时 ${state.elapsedSeconds} 秒" to
                Color(0xFF81C784)
        }
        GameStatus.LOST -> {
            "💥 踩到地雷了！用时 ${state.elapsedSeconds} 秒" to
                Color(0xFFE57373)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = color.copy(alpha = 0.14f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = message,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MineGrid(
    state: GameState,
    onTap: (Int) -> Unit,
    onLongPress: (Int) -> Unit,
    onDoubleTap: (Int) -> Unit
) {
    val difficulty = state.difficulty
    val cellSize = when (difficulty) {
        Difficulty.BEGINNER -> 34.dp
        Difficulty.INTERMEDIATE -> 28.dp
        Difficulty.EXPERT -> 24.dp
    }
    val horizontalScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF101624))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier.horizontalScroll(horizontalScrollState)
        ) {
            Column {
                repeat(difficulty.rows) { row ->
                    Row(
                        modifier = Modifier.width(
                            cellSize * difficulty.columns
                        )
                    ) {
                        repeat(difficulty.columns) { column ->
                            val index =
                                row * difficulty.columns + column
                            MineCell(
                                state = state,
                                index = index,
                                cell = state.cells[index],
                                cellSize = cellSize,
                                onTap = onTap,
                                onLongPress = onLongPress,
                                onDoubleTap = onDoubleTap
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MineCell(
    state: GameState,
    index: Int,
    cell: Cell,
    cellSize: Dp,
    onTap: (Int) -> Unit,
    onLongPress: (Int) -> Unit,
    onDoubleTap: (Int) -> Unit
) {
    val isWrongFlag =
        state.status == GameStatus.LOST &&
            cell.isFlagged &&
            !cell.isMine
    val isExploded = state.explodedIndex == index

    val backgroundColor = when {
        isWrongFlag -> Color(0xFF7F1D1D)
        isExploded -> Color(0xFFB91C1C)
        cell.isFlagged -> Color(0xFF3B4A60)
        cell.isRevealed && cell.isMine -> Color(0xFFE53935)
        cell.isRevealed -> Color(0xFFDCE2EC)
        else -> Color(0xFF64748B)
    }

    val content = when {
        isWrongFlag -> "❌"
        cell.isFlagged -> "🚩"
        cell.isMine && (
            state.status == GameStatus.LOST ||
                state.status == GameStatus.WON
            ) -> "💣"
        cell.isRevealed && cell.isMine -> "💣"
        cell.isRevealed && cell.adjacentMines > 0 -> {
            cell.adjacentMines.toString()
        }
        else -> ""
    }

    val textColor = if (
        cell.isRevealed &&
        cell.adjacentMines in 1..8
    ) {
        numberColor(cell.adjacentMines)
    } else {
        Color.Black
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .padding(1.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = {
                    onTap(index)
                },
                onLongClick = {
                    onLongPress(index)
                    true
                },
                onDoubleClick = {
                    onDoubleTap(index)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (content.isNotEmpty()) {
            Text(
                text = content,
                color = textColor,
                fontSize = (cellSize.value * 0.46f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun numberColor(number: Int): Color {
    return when (number) {
        1 -> Color(0xFF1565C0)
        2 -> Color(0xFF2E7D32)
        3 -> Color(0xFFC62828)
        4 -> Color(0xFF6A1B9A)
        5 -> Color(0xFFAD1457)
        6 -> Color(0xFF00838F)
        7 -> Color(0xFF212121)
        8 -> Color(0xFF757575)
        else -> Color.Black
    }
}

private fun formatCounter(value: Int): String {
    return if (value < 0) {
        "-${(-value).toString().padStart(2, '0')}"
    } else {
        value.toString().padStart(3, '0')
    }
}
