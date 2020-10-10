package io.innvideo.renderpoc.editor.videoRenderer

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.text.format.DateFormat
import java.io.File
import java.util.*


object VideoUtils {

    const val DIRECTORY_NAME = "render-poc"

    fun selectCodec(mimeType: String): MediaCodecInfo? {
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

    fun selectColorFormat(mimeType: String): Int {
        val codecInfo =
            selectCodec(
                mimeType
            )
        var colorFormat = 0
        val capabilities =
            codecInfo!!.getCapabilitiesForType(mimeType)
        for (i in capabilities.colorFormats.indices) {
            if (isRecognizedFormat(
                    capabilities.colorFormats[i]
                )
            ) {
                colorFormat = capabilities.colorFormats[i]
                break
            }
        }
        return colorFormat
    }

    fun isRecognizedFormat(colorFormat: Int): Boolean {
        return when (colorFormat) {
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar,
            MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar -> true
            else -> false
        }
    }

    fun getOutputName(): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = System.currentTimeMillis()
        return "${DateFormat.format("dd_MMM_yyyy_hh_mm_ss_aaa", cal)}"
    }
}

fun File.size(): String {
    val divideBy = 1000f
    val size = this.length().toFloat()
    var s = ""
    val kb = (size / divideBy).toDouble()
    val mb = kb / divideBy
    val gb = mb / divideBy
    val tb = gb / divideBy
    if (size < divideBy) {
        s = "$size Bytes"
    } else if (size >= divideBy && size < divideBy * divideBy) {
        s = String.format("%.2f", kb) + " KB"
    } else if (size >= divideBy * divideBy && size < divideBy * divideBy * divideBy) {
        s = String.format("%.2f", mb) + " MB"
    } else if (size >= divideBy * divideBy * divideBy && size < divideBy * divideBy * divideBy * divideBy) {
        s = String.format("%.2f", gb) + " GB"
    } else if (size >= divideBy * divideBy * divideBy * divideBy) {
        s = String.format("%.2f", tb) + " TB"
    }
    return s
}