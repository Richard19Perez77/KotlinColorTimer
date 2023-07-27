package com.drawing.kotlin

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment

class SurfaceFragment : Fragment() {
    /**
     * Will be used to hold the pixels to be drawn on.
     */
    var drawingSurface: DrawingSurface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
    }

    private val sharedPrefs: Unit
        private get() {
            val sharedpreferences: SharedPreferences = requireActivity().getSharedPreferences(
                "sharedPreferences", Context.MODE_PRIVATE
            )
            if (sharedpreferences.contains("time")) {
                drawingSurface!!.setTime(sharedpreferences.getLong("time", 0))
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_surface, container, false)
        // get reference to our surface
        drawingSurface = view.findViewById<View>(R.id.drawing_surface) as DrawingSurface
        drawingSurface!!.setTextView(view.findViewById<View>(R.id.pause_message) as TextView)
        drawingSurface!!.init()
        if (savedInstanceState != null) {
            drawingSurface!!.setTime(savedInstanceState.getLong("time"))
        }
        sharedPrefs
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        drawingSurface?.let { outState.putLong("time", it.getTime()) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.restart -> {
                drawingSurface?.menuRestart()
                return true
            }

            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        startAnimation()
        drawingSurface?.onResume()
    }

    private fun startAnimation() {
        // start the application out by modifying the view and don't accept user
        // input until the animation ends

        //animation has been not working on all devices... may need to remove
        //on start of app and use on restart button press or whatever you like
        val anim = AnimationUtils.loadAnimation(context, R.anim.intro)
        anim.reset()
        anim.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                // if the animation isn't finished we shouldn't be drawing on it
                drawingSurface?.introFinished  = false
            }

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                // this animation is longest and touch should be ready when this
                // completes
                drawingSurface?.introFinished  = true
            }
        })
        val anim2 = AnimationUtils.loadAnimation(context, R.anim.warp_in)
        anim2.reset()
        drawingSurface?.clearAnimation()
        //drawingSurface.startAnimation(anim);
        drawingSurface!!.introFinished = true
        drawingSurface!!.messageTextView!!.clearAnimation()
        drawingSurface!!.messageTextView!!.startAnimation(anim2)
    }

    override fun onPause() {
        super.onPause()
        drawingSurface?.onPause()
    }

    override fun onStop() {
        super.onStop()
        val sharedpreferences: SharedPreferences = requireActivity().getSharedPreferences(
            "sharedPreferences", Context.MODE_PRIVATE
        )
        val editor = sharedpreferences.edit()
        drawingSurface?.let { editor.putLong("time", it.getTime()) }
        editor.apply()
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SurfaceFragment.
         */
        fun newInstance(): SurfaceFragment {
            return SurfaceFragment()
        }
    }
}