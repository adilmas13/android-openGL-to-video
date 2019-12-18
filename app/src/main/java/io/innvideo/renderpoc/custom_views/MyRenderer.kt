package io.innvideo.renderpoc.custom_views

import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyRenderer : GLSurfaceView.Renderer {
    override fun onDrawFrame(gl: GL10?) {
        // Redraw background color

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val color = Color.parseColor("#FFFF00")
        val red = Color.red(color) / 255.0f
        val green = Color.green(color) / 255.0f
        val blue = Color.blue(color) / 255.0f
        // Set the background frame color
        GLES20.glClearColor(red, green, blue, 1.0f) // takes values from 0 - 1
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

}