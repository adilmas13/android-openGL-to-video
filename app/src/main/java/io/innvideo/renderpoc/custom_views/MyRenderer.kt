package io.innvideo.renderpoc.custom_views

import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.R
import android.graphics.BitmapFactory
import android.opengl.GLUtils
import java.nio.ByteOrder.nativeOrder
import java.nio.ByteBuffer.allocateDirect
import android.R.attr.order
import android.os.Environment
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.graphics.Bitmap





class MyRenderer : GLSurfaceView.Renderer {
//    private var i = 0;
//
//    override fun onDrawFrame(gl: GL10?) {
//        // Redraw background color
////        drawOnGL()
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//        drawOnGL()
//
//
//    }
//
//    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
//    }
//
//    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//        // takes values from 0 - 1
//        drawOnGL()
//
//    }
//
//    private fun drawOnGL() {
//        i++
//        if(i == 25) {
//            i = 0
//            val red = getRandom() / 255.0f
//            val green = getRandom() / 255.0f
//            val blue = getRandom() / 255.0f
//            GLES20.glClearColor(red, green, blue, 1.0f)
//            GLES20.gldr
//        }
//
//    }
//
//
//    private fun getRandom() = (0..255).random();

    private var textures: IntArray? = null


    private val VERTEX_COORDINATES =
        floatArrayOf(-1.0f, +1.0f, 0.0f, +1.0f, +1.0f, 0.0f, -1.0f, -1.0f, 0.0f, +1.0f, -1.0f, 0.0f)

    private val TEXTURE_COORDINATES = floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)

    private val TEXCOORD_BUFFER = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDINATES).rewind()
    private val VERTEX_BUFFER = ByteBuffer.allocateDirect(VERTEX_COORDINATES.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTEX_COORDINATES).rewind()

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        textures = IntArray(1)
        gl.glEnable(GL10.GL_TEXTURE_2D)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)

        gl.glGenTextures(1, textures, 0)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures!![0])

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )

//        val bitmap = BitmapFactory.decodeFile("${Environment.getExternalStorageDirectory()}/aa/image.jpg")
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap . Config . ARGB_8888
        val bitmap = BitmapFactory.decodeFile("${Environment.getExternalStorageDirectory()}/aa/image.jpg", options)
        GLUtils.texImage2D(
            GL10.GL_TEXTURE_2D,
            0,
            bitmap,
            0
        )
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        gl.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        gl.glActiveTexture(GL10.GL_TEXTURE0)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures!![0])

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, VERTEX_BUFFER)
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, TEXCOORD_BUFFER)
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)
    }

}