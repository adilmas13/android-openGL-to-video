package io.innvideo.renderpoc

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler
import nl.bravobit.ffmpeg.FFmpeg
import nl.bravobit.ffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


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
        var videoOne = copyInputStreamToFile(
            resources.openRawResource(R.raw.test_video_one),
            getOutputMediaFile("video_one")!!
        ).absolutePath
        var videoTwo = copyInputStreamToFile(
            resources.openRawResource(R.raw.test_video_two),
            getOutputMediaFile("video_two")!!
        ).absolutePath
        var outputFile = getOutputMediaFile("output").toString()

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
            outputFile.toString()
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

    private fun getOutputMediaFile(fileName: String): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        val mediaStorageDir = File(getExternalFilesDir(null)!!.absolutePath)

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }

        // Create a media file name
        val mediaFile: File
        mediaFile = File(mediaStorageDir.path + File.separator + fileName)
        return mediaFile
    }

    private fun copyInputStreamToFile(input: InputStream, file: File): File {
        var out: OutputStream? = null

        try {
            out = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int = 0
            while ((len == input.read(buf))) {
                out!!.write(buf, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if (out != null) {
                    out!!.close()
                }

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                input.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return file
        }
    }
}
