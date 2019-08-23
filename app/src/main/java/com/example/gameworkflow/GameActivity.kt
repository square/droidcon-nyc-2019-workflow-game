package com.example.gameworkflow

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gameworkflow.GameOutput.Win
import com.squareup.workflow.Workflow
import com.squareup.workflow.WorkflowAction.Companion.emitOutput
import com.squareup.workflow.stateless
import com.squareup.workflow.ui.ExperimentalWorkflowUi
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.WorkflowRunner
import com.squareup.workflow.ui.setContentWorkflow

@UseExperimental(ExperimentalWorkflowUi::class)
class GameActivity : AppCompatActivity() {
    private val viewRegistry = ViewRegistry(GameLayoutRunner)
    private lateinit var runner: WorkflowRunner<*>
    private val gameProps = GameProps(
        boardSize = Point(16, 16),
        spawnPosition = Point(4, 4),
        goalPosition = Point(12, 12)
    )

    private val appWorkflow: Workflow<Unit, GameOutput, GameRendering> = Workflow.stateless {
        renderChild(RealGameWorkflow(), gameProps) { output -> emitOutput(output) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runner = setContentWorkflow(
            savedInstanceState,
            configure = { WorkflowRunner.Config(appWorkflow, viewRegistry) },
            onResult = { result ->
                when (result) {
                    Win -> Toast.makeText(this, "You won!", Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        runner.onSaveInstanceState(outState)
    }
}
