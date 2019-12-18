package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.opengl.GLSurfaceView

class MyGLSurface(context: Context) : GLSurfaceView(context) {

    init {
        setEGLContextClientVersion(2)
    }
}