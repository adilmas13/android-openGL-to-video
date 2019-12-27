package io.innvideo.renderpoc.poc

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

        private val INPUT_FILE = "${Environment.getExternalStorageDirectory()}/aa/video.mp4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hope_it_combines)
        init()
    }

    private fun init() {
        btnRender.setOnClickListener { RenderVideo(this, this).startRendering() }
        textureView.onSurfaceTextureAvailable { surfaceTexture, _, _ ->
            val metaMetaDataRetriever = MediaMetadataRetriever()
            metaMetaDataRetriever.setDataSource(INPUT_FILE)

            val bitmaps = metaMetaDataRetriever.getFramesAtIndex(0, 200)
            ivPreview.setImageBitmap(bitmaps[0])
            metaMetaDataRetriever.release()
            renderer =
                RendererThread(surfaceTexture, list = bitmaps, context = this) {}
            renderer?.start()
            this.surfaceTexture = surfaceTexture
        }
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
