package io.innvideo.renderpoc

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import io.innvideo.renderpoc.utils.logIt
import io.innvideo.renderpoc.utils.toastIt
import kotlinx.android.synthetic.main.activity_trial.*


class HopeItWorksActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private val videoWidth = 640
    private val videoHeight = 360
    private val bitrate = 12200

    private var inputSurface: Surface? = null
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

    override fun onSurfaceTextureAvailable(
        surfaceTexture: SurfaceTexture?,
        width: Int,
        height: Int
    ) {
        renderer = ParentHopeItWorks(this, surfaceTexture!!, width, height)
        renderer?.letsRun()
        player = MediaPlayer().apply {
            setDataSource("${Environment.getExternalStorageDirectory()}/aa/video.mp4")
            setSurface(Surface(renderer?.getVideoTexture()))
            renderer?.setVideoSize(this.videoWidth, this.videoHeight)
            prepare()
            setOnCompletionListener {
                endRendering()
            }
        }
    }

    private fun playVideo() {
        player?.start()
    }

    private fun renderIt() {
        renderer?.stopIt()
        RenderTask(this, textureView.surfaceTexture, warp.surfaceTexture)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    var isRendering = false
    private fun endRendering() {
        isRendering = false
        inputSurface?.release()
        toastIt("END")
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

    inner class RenderTask(
        private val context: Context,
        private val texture: SurfaceTexture,
        private val outputPreview: SurfaceTexture
    ) :
        AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            val mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            val format =
                MediaFormat.createVideoFormat(
                    MediaFormat.MIMETYPE_VIDEO_AVC,
                    videoWidth,
                    videoWidth
                )
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
            mediaCodec.configure(
                format,
                Surface(outputPreview),
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
            inputSurface = mediaCodec.createInputSurface()
            player?.setSurface(inputSurface)
            val render = FinalParentHopeItWorks(
                context,
                texture,
                //  textureView.surfaceTexture,
                inputSurface!!,
                videoWidth,
                videoHeight
            )
            render.letsRun()
            mediaCodec.start()
            player?.start()
            val bufferInfo = MediaCodec.BufferInfo()

            var isEOS = false

            isRendering = true
            val outputFile = "${Environment.getExternalStorageDirectory()}/aa/v2.mp4"
            val muxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer.setOrientationHint(90)
            var trackIndex = muxer.addTrack(mediaCodec.outputFormat)
//            muxer.start()
            var exitCounter = 0
            var isVisited = false
            while (isEOS.not()) {
                val index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
                if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    logIt("BUFFER CHANGED")
                } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    trackIndex = muxer.addTrack(mediaCodec.outputFormat)
                    muxer.start()
                    logIt("INFO_OUTPUT_FORMAT_CHANGED")
                } else if (index < 0) {
                    logIt("Unexpected error")
                    break
                } else if (index >= 0) {
                    isVisited = true
                    val byteBuffer = mediaCodec.getOutputBuffer(index)

                    muxer.writeSampleData(trackIndex, byteBuffer!!, bufferInfo)
                    mediaCodec.releaseOutputBuffer(index, true)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEOS = true
                    }
                } else if (index == -1) {
                    if (isVisited && ++exitCounter == 20) {
                        isEOS = true
                    }
                }
            }
            toastIt("IS OUT")
            muxer.stop()
            muxer.release()
            mediaCodec.stop()
            mediaCodec.release()
            return false
        }

        override fun onPostExecute(result: Boolean?) {
            toastIt("ITS DONE")
            super.onPostExecute(result)
        }
    }
}
