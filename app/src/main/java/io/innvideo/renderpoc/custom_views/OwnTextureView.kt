package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import android.view.TextureView

class OwnTextureView(context: Context): TextureView(context) {

    override fun buildLayer() {
        super.buildLayer()
        MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC).apply {
         //  setInputSurface()
        }
    }


}