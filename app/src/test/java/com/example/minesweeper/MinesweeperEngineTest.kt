package com.example.minesweeper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MinesweeperEngineTest {

    @Test
    fun firstClickIsAlwaysSafe() {
        val initial = MinesweeperEngine.newGame(
            Difficulty.BEGINNER
        )
        val afterClick = MinesweeperEngine.reveal(
            state = initial,
            index = 40
        )

        assertEquals(
            GameStatus.PLAYING,
            afterClick.status
        )
        assertFalse(afterClick.cells[40].isMine)

        // 9×9 棋盘中，中心位置周围 3×3。
        val protectedIndexes = listOf(
            31, 32, 33,
            39, 40, 41,
            49, 50, 51
        )
        protectedIndexes.forEach {
            assertFalse(afterClick.cells[it].isMine)
        }
    }

    @Test
    fun flagUpdatesMineCounter() {
        val started = MinesweeperEngine.reveal(
            state = MinesweeperEngine.newGame(
                Difficulty.BEGINNER
            ),
            index = 40
        )
        val hiddenIndex = started.cells.indexOfFirst {
            !it.isRevealed
        }
        val flagged = MinesweeperEngine.toggleFlag(
            state = started,
            index = hiddenIndex
        )

        assertTrue(flagged.cells[hiddenIndex].isFlagged)
        assertEquals(9, flagged.minesRemaining)
    }
}
