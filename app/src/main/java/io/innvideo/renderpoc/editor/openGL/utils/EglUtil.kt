package io.innvideo.renderpoc.editor.openGL.utils

import android.opengl.EGL14
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

        private fun checkEglError(msg: String) {
            var error: Int
            if (EGL14.eglGetError().also { error = it } != EGL14.EGL_SUCCESS) {
                throw RuntimeException(
                    "$msg: EGL error: 0x" + Integer.toHexString(
                        error
                    )
                )
            }
        }
    }
}