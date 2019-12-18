package io.innvideo.renderpoc

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
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

    private val videoWidth = 640
    private val videoHeight = 360
    private val bitrate = 12200

    private var renderer: ParentHopeItWorks? = null
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trial)
        init()
    }

    private fun init() {
        textureView.surfaceTextureListener = this
        playBtn.setOnClickListener { playVideo() }
        renderBtn.setOnClickListener { renderIt() }
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

    override fun onSurfaceTextureAvailable(
        surfaceTexture: SurfaceTexture?,
        width: Int,
        height: Int
    ) {
        renderer = ParentHopeItWorks(this, surfaceTexture!!, width, height)
        player = MediaPlayer().apply {
            setDataSource("${Environment.getExternalStorageDirectory()}/aa/video.mp4")
            isLooping = true
            setSurface(Surface(renderer?.getVideoTexture()))
            renderer?.setVideoSize(this.videoWidth, this.videoHeight)
            prepare()
            setOnPreparedListener {
                //   it.start()
            }
        }
    }

    private fun hopeItWorks() {

    }

    private fun playVideo() {
        renderer?.letsRun()
        player?.start()
    }

    private fun renderIt() {
        renderer?.stopIt()
        val mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        val format =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoWidth, videoWidth)
                .apply {
                    setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0)
                    setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                    setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
                    setInteger(
                        MediaFormat.KEY_COLOR_FORMAT,
                        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                    )
                }
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = mediaCodec.createInputSurface()
        player?.setSurface(surface)
        mediaCodec.start()
        mediaCodec.setInputSurface(surface)
        mediaCodec.stop()
        mediaCodec.release()
    }

    override fun onPause() {
        super.onPause()
        renderer?.stopIt()
    }


    private fun selectCodec(mimeType: String): MediaCodecInfo? {
        val numCodecs = MediaCodecList.getCodecCount()
        for (i in 0 until numCodecs) {
            val codecInfo = MediaCodecList.getCodecInfoAt(i)
            if (!codecInfo.isEncoder) {
                continue
            }
            val types = codecInfo.supportedTypes
            for (j in types.indices) {
                if (types[j].equals(mimeType, ignoreCase = true)) {
                    return codecInfo
                }
            }
        }
        return null
    }

    private fun selectColorFormat(mimeType: String): Int {
        val codecInfo = selectCodec(mimeType)
        var colorFormat = 0
        val capabilities =
            codecInfo!!.getCapabilitiesForType(mimeType)
        for (i in capabilities.colorFormats.indices) {
            if (isRecognizedFormat(capabilities.colorFormats[i])) {
                colorFormat = capabilities.colorFormats[i]
                break
            }
        }
        return colorFormat
    }

    private fun isRecognizedFormat(colorFormat: Int): Boolean {
        return when (colorFormat) {
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar,
            MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar -> true
            else -> false
        }
    }
}
