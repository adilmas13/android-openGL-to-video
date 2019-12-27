package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.gles.Triangle
import io.innvideo.renderpoc.gles.utils.ELUtils
import io.innvideo.renderpoc.gles.utils.GLSLTextReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class Render4(val context: Context) : GLSurfaceView.Renderer {

    private lateinit var mTriangle: Triangle

    override fun onDrawFrame(gl: GL10) {
        mTriangle.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
// initialize a triangle
        mTriangle = Triangle(context)
    }

}