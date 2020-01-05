package io.innvideo.renderpoc.poc

import android.content.Context
import android.opengl.GLES20
import io.innvideo.renderpoc.Layer

class RendererThread(
    surfaceTexture: Any,
    private val layers: MutableList<Layer>,
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

    private var eglCore = OpenGLCore(surfaceTexture)

    override fun run() {
        eglCore.init()
        isRunning = true

        if (layers.hasBackground()) {
            val layer = layers.getBackgroundLayer()
            setColor(layer.red, layer.green, layer.blue, 1f)
        }
        eglCore.swapBuffer()
        layers.forEach {


        }
        isRunning = false
        completionListener()
    }

    private fun List<Layer>.hasBackground() = this.indexOfFirst { it is Layer.Background } > -1

    private fun List<Layer>.getBackgroundLayer() = this.first { it is Layer.Background } as Layer.Background

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

    fun release() {
        isRunning = false
        eglCore.release()
    }
}