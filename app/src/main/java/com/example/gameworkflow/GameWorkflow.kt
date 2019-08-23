package com.example.gameworkflow

import com.squareup.workflow.Workflow

typealias GameWorkflow = Workflow<GameProps, GameOutput, GameRendering>

data class GameProps(
    val boardSize: Point,
    val spawnPosition: Point,
    val goalPosition: Point
)

sealed class GameOutput {
    object Win : GameOutput()
}

data class GameRendering(
    val boardSize: Point,
    val playerPosition: Point,
    val goalPosition: Point,
    val onMove: ((Direction) -> Unit)?
)
