package com.example.gameworkflow

import com.squareup.workflow.Worker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private val DEFAULT_GAME = GameProps(
    boardSize = Point(16, 16),
    spawnPosition = Point(4, 4),
    goalPosition = Point(12, 12)
)

class GameLoader : Worker<GameProps> {
    override fun run(): Flow<GameProps> = flow {
        delay(2000)
        emit(DEFAULT_GAME)
    }

    override fun doesSameWorkAs(otherWorker: Worker<*>): Boolean = otherWorker is GameLoader
}
