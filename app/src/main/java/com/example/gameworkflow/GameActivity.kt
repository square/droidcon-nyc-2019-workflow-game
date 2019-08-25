package com.example.gameworkflow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.squareup.workflow.ui.ExperimentalWorkflowUi
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.WorkflowRunner
import com.squareup.workflow.ui.setContentWorkflow

// These would typically be provided by your dependency-injection library.
private val gameLoader = GameLoader()
private val gameWorkflow = RealGameWorkflow()
private val appWorkflow = AppWorkflow(gameLoader, gameWorkflow)

@UseExperimental(ExperimentalWorkflowUi::class)
private val viewRegistry = ViewRegistry(LoadingScreen, GameLayoutRunner)

@UseExperimental(ExperimentalWorkflowUi::class)
class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentWorkflow(savedInstanceState) {
            WorkflowRunner.Config(appWorkflow, viewRegistry)
        }
    }
}
