package io.innvideo.renderpoc.poc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.GLES20
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.custom_views.BitmapTexture
import io.innvideo.renderpoc.gles.Triangle
import io.innvideo.renderpoc.utils.logIt
import io.innvideo.renderpoc.utils.onSurfaceTextureAvailable
import kotlinx.android.synthetic.main.activity_hope_it_combines.*
import java.nio.ByteBuffer
import java.nio.ByteOrder


class HopeItCombinesActivity : AppCompatActivity() {

    private lateinit var surfaceTexture: SurfaceTexture

    private var renderer: RendererThread? = null

    companion object {
        // Media Codec Properties
        private const val VIDEO_WIDTH = 320
        private const val VIDEO_HEIGHT = 240
        private const val VIDEO_BITRATE = 2000000
        private const val FRAME_INTERVAL = 5
        private const val FPS = 30
        private const val MAX_INPUT_SIZE = 0
        private const val MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC
        private val INPUT_FILE = "${Environment.getExternalStorageDirectory()}/aa/video.mp4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hope_it_combines)
        init()
    }

    private fun init() {
        btnRender.setOnClickListener { startRendering() }
        textureView.onSurfaceTextureAvailable { surfaceTexture, _, _ ->
            /* val metaMetaDataRetriever = MediaMetadataRetriever()
             metaMetaDataRetriever.setDataSource(INPUT_FILE)
             val bitmap = metaMetaDataRetriever.getFrameAtIndex(0)
             val bitmap1 = drawableToBitmap(ContextCompat.getDrawable(this, R.mipmap.ic_launcher)!!)
             ivPreview.setImageBitmap(bitmap1)
             metaMetaDataRetriever.release()*/
            renderer =
                RendererThread(surfaceTexture, this) {}
            renderer?.start()
            this.surfaceTexture = surfaceTexture
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    private fun cancel() {
        renderer?.let {
            it.release()
            it.interrupt()
        }
    }

    private fun startRendering() {
        cancel()
        val format = MediaFormat.createVideoFormat(
            MIME_TYPE,
            VIDEO_WIDTH,
            VIDEO_HEIGHT
        )
            .apply {
                setInteger(
                    MediaFormat.KEY_MAX_INPUT_SIZE,
                    MAX_INPUT_SIZE
                )
                setInteger(
                    MediaFormat.KEY_BIT_RATE,
                    VIDEO_BITRATE
                )
                setInteger(
                    MediaFormat.KEY_FRAME_RATE,
                    FPS
                )
                setInteger(
                    MediaFormat.KEY_I_FRAME_INTERVAL,
                    FRAME_INTERVAL
                )
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                )
            }
        val mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE)
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = mediaCodec.createInputSurface()
        val thread =
            RendererThread(inputSurface, this) { mediaCodec.signalEndOfInputStream() }
        val bufferInfo = MediaCodec.BufferInfo()

        mediaCodec.start()
        thread.start()
        val muxer = MediaMuxer(getOutputFilePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        logIt("First track index => $trackIndex")
        var isEOS = false
        while (isEOS.not()) {
            val index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
            if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                logIt("OUTPUT BUFFER CHANGED")
            } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                trackIndex = muxer.addTrack(mediaCodec.outputFormat)
                muxer.start()
                logIt("Starting Muxer")
                logIt("INFO_OUTPUT_FORMAT_CHANGED")
            } else if (index >= 0) {

                val byteBuffer = mediaCodec.getOutputBuffer(index)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    logIt("Setting Buffer info size")
                    bufferInfo.size = 0
                }
                byteBuffer?.position(bufferInfo.offset)
                byteBuffer?.limit(bufferInfo.offset + bufferInfo.size)
                logIt("Adding in muxer => $trackIndex == buffer size => ${bufferInfo.size}")
                muxer.writeSampleData(trackIndex, byteBuffer!!, bufferInfo)
                mediaCodec.releaseOutputBuffer(index, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    isEOS = true
                }
            } else {
                logIt("NOTHING => $index")
            }
        }
//              muxer.stop()
        muxer.release()
        mediaCodec.stop()
        mediaCodec.release()
        logIt("OUT")
    }

    private fun getOutputFilePath() =
        resources.getString(
            R.string.output_file_name,
            Environment.getExternalStorageDirectory().absolutePath,
            VideoUtils.getOutputName()
        )
}


class RendererThread(
    surfaceTexture: Any,
    val context: Context,
    val completionListener: () -> Unit
) :
    Thread() {

    private val VERTEX_COORDINATES = floatArrayOf(
        -1.0f, +1.0f, 0.0f,
        +1.0f, +1.0f, 0.0f,
        -1.0f, -1.0f, 0.0f,
        +1.0f, -1.0f, 0.0f
    )

    private val TEXTURE_COORDINATES = floatArrayOf(
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f
    )

    private val TEXCOORD_BUFFER = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDINATES).rewind()
    private val VERTEX_BUFFER = ByteBuffer.allocateDirect(VERTEX_COORDINATES.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTEX_COORDINATES).rewind()
    private var isRunning = false

    private var eglCore = EglCore(surfaceTexture)

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun run() {
        logIt("=== STARTED ===")
        eglCore.init()
        isRunning = true
        var counter = 0
//        while (++counter < 100) {
        val red = getRandom() / 255.0f
        val green = getRandom() / 255.0f
        val blue = getRandom() / 255.0f


//            }

        setColor(red, green, blue)
        Triangle(context).draw()
        BitmapTexture(context).onSurfaceCreated().draw()

        eglCore.swapBuffer()
        isRunning = false
        //    sleep(10)
        completionListener()
    }

    private fun setPosition(x: Int, y: Int, width: Int, height: Int) {
        GLES20.glScissor(x, y, width, height)
    }

    private fun setColor(red: Float = 1f, green: Float = 1f, blue: Float = 1f, alpha: Float = 1f) {
        GLES20.glClearColor(red, green, blue, alpha)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    private fun createObject(callback: () -> Unit) {
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST)
        callback()
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST)
    }

    private fun getRandom() = (0..255).random()

    fun release() {
        isRunning = false
        eglCore.release()
    }

    interface CompletionListener {
        fun onCompleted()
    }
}
