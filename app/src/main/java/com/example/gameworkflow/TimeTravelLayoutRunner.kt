package com.example.gameworkflow

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.SeekBar
import androidx.constraintlayout.widget.Group
import androidx.transition.TransitionManager
import com.example.gameworkflow.TimeMachineWorkflow.TimeTravelScreen
import com.squareup.workflow.ui.*
import com.squareup.workflow.ui.LayoutRunner.Companion.bind

@UseExperimental(ExperimentalWorkflowUi::class)
class TimeTravelLayoutRunner(
    private val view: View,
    private val viewRegistry: ViewRegistry
) : LayoutRunner<TimeTravelScreen> {

    private val childContainer = view.findViewById<GlassFrameLayout>(R.id.child_container)!!
    private val seek = view.findViewById<SeekBar>(R.id.time_travel_seek)!!
    private val group = view.findViewById<Group>(R.id.group)!!
    private var currentChildView: View? = null
    private var wasLive: Boolean? = null

    override fun showRendering(rendering: TimeTravelScreen) {
        seek.max = rendering.historyEnd
        seek.progress = rendering.historyPosition

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                rendering.onSeek(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Don't care.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Don't care.
            }
        })

        if (wasLive != rendering.live) {
            wasLive = rendering.live

            val visibility = if (rendering.live) View.GONE else View.VISIBLE
            TransitionManager.beginDelayedTransition(view as ViewGroup)
            group.visibility = visibility
            childContainer.blockTouchEvents = !rendering.live
        }

        // Show the child screen.
        currentChildView = currentChildView
            ?.takeIf { it.canShowRendering(rendering.screen) }
            ?.also { it.showRendering(rendering.screen) }
            ?: viewRegistry.buildView(rendering.screen, childContainer).also {
                childContainer.removeAllViews()
                childContainer.addView(it, LayoutParams(MATCH_PARENT, MATCH_PARENT))
            }
    }

    companion object : ViewBinding<TimeTravelScreen> by bind(
        R.layout.time_travel_layout, ::TimeTravelLayoutRunner
    )
}
