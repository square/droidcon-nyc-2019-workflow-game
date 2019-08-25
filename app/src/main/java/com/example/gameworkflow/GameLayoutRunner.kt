package com.example.gameworkflow

import android.view.View
import android.widget.TextView
import com.example.gameworkflow.Direction.*
import com.squareup.workflow.ui.ExperimentalWorkflowUi
import com.squareup.workflow.ui.LayoutRunner
import com.squareup.workflow.ui.LayoutRunner.Companion.bind
import com.squareup.workflow.ui.ViewBinding

private const val BOX_TOP_LEFT = '┌'
private const val BOX_TOP_RIGHT = '┐'
private const val BOX_BOTTOM_LEFT = '└'
private const val BOX_BOTTOM_RIGHT = '┘'

@UseExperimental(ExperimentalWorkflowUi::class)
class GameLayoutRunner(view: View) : LayoutRunner<GameScreen> {

    private val boardView = view.findViewById<TextView>(R.id.board_view)
    private val upButton = view.findViewById<View>(R.id.up_button)
    private val rightButton = view.findViewById<View>(R.id.right_button)
    private val downButton = view.findViewById<View>(R.id.down_button)
    private val leftButton = view.findViewById<View>(R.id.left_button)

    override fun showRendering(rendering: GameScreen) {
        showBoard(rendering)

        val onMove = rendering.onMove
        upButton.isEnabled = onMove != null
        rightButton.isEnabled = onMove != null
        downButton.isEnabled = onMove != null
        leftButton.isEnabled = onMove != null
        if (onMove != null) {
            upButton.setOnClickListener { onMove(UP) }
            rightButton.setOnClickListener { onMove(RIGHT) }
            downButton.setOnClickListener { onMove(DOWN) }
            leftButton.setOnClickListener { onMove(LEFT) }
        }
    }

    private fun showBoard(rendering: GameScreen) {
        val text = buildString {
            // Top border.
            append(BOX_TOP_LEFT)
            // Two segments are roughly the size of an emoji.
            append("──".repeat(rendering.boardSize.x - 3))
            appendln(BOX_TOP_RIGHT)

            for (y in 0 until rendering.boardSize.y) {
                append('│')
                for (x in 0 until rendering.boardSize.x) {
                    append(rendering[x, y])
                }
                appendln('│')
            }

            // Bottom border.
            append(BOX_BOTTOM_LEFT)
            append("──".repeat(rendering.boardSize.x - 3))
            appendln(BOX_BOTTOM_RIGHT)
        }
        boardView.text = text
    }

    private operator fun GameScreen.get(x: Int, y: Int) =
        when (Point(x, y)) {
            playerPosition -> "👩"
            goalPosition -> "🍒"
            // Two spaces are roughly the width of an emoji.
            else -> "  "
        }

    companion object : ViewBinding<GameScreen> by bind(
        R.layout.game_layout, ::GameLayoutRunner
    )
}
