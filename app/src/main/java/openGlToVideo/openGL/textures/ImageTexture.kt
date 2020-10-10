package openGlToVideo.openGL.textures

import android.content.Context
import android.graphics.Bitmap
import openGlToVideo.newModels.parsed_models.LayerData


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