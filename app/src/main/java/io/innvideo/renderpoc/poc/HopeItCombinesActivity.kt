package io.innvideo.renderpoc.poc

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.poc.interfaces.RenderListener
import io.innvideo.renderpoc.utils.onSurfaceTextureAvailable
import kotlinx.android.synthetic.main.activity_hope_it_combines.*


class HopeItCombinesActivity : AppCompatActivity(), RenderListener {

    private lateinit var surfaceTexture: SurfaceTexture

    private var renderer: RendererThread? = null

    companion object {

        private val INPUT_FILE = "${Environment.getExternalStorageDirectory()}/aa/video_1.mp4"
        private val INPUT_FILE_1 = "${Environment.getExternalStorageDirectory()}/aa/video.mp4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hope_it_combines)
        init()
    }

    private fun init() {
        btnRender.setOnClickListener {
            RenderVideo(
                this,
                fetchBitmaps(INPUT_FILE),
                fetchBitmaps(INPUT_FILE_1))
                .startRendering()
        }
        textureView.onSurfaceTextureAvailable { surfaceTexture, _, _ ->

            val fullScreenVideoBitmap = fetchBitmaps(INPUT_FILE)
            val smallScreenVideoBitmap = fetchBitmaps(INPUT_FILE_1)

            renderer =
                RendererThread(
                    surfaceTexture,
                    fullScreenVideoBitmapList = fullScreenVideoBitmap,
                    smallVideoBitmapList = smallScreenVideoBitmap,
                    context = this
                ) {}
            renderer?.start()
            this.surfaceTexture = surfaceTexture
        }
    }

    private fun fetchBitmaps(inputFile: String): MutableList<Bitmap> {
        val metaMetaDataRetriever = MediaMetadataRetriever()
        metaMetaDataRetriever.setDataSource(inputFile)
        val frameCount =
            metaMetaDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                .toInt()
        val bitmaps = metaMetaDataRetriever.getFramesAtIndex(0, frameCount)
        metaMetaDataRetriever.release()
        return bitmaps
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    override fun cancel() {
        renderer?.let {
            it.release()
            it.interrupt()
        }
    }
}
