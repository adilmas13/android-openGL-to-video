package io.innvideo.renderpoc

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.innvideo.renderpoc.utils.getXTranslationBasedOnStartEndVal
import kotlinx.android.synthetic.main.activity_main.*
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

    private var screenWidth = 0

    companion object {
        private const val folderName = "RenderPOC"
        private const val FILE1_NAME = "test.mp4"
        private const val FILE2_NAME = "test2.mp4"
        private const val OUTPUT_NAME = "output.mp4"

        const val ALPHA_FROM = 0f
        const val ALPHA_TO = 1f
        const val ANIM_DURATION = 2000
        const val ANIM_END_VAL = 0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        tvSunny.alpha = 0f
        fetchIsFFmpegSupported()
        setScreenWidth()
        animateText()
    }

    private fun setScreenWidth() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
    }

    private fun getObjectAnimatorRightToLeft(view: View): ObjectAnimator {
        val viewObjAnim = view.getXTranslationBasedOnStartEndVal(
            fetchViewPositionOutOfScreenX(view), ANIM_END_VAL, ANIM_DURATION, ALPHA_FROM, ALPHA_TO
        )
        viewObjAnim.startDelay = 500
        return viewObjAnim
    }

    private fun fetchViewPositionOutOfScreenX(view: View): Float =
        (screenWidth - view.left).toFloat()

    private fun animateText() {
        tvSunny.post {
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(
                getObjectAnimatorRightToLeft(tvSunny)
            )
            animatorSet.startDelay = 1000
            animatorSet.start()
        }
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
        val list = generateList(arrayOf(videoTwo, videoOne))
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
            val len = 0
            while ((len == input.read(buf))) {
                out.write(buf, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                out?.close()

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
