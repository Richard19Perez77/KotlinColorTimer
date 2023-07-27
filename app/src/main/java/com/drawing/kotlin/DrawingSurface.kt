package com.drawing.kotlin

import android.content.Context
import android.graphics.Canvas
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView

/**
 *
 * The drawing surface class will be used to hold the thread and handler for
 * message to the UI.
 *
 * @author Rick
 */
class DrawingSurface(context: Context, attrs: AttributeSet) :
    SurfaceView(context, attrs), SurfaceHolder.Callback {
    /**
     * The thread that runs the cycle of run and update physics during the
     * applications lifetime.
     */
    var updateThread: UpdateThread

    /**
     * The targeting class holds most of the application logic and determines
     * what logic to use.
     */
    var controller: Controller = Controller()

    /**
     * Sends message to the UI via the thread
     */
    var myHandler: Handler

    /**
     * Set the message to the UI here.
     */
    var messageTextView: TextView? = null

    /**
     * Some logic like handling user touch has to wait until the intro animation
     * is finished.
     */
    var introFinished = false

    /**
     * As the application resumes it may need to recreate our thread.
     */
    var recreateThread = false

    /**
     * Used to toggle when the window is in focus for touch events.
     */
    var touchReady = false
    fun init() {
        controller.init(context)
    }

    /**
     * A class to handle setting the message and them showing the message.
     *
     */
    internal inner class IncomingHandlerCallback : Handler.Callback {
        /**
         * The Message object can contain more than one value.
         */
        override fun handleMessage(m: Message): Boolean {
            // handle message code
            val visible = if (m.data.getInt("show") == 0) VISIBLE else INVISIBLE
            messageTextView!!.visibility = visible
            messageTextView!!.text = m.data.getString("message")
            return true
        }
    }

    /**
     *
     * If we are creating our surface by calling the setContentView in the
     * MainActivity then you must have a constructor in this class that accepts
     * two parameters.
     *
     * @param context
     * The application Context.
     * @param attrs
     * XML defined attributes can be sent though here.
     */
    init {
        // register the call back interface
        val holder = holder
        holder.addCallback(this)

        // prepare the thread and its message handler (handlers can also execute
        // code if needed)
        myHandler = Handler(IncomingHandlerCallback())
        updateThread = UpdateThread(getHolder(), context, myHandler, this)
    }

    /**
     * Our draw class uses a canvas to draw on and we pass the work to our
     * targeting class.
     */
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        controller.draw(canvas)
    }

    /**
     * When the surface is created we should have a new thread from our class
     * constructor but if it was running and terminated then need to recreate
     * it.
     */
    override fun surfaceCreated(holder: SurfaceHolder) {
        if (updateThread.state === Thread.State.TERMINATED) {
            updateThread = UpdateThread(holder, context, myHandler, this)
            updateThread.start()
            updateThread.setRunning(true)
        } else if (updateThread.state === Thread.State.NEW) {
            updateThread.start()
            updateThread.setRunning(true)
        }
    }

    /**
     * Screen dimensions are set at this point and we can record them in the
     * targeting class.
     */
    override fun surfaceChanged(
        holder: SurfaceHolder, format: Int, width: Int,
        height: Int
    ) {
        controller.surfaceChanged(holder, height, width)
    }

    /**
     * Surface is destroyed and we can let the thread run out its execution
     * path.
     */
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        updateThread.setRunning(false)
        while (retry) {
            try {
                updateThread.join()
                retry = false
            } catch (_: InterruptedException) {
            }
        }
    }

    /**
     * Actual physics are encapsulated in the targeting class.
     */
    fun updatePhysics() {
        controller.updatePhysics()
    }

    /**
     * Allow the phone to execute accessibility methods. You should should make
     * sure the view objects in the UI have a concise but meaningful content
     * description in the layout XML.
     */
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    /**
     * The touch handler for the surface.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        synchronized(updateThread.mSurfaceHolder) {

            // if restarting the thread may not be valid, surface created will
            // not be called to do this for us.
            if (recreateThread) {
                recreateThread = false
                if (updateThread.state === Thread.State.TERMINATED) {
                    updateThread = UpdateThread(
                        holder, context,
                        myHandler, this
                    )
                    updateThread.start()
                    updateThread.setRunning(true)
                } else if (updateThread.state === Thread.State.NEW) {
                    updateThread.start()
                    updateThread.setRunning(true)
                }
            }
            if (event.action == MotionEvent.ACTION_UP) {
                performClick()
            }

            if (introFinished && touchReady) {
                when (updateThread.mMode) {
                    UpdateThread.STATE_PAUSE -> updateThread.setState(UpdateThread.STATE_RUNNING)
                    UpdateThread.STATE_RUNNING -> return controller.onTouch(event)
                }
            }

            /**
             * If the intro is finished we can perform actions on the touch
             * events.
             */
            /**
             * If the intro is finished we can perform actions on the touch
             * events.
             */
            return super.onTouchEvent(event)
        }
    }

    /**
     * The TextView object here will hold the messages to the user.
     *
     * @param textView
     * Holds messages from the handler.
     */
    fun setTextView(textView: TextView?) {
        messageTextView = textView
    }

    /**
     * Used when the surface view is in focus and ready to handle touch events
     * or has lost focus and will not handle touch events.
     */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        touchReady = hasWindowFocus
    }

    /**
     * When the user presses restart in the menu options we handle it in
     * targeting.
     */
    fun menuRestart() {
        controller.menuRestart()
    }

    /**
     * We need to set the flag to recreate our thread here, we want the method
     * to be lightweight so we wait to create it on touch when animation is
     * needed.
     */
    fun onResume() {
        if (updateThread.state === Thread.State.TERMINATED) {
            recreateThread = true
        }
    }

    /**
     * When we pause the thread we will set a flag and send a message to the
     * user.
     */
    fun onPause() {
        if (updateThread != null) {
            updateThread.pause()
            updateThread.setRunning(false)
        }
    }

    fun getTime(): Long {
        return controller.time
    }

    fun setTime(time: Long) {
        controller.time = time
    }
}