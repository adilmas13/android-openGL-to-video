package io.innvideo.renderpoc

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.innvideo.renderpoc.poc.VideoUtils
import io.innvideo.renderpoc.utils.logIt
import io.innvideo.renderpoc.utils.onSurfaceTextureAvailable
import io.innvideo.renderpoc.utils.toastIt
import kotlinx.android.synthetic.main.activity_trial.*


class TrialActivity : AppCompatActivity() {

    private var inputSurface: Surface? = null
    private var previewRenderer: VideoTextureRenderer? = null
    private var player: MediaPlayer? = null
    private lateinit var previewSurfaceTexture: SurfaceTexture
    private var previewTextureHeight = 0
    private var previewTextureWidth = 0

    companion object {
        private val INPUT_FILE = "${Environment.getExternalStorageDirectory()}/aa/video.mp4"

        // Media Codec Properties
        private const val VIDEO_WIDTH = 640
        private const val VIDEO_HEIGHT = 360
        private const val VIDEO_BITRATE = 12200
        private const val FRAME_INTERVAL = 10
        private const val FPS = 30
        private const val MAX_INPUT_SIZE = 0
        private const val MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trial)
        init()
        initTextureView()
    }

    private fun init() {
        playBtn.visibility = View.GONE
        playBtn.setOnClickListener { playVideo() }
        renderBtn.setOnClickListener { renderIt() }
    }

    private fun initTextureView() {
        initCodec()
        textureView.onSurfaceTextureAvailable { surfaceTexture, width, height ->
            this.previewSurfaceTexture = surfaceTexture
            this.previewTextureHeight = height
            this.previewTextureWidth = width
            previewRenderer = VideoTextureRenderer(
                this@TrialActivity,
                previewSurfaceTexture,
                previewTextureWidth,
                previewTextureHeight
            )
            previewRenderer?.letsRun {}
            this.player = MediaPlayer().apply {
                setDataSource(INPUT_FILE)
//                setSurface(Surface(previewRenderer?.getVideoTexture()))
                setSurface(inputSurface)
                previewRenderer?.setVideoSize(this.videoWidth, this.videoHeight)
                prepare()
                setOnPreparedListener {
                    player?.start()
                    renderIt()
                }
            }
        }
    }

    private lateinit var mediaCodec: MediaCodec
    private fun initCodec() {
        mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE)
        val format = MediaFormat.createVideoFormat(
            MIME_TYPE,
            VIDEO_WIDTH,
            VIDEO_HEIGHT
        )
        format.apply {
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INPUT_SIZE)
            setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BITRATE)
            setInteger(MediaFormat.KEY_FRAME_RATE, FPS)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, FRAME_INTERVAL)
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
        }
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = mediaCodec.createInputSurface()
    }

    private fun playVideo() {

    }

    private fun renderIt() {
        //  previewRenderer?.stopIt()

        //  player?.setSurface(inputSurface)
        /* val render = FinalParentHopeItWorks(
             this,
             textureView.surfaceTexture,
             inputSurface!!,
             VIDEO_WIDTH,
             VIDEO_HEIGHT
         )
         render.letsRun {
             mediaCodec.start()
             player?.start()
         }*/
        val bufferInfo = MediaCodec.BufferInfo()

        var isEOS = false

        isRendering = true
        mediaCodec.start()
        val muxer = MediaMuxer(getOutputFilePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
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
                logIt("Starting Muxer")
                logIt("INFO_OUTPUT_FORMAT_CHANGED")
            } else if (index >= 0) {
                isVisited = true
                val byteBuffer = mediaCodec.getOutputBuffer(index)

                logIt("Adding in muxer")
                muxer.writeSampleData(trackIndex, byteBuffer!!, bufferInfo)
                mediaCodec.releaseOutputBuffer(index, true)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    isEOS = true
                }
            } else {
                logIt("NOTHING => $index")
                if (isVisited && ++exitCounter == 20) {
                    logIt("EXITING")
                    isEOS = true
                    muxer.stop()
                    muxer.release()
                    mediaCodec.stop()
                    mediaCodec.release()
                }
            }
        }
        logIt("OUT")

        /* RenderTask(
             this,
             textureView.surfaceTexture
         ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getOutputFilePath())*/
    }

    var isRendering = false
    private fun endRendering() {
        isRendering = false
        inputSurface?.release()
        toastIt("END")
    }

    override fun onPause() {
        super.onPause()
        previewRenderer?.stopIt()
    }

    inner class RenderTask(
        private val context: Context,
        private val texture: SurfaceTexture
    ) :
        AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg params: String): Boolean {
            val mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE)
            val format = MediaFormat.createVideoFormat(
                MIME_TYPE,
                VIDEO_WIDTH,
                VIDEO_HEIGHT
            )
            format.apply {
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INPUT_SIZE)
                setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BITRATE)
                setInteger(MediaFormat.KEY_FRAME_RATE, FPS)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, FRAME_INTERVAL)
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                )
            }
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = mediaCodec.createInputSurface()
            player?.setSurface(inputSurface)
            val render = FinalParentHopeItWorks(
                context,
                texture,
                inputSurface!!,
                VIDEO_WIDTH,
                VIDEO_HEIGHT
            )
            render.letsRun {}
            mediaCodec.start()
            player?.start()
            val bufferInfo = MediaCodec.BufferInfo()

            var isEOS = false

            isRendering = true
            val muxer = MediaMuxer(params[0], MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
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

                    //        muxer.writeSampleData(trackIndex, byteBuffer!!, bufferInfo)
                    mediaCodec.releaseOutputBuffer(index, true)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEOS = true
                    }
                } else if (index == -1) {
                    if (isVisited && ++exitCounter == 20) {
                        isEOS = true
                        mediaCodec.stop()
                        mediaCodec.release()
                    }
                }
            }
            /*  muxer.stop()
              muxer.release()*/
            return false
        }

        override fun onPostExecute(result: Boolean?) {
            toastIt("ITS DONE")
            super.onPostExecute(result)
        }
    }

    private fun getOutputFilePath() =
        resources.getString(
            R.string.output_file_name,
            Environment.getExternalStorageDirectory().absolutePath,
            VideoUtils.getOutputName()
        )
}
