package io.innvideo.renderpoc

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import io.innvideo.renderpoc.gles.Sprite2d
import io.innvideo.renderpoc.gles.Texture2dProgram
import io.innvideo.renderpoc.gles.WindowSurface
import kotlinx.android.synthetic.main.activity_trial.*


class HopeItWorksActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trial)
        init()
    }

    private fun init() {
        textureView.surfaceTextureListener = this
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    private lateinit var windowSurface: WindowSurface
    private lateinit var mTexProgram: Texture2dProgram
    private lateinit var mRect: Sprite2d
    private lateinit var mCameraTexture: SurfaceTexture

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        val renderer = ParentHopeItWorks(this, surface!!, width, height)
        val player = MediaPlayer().apply {
            setDataSource("${Environment.getExternalStorageDirectory()}/aa/video.mp4")
            isLooping = true
            setSurface(Surface(renderer.getVideoTexture()))
            renderer.setVideoSize(this.videoWidth, this.videoHeight)
            prepare()
            setOnPreparedListener {
                it.start()
            }
        }
    }


    private fun playVideo() {

    }

    override fun onPause() {
        super.onPause()

    }
}
