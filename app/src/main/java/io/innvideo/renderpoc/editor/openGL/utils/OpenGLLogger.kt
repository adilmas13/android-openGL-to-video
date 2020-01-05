package io.innvideo.renderpoc.editor.openGL.utils

import android.annotation.SuppressLint
import android.util.Log

class OpenGLLogger {

    companion object {
        private const val LOG_TAG = "LOG_IT"
        @SuppressLint("DefaultLocale")
        fun logIt(message: String) {
            Log.d(LOG_TAG, message.toUpperCase())
        }
    }
}