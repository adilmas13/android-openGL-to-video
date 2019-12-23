package io.innvideo.renderpoc.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast

private const val LOG_TAG = "LOG_IT"
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