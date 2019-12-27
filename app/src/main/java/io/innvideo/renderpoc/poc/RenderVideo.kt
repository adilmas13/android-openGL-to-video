package io.innvideo.renderpoc.poc

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.poc.EglUtil.Companion.logIt
import io.innvideo.renderpoc.poc.interfaces.RenderListener

class RenderVideo(
    private val context: Context,
    private val listener: RenderListener
) {

    companion object {
        private const val VIDEO_WIDTH = 320
        private const val VIDEO_HEIGHT = 240
        private const val VIDEO_BITRATE = 2000000
        private const val FRAME_INTERVAL = 5
        private const val FPS = 30
        private const val MAX_INPUT_SIZE = 0
        private const val MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC
    }

     fun startRendering() {
        listener.cancel()

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
            RendererThread(
                inputSurface,
                list = null,
                context = context
            ) { mediaCodec.signalEndOfInputStream() }
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
        context.resources.getString(
            R.string.output_file_name,
            Environment.getExternalStorageDirectory().absolutePath,
            VideoUtils.getOutputName()
        )
}