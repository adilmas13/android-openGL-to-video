package io.innvideo.renderpoc.custom_views

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


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

        // set background color
        setColor(0f, 1f, 0f, 1f)

        // create first object
        createObject {
            setPosition(0, 0, 100, 100)
            setColor(1.0f, 0.0f, 1.0f, 1.0f)
        }

        // create second object
        createObject {
            setPosition(200, 200, 100, 100)
            setColor(1.0f, 0.0f, 1.0f, 1.0f)
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        gl.glViewport(0, 0, width, height)
    }

    private fun setPosition(x: Int, y: Int, width: Int, height: Int) {
        GLES20.glScissor(x, y, width, height)
    }

    private fun setColor(red: Float = 1f, green: Float = 1f, blue: Float = 1f, alpha: Float = 1f) {
        GLES20.glClearColor(red, green, blue, alpha)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    private fun createObject(callback: () -> Unit) {
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST)
        callback()
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST)
    }

    override fun onDrawFrame(gl: GL10) {
        /*  gl.glActiveTexture(GL10.GL_TEXTURE0)
          gl.glBindTexture(GL10.GL_TEXTURE_2D, textures!![0])

          gl.glVertexPointer(3, GL10.GL_FLOAT, 0, VERTEX_BUFFER)
          gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, TEXCOORD_BUFFER)
          gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)*/
    }

}