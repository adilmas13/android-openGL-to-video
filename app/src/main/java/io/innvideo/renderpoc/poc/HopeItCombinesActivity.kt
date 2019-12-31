package io.innvideo.renderpoc.poc

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import io.innvideo.renderpoc.Layer
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.poc.interfaces.RenderListener
import io.innvideo.renderpoc.utils.onSurfaceTextureAvailable
import kotlinx.android.synthetic.main.activity_hope_it_combines.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HopeItCombinesActivity : AppCompatActivity(), RenderListener {

    private lateinit var surfaceTexture: SurfaceTexture

    private var renderer: RendererThread? = null

    private var layers = mutableListOf<Layer>()

    companion object {
        private val INPUT_FILE = "${Environment.getExternalStorageDirectory()}/aa/video_1.mp4"
        private val INPUT_FILE_1 = "${Environment.getExternalStorageDirectory()}/aa/video.mp4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hope_it_combines)
        addBackgroundToLayers()
        addVideosToLayers()
        init()
    }

    private fun addBackgroundToLayers() {
        val red = getRandom() / 255.0f
        val green = getRandom() / 255.0f
        val blue = getRandom() / 255.0f
        // adding background to Layers
        layers.add(Layer.Background(red, green, blue, 1.0f))
    }

    private fun addVideosToLayers() {
        // adding videos to Layers
        layers.add(Layer.Video(INPUT_FILE_1))
        layers.add(Layer.Video(INPUT_FILE))
    }

    private fun init() {
        btnRender.setOnClickListener {
            /* RenderVideo(
                 this,
                 fetchBitmaps(INPUT_FILE),
                 fetchBitmaps(INPUT_FILE_1)
             ).startRendering()*/
        }
        textureView.onSurfaceTextureAvailable { surfaceTexture, _, _ ->
            test()
            /*renderer =
                RendererThread(
                    surfaceTexture,
                    layers,
                    context = this
                ) {}
            renderer?.start()*/
            this.surfaceTexture = surfaceTexture
        }
    }

    private fun test() {
        CoroutineScope(Dispatchers.IO).launch {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(INPUT_FILE)
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
            retriever.getFramesAtIndex(0,10)
            for (i in 0 until 30) {
                val bitmap = retriever.getFrameAtTime((i * 10000).toLong(), MediaMetadataRetriever.OPTION_CLOSEST)
                withContext(Dispatchers.Main) {
                    ivPreview.setImageBitmap(bitmap)
                }
                delay(20)
            }
        }

    }

    private fun getRandom() = (0..255).random()

    private fun fetchBitmaps(inputFile: String): MutableList<Bitmap> {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(inputFile)
        MediaMetadataRetriever.METADATA_KEY_DURATION
        val frameCount =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                .toInt()
        val bitmaps = retriever.getFramesAtIndex(0, frameCount)
        retriever.release()
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