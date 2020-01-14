package io.innvideo.renderpoc.editor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.api.load
import com.google.gson.Gson
import io.innvideo.renderpoc.BuildConfig
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.editor.new_models.parsed_models.LayerData
import io.innvideo.renderpoc.editor.new_models.parsed_models.MainUiData
import io.innvideo.renderpoc.editor.new_models.response_models.ParentResponseModel
import io.innvideo.renderpoc.editor.openGL.utils.GLSLTextReader.Companion.readGlslFromRawRes
import io.innvideo.renderpoc.editor.parser.EditorDataParser
import io.innvideo.renderpoc.editor.video_renderer.VideoRenderer
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditorActivity : AppCompatActivity() {

    private lateinit var uiData: MainUiData

    private lateinit var renderer: EditorRenderer

    private lateinit var sharableFilepath: String

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
        group.visibility = View.GONE
        tvShare.setOnClickListener { shareVideo() }
        canvas.setOnTouchListener(touchListener)
        canvas.onSurfaceTextureAvailable { _, _, _ -> start() }
        btnExport.setOnClickListener {
            group.visibility = View.VISIBLE
            tvShare.visibility = View.GONE
            val thread = Thread(Runnable {
                VideoRenderer(this, uiData)
                    .withAudio()
                    .onCompleted {
                        sharableFilepath = it
                        group.visibility = View.GONE
                        tvShare.visibility = View.VISIBLE
                        toastIt("Video Rendered")
                    }
                    .onError { toastIt("Failed to create video") }
                    .enableDebug(BuildConfig.DEBUG)
                    .render()
            })
            thread.start()
        }
    }

    private fun shareVideo() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        val screenshotUri: Uri = Uri.parse(sharableFilepath)
        sharingIntent.type = "video/mp4"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri)
        startActivity(Intent.createChooser(sharingIntent, "Share using"))
    }

    private fun start() {
        progress.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val response = fetchJsonResponse()
            if (response != null) {
                uiData = EditorDataParser(this@EditorActivity, response).parse()
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

    private fun fetchJsonResponse() = Gson().fromJson(
        readGlslFromRawRes(this@EditorActivity, R.raw.sample_mobile),
        ParentResponseModel::class.java
    )

    private fun setParsedDataOnUi() {
        canvasParent.getWidthAndHeightAfterRender { width, height ->
            setCanvasDimension(width, height)
            getAllBitmaps { startScreenRenderer() }
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

    private fun startScreenRenderer() {
        renderer = EditorRenderer(
            context = this,
            surfaceTexture = canvas.surfaceTexture
        ) { renderer.addLayers(uiData.layers) }
        renderer.start()
    }

    private fun setCanvasDimension(width: Int, height: Int) {
        // setting the canvas size
        val params = canvas.layoutParams as ConstraintLayout.LayoutParams
        val ratio = if (height > width) "H,${uiData.dimension}" else "W,${uiData.dimension}"
        params.dimensionRatio = ratio
        canvas.layoutParams = params
    }

    override fun onDestroy() {
        renderer.release()
        super.onDestroy()
    }
}
