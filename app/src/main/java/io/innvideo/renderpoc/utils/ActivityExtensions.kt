package io.innvideo.renderpoc.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast

fun Activity.logIt(message: String) {
    Log.d("LOG_IT", message)
}

fun Activity.toastIt(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Runnable.logIt(message: String) {
    Log.d("LOG_IT", message)
}