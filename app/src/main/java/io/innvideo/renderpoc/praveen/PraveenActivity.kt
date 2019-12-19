package io.innvideo.renderpoc.praveen

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import io.innvideo.renderpoc.R
import kotlinx.android.synthetic.main.activity_praveen.*

class PraveenActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_praveen)
        initViews()
    }

    private fun initViews() {
        glSurfaceView.setRenderer(GLRenderer())
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

}