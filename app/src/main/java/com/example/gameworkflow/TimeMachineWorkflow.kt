package com.example.gameworkflow

import com.example.gameworkflow.TimeMachineWorkflow.State
import com.squareup.workflow.*

/**
 * Workflow that records its child's renderings and allows you to travel back in time.
 */
class TimeMachineWorkflow(
    private val child: Workflow<Unit, Nothing, Any>,
    private val shakeWorker: ShakeWorker
) : StatefulWorkflow<Unit, State, Nothing, Any>() {

    /**
     * @param replayIndex The index into [history] that is currently being "replayed", or [PRESENT]
     * if we're just showing [child] live.
     *
     * @param history List of all the renderings that [child] has emitted while the child is being
     * shown live ([replayIndex] is [PRESENT]). When the child is not live, it is still rendered to
     * keep its internal state, but its renderings are not added to the list.
     *
     * **This list is mutable –** workflow states should generally not be mutable, nor have any
     * mutable properties.
     */
    data class State(
        val replayIndex: Int = PRESENT,
        val history: MutableList<Any> = mutableListOf()
    )

    /**
     * A screen that shows a previous rendering of the child, with a seek bar for scrubbing time.
     *
     * @param screen The child rendering to display.
     * @param historyEnd The last valid position in the history.
     * @param historyPosition The position in the history that [screen] is from.
     * @param onSeek Event handler to call to scrub the history.
     */
    data class TimeTravelScreen(
        val screen: Any,
        val historyEnd: Int,
        val historyPosition: Int,
        val onSeek: (position: Int) -> Unit
    )

    /**
     * [WorkflowAction] to perform when the device is shaken (see [ShakeWorker]).
     */
    private val onShake = WorkflowAction<State, Nothing> {
        state = state.copy(replayIndex = state.history.size - 1)
        return@WorkflowAction null
    }

    /**
     * [WorkflowAction] to perform when the user drags the history seekbar.
     */
    private fun onSeek(position: Int) = WorkflowAction<State, Nothing> {
        val replayPoint = if (position >= state.history.size - 1) {
            PRESENT
        } else {
            position.coerceAtLeast(0)
        }
        state = state.copy(replayIndex = replayPoint)
        return@WorkflowAction null
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State()

    override fun render(props: Unit, state: State, context: RenderContext<State, Nothing>): Any {
        // Keep the child alive even when replaying to preserve its internal state.
        val currentChildRendering = context.renderChild(child)

        return if (state.replayIndex == PRESENT) {
            // Shaking enters time travel mode.
            context.runningWorker(shakeWorker) { onShake }

            // Only record renderings when we're actually displaying them live.
            state.history += currentChildRendering
            currentChildRendering
        } else {
            val seekSink = context.makeActionSink<WorkflowAction<State, Nothing>>()
            TimeTravelScreen(
                screen = state.history[state.replayIndex],
                historyEnd = state.history.size - 1,
                historyPosition = state.replayIndex,
                onSeek = { position -> seekSink.send(onSeek(position)) }
            )
        }
    }

    override fun snapshotState(state: State): Snapshot = Snapshot.EMPTY

    companion object {
        /**
         * Index for [State.replayIndex] that indicates we're running the child workflow and
         * recording history, not replaying it.
         */
        private const val PRESENT = -1
    }
}