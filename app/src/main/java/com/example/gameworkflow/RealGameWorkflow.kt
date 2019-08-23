package com.example.gameworkflow

import com.example.gameworkflow.GameOutput.Win
import com.example.gameworkflow.RealGameWorkflow.State
import com.squareup.workflow.RenderContext
import com.squareup.workflow.Snapshot
import com.squareup.workflow.StatefulWorkflow
import com.squareup.workflow.makeEventSink

class RealGameWorkflow : GameWorkflow,
    StatefulWorkflow<GameProps, State, GameOutput, GameRendering>() {

    data class State(val playerPosition: Point)

    override fun initialState(props: GameProps, snapshot: Snapshot?): State =
        State(props.spawnPosition)

    override fun render(
        props: GameProps,
        state: State,
        context: RenderContext<State, GameOutput>
    ): GameRendering {
        fun isGameOver(playerPosition: Point) = playerPosition == props.goalPosition

        val moveSink = context.makeEventSink { direction: Direction ->
            val newPosition = state.playerPosition.moved(direction)
                .constrainTo(props.boardSize)
            this.state = state.copy(playerPosition = newPosition)

            return@makeEventSink if (isGameOver(newPosition)) Win else null
        }

        return GameRendering(
            boardSize = props.boardSize,
            playerPosition = state.playerPosition,
            goalPosition = props.goalPosition,
            onMove = if (!isGameOver(state.playerPosition)) moveSink::send else null
        )
    }

    override fun snapshotState(state: State): Snapshot = Snapshot.EMPTY
}
