package io.innvideo.renderpoc.gles

import android.content.Context
import io.innvideo.renderpoc.custom_views.ImageTexture
import io.innvideo.renderpoc.custom_views.TextTexture
import io.innvideo.renderpoc.editor.new_models.parsed_models.LayerData
import io.innvideo.renderpoc.poc.OpenGLCore

class EditorRenderer(
    surfaceTexture: Any,
    private val layers: MutableList<LayerData>,
    private val context: Context,
    val completionListener: () -> Unit
) : Thread() {

    private var isRunning = false

    private var glCore = OpenGLCore(surfaceTexture)

    override fun run() {
        glCore.init()
        isRunning = true
        layers.forEach {
            if (it is LayerData.Image) {
                ImageTexture(context, it).draw()
            }
            if (it is LayerData.Text) {
                TextTexture(context, it).draw()
            }
        }
        glCore.swapBuffer()
        isRunning = false
        completionListener()
    }

    fun release() {
        isRunning = false
        glCore.release()
    }

}