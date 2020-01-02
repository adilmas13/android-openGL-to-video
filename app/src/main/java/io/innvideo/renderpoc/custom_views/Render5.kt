package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLSurfaceView
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.gles.data.Image
import io.innvideo.renderpoc.gles.utils.ELUtils
import io.innvideo.renderpoc.gles.utils.TextureShaderProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class Render5(val context: Context) : GLSurfaceView.Renderer {
    private val projectionMatrix = FloatArray(16)
    var textureCoordinates = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
    )

    private lateinit var textureProgram: TextureShaderProgram
    // private val colorProgram: ColorShaderProgram? = null
    private var texture = 0

    private lateinit var image: Image

    override fun onDrawFrame(gl: GL10) {
        glClear(GL_COLOR_BUFFER_BIT)

        // Draw the table.
        textureProgram.useProgram()
        textureProgram.setUniforms(projectionMatrix, texture)
        image.bindData(textureProgram)
        image.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        image = Image()
        textureProgram = TextureShaderProgram(context)
        texture = ELUtils.createTexture(context, R.drawable.wall)
    }


}