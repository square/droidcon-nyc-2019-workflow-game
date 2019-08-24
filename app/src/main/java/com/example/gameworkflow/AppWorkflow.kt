package com.example.gameworkflow

import com.example.gameworkflow.AppWorkflow.State
import com.example.gameworkflow.AppWorkflow.State.*
import com.squareup.workflow.*
import com.squareup.workflow.WorkflowAction.Companion.noAction
import com.squareup.workflow.WorkflowAction.Mutator
import com.squareup.workflow.ui.AlertContainerScreen
import com.squareup.workflow.ui.AlertScreen
import com.squareup.workflow.ui.AlertScreen.Button.POSITIVE

class AppWorkflow(
    private val gameLoader: Worker<GameProps>,
    private val gameWorkflow: GameWorkflow
) : StatefulWorkflow<Unit, State, Nothing, Any>() {

    sealed class State {
        object LoadingGame : State()
        data class PlayingGame(val game: GameProps) : State()
        data class GameOver(val game: GameProps) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State {
        return LoadingGame
    }

    override fun render(props: Unit, state: State, context: RenderContext<State, Nothing>): Any {
        when (state) {
            LoadingGame -> {
                context.runningWorker(gameLoader) { startGame(it) }
                return LoadingScreen
            }
            is PlayingGame -> {
                return context.renderChild(gameWorkflow, state.game) { FinishGame }
            }
            is GameOver -> {
                val gameRendering = context.renderChild(gameWorkflow, state.game) {
                    // Ignore outputs.
                    noAction()
                }

                val restartSink = context.makeActionSink<RestartGame>()
                val gameOverDialog = AlertScreen(
                    buttons = mapOf(POSITIVE to "Restart"),
                    message = "You won!",
                    cancelable = false,
                    onEvent = { restartSink.send(RestartGame) }
                )

                return AlertContainerScreen(gameRendering, gameOverDialog)
            }
        }
    }

    override fun snapshotState(state: State): Snapshot = Snapshot.EMPTY

    private fun startGame(game: GameProps) = WorkflowAction<State, Nothing> {
        state = PlayingGame(game)
        return@WorkflowAction null
    }

    private object FinishGame : WorkflowAction<State, Nothing> {
        override fun Mutator<State>.apply(): Nothing? {
            val playingGame = state as? PlayingGame ?: error("Can only finish game while playing.")
            state = GameOver(playingGame.game)
            return null
        }
    }

    private object RestartGame : WorkflowAction<State, Nothing> {
        override fun Mutator<State>.apply(): Nothing? {
            state = LoadingGame
            return null
        }
    }
}
