package io.innvideo.renderpoc.editor

import android.app.Activity
import android.util.Log
import android.widget.Toast

private const val LOG_TAG = "INVIDEO_RENDERER"
fun Activity.logIt(message: String) {
    Log.d(LOG_TAG, message)
}

fun Activity.toastIt(message: String): Toast {
    val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    toast.show()
    return toast
}

fun Runnable.logIt(message: String) {
    Log.d(LOG_TAG, message)
}