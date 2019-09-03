package com.example.gameworkflow

import com.example.gameworkflow.RecordingWorkflow.Props
import com.example.gameworkflow.RecordingWorkflow.Props.Pause
import com.example.gameworkflow.RecordingWorkflow.Props.Record
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
     * @param replayIndex The index into the history that is currently being "replayed", or [LIVE]
     * if we're just showing [child] live.
     */
    data class State(val replayIndex: Int = LIVE)

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
        val live: Boolean,
        val onSeek: (position: Int) -> Unit
    )

    /**
     * [WorkflowAction] to perform when the device is shaken (see [ShakeWorker]).
     */
    private fun onShake(replayIndex: Int) = WorkflowAction<State, Nothing> {
        state = state.copy(replayIndex = replayIndex)
        return@WorkflowAction null
    }

    /**
     * [WorkflowAction] to perform when the user drags the history seekbar.
     */
    private fun onSeek(position: Int, historySize: Int) = WorkflowAction<State, Nothing> {
        val replayPoint = if (position >= historySize - 1) {
            LIVE
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
        val seekSink = context.makeActionSink<WorkflowAction<State, Nothing>>()
        val live = state.replayIndex == LIVE

        // Only record renderings when we're actually displaying them live.
        val recordControl = if (live) Record(currentChildRendering) else Pause
        val history = context.renderChild(RecordingWorkflow(), recordControl)

        if (live) {
            // Shaking enters time travel mode.
            context.runningWorker(shakeWorker) { onShake(history.size - 1) }
        }

        val historyEnd = history.size - 1
        return TimeTravelScreen(
            screen = if (live) currentChildRendering else history[state.replayIndex],
            historyEnd = historyEnd,
            historyPosition = if (live) historyEnd else state.replayIndex,
            live = live,
            onSeek = { position -> seekSink.send(onSeek(position, history.size)) }
        )
    }

    override fun snapshotState(state: State): Snapshot = Snapshot.EMPTY

    companion object {
        /**
         * Index for [State.replayIndex] that indicates we're running the child workflow and
         * recording history, not replaying it.
         */
        private const val LIVE = -1
    }
}

/**
 * A workflow that records every props ever sent to it in a list, and returns that list as its
 * rendering.
 */
private class RecordingWorkflow<RenderingT> :
    StatefulWorkflow<Props<RenderingT>, List<RenderingT>, Nothing, List<RenderingT>>() {

    sealed class Props<out RenderingT> {
        data class Record<RenderingT>(val rendering: RenderingT) : Props<RenderingT>()
        object Pause : Props<Nothing>()
    }

    override fun initialState(props: Props<RenderingT>, snapshot: Snapshot?): List<RenderingT> =
        emptyList()

    override fun onPropsChanged(
        old: Props<RenderingT>,
        new: Props<RenderingT>,
        state: List<RenderingT>
    ): List<RenderingT> = if (new is Record) state + new.rendering else state

    override fun render(
        props: Props<RenderingT>,
        state: List<RenderingT>,
        context: RenderContext<List<RenderingT>, Nothing>
    ): List<RenderingT> = state

    override fun snapshotState(state: List<RenderingT>): Snapshot = Snapshot.EMPTY
}
