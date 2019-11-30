package io.innvideo.renderpoc

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler
import nl.bravobit.ffmpeg.FFmpeg
import nl.bravobit.ffmpeg.exceptions.FFmpegCommandAlreadyRunningException


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fetchIsFFmpegSupported()
    }


    private fun fetchIsFFmpegSupported() {
        if (FFmpeg.getInstance(this).isSupported) {
            startFFmpeg()
        } else {
            Log.e("FFMPEG", "FF MPeg is not supported")
        }
    }

    private fun startFFmpeg() {
        val ffmpeg = FFmpeg.getInstance(this)
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(arrayOf("-version"), object : ExecuteBinaryResponseHandler() {

                override fun onStart() {}

                override fun onProgress(message: String?) {}

                override fun onFailure(message: String?) {}

                override fun onSuccess(message: String?) {
                    Log.e("", "message --->$message")
                }

                override fun onFinish() {}

            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            // Handle if FFmpeg is already running
        }

    }
}
