package io.innvideo.renderpoc.gles

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.innvideo.renderpoc.custom_views.MyGLSurface
import io.innvideo.renderpoc.custom_views.Render7
import io.innvideo.renderpoc.custom_views.Render8


class LearningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = MyGLSurface(this)
        setContentView(view)
        view.setEGLContextClientVersion(2)
        view.setRenderer(Render7(this))
        view.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}
