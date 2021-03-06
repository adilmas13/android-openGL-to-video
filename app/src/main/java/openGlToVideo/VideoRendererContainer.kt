package openGlToVideo

import android.content.Context
import android.opengl.GLES10.GL_COLOR_BUFFER_BIT
import android.opengl.GLES10.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES10.glClear
import android.opengl.GLES10.glClearColor
import android.os.Bundle
import android.os.Handler
import openGlToVideo.constants.EditorConstants.LAYER
import openGlToVideo.constants.EditorConstants.LAYERS
import openGlToVideo.newModels.parsed_models.LayerData
import openGlToVideo.newModels.parsed_models.MainUiData
import openGlToVideo.openGL.OpenGLCore
import openGlToVideo.openGL.textures.ImageTexture
import openGlToVideo.openGL.textures.TextTexture

class VideoRendererContainer(
    private val context: Context,
    private val uiData: MainUiData,
    surfaceTexture: Any,
    private val onCompleted: () -> Unit
) : Thread() {

    private var openGLCore = OpenGLCore(surfaceTexture)

    private lateinit var handler: Handler

    private val layers = mutableListOf<LayerData>()

    private var isRunning = true

    override fun run() {
        try {
            openGLCore.init()
            layers.addAll(uiData.layers)
            while (isRunning) {
                logIt("RUNNING")
                renderOnScreen()
                sleep(33)
            }
            logIt("UI RENDERING COMPLETED => ")
            onCompleted()
        } catch (e: Throwable) {
            e.printStackTrace()
            logIt("THREAD EXCEPTION")
        } finally {
            logIt("FINAL BLOCK")
        }
    }

    private fun renderOnScreen() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT)
        glClear(GL_DEPTH_BUFFER_BIT)
        layers.forEach { layer ->
            when (layer) {
                is LayerData.Image -> ImageTexture(context, layer).draw()
                is LayerData.Text -> TextTexture(context, layer).draw()
            }
        }
        openGLCore.swapBuffer()
    }

    fun addLayer(layer: LayerData) {
        val message = handler.obtainMessage(0)
        message.data = Bundle().apply { putParcelable(LAYER, layer) }
        handler.sendMessage(message)
    }

    fun addLayers(layers: MutableList<LayerData>) {
        val message = handler.obtainMessage(0)
        message.data = Bundle().apply { putParcelableArrayList(LAYERS, ArrayList(layers)) }
        handler.sendMessage(message)
    }

    fun release() {
        isRunning = false
        openGLCore.release()
//        handler.looper.quit()
      //  this.interrupt()
    }

}