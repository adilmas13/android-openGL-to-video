package io.innvideo.renderpoc.new_models.parsed_models

import android.graphics.Bitmap
import android.graphics.Typeface
import io.innvideo.renderpoc.new_models.response_models.PositionResponseModel

class MainUiData {
    var dimension = "1:1"
    var audioData = AudioData()
    var layers = mutableListOf<LayerData>()
}

class AudioData {
    var url = ""
}

sealed class LayerData {
    class Image(val image: String, var bitmap: Bitmap? = null) : LayerData()
    class Text(val text: String) : LayerData() {
        var fontFamily: Typeface? = null
    }

    companion object {
        private const val PER_VERTEX_SIZE = 3
        private const val NUMBER_OF_VERTICES = 4
    }

    var coordinates = FloatArray(NUMBER_OF_VERTICES * PER_VERTEX_SIZE)
    var type: String = ""
    private var topLeft = FloatArray(PER_VERTEX_SIZE)     // topX & topY
    private var bottomLeft = FloatArray(PER_VERTEX_SIZE)  // topX & bottomY
    private var bottomRight = FloatArray(PER_VERTEX_SIZE) // bottomX & bottomY
    private var topRight = FloatArray(PER_VERTEX_SIZE)    // bottomX & topY

    fun setPosition(position: PositionResponseModel) {
        topLeft[0] = (-(0.5 - position.topX) * 2).toFloat()
        topLeft[1] = ((0.5 - position.topY) * 2).toFloat()

        bottomLeft[0] = (-(0.5 - position.topX) * 2).toFloat()
        bottomLeft[1] = ((0.5 - position.bottomY) * 2).toFloat()

        bottomRight[0] = (-(0.5 - position.bottomX) * 2).toFloat()
        bottomRight[1] = ((0.5 - position.bottomY) * 2).toFloat()

        topRight[0] = (-(0.5 - position.bottomX) * 2).toFloat()
        topRight[1] = ((0.5 - position.topY) * 2).toFloat()

        coordinates = bottomLeft + bottomRight + topLeft + topRight

    }
}