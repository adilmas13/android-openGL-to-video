package openGlToVideo.parser

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import openGlToVideo.constants.EditorConstants.TYPE_AUDIO
import openGlToVideo.constants.EditorConstants.TYPE_COMPOSITE
import openGlToVideo.constants.EditorConstants.TYPE_CUSTOM_BLOCK
import openGlToVideo.constants.EditorConstants.TYPE_TEXT
import openGlToVideo.newModels.parsed_models.LayerData
import openGlToVideo.newModels.parsed_models.MainUiData
import openGlToVideo.newModels.response_models.BlockResponseModel
import openGlToVideo.newModels.response_models.ParentResponseModel
import io.opengltovideo.R

class EditorDataParser(private val context: Context, private val data: ParentResponseModel) {

    private var uiData = MainUiData()

    fun parse(): MainUiData {

        // add first dimension from array to main object
        if (data.dimensions.isNotEmpty()) {
            uiData.dimension = data.dimensions.first()
        }

        if (data.blocks.isNotEmpty()) {
            data.blocks.forEach { block ->
                if (block.type != TYPE_AUDIO) {
                    val layers = setLayers(block)
                    uiData.layers.addAll(layers)
                } else {
                    extractAudio(block)
                }
            }
        }
        return uiData
    }

    private fun setLayers(block: BlockResponseModel): MutableList<LayerData> {
        val layers = mutableListOf<LayerData>()
        if (block.type == TYPE_CUSTOM_BLOCK && block.components.isNotEmpty()) {
            // start adding the layers
            block.components.forEach {
                if (it.type == TYPE_COMPOSITE && it.components.isNotEmpty()) {
                    val temp = it.components[it.components.size - 1]
                    val layer = when (temp.type) {
                        TYPE_TEXT -> LayerData.Text(temp.text ?: "").apply {
                            fontFamily = ResourcesCompat.getFont(context, R.font.muli_bold)
                        }
                        else -> LayerData.Image(temp.url ?: "")
                    }
                    layer.type = temp.type
                    layer.setPosition(it.position)
                    layers.add(layer)
                }
            }
        }
        return layers
    }

    private fun extractAudio(block: BlockResponseModel) =
        uiData.audioData.apply { url = block.url ?: "" }
}