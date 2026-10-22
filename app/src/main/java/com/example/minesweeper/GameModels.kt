package com.example.minesweeper

enum class Difficulty(
    val title: String,
    val rows: Int,
    val columns: Int,
    val mineCount: Int
) {
    BEGINNER("初级", 9, 9, 10),
    INTERMEDIATE("中级", 16, 16, 40),
    EXPERT("高级", 16, 30, 99)
}

data class Cell(
    val isMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val adjacentMines: Int = 0
)

enum class GameStatus {
    READY,
    PLAYING,
    WON,
    LOST
}

data class GameState(
    val difficulty: Difficulty,
    val cells: List<Cell>,
    val status: GameStatus,
    val minesRemaining: Int,
    val elapsedSeconds: Int = 0,
    val explodedIndex: Int? = null
)
