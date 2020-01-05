package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.graphics.Bitmap
import io.innvideo.renderpoc.new_models.parsed_models.LayerData


class ImageTexture(
    context: Context,
    private val layerData: LayerData.Image
) :
    BaseTexture(context, layerData.coordinates) {

    override fun getBitmap(): Bitmap {
        if (layerData.bitmap == null) {
            throw IllegalStateException("Bitmap is missing")
        } else {
            return layerData.bitmap!!
        }
    }

}