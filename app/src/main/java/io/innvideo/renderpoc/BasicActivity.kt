package io.innvideo.renderpoc

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_basic.*


class BasicActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic)
        init()
    }

    private fun init() {
        val temp = encoder.createInputSurface()
        btn.setOnClickListener { startCodes() }
        textureView.surfaceTextureListener = this
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        val canvas = textureView.lockCanvas()
        canvas.drawARGB(255, getRandomColor(), getRandomColor(), getRandomColor())
        canvas.drawText("SOMETHING", 100f, 100f, Paint().apply {
            color = Color.CYAN
            textSize = 100f
        })
        canvas.drawText("SOMETHING ELSE", 300f, 300f, Paint().apply {
            color = Color.BLUE
            textSize = 100f
        })
        val drawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher)
        drawable?.let {
            val bmp = drawableToBitmap(it)
            bmp?.let { canvas.drawBitmap(bmp, 200f, 200f, Paint()) }
        }
        textureView.unlockCanvasAndPost(canvas)
        // addMedia()
    }

    private fun startCodes() {
        try {
            val width = 320
            val height = 240
            val bitrate = 125000
            val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
            val encoder = MediaCodec.createEncoderByType(mimeType)
            val mediaFormat = MediaFormat.createVideoFormat(mimeType, width, height).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    selectColorFormat(MediaFormat.MIMETYPE_VIDEO_AVC)
                )
            }
            encoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.setInputSurface(Surface(textureView.surfaceTexture))
        } catch (e: MediaCodec.CodecException) {
            e.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
//        encoder.start()


//        encoder.stop()
//        encoder.release()
    }

    private var mediaPlayer: MediaPlayer? = null
    private fun addMedia() {
        mediaPlayer = MediaPlayer()

    }

    private fun getRandomColor() = (0..255).random()

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
            CodecCapabilities.COLOR_FormatYUV420Planar,
            CodecCapabilities.COLOR_FormatYUV420PackedPlanar,
            CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
            CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar,
            CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar -> true
            else -> false
        }
    }
}
