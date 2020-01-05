package io.innvideo.renderpoc.gles

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.api.load
import com.google.gson.Gson
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.gles.utils.GLSLTextReader
import io.innvideo.renderpoc.new_models.parsed_models.LayerData
import io.innvideo.renderpoc.new_models.parsed_models.MainUiData
import io.innvideo.renderpoc.new_models.response_models.BlockResponseModel
import io.innvideo.renderpoc.new_models.response_models.ParentResponseModel
import io.innvideo.renderpoc.utils.getWidthAndHeightAfterRender
import io.innvideo.renderpoc.utils.onSurfaceTextureAvailable
import io.innvideo.renderpoc.utils.toastIt
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditorActivity : AppCompatActivity() {

    private lateinit var uiData: MainUiData

    private var touchListener = View.OnTouchListener { v, event ->
        // Convert touch coordinates into normalized device
        // coordinates, keeping in mind that Android's Y
        // coordinates are inverted.
        val normalizedX = event.x / v.width.toFloat() * 2 - 1
        val normalizedY = -(event.y / v.height.toFloat() * 2 - 1)

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        canvas.setOnTouchListener(touchListener)
        canvas.onSurfaceTextureAvailable { _, _, _ -> start() }
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
        canvasParent.getWidthAndHeightAfterRender { width, height ->
            setCanvasDimension(width, height)
            getAllBitmaps { setLayersOnScreen() }
        }
    }

    private fun getAllBitmaps(success: () -> Unit) {
        var counter = 0
        uiData.layers.forEach { layer ->
            if ((layer is LayerData.Text).not()) {
                Coil.load(this, (layer as LayerData.Image).image) {
                    target {
                        layer.bitmap = it.toBitmap()
                        if (++counter == 2) {
                            success()
                        }
                    }
                }
            }
        }
    }

    private fun setLayersOnScreen() {
        val renderer = EditorRenderer(canvas.surfaceTexture, uiData.layers, this) {}
        renderer.start()
    }

    private fun setCanvasDimension(width: Int, height: Int) {
        // setting the canvas size
        val params = canvas.layoutParams as ConstraintLayout.LayoutParams
        val ratio = if (height > width) "H,${uiData.dimension}" else "W,${uiData.dimension}"
        params.dimensionRatio = ratio
        canvas.layoutParams = params
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
                    val layer = when (temp.type) {
                        "text" -> LayerData.Text(temp.text ?: "").apply {
                            fontFamily =
                                ResourcesCompat.getFont(this@EditorActivity, R.font.muli_bold)
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

    private fun extractAudio(block: BlockResponseModel) {
        uiData.audioData.apply {
            url = block.url ?: ""
        }
    }
}
