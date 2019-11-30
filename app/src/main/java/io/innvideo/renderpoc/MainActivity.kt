package io.innvideo.renderpoc

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler
import nl.bravobit.ffmpeg.FFmpeg
import nl.bravobit.ffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer


class MainActivity : AppCompatActivity() {

    companion object {
        private const val folderName = "RenderPOC"
        private const val FILE1_NAME = "test.mp4"
        private const val FILE2_NAME = "test2.mp4"
        private const val OUTPUT_NAME = "output.mp4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fetchIsFFmpegSupported()
    }


    private fun fetchIsFFmpegSupported() {
        if (FFmpeg.getInstance(this).isSupported) {
            copyFilesFromRawToStorage()
            //  startFFmpeg()
        } else {
            Log.e("FFMPEG", "FF MPeg is not supported")
        }
    }

    private fun copyFilesFromRawToStorage() {
        createDirectoryIfNotExisting {
            copyFileToExternalStorage(R.raw.test_video_one, FILE1_NAME)
            copyFileToExternalStorage(R.raw.test_video_two, FILE2_NAME)
            startFFmpeg(getFilePath(FILE1_NAME), getFilePath(FILE2_NAME))
        }
    }

    private fun getFilePath(fileName: String) =
        File("${getAppFolderPath()}${fileName}").absolutePath

    private fun getAppFolderPath() = "${Environment.getExternalStorageDirectory()}/${folderName}/"

    private fun startFFmpeg(videoOne: String, videoTwo: String) {
        /*  var videoOne = copyInputStreamToFile(
              resources.openRawResource(R.raw.test_video_one),
              getOutputMediaFile("video_one.mp4")!!
          ).absolutePath
          var videoTwo = copyInputStreamToFile(
              resources.openRawResource(R.raw.test_video_two),
              getOutputMediaFile("video_two")!!
          ).absolutePath*/
//        var outputFile = getOutputMediaFile("output").toString()
        val list = generateList(arrayOf(videoOne, videoTwo))
        var outputFile = "${getAppFolderPath()}${OUTPUT_NAME}"
        val command1 = arrayOf(
            "-f",
            "concat",
            "-safe",
            "0",
            "-i",
            list,
            "-c",
            "copy",
            outputFile
        )
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
        val TAG = "VIDEO_STITCHING"
        val ffmpeg = FFmpeg.getInstance(this)
        var hasError = false
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(command1, object : ExecuteBinaryResponseHandler() {

                override fun onStart() {
                    showToast("VIDEO STITCHING STARTED")
                }

                override fun onProgress(message: String?) {
                    Log.d(TAG, "Progress --->$message")
                    showToast("PROGRESS $message")
                }

                override fun onFailure(message: String?) {
                    Log.e(TAG, message ?: "Something went wrong")
                    showToast("VIDEO FAILED => $message")
                    hasError = true
                }

                override fun onSuccess(message: String?) {
                    Log.d(TAG, "VIDEO STITCHED SUCCESSFULLY --->$message")
                    showToast("VIDEO STITCHED SUCCESSFULLY")
                }

                override fun onFinish() {
                    showToast(
                        "VIDEO STITCHED COMPLETED ${
                        if (hasError) "WITH ERRORS" else ""
                        }"
                    )
                }

            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            showToast("FFMPEG ALREADY RUNNING")
            // Handle if FFmpeg is already running
        }
    }

    private fun generateList(inputs: Array<String>): String? {
        val list: File
        var writer: Writer? = null
        try {
            list = File(getAppFolderPath() + "ffmpeg-list.txt")
            writer = BufferedWriter(OutputStreamWriter(FileOutputStream(list)))
            for (input in inputs) {
                writer.write("file '$input'\n")
                Log.d(
                    "test",
                    "Writing to list file: file '$input'"
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return "/"
        } finally {
            try {
                if (writer != null) writer.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
        Log.d("test", "Wrote list file to " + list.absolutePath)
        return list.absolutePath
    }

    private fun showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun createDirectoryIfNotExisting(callback: () -> Unit) {
        try {
            val f = File(Environment.getExternalStorageDirectory(), folderName)
            if (!f.exists()) {
                f.mkdirs()
            }
            callback()
        } catch (e: Exception) {
            Toast.makeText(this, "Error in folder creation", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun copyFileToExternalStorage(
        resourceId: Int,
        resourceName: String
    ) {
        val pathSDCard = "${getAppFolderPath()}${resourceName}"
        try {
            val `in` = resources.openRawResource(resourceId)
            var out: FileOutputStream? = null
            out = FileOutputStream(pathSDCard)
            val buff = ByteArray(1024)
            var read = 0
            try {
                while (`in`.read(buff).also { read = it } > 0) {
                    out.write(buff, 0, read)
                }
            } finally {
                `in`.close()
                out.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
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
