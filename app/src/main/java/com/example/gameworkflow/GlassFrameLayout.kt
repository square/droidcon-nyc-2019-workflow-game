package com.example.gameworkflow

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * [FrameLayout] that can block all touch events.
 */
class GlassFrameLayout(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    var blockTouchEvents: Boolean = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = blockTouchEvents
}
