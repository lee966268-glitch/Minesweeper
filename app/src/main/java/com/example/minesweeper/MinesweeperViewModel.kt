package com.example.minesweeper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MinesweeperViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        MinesweeperEngine.newGame(Difficulty.BEGINNER)
    )
    val state = _state.asStateFlow()

    private var timerJob: Job? = null

    fun onCellTap(index: Int) {
        val before = _state.value
        val after = MinesweeperEngine.reveal(before, index)
        _state.value = after

        if (
            before.status == GameStatus.READY &&
            after.status != GameStatus.READY
        ) {
            startTimer()
        }
        if (
            after.status == GameStatus.WON ||
            after.status == GameStatus.LOST
        ) {
            stopTimer()
        }
    }

    fun onCellLongPress(index: Int) {
        _state.value = MinesweeperEngine.toggleFlag(
            state = _state.value,
            index = index
        )
    }

    fun onCellDoubleTap(index: Int) {
        val after = MinesweeperEngine.chord(
            state = _state.value,
            index = index
        )
        _state.value = after

        if (
            after.status == GameStatus.WON ||
            after.status == GameStatus.LOST
        ) {
            stopTimer()
        }
    }

    fun restart() {
        stopTimer()
        _state.value = MinesweeperEngine.newGame(
            _state.value.difficulty
        )
    }

    fun changeDifficulty(difficulty: Difficulty) {
        stopTimer()
        _state.value = MinesweeperEngine.newGame(difficulty)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _state.update {
                    it.copy(
                        elapsedSeconds = it.elapsedSeconds + 1
                    )
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }
}
