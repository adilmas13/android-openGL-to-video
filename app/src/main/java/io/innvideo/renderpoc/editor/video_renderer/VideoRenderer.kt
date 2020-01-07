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
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.editor.VideoRendererContainer
import io.innvideo.renderpoc.editor.new_models.parsed_models.MainUiData
import io.innvideo.renderpoc.poc.EglUtil
import io.innvideo.renderpoc.poc.VideoUtils
import java.nio.ByteBuffer

class VideoRenderer(
    private val context: Context,
    private val uiData: MainUiData
) {
    private var audioExist = false
    private var completedCallback: (() -> Unit)? = null
    private var errorCallback: (() -> Unit)? = null

    fun withAudio(): VideoRenderer {
        audioExist = true
        return this
    }

    fun onCompleted(callback: () -> Unit): VideoRenderer {
        this.completedCallback = callback
        return this
    }

    fun onError(callback: () -> Unit): VideoRenderer {
        this.errorCallback = callback
        return this
    }

    companion object {
        private const val TYPE_VIDEO = "video/"
        private const val TYPE_AUDIO = "audio/"
        private const val VIDEO_WIDTH = 320
        private const val VIDEO_HEIGHT = 240
        private const val VIDEO_BITRATE = 2000000
        private const val FRAME_INTERVAL = 5
        private const val FPS = 30
        private const val MAX_INPUT_SIZE = 0
        private const val VIDEO_MIME_TYPE = MIMETYPE_VIDEO_AVC
        private const val MUXER_OUTPUT_FORMAT = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
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
        setInteger(KEY_MAX_INPUT_SIZE,1048576 /*inputAudioFormat.getInteger(KEY_MAX_INPUT_SIZE)*/)
    }

    fun render() {
        if (audioExist) {
            renderWithAudio()
        } else {
            renderOnlyWithVideo()
        }
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
        throw IllegalStateException("'$type' track not found")
    }

    private fun renderWithAudio() {
        // first extract the audio track
        val mediaExtractor = MediaExtractor().apply { setDataSource(uiData.audioData.url) }
        val audioFormat = getTrackFormat(mediaExtractor, TYPE_AUDIO)

        if (audioFormat.isSupportedAudio()) {
            renderWithSupportedAudioFormat(mediaExtractor, audioFormat)
        } else {
            convertAudioToSupportedFormatAndRenderVideo(audioFormat, mediaExtractor)
        }
    }

    private fun convertAudioToSupportedFormatAndRenderVideo(
        oldAudioFormat: MediaFormat,
        extractor: MediaExtractor
    ) {
        val videoFormat = getVideoFormat()
        val newAudioFormat = getNewAudioFormat(oldAudioFormat)
        // Init audio decoder
        val audioDecoder = MediaCodec.createDecoderByType(oldAudioFormat.getString(KEY_MIME) ?: "")
            .apply {
                configure(oldAudioFormat, null, null, 0)
            }

        // Init audio encoder
        val audioEncoder =
            MediaCodec.createEncoderByType(newAudioFormat.getString(KEY_MIME) ?: MIMETYPE_AUDIO_AAC)
                .apply {
                    configure(newAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                }

        // Init video encoder
        val videoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE).apply {
            configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }

        val muxer = MediaMuxer(getOutputFilePath(), MUXER_OUTPUT_FORMAT)
        var videoTrackIndex = -1 //mediaMuxer.addTrack(videoFormat)
        var audioTrackIndex = -1 // muxer.addTrack(newAudioFormat)

        val inputSurface = videoEncoder.createInputSurface()
        val thread = VideoRendererContainer(context, uiData, inputSurface) {
            videoEncoder.signalEndOfInputStream()
        }

        val videoBufferInfo = MediaCodec.BufferInfo()
        val audioBufferInfo = MediaCodec.BufferInfo()


        audioDecoder.start()
        audioEncoder.start()
//        videoEncoder.start()
//        thread.start()
        var allInputExtracted = false
        var allInputDecoded = false
        var allOutputEncoded = false

        val timeoutUs = 10000L
        val bufferInfo = MediaCodec.BufferInfo()
        var trackIndex = -1
var counter = 0
        while (!allOutputEncoded) {

            // Feed input to audioDecoder
            if (!allInputExtracted) {
                val inBufferId = audioDecoder.dequeueInputBuffer(timeoutUs)
                if (inBufferId >= 0) {
                    val buffer = audioDecoder.getInputBuffer(inBufferId)!!
                    val sampleSize = extractor.readSampleData(buffer, 0)

                    if (sampleSize >= 0 && counter++ < 100) {
                        audioDecoder.queueInputBuffer(
                            inBufferId, 0, sampleSize,
                            extractor.sampleTime, extractor.sampleFlags
                        )

                        extractor.advance()
                    } else {
                        audioDecoder.queueInputBuffer(
                            inBufferId, 0, 0,
                            0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        allInputExtracted = true
                    }
                }
            }

            var encoderOutputAvailable = true
            var decoderOutputAvailable = !allInputDecoded

            while (encoderOutputAvailable || decoderOutputAvailable) {

                // Drain audioEncoder & mux first
                val outBufferId = audioEncoder!!.dequeueOutputBuffer(bufferInfo, timeoutUs)
                if (outBufferId >= 0) {

                    val encodedBuffer = audioEncoder!!.getOutputBuffer(outBufferId)!!

                    muxer!!.writeSampleData(trackIndex, encodedBuffer, bufferInfo)

                    audioEncoder!!.releaseOutputBuffer(outBufferId, false)

                    // Are we finished here?
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        allOutputEncoded = true
                        break
                    }
                } else if (outBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    encoderOutputAvailable = false
                } else if (outBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    trackIndex = muxer!!.addTrack(audioEncoder!!.outputFormat)
                    muxer!!.start()
                }

                if (outBufferId != MediaCodec.INFO_TRY_AGAIN_LATER)
                    continue

                // Get output from audioDecoder and feed it to audioEncoder
                if (!allInputDecoded) {
                    val outBufferId = audioDecoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
                    if (outBufferId >= 0) {
                        val outBuffer = audioDecoder.getOutputBuffer(outBufferId)

                        // If needed, process decoded data here
                        // ...

                        // We drained the audioEncoder, so there should be input buffer
                        // available. If this is not the case, we get a NullPointerException
                        // when touching inBuffer
                        val inBufferId = audioEncoder.dequeueInputBuffer(timeoutUs)
                        val inBuffer = audioEncoder.getInputBuffer(inBufferId)!!

                        // Copy buffers - audioDecoder output goes to audioEncoder input
                        inBuffer.put(outBuffer)

                        // Feed audioEncoder
                        audioEncoder.queueInputBuffer(
                            inBufferId, bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs,
                            bufferInfo.flags)

                        audioDecoder.releaseOutputBuffer(outBufferId, false)

                        // Did we get all output from audioDecoder?
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
                            allInputDecoded = true

                    } else if (outBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        decoderOutputAvailable = false
                    }
                }
            }
        }

        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
            videoCreationError()
        }
        videoCreationCompleted()
    }

    private fun renderWithSupportedAudioFormat(
        mediaExtractor: MediaExtractor,
        audioFormat: MediaFormat
    ) {
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
                EglUtil.logIt("NOTHING => $index")
            }
        }
        try {
            mediaExtractor.release()
            mediaMuxer.release()
            videoEncoder.stop()
            videoEncoder.release()
        } catch (e: Exception) {
            e.printStackTrace()
            videoCreationError()
        }
        videoCreationCompleted()
    }

    private fun renderOnlyWithVideo() {
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
                EglUtil.logIt("NOTHING => $index")
            }
        }
        try {
            mediaMuxer.release()
            videoEncoder.stop()
            videoEncoder.release()
        } catch (e: Exception) {
            e.printStackTrace()
            videoCreationError()
        }
        videoCreationCompleted()
    }

    private fun MediaFormat.isSupportedAudio() = this.getString(KEY_MIME) == MIMETYPE_AUDIO_AAC

    private fun videoCreationCompleted() = completedCallback?.invoke()

    private fun videoCreationError() = errorCallback?.invoke()

    private fun getOutputFilePath() =
        context.resources.getString(
            R.string.output_file_name,
            Environment.getExternalStorageDirectory().absolutePath,
            VideoUtils.getOutputName()
        )
}