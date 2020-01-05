package io.innvideo.renderpoc

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_surface.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer


class SurfaceActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private var isLogEnabled = true

    companion object {
        private const val LOG_TAG = "COBRA"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surface)
        btnHopeItWorks.setOnClickListener { videoConverter() }
        surfaceView.surfaceTextureListener = this
    }

    inner class ConverterTask(private val callback: () -> Unit) : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                callback
            }
        }
    }

    private fun videoConverter() {

        val thread = Thread()
        thread.run {
            val mWidth = 320
            val mHeight = 240
            val mBitRate = 125000

            val inputFile = File("${Environment.getExternalStorageDirectory()}/aa/video.mp4")
            val outputFile = File("${Environment.getExternalStorageDirectory()}/aa/videooo.mp4")
            val muxer =
                MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var bufferSize: Int
            var selectedTrack: MediaFormat? = null
            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(inputFile.absolutePath)
            // only select one track, the first video track
            for (i in 0 until mediaExtractor.trackCount) {
                val mediaFormat = mediaExtractor.getTrackFormat(i)
                val mimeType = mediaFormat.getString(MediaFormat.KEY_MIME) ?: ""
                if (mimeType.startsWith("video/")) {
                    mediaExtractor.selectTrack(i)
                    selectedTrack = mediaFormat
                    // set a buffer size for the byte buffer
                    bufferSize = if (mediaFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE))
                        mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    else
                        DEFAULT_BUFFER_SIZE
                    break
                }
            }

            // create decoder
            val decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            // set decoder format
            decoder.configure(selectedTrack, null, null, 0)
            val track = muxer.addTrack(selectedTrack!!)
            // create encoder
            val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            val encoderMediaFormat =
                MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight)
                    .apply {
                        setInteger(MediaFormat.KEY_BIT_RATE, mBitRate)
                        setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                        setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
                        setInteger(
                            MediaFormat.KEY_COLOR_FORMAT,
                            selectColorFormat(MediaFormat.MIMETYPE_VIDEO_AVC)
                        )
                    }
            encoder.configure(
                encoderMediaFormat,
                Surface(surfaceView.surfaceTexture),
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
            decoder.start()
            encoder.start()
            muxer.start()
            val decoderInputBuffer = decoder.inputBuffers
            var decoderOutputBuffer = decoder.outputBuffers
            val encoderInputBuffer = encoder.inputBuffers
            var encoderOutputBuffer = encoder.outputBuffers

            var inputEOS = false
            var outputEOS = false
            var isFinalEOS = false

            val decoderBufferInfo = MediaCodec.BufferInfo()
            val encoderBufferInfo = MediaCodec.BufferInfo()

            while (inputEOS.not() || outputEOS.not()) {
                if (inputEOS.not()) {
                    inputEOS = decodeHere(mediaExtractor, decoder, decoderInputBuffer)
                }
                if (outputEOS.not()) {
                    val index = decoder.dequeueOutputBuffer(decoderBufferInfo, 0)
                    if (index >= 0) {
                        outputEOS = encodeHere(
                            decoder,
                            encoder,
                            index,
                            decoderBufferInfo,
                            decoderOutputBuffer,
                            encoderInputBuffer,
                            encoderBufferInfo,
                            muxer, track
                        )
                    } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        logIt("MediaCodec.INFO_OUTPUT_FORMAT_CHANGED")
                    } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        decoderOutputBuffer = decoder.outputBuffers
                        logIt("MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED")
                    } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        logIt("MediaCodec.INFO_TRY_AGAIN_LATER")
                    }
                }
                /* if (isFinalEOS.not()) {
                     val index = encoder.dequeueOutputBuffer(encoderBufferInfo, 0)
                     if (index >= 0) {
                         isFinalEOS = finalEncodeHere(
                             encoder,
                             index,
                             encoderBufferInfo,
                             encoderOutputBuffer
                         )
                     } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                         logIt("MediaCodec.INFO_OUTPUT_FORMAT_CHANGED")
                     } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                         encoderOutputBuffer = decoder.outputBuffers
                         logIt("MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED")
                     } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                         logIt("MediaCodec.INFO_TRY_AGAIN_LATER")
                     }
                 }*/
            }

            encoder.stop()
            encoder.release()
            decoder.stop()
            decoder.release()
            mediaExtractor.release()
            muxer.stop()
            muxer.release()
            this.interrupt()
        }
        thread.start()
    }

    private fun finalEncodeHere(
        encoder: MediaCodec,
        outputIndex: Int,
        info: MediaCodec.BufferInfo,
        encoderOutputBuffer: Array<ByteBuffer>
    ): Boolean {
        var isEndOfStream = false

        val bufferByte = encoderOutputBuffer[outputIndex]
        encoder.releaseOutputBuffer(outputIndex, true)
        if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            isEndOfStream = true
        }
        return isEndOfStream
    }

    val muxerBufferInfo = MediaCodec.BufferInfo()
    private fun encodeHere(
        decoder: MediaCodec,
        encoder: MediaCodec,
        outputIndex: Int,
        info: MediaCodec.BufferInfo,
        decoderOutputBuffer: Array<ByteBuffer>,
        encoderInputBuffer: Array<ByteBuffer>,
        encoderBufferInfo: MediaCodec.BufferInfo,
        muxer: MediaMuxer,
        track: Int
    ): Boolean {
        var isEndOfStream = false

        val bufferByte = decoderOutputBuffer[outputIndex]
        decoder.releaseOutputBuffer(outputIndex, true)
        if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            isEndOfStream = true
        }
        val index = encoder.dequeueInputBuffer(0)
        if (index >= 0) {
            val buff = encoderInputBuffer[index]
            muxerBufferInfo.size = buff.capacity()
            muxerBufferInfo.offset = 0
            //     muxerBufferInfo.presentationTimeUs = info.presentationTimeUs
            muxerBufferInfo.flags =
                if (isEndOfStream.not()) MediaCodec.BUFFER_FLAG_KEY_FRAME else MediaCodec.BUFFER_FLAG_END_OF_STREAM
            /* buff.clear()
             buff.put(bufferByte)
             encoder.queueInputBuffer(index, 0, buff.limit(), info.presentationTimeUs, 0)*/
            // info.presentationTimeUs = mediaExtractor.sampleTime
            muxer.writeSampleData(track, buff, muxerBufferInfo)
        }

        return isEndOfStream
    }

    private fun decodeHere(
        extractor: MediaExtractor,
        decoder: MediaCodec,
        decoderInputBuffer: Array<ByteBuffer>
    ): Boolean {
        var isEndOfStream = false
        val index = decoder.dequeueInputBuffer(0)

        if (index >= 0) {
            val bufferByte = decoderInputBuffer[index]
            var sampleSize = extractor.readSampleData(bufferByte, 0)
            var presentationTime = 0L
            if (sampleSize < 0) {
                isEndOfStream = true
                sampleSize = 0
            } else {
                presentationTime = extractor.sampleTime
            }
            decoder.queueInputBuffer(
                index,
                0,
                sampleSize,
                presentationTime,
                if (isEndOfStream) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
            )
            if (isEndOfStream.not()) {
                extractor.advance()
            }
        }

        return isEndOfStream
    }

    private fun drawOnSurface() {
        val inputFile = File("${Environment.getExternalStorageDirectory()}/aa/video.mp4")

        val images = arrayOf(
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00000.jpg",
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00001.jpg",
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00002.jpg",
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00003.jpg",
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00004.jpg",
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00005.jpg",
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00006.jpg",
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00007.jpg",
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00008.jpg",
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00009.jpg",
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00010.jpg"
        )


        val mWidth = 320
        val mHeight = 240
        val mBitRate = 125000
        val mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)

        val mediaFormat =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, mBitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    selectColorFormat(MediaFormat.MIMETYPE_VIDEO_AVC)
                )
            }
        try {
            mediaCodec.configure(
                mediaFormat,
                Surface(surfaceView.surfaceTexture),
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
            mediaCodec.start()
            var isCompleted = false
            var frameCount = 0
            for (x in images.indices) {
                while (isCompleted.not()) {
                    val inputIndex = mediaCodec.dequeueInputBuffer(-1)
                    if (inputIndex >= 0 && frameCount < images.size) {
                        val byteBuffer = mediaCodec.getInputBuffer(inputIndex)
                        val image = images[frameCount]
                        val img = getBytesArray(image)
                        byteBuffer?.put(img)
                        // start operation
                        mediaCodec.queueInputBuffer(inputIndex, 0, img.size, 0, 0)
                        ++frameCount
                    }

                    val bufferInfo = MediaCodec.BufferInfo()
                    val outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
                    if (outputIndex >= 0) {
                        mediaCodec.releaseOutputBuffer(outputIndex, true)
                        if (frameCount >= images.size) {
                            isCompleted = true
                        }
                    }
                }
            }
            mediaCodec.stop()
            mediaCodec.release()
        } catch (e: MediaCodec.CodecException) {
            e.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun tryMediaCodec() {
        val mWidth = 320
        val mHeight = 240
        val mBitRate = 125000
        val outputFile = "${Environment.getExternalStorageDirectory()}/aa/muxedd.mp4"
        val image = "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00000.jpg"
        val image1 =
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00001.jpg"
        val image2 =
            "${Environment.getExternalStorageDirectory()}/aa/Jpg/LandingPageVideo_00002.jpg"

        val mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        val mediaFormat =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, mBitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    selectColorFormat(MediaFormat.MIMETYPE_VIDEO_AVC)
                )
            }
        try {
            val muxer: MediaMuxer
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaCodec.start()
            val inputBuffers = mediaCodec.inputBuffers
            val outputBuffers = mediaCodec.outputBuffers
            val options = BitmapFactory.Options()
            options.inSampleSize = 8
            val byteArrayOutputStream = ByteArrayOutputStream()
            val bitmap = BitmapFactory.decodeFile(image, options)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream)
            val input: ByteArray = byteArrayOutputStream.toByteArray()

            val inputBufferIndex: Int = mediaCodec.dequeueInputBuffer(-1)
            if (inputBufferIndex >= 0) {
                muxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val trackIndex = muxer.addTrack(mediaCodec.outputFormat)
                val inputBuffer: ByteBuffer = inputBuffers[inputBufferIndex]
                inputBuffer.clear()
                inputBuffer.put(input)
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.size, 0, 0)
                while (true) {
                    val status = mediaCodec.dequeueOutputBuffer(MediaCodec.BufferInfo(), 0)
                    if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        logIt("NO OUTPUT YET")
                    } else if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        logIt("BUFFER CHANGED")
                    } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        logIt("INFO_OUTPUT_FORMAT_CHANGED")
                    } else if (status < 0) {
                        logIt("Unexpected error")
                        break
                    } else {
                        val info = MediaCodec.BufferInfo()
                        muxer.start()
                        val data = outputBuffers[status]
                        muxer.writeSampleData(trackIndex, data, info)
                        break
                        /*  if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM !== 0) {
                              break
                          }*/
                    }
                }
                mediaCodec.stop()
                mediaCodec.release()
                muxer.stop()
                muxer.release()
            }
        } catch (e: MediaCodec.CodecException) {
            e.printStackTrace()
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        }
        mediaCodec.release()
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
        val capabilities = codecInfo!!.getCapabilitiesForType(mimeType)
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

    /*
    * demo showing media extractor and media muxer
    * take an input video and generate the same video without audio
    * */
    private fun tryingMediaExtractor() {
        val inputFile = File("${Environment.getExternalStorageDirectory()}/aa/video.mp4")
        val outputFile = "${Environment.getExternalStorageDirectory()}/aa/muxed.mp4"
        var selectedTrack: MediaFormat? = null
        var bufferSize = -1
        if (inputFile.exists()) {
            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(inputFile.absolutePath)
            // only select one track, the first video track
            for (i in 0 until mediaExtractor.trackCount) {
                val mediaFormat = mediaExtractor.getTrackFormat(i)
                val mimeType = mediaFormat.getString(MediaFormat.KEY_MIME) ?: ""
                if (mimeType.startsWith("video/")) {
                    mediaExtractor.selectTrack(i)
                    selectedTrack = mediaFormat
                    // set a buffer size for the byte buffer
                    bufferSize = if (mediaFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE))
                        mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    else
                        DEFAULT_BUFFER_SIZE
                    break
                }
            }

            // start muxing
            var offset = 0
            val muxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            // maintain the same orientation as per input source video
            muxer.setOrientationHint(selectedTrack?.getInteger(MediaFormat.KEY_ROTATION) ?: 90)
            selectedTrack?.let { track ->
                val trackIndex = muxer.addTrack(track)
                val byteBuffer = ByteBuffer.allocate(bufferSize)
                val bufferInfo = MediaCodec.BufferInfo()
                muxer.start()
                while (true) {
                    bufferInfo.offset = offset
                    bufferInfo.size = mediaExtractor.readSampleData(byteBuffer, offset)
                    if (bufferInfo.size < 0) {
                        logIt("SAW END OF BUFFER")
                        toastIt("SAW END OF BUFFER")
                        break
                    }
                    bufferInfo.presentationTimeUs = mediaExtractor.sampleTime
                    bufferInfo.flags = mediaExtractor.sampleFlags
                    // val trackIndex = mediaExtractor.sampleTrackIndex
                    muxer.writeSampleData(trackIndex, byteBuffer, bufferInfo)
                    mediaExtractor.advance()
                }
            }
            toastIt("STOPPING MUXER")
            muxer.stop()
            muxer.release()
            mediaExtractor.release()
        } else {
            toastIt("Input file missing")
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        logIt("onSurfaceTextureSizeChanged")
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        logIt("onSurfaceTextureSizeChanged")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        logIt("onSurfaceTextureDestroyed")
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        logIt("onSurfaceTextureAvailable")
    }

    private fun getRandomColor() = (0..255).random()

    private fun logIt(message: String = "You didn't log anything") {
        if (isLogEnabled) {
            Log.d(LOG_TAG, message)
        }
    }

    private fun toastIt(message: String = "Something went wrong") {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun getBytesArray(image: String): ByteArray {
        val options = BitmapFactory.Options()
        val byteArrayOutputStream = ByteArrayOutputStream()
        options.inSampleSize = 8
        val bitmap = BitmapFactory.decodeFile(image, options)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun tryDrawing(holder: SurfaceTexture?) {
        /*  holder?.let {
              val canvas = it.sur
              canvas.drawRGB(getRandomColor(), getRandomColor(), getRandomColor())
              holder.unlockCanvasAndPost(canvas)
          }*/
    }

}
