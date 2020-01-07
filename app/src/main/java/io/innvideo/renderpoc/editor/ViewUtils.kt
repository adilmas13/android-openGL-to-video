package io.innvideo.renderpoc.editor

import android.graphics.SurfaceTexture
import android.view.TextureView
import android.view.View
import android.view.ViewTreeObserver

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

fun View.getWidthAndHeightAfterRender(callback: (Int, Int) -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (width > 0 && height > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                callback(width, height)
            }
        }
    })
}