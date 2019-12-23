package io.innvideo.renderpoc

import android.util.Log

class EglUtil {
    companion object {
        private const val LOG_TAG = "LOG_IT"
        private const val IS_LOG_ACTIVE = true

        fun logIt(message: String) {
            if (IS_LOG_ACTIVE) {
                Log.d(LOG_TAG, message)
            }
        }
    }
}