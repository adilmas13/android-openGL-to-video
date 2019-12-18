package io.innvideo.renderpoc

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLUtils
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import io.innvideo.renderpoc.custom_views.MyGLSurface
import io.innvideo.renderpoc.custom_views.MyRenderer
import javax.microedition.khronos.opengles.GL10

class GLActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = MyGLSurface(this)
        setContentView(view)
        view.setRenderer(MyRenderer())
    }
}
