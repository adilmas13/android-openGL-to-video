package io.innvideo.renderpoc

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Level
import com.arthenica.mobileffmpeg.util.AsyncExecuteTask
import com.arthenica.mobileffmpeg.util.ExecuteCallback
import io.innvideo.renderpoc.utils.getXTranslationBasedOnStartEndVal
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
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
    lateinit var timeline: ValueAnimator

    companion object {
        private const val folderName = "aa"
        private const val FILE1_NAME = "test.mp4"
        private const val FILE2_NAME = "test2.mp4"
        private const val OUTPUT_NAME = "output.mp4"

        const val ALPHA_FROM = 0f
        const val ALPHA_TO = 1f
        const val ANIM_DURATION = 2000
        const val ANIM_END_VAL = 0f

        const val SECONDS = 5
        const val DURATION = SECONDS * 1000
        const val VIDEO_OUTPUT = "awesome.mp4"
        const val VIDEO_OUTPUT2 = "awesome2.mp4"
    }

    private fun myFunction() {
        val list = generateList(arrayOf(VIDEO_OUTPUT, VIDEO_OUTPUT2))
        val image = "/storage/emulated/0/Pictures/Screenshots/1.jpg"
        val outputFile = File("${getAppFolderPath()}${VIDEO_OUTPUT}")
        val outputFile2 = File("${getAppFolderPath()}${VIDEO_OUTPUT2}")
        if (outputFile.exists()) {
            outputFile.delete()
        }
        Log.d("TIMING", "STARTED => " + System.currentTimeMillis())
        for (x in 0 until 100) {
            Log.d("TIMING", "FRAME START => ")
            var command = ""
            if (outputFile.exists()) {
                command = " -y -i $image -t 0.04 ${outputFile2.absolutePath}"
                com.arthenica.mobileffmpeg.FFmpeg.execute(command)
                command =
                    "-y -f concat -safe 0 -i $list -c copy ${outputFile.absolutePath}"
                com.arthenica.mobileffmpeg.FFmpeg.execute(command)
            } else {
                command = "-i $image -t 0.04 ${outputFile.absolutePath}"
                com.arthenica.mobileffmpeg.FFmpeg.execute(command)
            }
        }
        Log.d("TIMING", "END => " + System.currentTimeMillis())
    }

    private fun myFunction3() {
        val stroke = "/storage/emulated/0/aa/stroke.png"
        val video = "/storage/emulated/0/aa/video.mp4"
        val outputFile = File("${getAppFolderPath()}${VIDEO_OUTPUT}").absoluteFile
//        val commandw = "-y -i $video -i $stroke -filter_complex \"[0:v][1:v] overlay=25:25:enable='between(t,0,4)'\" -pix_fmt yuv420p -c:a copy $outputFile"
        val commandw =
            "-y -i $video -i $stroke -filter_complex 'overlay=10:main_h-overlay_h-10' -vcodec libx264 -c:a copy -preset ultrafast -y -tune fastdecode -profile:v high $outputFile"
        Log.d("COMMAND", commandw)

        CoroutineScope(Dispatchers.IO).launch {
            val started = System.currentTimeMillis()
            Log.d("TIMING", "STARTED => $started")
            val code = com.arthenica.mobileffmpeg.FFmpeg.execute(commandw)
            val ended = System.currentTimeMillis()
            if (code == 0) {
                Log.d("TIMING", "ENDED => $ended")
                Log.d("TIMING", "FINAL => ${(ended - started) / 1000}")
                //      showToast("COMPLETED IN ${ended - started}")
            } else {
                Log.d("TIMING", "SOMETHING WENT WRONG")
                //   showToast("SOMETHING WENT WRONG")
            }
        }
    }

    private fun myFunction2() {
        val outputFile = File("${getAppFolderPath()}${VIDEO_OUTPUT}").absoluteFile
        val type = "Jpg"
//        val type = "PNG"
        val imageNameFormat = "LandingPageVideo_%5d"
        val images = "/storage/emulated/0/test/$type/$imageNameFormat.${type.toLowerCase()}"
        val command =
            "-hide_banner -y -c:v libopenh264 -r 30 -start_number 0 -i $images $outputFile"
        CoroutineScope(Dispatchers.IO).launch {
            val started = System.currentTimeMillis()
            Log.d("TIMING", "STARTED => $started")
            val code = com.arthenica.mobileffmpeg.FFmpeg.execute(command)
            val ended = System.currentTimeMillis()
            if (code == 0) {
                Log.d("TIMING", "ENDED => $ended")
                Log.d("TIMING", "FINAL => ${(ended - started) / 1000}")
                //      showToast("COMPLETED IN ${ended - started}")
            } else {
                Log.d("TIMING", "SOMETHING WENT WRONG")
                //   showToast("SOMETHING WENT WRONG")
            }
        }
    }

    fun View.getWidthAndHeightAfterRender(callback: (Int, Int) -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (width > 0 && height > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    callback(width, height)
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
//        start()
//        startFFmpeg(FILE1_NAME, FILE2_NAME)
        /* canvas.getWidthAndHeightAfterRender { i, i2 ->

         }
         GlobalScope.launch {
             delay(3000)
             withContext(Dispatchers.Main) {
                 startNewFFMpeg()
             }
         }*/
        //startNewFFMpeg()
//        copyFilesFromRawToStorage()
        Config.setLogLevel(Level.AV_LOG_VERBOSE)
        Config.enableLogCallback {
            Log.d("FFMPEG_IT", it.text)
        };
        Handler().postDelayed({
            myFunction3()
        }, 3000)

    }

    private fun trial() {
        /*   val outputFilePath = "${getAppFolderPath()}${OUTPUT_NAME}"
           val pipe1 = Config.registerNewFFmpegPipe(this)

           MyTask(object : DoneCallback {
               override fun done() {
                   Log.d("DONE", "DONE")
               }
           }).execute(command)*/
        val outputFilePath = "${getAppFolderPath()}${OUTPUT_NAME}"
        val pipe1 = Config.registerNewFFmpegPipe(this)
        val command =
            "-y -i \"$pipe1\" -c:v mpeg4 -r 25 $outputFilePath"
        startFFMPEG(command)
    }


    private fun startNewFFMpeg() {
        Config.setLogLevel(Level.AV_LOG_VERBOSE)
        Config.enableLogCallback {
            Log.d("FFMPEG", it.text)
        };
        copyFilesFromRawToStorage()
        val outputFilePath = "${getAppFolderPath()}${OUTPUT_NAME}"
        val inputFIle = "${getAppFolderPath()}${FILE1_NAME}"

        val pipe1 = Config.registerNewFFmpegPipe(this)
        /*val ffmpegCommand =
            "-y -f rawvideo -pix_fmt argb -s 1080x1920 -r 10 -i $pipe1  -c:v libx264 $outputFilePath"*/

        val ffmpegCommand =
            "-y -i " + pipe1 + " -filter:v loop=loop=25*3:size=1 -c:v mpeg4 -r 25 " + outputFilePath
        startCommand(ffmpegCommand)

        /* CoroutineScope(Dispatchers.IO).launch {
             com.arthenica.mobileffmpeg.FFmpeg.execute(ffmpegCommand)
             val bytes = getBitmap(getBitmapFromView(canvas))
             for (x in 0 until 10) {
                 val appendCommand = arrayOf("sh", "-c", "cat $inputFIle > $pipe1")
                 Runtime.getRuntime().exec(appendCommand)
             }
             Config.closeFFmpegPipe(ffmpegCommand)
         }*/
    }


    private fun startFFMPEG(command: String) {
        var image1File = File(this.cacheDir, "abc.jpg")
        val image =
            "/storage/emulated/0/Pictures/Screenshots/1.jpg" //getFilePath("Pictures/Screenshots/1.jpg")
        val pipe1 = Config.registerNewFFmpegPipe(this)
        try {
            var asyncCommand = AsyncExecuteTask(object : ExecuteCallback {
                override fun apply(returnCode: Int, executeOutput: String?) {
                    showToast("AA GAYA")
                }

            });
            asyncCommand.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, command);
            for (i in 0..50) {
                var asyncCatImageCmd = AsyncCatImageTask();
                asyncCatImageCmd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image, pipe1)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun startCommand(command: String) {
        val callback = object : ExecuteCallback {
            override fun apply(returnCode: Int, executeOutput: String?) {
                Log.d("TEST", "HERE")
            }
        }
        val task = AsyncExecuteTask(callback)
        task.execute(command)
    }

    private fun getBitmap(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun getBitmapFromView(view: View): Bitmap {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth, view.measuredHeight,
            Bitmap.Config.ARGB_8888
        );
        val canvas = Canvas(bitmap);
        view.layout(0, 0, view.measuredWidth, view.measuredHeight);
        view.draw(canvas);
        return bitmap;
    }

    private fun start() {
        timeline = ValueAnimator()
        timeline.setValues(PropertyValuesHolder.ofInt("x", 300, 600))
        timeline.addUpdateListener {
            Log.d("VALUE", it.animatedFraction.toString())
        }
        timeline.duration = 4000
        timeline.startDelay = 2000
        timeline.start()
    }

    private fun moveTo(time: Long) {
        timeline.currentPlayTime = time
    }

    private fun initViews() {
        //    tvSunny.alpha = 0f
        fetchIsFFmpegSupported()
        setScreenWidth()
        animateText()
        seekbar.apply {
            max = DURATION
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    Log.d("PROGRESS => ", progress.toString())
                    if (progress > 0) {
                        timeline.currentPlayTime = progress.toLong()
                    }
                }

            })
        }
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
        /*   if (FFmpeg.getInstance(this).isSupported) {
               //  startFFmpeg()
           } else {
               Log.e("FFMPEG", "FF MPeg is not supported")
           }*/
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
        /* val ffmpeg = FFmpeg.getInstance(this)
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
         }*/
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

    inner class MyTask(private val callback: DoneCallback) : AsyncTask<String, Int, Int>() {

        override fun doInBackground(vararg command: String?): Int {
            return com.arthenica.mobileffmpeg.FFmpeg.execute(command[0])
        }

        override fun onPostExecute(result: Int?) {
            callback.done()
        }
    }

    interface DoneCallback {
        fun done()
    }


    inner class AsyncCatImageTask : AsyncTask<String?, Int?, Int>() {
        override fun doInBackground(vararg inputs: String?): Int {
            try {
                val asyncCommand = "cat " + inputs[0] + " > " + inputs[1]
                Log.d("TEST", String.format("Starting async cat image command: %s", asyncCommand))
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", asyncCommand))
                val rc = process.waitFor()
                Log.d(
                    "TEST",
                    String.format(
                        "Async cat image command: %s exited with %d.",
                        asyncCommand,
                        rc
                    )
                )
                return rc
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return -1
            }
        }
    }

}
