package com.example.gameworkflow

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.SeekBar
import com.example.gameworkflow.TimeMachineWorkflow.TimeTravelScreen
import com.squareup.workflow.ui.*
import com.squareup.workflow.ui.LayoutRunner.Companion.bind

@UseExperimental(ExperimentalWorkflowUi::class)
class TimeTravelLayoutRunner(
    view: View,
    private val viewRegistry: ViewRegistry
) : LayoutRunner<TimeTravelScreen> {

    private val childContainer = view.findViewById<ViewGroup>(R.id.child_container)!!
    private val seek = view.findViewById<SeekBar>(R.id.time_travel_seek)!!
    private var currentChildView: View? = null

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
