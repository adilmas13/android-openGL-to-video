package openGlToVideo

import android.content.Context
import android.opengl.GLES10.GL_COLOR_BUFFER_BIT
import android.opengl.GLES10.glClear
import android.opengl.GLES10.glClearColor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import openGlToVideo.constants.EditorConstants.LAYER
import openGlToVideo.constants.EditorConstants.LAYERS
import openGlToVideo.newModels.parsed_models.LayerData
import openGlToVideo.openGL.OpenGLCore
import openGlToVideo.openGL.textures.ImageTexture
import openGlToVideo.openGL.textures.TextTexture

class EditorRenderer(
    private val context: Context,
    surfaceTexture: Any,
    private val onRendererStarted: () -> Unit
) : Thread() {

    private var openGLCore = OpenGLCore(surfaceTexture)

    private lateinit var handler: Handler

    private val layers = mutableListOf<LayerData>()

    override fun run() {
        openGLCore.init()
        Looper.prepare()
        initCommunicator()
        onRendererStarted()
        Looper.loop()
    }

    private fun initCommunicator() {
        handler = Handler { msg ->
            msg.data?.let { bundle ->
                if (bundle.containsKey(LAYER)) {
                    layers.add(bundle.getParcelable(LAYER)!!)
                    renderOnScreen()
                }
                if (bundle.containsKey(LAYERS)) {
                    layers.addAll(bundle.getParcelableArrayList(LAYERS)!!)
                    renderOnScreen()
                }
            }
            true
        }
    }

    private fun renderOnScreen() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT)
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
        openGLCore.release()
        handler.looper.quit()
        this.interrupt()
    }

}