package io.innvideo.renderpoc.editor

import android.content.Context
import io.innvideo.renderpoc.editor.new_models.parsed_models.LayerData
import io.innvideo.renderpoc.editor.openGL.OpenGLCore
import io.innvideo.renderpoc.editor.openGL.textures.ImageTexture
import io.innvideo.renderpoc.editor.openGL.textures.TextTexture

class EditorRenderer(
    private val context: Context,
    surfaceTexture: Any,
    private val layers: MutableList<LayerData>,
    val completionListener: () -> Unit
) : Thread() {

    private var isRunning = false

    private var openGLCore = OpenGLCore(surfaceTexture)

    override fun run() {
        openGLCore.init()
        isRunning = true
        layers.forEach { layer ->
            when (layer) {
                is LayerData.Image -> ImageTexture(context, layer).draw()
                is LayerData.Text -> TextTexture(context, layer).draw()
            }
        }
        openGLCore.swapBuffer()
        isRunning = false
        completionListener()
    }

    fun release() {
        isRunning = false
        openGLCore.release()
    }

}