package com.example.gameworkflow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private val DEFAULT_GAME = GameProps(
    boardSize = Point(16, 16),
    spawnPosition = Point(4, 4),
    goalPosition = Point(12, 12)
)

interface GameLoader {
    fun loadGame(): Flow<GameProps>
}

class RealGameLoader : GameLoader {
    override fun loadGame(): Flow<GameProps> = flow {
        delay(2000)
        emit(DEFAULT_GAME)
    }
}
