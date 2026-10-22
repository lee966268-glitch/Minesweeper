package com.example.minesweeper

import java.util.ArrayDeque

object MinesweeperEngine {

    fun newGame(difficulty: Difficulty): GameState {
        val cellCount = difficulty.rows * difficulty.columns
        return GameState(
            difficulty = difficulty,
            cells = List(cellCount) { Cell() },
            status = GameStatus.READY,
            minesRemaining = difficulty.mineCount
        )
    }

    /**
     * 点击格子。
     */
    fun reveal(state: GameState, index: Int): GameState {
        if (state.status == GameStatus.WON || state.status == GameStatus.LOST) {
            return state
        }
        if (index !in state.cells.indices) {
            return state
        }
        val target = state.cells[index]
        if (target.isRevealed || target.isFlagged) {
            return state
        }

        // 第一次点击时才生成地雷，并保护点击位置周围 3×3。
        val preparedState = if (state.status == GameStatus.READY) {
            state.copy(
                cells = createMinesLayout(
                    difficulty = state.difficulty,
                    safeIndex = index
                ),
                status = GameStatus.PLAYING
            )
        } else {
            state
        }

        return openCells(preparedState, listOf(index))
    }

    /**
     * 插旗或取消插旗。
     *
     * 为了保证第一次点击绝对安全，只有游戏开始后才允许插旗。
     */
    fun toggleFlag(state: GameState, index: Int): GameState {
        if (state.status != GameStatus.PLAYING) {
            return state
        }
        if (index !in state.cells.indices) {
            return state
        }
        val target = state.cells[index]
        if (target.isRevealed) {
            return state
        }
        val cells = state.cells.toMutableList()
        cells[index] = target.copy(
            isFlagged = !target.isFlagged
        )
        val flagCount = cells.count { it.isFlagged }
        return state.copy(
            cells = cells,
            minesRemaining = state.difficulty.mineCount - flagCount
        )
    }

    /**
     * 双击数字格子的快速展开。
     *
     * 当周围旗子数量等于数字时，翻开周围所有未翻开且未标记的格子。
     */
    fun chord(state: GameState, index: Int): GameState {
        if (state.status != GameStatus.PLAYING) {
            return state
        }
        if (index !in state.cells.indices) {
            return state
        }
        val target = state.cells[index]
        if (!target.isRevealed || target.isMine || target.adjacentMines == 0) {
            return state
        }
        val adjacentIndexes = neighbors(index, state.difficulty)
        val flagCount = adjacentIndexes.count {
            state.cells[it].isFlagged
        }
        if (flagCount != target.adjacentMines) {
            return state
        }
        val targets = adjacentIndexes.filter {
            !state.cells[it].isRevealed && !state.cells[it].isFlagged
        }
        if (targets.isEmpty()) {
            return state
        }
        return openCells(state, targets)
    }

    /**
     * 翻开格子，并自动展开相连的空白区域。
     */
    private fun openCells(
        state: GameState,
        targets: List<Int>
    ): GameState {
        val cells = state.cells.toMutableList()
        val queue = ArrayDeque<Int>()

        targets.forEach { index ->
            val cell = cells[index]
            if (!cell.isRevealed && !cell.isFlagged) {
                queue.addLast(index)
            }
        }

        var explodedIndex: Int? = null

        while (queue.isNotEmpty()) {
            val index = queue.removeFirst()
            val cell = cells[index]
            if (cell.isRevealed || cell.isFlagged) {
                continue
            }
            if (cell.isMine) {
                explodedIndex = index
                cells[index] = cell.copy(isRevealed = true)
                break
            }
            cells[index] = cell.copy(isRevealed = true)
            // 数字为 0 时，继续向周围扩散。
            if (cell.adjacentMines == 0) {
                neighbors(index, state.difficulty).forEach { neighborIndex ->
                    val neighbor = cells[neighborIndex]
                    if (!neighbor.isRevealed && !neighbor.isFlagged) {
                        queue.addLast(neighborIndex)
                    }
                }
            }
        }

        if (explodedIndex != null) {
            // 游戏失败时显示所有地雷。
            cells.indices.forEach { index ->
                if (cells[index].isMine) {
                    cells[index] = cells[index].copy(isRevealed = true)
                }
            }
            return state.copy(
                cells = cells,
                status = GameStatus.LOST,
                explodedIndex = explodedIndex
            )
        }

        val totalCells = state.difficulty.rows * state.difficulty.columns
        val safeRevealedCount = cells.count {
            it.isRevealed && !it.isMine
        }
        val hasWon =
            safeRevealedCount == totalCells - state.difficulty.mineCount

        return state.copy(
            cells = cells,
            status = if (hasWon) GameStatus.WON else GameStatus.PLAYING
        )
    }

    /**
     * 生成地雷布局。
     *
     * 首次点击位置及周围 3×3 区域不会放置地雷。
     */
    private fun createMinesLayout(
        difficulty: Difficulty,
        safeIndex: Int
    ): List<Cell> {
        val safeIndexes = mutableSetOf<Int>()
        val safeRow = safeIndex / difficulty.columns
        val safeColumn = safeIndex % difficulty.columns

        for (rowOffset in -1..1) {
            for (columnOffset in -1..1) {
                val row = safeRow + rowOffset
                val column = safeColumn + columnOffset
                if (
                    row in 0 until difficulty.rows &&
                    column in 0 until difficulty.columns
                ) {
                    safeIndexes += row * difficulty.columns + column
                }
            }
        }

        val totalCells = difficulty.rows * difficulty.columns
        val candidates = (0 until totalCells).filter {
            it !in safeIndexes
        }
        val mineIndexes = candidates
            .shuffled()
            .take(difficulty.mineCount)
            .toSet()

        return List(totalCells) { index ->
            Cell(
                isMine = index in mineIndexes,
                adjacentMines = if (index in mineIndexes) {
                    0
                } else {
                    neighbors(index, difficulty).count {
                        it in mineIndexes
                    }
                }
            )
        }
    }

    /**
     * 返回指定格子周围所有有效格子的索引。
     */
    private fun neighbors(
        index: Int,
        difficulty: Difficulty
    ): List<Int> {
        val row = index / difficulty.columns
        val column = index % difficulty.columns
        return buildList {
            for (rowOffset in -1..1) {
                for (columnOffset in -1..1) {
                    if (rowOffset != 0 || columnOffset != 0) {
                        val neighborRow = row + rowOffset
                        val neighborColumn = column + columnOffset
                        if (
                            neighborRow in 0 until difficulty.rows &&
                            neighborColumn in 0 until difficulty.columns
                        ) {
                            add(
                                neighborRow * difficulty.columns +
                                    neighborColumn
                            )
                        }
                    }
                }
            }
        }
    }
}
