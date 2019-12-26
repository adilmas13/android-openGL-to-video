package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import androidx.core.content.ContextCompat
import io.innvideo.renderpoc.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyRenderer(val context: Context) : GLSurfaceView.Renderer {
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
        Log.d("LOG_IT", "onSurfaceCreated")
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        val options = BitmapFactory.Options()
        options.inScaled = false
        // Read in the resource
        val bitmap =
            drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.shape_rounded)!!)
        if (bitmap != null) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            // bind the texture and set parameters
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

            GLES20.glEnable(GLES20.GL_BLEND);

            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            //Create Nearest Filtered Texture
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )

            //Different possible texture parameters, e.g. GLES20.GL_CLAMP_TO_EDGE
//            GLES20.glTexParameteri(
//                GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_WRAP_S,
//                GLES20.GL_REPEAT
//            )
//            GLES20.glTexParameteri(
//                GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_WRAP_T,
//                GLES20.GL_REPEAT
//            )
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0)

            bitmap.recycle()

            // Initialize the accumulated rotation matrix
            // Initialize the accumulated rotation matrix
            //  Matrix.setIdentityM(t, 0)

        }
        // set background color
        //   setColor(0f, 1f, 0f, 1f)

        // create first object
        /* createObject {
             setPosition(0, 0, 100, 100)
             setColor(1.0f, 0.0f, 1.0f, 1.0f)
         }*/

        // create second object
        /*createObject {
            setPosition(200, 200, 100, 100)
            setColor(1.0f, 0.0f, 1.0f, 1.0f)
        }*/
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        Log.d("LOG_IT", "onSurfaceChanged")
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

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onDrawFrame(gl: GL10) {
        Log.d("LOG_IT", "onDrawFrame")
        /*  gl.glActiveTexture(GL10.GL_TEXTURE0)
          gl.glBindTexture(GL10.GL_TEXTURE_2D, textures!![0])

          gl.glVertexPointer(3, GL10.GL_FLOAT, 0, VERTEX_BUFFER)
          gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, TEXCOORD_BUFFER)
          gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)*/


    }

}