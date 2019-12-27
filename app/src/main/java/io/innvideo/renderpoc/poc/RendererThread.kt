package io.innvideo.renderpoc.poc

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import io.innvideo.renderpoc.custom_views.BitmapTexture
import io.innvideo.renderpoc.utils.logIt

class RendererThread(
    surfaceTexture: Any,
    private val list: List<Bitmap>? = null,
    private val context: Context,
    val completionListener: () -> Unit
) : Thread() {

    private var isRunning = false

    private var eglCore = EglCore(surfaceTexture)

    override fun run() {
        logIt("=== STARTED ===")
        eglCore.init()
        isRunning = true

        val red = getRandom() / 255.0f
        val green = getRandom() / 255.0f
        val blue = getRandom() / 255.0f

        this.list?.forEach {
            setColor(red, green, blue)
            BitmapTexture(context, it).onSurfaceCreated().draw()
            eglCore.swapBuffer()
            sleep(20)
        }
        isRunning = false
        completionListener()
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

    interface CompletionListener {
        fun onCompleted()
    }
}