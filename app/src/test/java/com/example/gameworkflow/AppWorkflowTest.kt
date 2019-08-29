package com.example.gameworkflow

import com.example.gameworkflow.AppWorkflow.State.*
import com.google.common.truth.Truth.assertThat
import com.squareup.workflow.asWorker
import com.squareup.workflow.testing.MockChildWorkflow
import com.squareup.workflow.testing.testRender
import com.squareup.workflow.ui.AlertContainerScreen
import com.squareup.workflow.ui.AlertScreen.Button.POSITIVE
import com.squareup.workflow.ui.AlertScreen.Event.ButtonClicked
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Test

class AppWorkflowTest {

    private val gameWorkflow: GameWorkflow = MockChildWorkflow {
        GameScreen(it.boardSize, it.spawnPosition, it.goalPosition, onMove = { error("stub") })
    }
    private val loader: GameLoader = object : GameLoader {
        // This doesn't need to actually emit, we just need something to pass into the AppWorkflow
        // constructor.
        override fun loadGame(): Flow<GameProps> = flow { error("stub") }
    }
    private val workflow = AppWorkflow(loader, gameWorkflow)
    private val gameProps = GameProps(
        boardSize = Point(2, 1),
        spawnPosition = Point(0, 0),
        goalPosition = Point(1, 0)
    )

    @Test
    fun `starts game when loaded`() {
        // Use the same worker helper as the real workflow, so they'll be treated as equivalent.
        val gameWorker = loader.loadGame().asWorker()

        workflow.testRender(LoadingGame) {
            assertThat(rendering).isEqualTo(LoadingScreen)
            assertNoWorkflowsRendered()
            gameWorker.assertRan()
        }
    }

    @Test
    fun `game workflow is rendered while playing`() {
        workflow.testRender(PlayingGame(gameProps)) {
            gameWorkflow.assertRendered()
            assertThat(rendering).isInstanceOf(GameScreen::class.java)
        }
    }

    @Test
    fun `finishes game when won`() {
        workflow.testRender(PlayingGame(gameProps)) {
            val (finishedState, _) = gameWorkflow.handleOutput(GameEnded)
            assertThat(finishedState).isEqualTo(GameOver(gameProps))
        }
    }

    @Test
    fun `game over dialog is rendered when finished`() {
        workflow.testRender(GameOver(gameProps)) {
            gameWorkflow.assertRendered()
            @Suppress("UNCHECKED_CAST")
            val alertRendering = rendering as AlertContainerScreen<GameScreen>
            with(alertRendering.modals.single()) {
                assertThat(buttons).isEqualTo(mapOf(POSITIVE to "Restart"))
                assertThat(message).isEqualTo("You won!")
                assertThat(cancelable).isFalse()
            }
        }
    }

    @Test
    fun `restarts loading when restart clicked`() {
        workflow.testRender(GameOver(gameProps)) {
            @Suppress("UNCHECKED_CAST")
            val alertRendering = rendering as AlertContainerScreen<GameScreen>
            alertRendering.modals.single().onEvent(ButtonClicked(POSITIVE))
            val (state, _) = getEventResult()
            assertThat(state).isEqualTo(LoadingGame)
        }
    }
}
