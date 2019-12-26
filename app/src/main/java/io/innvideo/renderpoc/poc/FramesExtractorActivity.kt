package io.innvideo.renderpoc.poc

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.utils.logIt
import io.innvideo.renderpoc.utils.toastIt
import kotlinx.android.synthetic.main.activity_frames_extractor.*

class FramesExtractorActivity : AppCompatActivity() {

    companion object {
        private val INPUT_FILE = "${Environment.getExternalStorageDirectory()}/aa/video.mp4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frames_extractor)
        btnExtract.setOnClickListener { this.extractFrames() }
    }

    private fun extractFrames() {
        val metaMetaDataRetriever = MediaMetadataRetriever()
        metaMetaDataRetriever.setDataSource(INPUT_FILE)
        try {
            val frames = metaMetaDataRetriever.getFramesAtIndex(0, 200)
            rvTimeline.apply {
                layoutManager = LinearLayoutManager(this@FramesExtractorActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = TimelineAdapter(frames)
            }
        }catch (e:Exception){
            toastIt("Something went wrong")
        }
        metaMetaDataRetriever.release()
        logIt("SOMETHING")
    }
}
