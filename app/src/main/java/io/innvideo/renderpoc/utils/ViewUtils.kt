package io.innvideo.renderpoc.utils

import android.graphics.SurfaceTexture
import android.view.TextureView

fun TextureView.onSurfaceTextureAvailable(callback: (surfaceTexture: SurfaceTexture, width: Int, height: Int) -> Unit): TextureView.SurfaceTextureListener {
    val listener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture?,
            width: Int,
            height: Int
        ) = Unit

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = true

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            callback(surface, width, height)
        }
    }
    this.surfaceTextureListener = listener
    return listener
}