package io.innvideo.renderpoc.gles

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.api.load
import io.innvideo.renderpoc.custom_views.BitmapTexture
import io.innvideo.renderpoc.custom_views.TextTexture
import io.innvideo.renderpoc.new_models.parsed_models.LayerData
import io.innvideo.renderpoc.poc.EglCore

class EditorRenderer(
    surfaceTexture: Any,
    private val layers: MutableList<LayerData>,
    private val context: Context,
    val completionListener: () -> Unit
) : Thread() {

    private var isRunning = false

    private var eglCore = EglCore(surfaceTexture)

    override fun run() {
        eglCore.init()
        isRunning = true
        layers.forEach {
            if (it is LayerData.Image) {
                BitmapTexture(
                    context,
                    it.coordinates,
                    it.bitmap!!
                ).onSurfaceCreated().draw()
            }
            if (it is LayerData.Text) {
                TextTexture(context, it).onSurfaceCreated().draw()
            }
        }
        eglCore.swapBuffer()
        isRunning = false
        completionListener()
    }

    fun release() {
        isRunning = false
        eglCore.release()
    }

    fun String.getBitmap(context: Context, success: (Bitmap) -> Unit) {
        Coil.load(context, this) {
            target { drawable ->
                success(drawable.toBitmap())
            }
        }
    }

}