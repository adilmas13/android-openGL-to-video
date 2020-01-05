package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLSurfaceView
import io.innvideo.renderpoc.editor.openGL.textures.ImageTexture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class Render6(val context: Context) : GLSurfaceView.Renderer {
    private lateinit var bitmapTexture: ImageTexture

    override fun onDrawFrame(gl: GL10) {
        glClear(GL_COLOR_BUFFER_BIT)
        bitmapTexture.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        //  glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
      /*  bitmapTexture = BitmapTexture(context, ELUtils.drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.wall)!!)!!);
        bitmapTexture.onSurfaceCreated();*/
    }

}