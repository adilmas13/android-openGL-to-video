package io.innvideo.renderpoc

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.api.load
import com.google.gson.Gson
import io.innvideo.renderpoc.custom_views.CustomImageView
import io.innvideo.renderpoc.custom_views.CustomTextView
import io.innvideo.renderpoc.model.Component
import io.innvideo.renderpoc.model.Position
import io.innvideo.renderpoc.model.VideoDataModel
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var data: VideoDataModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        readJson()
    }

    private fun readJson() {
        val homeResponseString: String =
            assets.open("video.json").bufferedReader().use { it.readText() }
        data = Gson().fromJson(homeResponseString, VideoDataModel::class.java)
        readBlocks()
    }

    private fun readBlocks() {
        val blocks = data.blocks
        for (blockItem in blocks) {
            val components = blockItem.components
            Log.e("", "*********** BLOCK **************")
            Log.e("", "Name ---->" + blockItem.name)
            Log.e("", "************* END OF BLOCK ************")
            readComponentList(components)
        }
    }

    private fun readComponentList(components: MutableList<Component>) {
        for (component in components) {
            if (component.components.isNullOrEmpty()) {
                readComponent(component)
            } else {
                Log.e("", "************ COMPOSITE COMPONENT *************")
                Log.e("", "Name ---->" + component.name)
                Log.e("", "************* END OF COMPOSITE COMPONENT ************")
                readComponentList(component.components)
            }
        }
    }

    private fun readComponent(component: Component) {
        Log.e("", "************ COMPONENT *************")
        Log.e("", "Name ---->" + component.name)
        Log.e("", "************* END OF COMPONENT ************")
        when (component.type) {
            "image" -> setImage(component)
            "text" -> setTextComponent(component)
        }
    }

    private fun setImage(imageComponent: Component) {
        val image: CustomImageView =
            LayoutInflater.from(this).inflate(R.layout.imageview, canvas, false) as CustomImageView
        image.load(imageComponent.url)
        image.scaleType = ImageView.ScaleType.CENTER_CROP
        canvas.addView(image)
    }

    private fun setTextComponent(component: Component) {
        val textView: CustomTextView =
            LayoutInflater.from(this).inflate(R.layout.textview, canvas, false) as CustomTextView
        textView.text = component.text
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        textView.textSize = 18f
        canvas.addView(textView)
    }

    private fun setViewPosition(position: Position, view: View) {
        view.top = position.top_x.toInt()
        view.bottom = position.bottom_x.toInt()
        //dummy
    }
}
