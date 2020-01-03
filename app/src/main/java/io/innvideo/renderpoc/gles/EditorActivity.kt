package io.innvideo.renderpoc.gles

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.gles.utils.GLSLTextReader
import io.innvideo.renderpoc.new_models.parsed_models.LayerData
import io.innvideo.renderpoc.new_models.parsed_models.MainUiData
import io.innvideo.renderpoc.new_models.response_models.BlockResponseModel
import io.innvideo.renderpoc.new_models.response_models.ParentResponseModel
import io.innvideo.renderpoc.utils.toastIt
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorActivity : AppCompatActivity() {

    private lateinit var uiData: MainUiData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        start()
    }

    private fun start() {
        progress.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val response = fetchJsonResponse()
            if (response != null) {
                parseToUiModel(response)
            }
            withContext(Dispatchers.Main) {
                if (response == null) {
                    toastIt("You fucked up!! Search for a new job")
                } else {
                    setParsedDataOnUi()
                }
                progress.visibility = View.GONE
            }
        }
    }

    private fun fetchJsonResponse(): ParentResponseModel? {
        val stringResponse =
            GLSLTextReader.readGlslFromRawRes(this@EditorActivity, R.raw.sample_mobile)
        return Gson().fromJson(
            stringResponse,
            ParentResponseModel::class.java
        )
    }

    private fun setParsedDataOnUi() {

    }

    private fun parseToUiModel(data: ParentResponseModel) {
        uiData = MainUiData()
        // add first dimension from array to main object
        if (data.dimensions.isNotEmpty()) {
            uiData.dimension = data.dimensions.first()
        }

        if (data.blocks.isNotEmpty()) {
            data.blocks.forEach { block ->
                if (block.type != "audio") {
                    val layers = setLayers(block)
                    uiData.layers.addAll(layers)
                } else {
                    extractAudio(block)
                }
            }
        }
    }

    private fun setLayers(block: BlockResponseModel): MutableList<LayerData> {
        val layers = mutableListOf<LayerData>()
        if (block.type == "custom_block" && block.components.isNotEmpty()) {
            // start adding the layers
            block.components.forEach {
                if (it.type == "composite" && it.components.isNotEmpty()) {
                    val temp = it.components[it.components.size - 1]
                    val layer = LayerData()
                    layer.type = temp.type
                    layer.setPosition(it.position)
                    layers.add(layer)
                }
            }
        }
        return layers
    }

    private fun extractAudio(block: BlockResponseModel) {
        uiData.audioData.apply {
            url = block.url ?: ""
        }
    }
}
