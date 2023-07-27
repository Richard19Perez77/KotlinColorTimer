package com.drawing.kotlin

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.SurfaceHolder
import android.view.View

import com.drawing.kotlin.R

/**
 *
 * A class to run the update physics and draw loop.
 *
 * @author Rick
 */
class UpdateThread(
    /**
     * A reference to the surface we are working on, we synchronize on this to
     * ensure it is valid.
     */
    var mSurfaceHolder: SurfaceHolder, c: Context,
    /**
     * Sends message to the UI via the thread
     */
    var mHandler: Handler, drawSurface: DrawingSurface
) : Thread() {
    /**
     * The context object allows access to the resources needed and also
     * contains information about the application.
     */
    var context: Context

    /**
     * The thread that runs the cycle of run and update physics during the
     * applications lifetime.
     */
    private val drawingSurface: DrawingSurface

    /**
     * Flag that is used to tell the thread to start updating or drawing.
     */
    var mRun = false

    /**
     * Mode states tell us what implementation of the game we should be using
     * and when to send messages to the UI.
     */
    var mMode: Int

    /**
     * Constructs a new thread for our application loop.
     *
     * @param surfaceHolder
     * reference to the drawing surface's holder.
     * @param c
     * our application context.
     * @param handler
     * the message handler to show messages or run code.
     * @param drawSurface
     * the actual surface we are drawing on.
     */
    init {
        mHandler = mHandler
        context = c
        mMode = STATE_PAUSE
        drawingSurface = drawSurface
    }

    /**
     * The loop here if running will draw the screen to the user but not animate
     * it until the mode has changed.
     */
    override fun run() {
        while (mRun) {
            var c: Canvas? = null
            try {
                c = mSurfaceHolder.lockCanvas(null)
                if (c != null) {
                    synchronized(mSurfaceHolder) {
                        if (mMode == STATE_RUNNING) drawingSurface.updatePhysics()
                        drawingSurface.draw(c)
                    }
                }
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c)
                }
            }
        }
    }

    /**
     * Sets the flag to let us know if the thread should start drawing or
     * updating.
     *
     * @param b
     */
    fun setRunning(b: Boolean) {
        synchronized(mSurfaceHolder) { mRun = b }
    }

    /**
     * Set a new application state.
     *
     * @param mode
     * the state to set.
     */
    fun setState(mode: Int) {
        synchronized(mSurfaceHolder) { setState(mode, null) }
    }

    /**
     * Sets the state and creates a message to the UI if needed. Use the Bundle
     * object to carry the message and if to show it.
     *
     * @param mode
     * the new state.
     * @param message
     * the message to the UI.
     */
    fun setState(mode: Int, message: CharSequence?) {
        synchronized(mSurfaceHolder) {
            mMode = mode
            val msg: Message
            val bundle: Bundle
            when (mMode) {
                STATE_RUNNING -> {
                    msg = mHandler.obtainMessage()
                    val b = Bundle()
                    b.putString("message", "")
                    b.putInt("show", View.INVISIBLE)
                    msg.data = b
                    mHandler.sendMessage(msg)
                }

                STATE_PAUSE -> {
                    val res = context.resources
                    var str: CharSequence = ""
                    str = res.getText(R.string.message_text)
                    if (message != null) {
                        str = """
                            $message
                            $str
                            """.trimIndent()
                    }
                    msg = mHandler.obtainMessage()
                    bundle = Bundle()
                    bundle.putString("message", str.toString())
                    bundle.putInt("show", View.VISIBLE)
                    msg.data = bundle
                    mHandler.sendMessage(msg)
                }

                else -> {}
            }
        }
    }

    /**
     * Handles putting the thread into a pause state.
     */
    fun pause() {
        synchronized(mSurfaceHolder) { setState(STATE_PAUSE) }
    }

    companion object {
        /**
         * Thread is paused.
         */
        const val STATE_PAUSE = 0

        /**
         * Thread is running
         */
        const val STATE_RUNNING = 1
    }
}