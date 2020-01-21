package io.innvideo.renderpoc.editor.video_renderer

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaFormat.KEY_AAC_PROFILE
import android.media.MediaFormat.KEY_BIT_RATE
import android.media.MediaFormat.KEY_CHANNEL_COUNT
import android.media.MediaFormat.KEY_COLOR_FORMAT
import android.media.MediaFormat.KEY_FRAME_RATE
import android.media.MediaFormat.KEY_I_FRAME_INTERVAL
import android.media.MediaFormat.KEY_MAX_INPUT_SIZE
import android.media.MediaFormat.KEY_MIME
import android.media.MediaFormat.KEY_SAMPLE_RATE
import android.media.MediaFormat.MIMETYPE_AUDIO_AAC
import android.media.MediaFormat.MIMETYPE_VIDEO_AVC
import android.media.MediaFormat.createAudioFormat
import android.media.MediaFormat.createVideoFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.editor.VideoRendererContainer
import io.innvideo.renderpoc.editor.new_models.parsed_models.MainUiData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.system.measureTimeMillis

class VideoRenderer(
    private val context: Context,
    private val uiData: MainUiData
) {
    private var audioExist = false
    private var isDebugEnabled = false
    private var completedCallback: ((filePath: String) -> Unit)? = null
    private var progressCallback: ((progress: Int) -> Unit)? = null
    private var errorCallback: (() -> Unit)? = null
    private lateinit var generateFilePath: String

    companion object {
        private const val LOG_TAG = "LOG_VIDEO"

        private const val TYPE_AUDIO = "audio/"
        private const val VIDEO_WIDTH = 320
        private const val VIDEO_HEIGHT = 240
        private const val VIDEO_BITRATE = 2000000
        private const val FRAME_INTERVAL = 5
        private const val FPS = 30
        private const val MAX_INPUT_SIZE = 0
        private const val VIDEO_MIME_TYPE = MIMETYPE_VIDEO_AVC
        private const val MUXER_OUTPUT_FORMAT = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
        private const val TIME_OUT = 10000L
    }

    fun withAudio(): VideoRenderer {
        audioExist = true
        return this
    }

    fun enableDebug(isDebugEnabled: Boolean = false): VideoRenderer {
        this.isDebugEnabled = isDebugEnabled
        return this
    }

    fun onCompleted(callback: (String) -> Unit): VideoRenderer {
        this.completedCallback = callback
        return this
    }

    fun onProgress(callback: (Int) -> Unit): VideoRenderer {
        this.progressCallback = callback
        return this
    }

    fun onError(callback: () -> Unit): VideoRenderer {
        this.errorCallback = callback
        return this
    }

    fun render() {
        CoroutineScope(Dispatchers.IO).launch {
            val timeTaken = measureTimeMillis {
                if (audioExist) {
                    renderWithAudio()
                } else {
                    renderOnlyWithVideo()
                }
            }
            logIt("TIME TAKEN => ${timeTaken.toFloat() / 1000f}secs")
        }
    }

    private fun getVideoFormat() = createVideoFormat(
        VIDEO_MIME_TYPE,
        VIDEO_WIDTH,
        VIDEO_HEIGHT
    ).apply {
        setInteger(
            KEY_MAX_INPUT_SIZE,
            MAX_INPUT_SIZE
        )
        setInteger(
            KEY_BIT_RATE,
            VIDEO_BITRATE
        )
        setInteger(
            KEY_FRAME_RATE,
            FPS
        )
        setInteger(
            KEY_I_FRAME_INTERVAL,
            FRAME_INTERVAL
        )
        setInteger(
            KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
    }

    private fun getNewAudioFormat(inputAudioFormat: MediaFormat) = createAudioFormat(
        MIMETYPE_AUDIO_AAC,
        inputAudioFormat.getInteger(KEY_SAMPLE_RATE),
        inputAudioFormat.getInteger(KEY_CHANNEL_COUNT)
    ).apply {
        setInteger(KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        setInteger(KEY_BIT_RATE, inputAudioFormat.getInteger(KEY_BIT_RATE))
        setInteger(KEY_MAX_INPUT_SIZE, 1048576 /*inputAudioFormat.getInteger(KEY_MAX_INPUT_SIZE)*/)
    }

    /**
     * Get the track based on the mediaExtractor passed and track type
     * @param mediaExtractor : instance of the mediaExtractor
     * @param type: the type of track to be extracted ie "video/" or "audio/"
     * @return: the found mediaFormat
     * @throws IllegalStateException if the track is not found
     */
    private fun getTrackFormat(mediaExtractor: MediaExtractor, type: String): MediaFormat {
        for (index in 0..mediaExtractor.trackCount) {
            val trackFormat = mediaExtractor.getTrackFormat(index)
            val mimeType = trackFormat.getString(KEY_MIME) ?: ""
            if (mimeType.startsWith(type)) {
                mediaExtractor.selectTrack(index)
                return trackFormat
            }
        }
        throw IllegalStateException("'$type' TRACK NOT FOUND")
    }

    private suspend fun renderWithAudio() {
        errorWrapper {
            // first extract the audio track
            val mediaExtractor = MediaExtractor()
            withContext(Dispatchers.IO) { mediaExtractor.setDataSource(uiData.audioData.url) }
            val audioFormat = getTrackFormat(mediaExtractor, TYPE_AUDIO)

            if (audioFormat.isSupportedAudio()) {
                renderWithSupportedAudioFormat(mediaExtractor, audioFormat)
            } else {
                convertAudioToSupportedFormatAndRenderVideo(audioFormat, mediaExtractor)
            }
        }
    }

    private fun decodeAudioFromMediaExtractor(
        audioDecoder: MediaCodec,
        extractor: MediaExtractor
    ): Boolean {
        var hasAudioToEncode = true
        val bufferId = audioDecoder.dequeueInputBuffer(TIME_OUT)
        if (bufferId >= 0) {
            val buffer = audioDecoder.getInputBuffer(bufferId)!!
            val sampleSize = extractor.readSampleData(buffer, 0)

            if (sampleSize >= 0) {
                audioDecoder.queueInputBuffer(
                    bufferId, 0, sampleSize,
                    extractor.sampleTime, extractor.sampleFlags
                )
                hasAudioToEncode = true
                extractor.advance()
            } else {
                audioDecoder.queueInputBuffer(
                    bufferId, 0, 0,
                    0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                hasAudioToEncode = true
            }
        }
        return hasAudioToEncode
    }


    private suspend fun convertAudioToSupportedFormatAndRenderVideo(
        oldAudioFormat: MediaFormat,
        extractor: MediaExtractor
    ) {
        executeSafely {
            val videoFormat = getVideoFormat()
            val audioFormat = getNewAudioFormat(oldAudioFormat)
            // Init audio decoder
            val audioDecoder =
                MediaCodec.createDecoderByType(oldAudioFormat.getString(KEY_MIME) ?: "")
                    .apply {
                        configure(oldAudioFormat, null, null, 0)
                    }

            // Init audio encoder
            val audioEncoder =
                MediaCodec.createEncoderByType(
                    audioFormat.getString(KEY_MIME) ?: MIMETYPE_AUDIO_AAC
                )
                    .apply {
                        configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                    }

            // Init video encoder
            val videoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE)
                .apply {
                    configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                }

            val muxer = MediaMuxer(getOutputFilePath(), MUXER_OUTPUT_FORMAT)
            var videoTrackIndex = -1
            var audioTrackIndex = -1
            var isCompleted = false
            val inputSurface = videoEncoder.createInputSurface()
            val thread = VideoRendererContainer(
                context,
                uiData,
                inputSurface
            ) {
            }
            val videoBufferInfo = MediaCodec.BufferInfo()
            val audioBufferInfo = MediaCodec.BufferInfo()
            thread.start()
            audioDecoder.start()
            audioEncoder.start()
            videoEncoder.start()
            val TRACK_COUNT = 2
            var muxerCounter = 0
            var hasAudioToEncode: Boolean
            var muxing = false
            val timeLimitInSeconds = 15
            var startTime = 0L
            while (isCompleted.not()) {
                if (muxerCounter == TRACK_COUNT && muxing.not()) {
                    muxer.start()
                    muxing = true
                }
                // video encoder
                videoEncoderLoop@ while (true) {
                    val videoEncoderBufferId =
                        videoEncoder.dequeueOutputBuffer(videoBufferInfo, TIME_OUT)
                    when (videoEncoderBufferId) {
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            videoTrackIndex = muxer.addTrack(videoEncoder.outputFormat)
                            ++muxerCounter
                        } // INFO_OUTPUT_FORMAT_CHANGED
                        MediaCodec.INFO_TRY_AGAIN_LATER -> break@videoEncoderLoop
                        else -> {
                            if (!muxing) {
                                break@videoEncoderLoop
                            }
                            val byteBuffer = videoEncoder.getOutputBuffer(videoEncoderBufferId)
                            if (videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                                videoBufferInfo.size = 0
                            }
                            if (startTime == 0L) {
                                startTime = videoBufferInfo.presentationTimeUs
                            }
                            val currentTime = videoBufferInfo.presentationTimeUs
                            sendProgress(currentTime - startTime, timeLimitInSeconds)
                            if (((currentTime - startTime) / 1000000) >= timeLimitInSeconds) {
                                thread.release()
                                thread.interrupt()
                                videoEncoder.signalEndOfInputStream()
                            }
                            byteBuffer?.position(videoBufferInfo.offset)
                            byteBuffer?.limit(videoBufferInfo.offset + videoBufferInfo.size)
                            muxer.writeSampleData(
                                videoTrackIndex,
                                byteBuffer!!,
                                videoBufferInfo
                            )
                            videoEncoder.releaseOutputBuffer(videoEncoderBufferId, false)
                            if ((videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                isCompleted = true
                                break@videoEncoderLoop
                            }
                        } // else in when
                    }
                } // end of while for video encoder
                // audio encoder
                hasAudioToEncode = decodeAudioFromMediaExtractor(audioDecoder, extractor)
                if (hasAudioToEncode) {
                    audioEncoderLoop@ while (true) {

                        val audioEncoderBufferId =
                            audioEncoder.dequeueOutputBuffer(audioBufferInfo, TIME_OUT)

                        when (audioEncoderBufferId) {
                            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                                audioTrackIndex = muxer.addTrack(audioEncoder.outputFormat)
                                ++muxerCounter
                                break@audioEncoderLoop
                            }
                            MediaCodec.INFO_TRY_AGAIN_LATER -> break@audioEncoderLoop
                            else -> {
                                if (!muxing) {
                                    break@audioEncoderLoop
                                }
                                val encodedBuffer =
                                    audioEncoder.getOutputBuffer(audioEncoderBufferId)!!
                                muxer.writeSampleData(
                                    audioTrackIndex,
                                    encodedBuffer,
                                    audioBufferInfo
                                )
                                audioEncoder.releaseOutputBuffer(audioEncoderBufferId, false)
                            }
                        } // end of when audioEncoderBufferId
                    } // end of while loop for audioEncoder draining
                    val audioDecoderBufferId =
                        audioDecoder.dequeueOutputBuffer(audioBufferInfo, TIME_OUT)
                    if (audioDecoderBufferId >= 0) {
                        val outBuffer = audioDecoder.getOutputBuffer(audioDecoderBufferId)!!

                        // If needed, process decoded data here
                        // ...

                        // We drained the audioEncoder, so there should be input buffer
                        // available. If this is not the case, we get a NullPointerException
                        // when touching inBuffer
                        val inBufferId = audioEncoder.dequeueInputBuffer(TIME_OUT)
                        val inBuffer = audioEncoder.getInputBuffer(inBufferId)!!

                        // Copy buffers - audioDecoder output goes to audioEncoder input
                        inBuffer.put(outBuffer)

                        // Feed audioEncoder
                        audioEncoder.queueInputBuffer(
                            inBufferId,
                            audioBufferInfo.offset,
                            audioBufferInfo.size,
                            System.nanoTime() / 1000,
                            audioBufferInfo.flags
                        )

                        audioDecoder.releaseOutputBuffer(audioDecoderBufferId, false)
                    }
                } // end of hasAudioToEncode

            } // end of parent while loop

            muxer.apply {
                stop()
                release()
            }
            audioDecoder.apply {
                stop()
                release()
            }
            audioEncoder.apply {
                stop()
                release()
            }
            videoEncoder.apply {
                stop()
                release()
            }
        }
    }

    private suspend fun renderWithSupportedAudioFormat(
        mediaExtractor: MediaExtractor,
        audioFormat: MediaFormat
    ) {
        executeSafely {
            val videoFormat = getVideoFormat()
            val videoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE)
            videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            val inputSurface = videoEncoder.createInputSurface()
            val thread = VideoRendererContainer(context, uiData, inputSurface) {
                videoEncoder.signalEndOfInputStream()
            }
            val bufferInfo = MediaCodec.BufferInfo()

            videoEncoder.start()
            thread.start()
            val mediaMuxer = MediaMuxer(getOutputFilePath(), MUXER_OUTPUT_FORMAT)
            val audioTrackIndex = mediaMuxer.addTrack(audioFormat)
            var videoTrackIndex = mediaMuxer.addTrack(videoFormat)
            val byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
            val audioBufferInfo = MediaCodec.BufferInfo()
            var isVideoEOS = false
            var isAudioEOS = false
            mediaMuxer.start()
            while (isVideoEOS.not() || isAudioEOS.not()) {

                if (isAudioEOS.not()) {
                    audioBufferInfo.offset = 0
                    audioBufferInfo.size = mediaExtractor.readSampleData(byteBuffer, 0)
                    if (bufferInfo.size < 0) {
                        isAudioEOS = true
                    }
                    bufferInfo.presentationTimeUs = mediaExtractor.sampleTime
                    bufferInfo.flags = mediaExtractor.sampleFlags
                    // val trackIndex = mediaExtractor.sampleTrackIndex
                    mediaMuxer.writeSampleData(audioTrackIndex, byteBuffer, bufferInfo)
                    mediaExtractor.advance()
                }


                val index = videoEncoder.dequeueOutputBuffer(bufferInfo, 0)
                if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    videoTrackIndex = mediaMuxer.addTrack(videoEncoder.outputFormat)
                    mediaMuxer.start()
                } else if (index >= 0) {
                    val byteBuffer = videoEncoder.getOutputBuffer(index)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        // The codec config data was pulled out and fed to the muxer when we got
                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        bufferInfo.size = 0
                    }
                    byteBuffer?.position(bufferInfo.offset)
                    byteBuffer?.limit(bufferInfo.offset + bufferInfo.size)
                    mediaMuxer.writeSampleData(videoTrackIndex, byteBuffer!!, bufferInfo)
                    videoEncoder.releaseOutputBuffer(index, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isVideoEOS = true
                    }
                } else {
                    logIt("NOTHING => $index")
                }
            }
            mediaExtractor.release()
            mediaMuxer.release()
            videoEncoder.stop()
            videoEncoder.release()
        }
    }

    private suspend fun renderOnlyWithVideo() {
        executeSafely {
            val videoFormat = getVideoFormat()
            val videoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE)
            videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            val inputSurface = videoEncoder.createInputSurface()
            val thread = VideoRendererContainer(context, uiData, inputSurface) {
                videoEncoder.signalEndOfInputStream()
            }
            val bufferInfo = MediaCodec.BufferInfo()

            videoEncoder.start()
            thread.start()
            val mediaMuxer = MediaMuxer(getOutputFilePath(), MUXER_OUTPUT_FORMAT)
            var trackIndex = -1
            var isEOS = false
            while (isEOS.not()) {
                val index = videoEncoder.dequeueOutputBuffer(bufferInfo, 0)
                if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    trackIndex = mediaMuxer.addTrack(videoEncoder.outputFormat)
                    mediaMuxer.start()
                } else if (index >= 0) {

                    val byteBuffer = videoEncoder.getOutputBuffer(index)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        // The codec config data was pulled out and fed to the muxer when we got
                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        bufferInfo.size = 0
                    }
                    byteBuffer?.position(bufferInfo.offset)
                    byteBuffer?.limit(bufferInfo.offset + bufferInfo.size)
                    mediaMuxer.writeSampleData(trackIndex, byteBuffer!!, bufferInfo)
                    videoEncoder.releaseOutputBuffer(index, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEOS = true
                    }
                } else {
                    logIt("NOTHING => $index")
                }
            }
            mediaMuxer.release()
            videoEncoder.stop()
            videoEncoder.release()
        }
    }

    private suspend fun sendProgress(currentTime: Long, videoLimitIsSecs: Int) {
        val videoLimit = videoLimitIsSecs * 1000000
        val progress = (currentTime * 100) / videoLimit
        if (progress in 1..100) {
            withContext(Dispatchers.Main) {
                progressCallback?.invoke(progress.toInt())
            }
        }
    }

    private suspend fun errorWrapper(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
            sendErrorOrCompletionCallback(true)
        }
    }

    private suspend inline fun executeSafely(body: () -> Unit) {
        var isError = false
        try {
            body.invoke()
        } catch (e: Exception) {
            isError = true
            e.printStackTrace()
        }
        sendErrorOrCompletionCallback(isError)
    }

    private suspend inline fun sendErrorOrCompletionCallback(isError: Boolean) {
        withContext(Dispatchers.Main) {
            if (isError.not()) {
                completedCallback?.invoke(generateFilePath)
            } else {
                errorCallback?.invoke()
            }
        }
    }

    private fun MediaFormat.isSupportedAudio() = this.getString(KEY_MIME) == MIMETYPE_AUDIO_AAC

    private fun getOutputFilePath(): String {
        generateFilePath = context.resources.getString(
            R.string.output_file_name,
            Environment.getExternalStorageDirectory().absolutePath,
            VideoUtils.getOutputName()
        )
        return generateFilePath
    }

    private fun logIt(message: String) {
        if (isDebugEnabled) {
            Log.d(LOG_TAG, message)
        }
    }
}