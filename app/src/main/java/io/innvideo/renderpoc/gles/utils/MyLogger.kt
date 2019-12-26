package io.innvideo.renderpoc.gles.utils

import android.util.Log

class MyLogger {

    companion object {
        private const val LOG_TAG = "LOG_IT"
        fun logIt(message: String) {
            Log.d(LOG_TAG, message)
        }
    }
}