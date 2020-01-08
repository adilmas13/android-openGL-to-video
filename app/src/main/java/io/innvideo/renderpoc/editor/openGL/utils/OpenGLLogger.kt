package io.innvideo.renderpoc.editor.openGL.utils

import android.annotation.SuppressLint
import android.util.Log

class OpenGLLogger {

    companion object {
        private const val LOG_TAG = "LOG_IT"
        private const val IS_ENABLED = false
        @SuppressLint("DefaultLocale")
        fun logIt(message: String) {
            if (IS_ENABLED)
                Log.d(LOG_TAG, message.toUpperCase())
        }
    }
}