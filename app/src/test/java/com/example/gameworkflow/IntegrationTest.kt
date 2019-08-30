package com.example.gameworkflow

import com.example.gameworkflow.Direction.DOWN
import com.example.gameworkflow.Direction.RIGHT
import com.google.common.truth.Truth.assertThat
import com.squareup.workflow.testing.WorkflowTester
import com.squareup.workflow.testing.testFromStart
import com.squareup.workflow.ui.AlertContainerScreen
import com.squareup.workflow.ui.AlertScreen.Button.POSITIVE
import com.squareup.workflow.ui.AlertScreen.Event.ButtonClicked
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Test

class IntegrationTest {

    private var loaderChannel = Channel<GameProps>(1)
    private val loader: GameLoader = object : GameLoader {
        @UseExperimental(FlowPreview::class, ExperimentalCoroutinesApi::class)
        override fun loadGame(): Flow<GameProps> = flow {
            // Don't close the channel.
            for (props in loaderChannel) emit(props)
        }
    }
    private val workflow = AppWorkflow(loader, RealGameWorkflow())
    private val gameProps = GameProps(
        boardSize = Point(16, 16),
        spawnPosition = Point(0, 0),
        goalPosition = Point(15, 15)
    )

    @Test
    fun `integration test`() {
        workflow.testFromStart {
            // Start in loading state.
            assertThat(awaitNextRendering()).isEqualTo(LoadingScreen)
            assertThat(hasOutput).isFalse()

            // When game is loaded, move to playing state.
            loaderChannel.offer(gameProps)
            (awaitNextRendering() as GameScreen).let { rendering ->
                assertThat(rendering.boardSize).isEqualTo(gameProps.boardSize)
                assertThat(rendering.playerPosition).isEqualTo(gameProps.spawnPosition)
                assertThat(rendering.goalPosition).isEqualTo(gameProps.goalPosition)
                assertThat(rendering.onMove).isNotNull()
                assertThat(hasOutput).isFalse()

                // Move event moves the player.
                rendering.onMove!!.invoke(RIGHT)
            }
            (awaitNextRendering() as GameScreen).let { rendering ->
                assertThat(rendering.boardSize).isEqualTo(gameProps.boardSize)
                assertThat(rendering.playerPosition).isEqualTo(Point(1, 0))
                assertThat(rendering.goalPosition).isEqualTo(gameProps.goalPosition)
                assertThat(rendering.onMove).isNotNull()
                assertThat(hasOutput).isFalse()

                // Move the player close to the goal.
                @Suppress("ComplexRedundantLet")
                rendering
                    .let { move(it, RIGHT, 15) }
                    .let { move(it, DOWN, 15) }
                    // Final step to goal.
                    .let { it.onMove!!.invoke(DOWN) }
            }
            @Suppress("UNCHECKED_CAST")
            (awaitNextRendering() as AlertContainerScreen<GameScreen>).let { gameOverScreen ->
                assertThat(gameOverScreen.baseScreen.onMove).isNull()
                val alertScreen = gameOverScreen.modals.single()
                assertThat(alertScreen.message).isEqualTo("You won!")
                assertThat(hasOutput).isFalse()

                // Dismissing dialog restarts game.
                alertScreen.onEvent(ButtonClicked(POSITIVE))
            }
            assertThat(awaitNextRendering()).isEqualTo(LoadingScreen)
        }
    }

    /**
     * Trigger [distance] move events on [initialRendering], and return the final rendering.
     */
    private fun WorkflowTester<*, *, *>.move(
        initialRendering: GameScreen,
        direction: Direction,
        distance: Int
    ): GameScreen {
        val moves = generateSequence(initialRendering) { rendering ->
            rendering.onMove!!.invoke(direction)
            awaitNextRendering() as GameScreen
        }
        return moves.take(distance).last()
    }
}
