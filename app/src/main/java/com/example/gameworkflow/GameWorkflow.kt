package com.example.gameworkflow

import com.squareup.workflow.Workflow

typealias GameWorkflow = Workflow<GameProps, GameEnded, GameScreen>

data class GameProps(
    val boardSize: Point,
    val spawnPosition: Point,
    val goalPosition: Point
)

object GameEnded

data class GameScreen(
    val boardSize: Point,
    val playerPosition: Point,
    val goalPosition: Point,
    val onMove: ((Direction) -> Unit)?
)
