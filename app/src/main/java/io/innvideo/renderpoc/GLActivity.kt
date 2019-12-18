package io.innvideo.renderpoc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.innvideo.renderpoc.custom_views.MyGLSurface
import io.innvideo.renderpoc.custom_views.MyRenderer

class GLActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = MyGLSurface(this)
        setContentView(view)
        view.setRenderer(MyRenderer())
    }
}
