package io.innvideo.renderpoc.poc

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import io.innvideo.renderpoc.custom_views.BitmapTexture
import io.innvideo.renderpoc.utils.logIt

class RendererThread(
    surfaceTexture: Any,
    private var fullScreenVideoBitmapList: List<Bitmap>? = null,
    private var smallVideoBitmapList: List<Bitmap>? = null,
    private val context: Context,
    val completionListener: () -> Unit
) : Thread() {

    var vertexDataOne = floatArrayOf( // in counterclockwise order:
        -1.0f, -1.0f, 0.0f,  // bottom left
        1f, -1.0f, 0.0f,  // bottom right
        -1.0f, 1.0f, 0.0f,  // top left
        1.0f, 1.0f, 0.0f // top right
    )

    var vertexDataTwo = floatArrayOf( // in counterclockwise order:
        0.0f, 0.0f, 0.0f,  // bottom left
        1f, 0.0f, 0.0f,  // bottom right
        0.0f, 1.0f, 0.0f,  // top left
        1.0f, 1.0f, 0.0f // top right
    )

    private var isRunning = false

    private var eglCore = EglCore(surfaceTexture)

    override fun run() {
        eglCore.init()
        isRunning = true

        val red = getRandom() / 255.0f
        val green = getRandom() / 255.0f
        val blue = getRandom() / 255.0f

        this.fullScreenVideoBitmapList?.forEachIndexed { index, bitmap ->
            setColor(red, green, blue)
            BitmapTexture(context, vertexDataOne, bitmap).onSurfaceCreated().draw()
            if(smallVideoBitmapList!!.size-1 != index) {
                BitmapTexture(
                    context,
                    vertexDataTwo,
                    smallVideoBitmapList!![index]
                ).onSurfaceCreated()
                    .draw()
            }
            eglCore.swapBuffer()
            smallVideoBitmapList!![index].recycle()
            bitmap.recycle()
            sleep(20)
        }
        clearAllBitmaps()
        isRunning = false
        completionListener()
    }

    private fun clearAllBitmaps() {
        smallVideoBitmapList?.forEach {
            if (it.isRecycled.not()){
                it.recycle()
            }
        }
        fullScreenVideoBitmapList?.forEach {
            if (it.isRecycled.not()){
                it.recycle()
            }
        }
        smallVideoBitmapList = null
        fullScreenVideoBitmapList = null
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

    private fun getRandom() = (0..255).random()

    fun release() {
        isRunning = false
        eglCore.release()
    }
}