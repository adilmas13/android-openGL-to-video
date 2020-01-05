package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import io.innvideo.renderpoc.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class Render3(val context: Context) : GLSurfaceView.Renderer {


    override fun onDrawFrame(gl: GL10) {
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
// Create an empty, mutable bitmap
        // Create an empty, mutable bitmap
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444)
// get a canvas to paint over the bitmap
        // get a canvas to paint over the bitmap
        val canvas = Canvas(bitmap)
        bitmap.eraseColor(0)

// get a background image from resources
// note the image format must match the bitmap format
        // get a background image from resources
// note the image format must match the bitmap format
        val background =
            context.resources.getDrawable(R.drawable.shape_rounded)
        background.setBounds(0, 0, 256, 256)
        background.draw(canvas) // draw the background to our bitmap


// Draw the text
        // Draw the text
        val textPaint = Paint()
        textPaint.textSize = 32f
        textPaint.isAntiAlias = true
        textPaint.setARGB(255, 0, 255, 255)
// draw the text centered
        // draw the text centered
        canvas.drawText("Hello World", 16f, 112f, textPaint)
        val textures = IntArray(1)
//Generate one texture pointer...
        //Generate one texture pointer...
        gl.glGenTextures(1, textures, 0)
//...and bind it to our array
        //...and bind it to our array
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures.get(0))

//Create Nearest Filtered Texture
        //Create Nearest Filtered Texture
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_NEAREST.toFloat()
        )
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())

//Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
        //Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT.toFloat())

//Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
        //Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)

//Clean up
        //Clean up
        bitmap.recycle()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {

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
}