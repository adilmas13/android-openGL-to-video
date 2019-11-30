package io.innvideo.renderpoc

import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_MOVIES
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.Environment.getExternalStorageState
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler
import nl.bravobit.ffmpeg.FFmpeg
import nl.bravobit.ffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


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
        var videoOne = "android.resource://" + packageName + "/" + R.raw.test_video_one
        var videoTwo = "android.resource://" + packageName + "/" + R.raw.test_video_two
        var outputFile = getOutputMediaFile(MEDIA_TYPE_VIDEO)

        val command = arrayOf(
            "-y",
            "-i",
            videoOne,
            "-i",
            videoTwo,
            "-strict",
            "experimental",
            "-filter_complex",
            "[0:v]scale=480x640,setsar=1:1[v0];[1:v]scale=480x640,setsar=1:1[v1];[v0][0:a][v1][1:a] concat=n=2:v=1:a=1",
            "-ab",
            "48000",
            "-ac",
            "2",
            "-ar",
            "22050",
            "-s",
            "480x640",
            "-vcodec",
            "libx264",
            "-crf",
            "27",
            "-q",
            "4",
            "-preset",
            "ultrafast",
            outputFile!!.toFile().toString()
        )

        val ffmpeg = FFmpeg.getInstance(this)
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(command, object : ExecuteBinaryResponseHandler() {

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

    private fun getOutputMediaFile(type: Int): Uri? {
        // To be safe, you should check that the SDCard is mounted

        if (getExternalStorageState() != null) {
            // this works for Android 2.2 and above
            val mediaStorageDir = File(
                getExternalStoragePublicDirectory(DIRECTORY_MOVIES),
                "SMW_VIDEO"
            )

            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("ffmpeg", "failed to create directory")
                    return null
                }
            }

            // Create a media file name
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val mediaFile: File
            if (type == MEDIA_TYPE_VIDEO) {
                mediaFile = File(
                    mediaStorageDir.getPath() + File.separator +
                            "VID_" + timeStamp + ".mp4"
                )
            } else {
                return null
            }

            return Uri.fromFile(mediaFile)
        }

        return null
    }
}
