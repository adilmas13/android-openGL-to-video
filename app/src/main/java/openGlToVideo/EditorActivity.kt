package openGlToVideo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.api.load
import com.google.gson.Gson
import openGlToVideo.newModels.parsed_models.LayerData
import openGlToVideo.newModels.parsed_models.MainUiData
import openGlToVideo.newModels.response_models.ParentResponseModel
import openGlToVideo.openGL.utils.GLSLTextReader.Companion.readGlslFromRawRes
import openGlToVideo.parser.EditorDataParser
import openGlToVideo.videoRenderer.VideoRenderer
import openGlToVideo.videoRenderer.VideoUtils
import openGlToVideo.videoRenderer.size
import io.opengltovideo.BuildConfig
import io.opengltovideo.R
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class EditorActivity : AppCompatActivity() {

    private lateinit var uiData: MainUiData

    private lateinit var renderer: EditorRenderer

    private lateinit var renderedVideoFilePath: String

    @SuppressLint("ClickableViewAccessibility")
    private var touchListener = View.OnTouchListener { v, event ->
        // Convert touch coordinates into normalized device
        // coordinates, keeping in mind that Android's Y
        // coordinates are inverted.
        val normalizedX = event.x / v.width.toFloat() * 2 - 1
        val normalizedY = -(event.y / v.height.toFloat() * 2 - 1)

        true
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        checkForStoragePermission()
        group.visibility = View.GONE
        tvShare.setOnClickListener { shareVideo() }
        canvas.setOnTouchListener(touchListener)
        canvas.onSurfaceTextureAvailable { _, _, _ -> start() }
        btnExport.setOnClickListener { exportVideo() }
    }

    private fun checkForStoragePermission() {
        if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED).not()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        } else {
            makeStorageDirectory()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            makeStorageDirectory()
        } else {
            toastIt(getString(R.string.storage_permission_required))
            finish()
        }
    }

    private fun makeStorageDirectory() {
        val directory =
            File(Environment.getExternalStorageDirectory().absolutePath + File.separator + VideoUtils.DIRECTORY_NAME)
        directory.mkdirs()
    }

    private fun exportVideo() {
        group.visibility = View.VISIBLE
        tvShare.visibility = View.GONE
        VideoRenderer(this@EditorActivity, uiData)
            .withAudio()
            .onProgress(::onProgressReceived)
            .onCompleted(::onRenderSuccess)
            .onError(::onRenderFailed)
            .enableDebug(BuildConfig.DEBUG)
            .render()
    }

    private fun onProgressReceived(progress: Int) {
        pbLoader.progress = progress
        tvProgress.text = resources.getString(
            R.string.progress_in_percentage,
            progress.toString()
        )
    }

    private fun onRenderFailed() {
        showFailedDialog()
        resetProgress()
        group.visibility = View.GONE
    }

    private fun onRenderSuccess(path: String) {
        renderedVideoFilePath = path
        group.visibility = View.GONE
        tvShare.visibility = View.VISIBLE
        showSuccessDialog()
        resetProgress()
    }

    private fun resetProgress() {
        pbLoader.progress = 0
        tvProgress.text = ""
    }

    private fun showFailedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Video Failed!! \uD83D\uDE2D")
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog?.dismiss() }
        builder.create().show()
    }

    private fun showSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Video Created!! \uD83D\uDC4D")
        builder.setMessage(getFileDetails())
        builder.setPositiveButton("Share") { _, _ -> shareVideo() }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog?.dismiss() }
        builder.create().show()
    }

    private fun getFileDetails(): String {
        val builder = StringBuilder()
        val file = File(renderedVideoFilePath)
        if (file.exists()) {
            builder.append("SIZE : ${file.size()}\n")
        }
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(renderedVideoFilePath)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.let {
            builder.append("DURATION : ${it.toFloat() / 1000f} secs\n")
        }
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.let { width ->
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.let { height ->
                    builder.append("RESOLUTION : $width x ${height}\n")
                }
        }
        retriever.release()
        builder.append("LOCATION : $renderedVideoFilePath\n")
        return builder.toString()
    }

    private fun shareVideo() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            Uri.parse(renderedVideoFilePath)
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, renderedVideoFilePath)
        }
        startActivity(Intent.createChooser(intent, "Share using"))
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
