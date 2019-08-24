package com.example.gameworkflow

import com.squareup.workflow.ui.ExperimentalWorkflowUi
import com.squareup.workflow.ui.LayoutRunner.Companion.bindNoRunner
import com.squareup.workflow.ui.ViewBinding

@UseExperimental(ExperimentalWorkflowUi::class)
object LoadingScreen : ViewBinding<LoadingScreen> by bindNoRunner(R.layout.loading_layout)
