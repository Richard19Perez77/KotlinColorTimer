package com.drawing.kotlin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat

class Controller {

    /**
     * store the screen size for scaling objects
     */
    var screenWidth = 0
    var screenHeight = 0
    var centerX = 0
    var rect = Rect()
    var paint: Paint = Paint()
    var time: Long = 0
    private var primaryColor = 0
    private var primaryPaint: Paint = Paint()
    private var primaryDarkPaint: Paint = Paint()
    private var accentPaint: Paint = Paint()
    private var paintWhite: Paint = Paint()
    private var primaryColorDark = 0
    private var primaryColorAccent = 0
    private var textSize = 0

    fun init(context: Context) {
        primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
        primaryColorDark = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        primaryColorAccent = ContextCompat.getColor(context, R.color.colorAccent)
        primaryPaint = Paint()
        primaryPaint.color = primaryColor
        primaryDarkPaint = Paint()
        primaryDarkPaint.color = primaryColorDark
        accentPaint = Paint()
        accentPaint.color = primaryColorAccent
        paintWhite = Paint()
        paintWhite.color = Color.WHITE
        paintWhite.textAlign = Paint.Align.CENTER
        randPaint = accentPaint
        paint = accentPaint
    }

    /**
     * Calling draw will hand the work off to the Draw class that is implemented
     * at that time.
     *
     * @param canvas our drawing surface's canvas.
     */
    fun draw(canvas: Canvas) {
        canvas.drawRect(rect, paint)
        val text = "Time: $time"
        canvas.drawText(text, centerX.toFloat(), (textSize * 2).toFloat(), paintWhite)
    }

    /**
     * On start of the application we can set the screen height and widtht.
     *
     * @param holder our surface holder object.
     * @param height height of our screen in pixels.
     * @param width  width of our screen in pixels.
     */
    fun surfaceChanged(holder: SurfaceHolder?, height: Int, width: Int) {
        // called when the surface is created for the thread
        screenHeight = height
        screenWidth = width

        // place targets
        initRects()
        initTextArea()
    }

    private fun initTextArea() {
        centerX = screenWidth / 2
        textSize = screenHeight / 20
        paintWhite.textSize = textSize.toFloat()
    }

    /**
     * If the application is in a state to accept touch events, handle them
     * here. Returning false means until the user presses down the handling is
     * over, returning true means the touch events will keep being fed to here.
     */
    fun onTouch(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            paint = getRandPaint()
        }
        return false
    }

    private fun getRandPaint() : Paint {
        return when (paint) {
            accentPaint -> primaryDarkPaint
            primaryDarkPaint -> primaryPaint
            else -> accentPaint
        }
    }



    private var randPaint: Paint = Paint()


    /**
     * Update the current objects, reset after screen cycle has ended.
     */
    fun updatePhysics() {
        time++
    }

    private fun initRects() {
        rect.top = 0
        rect.left = 0
        rect.right = screenWidth
        rect.bottom = screenHeight
    }

    /**
     * The user may have pressed the menu restart button this resets the factory
     * pattern created classes for physics and draw methods.
     */
    fun menuRestart() {
        time = 0
    }
}