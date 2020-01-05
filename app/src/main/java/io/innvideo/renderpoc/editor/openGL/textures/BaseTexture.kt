package io.innvideo.renderpoc.editor.openGL.textures

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.editor.openGL.program.OpenGLProgram
import io.innvideo.renderpoc.editor.openGL.utils.OpenGLConstants.FLOAT_SIZE_IN_BYTES
import java.nio.ByteBuffer
import java.nio.ByteOrder

/*
* vertex float example
* var vertexData = floatArrayOf( // in counterclockwise order:
             -1.0f, -1.0f, 0.0f,  // bottom left
             1f, -1.0f, 0.0f,  // bottom right
             -1.0f, 1.0f, 0.0f,  // top left
             1.0f, 1.0f, 0.0f // top right
* */
abstract class BaseTexture(
    private val context: Context,
    vertexData: FloatArray
) {

    abstract fun getBitmap(): Bitmap

    companion object {
        var textureData = floatArrayOf( // in counterclockwise order:
            0f, 1f, 0.0f,  // bottom left
            1f, 1f, 0.0f,  // bottom right
            0f, 0f, 0.0f,  // top left
            1f, 0f, 0.0f
        )
        const val COORDS_PER_VERTEX = 3
    }

    private val vertexCount = vertexData.size / COORDS_PER_VERTEX
    private val vertexStride = COORDS_PER_VERTEX * FLOAT_SIZE_IN_BYTES // 4 bytes per vertex
    private val vertexBuffer =
        ByteBuffer.allocateDirect(vertexData.size * FLOAT_SIZE_IN_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
    private val textureBuffer =
        ByteBuffer.allocateDirect(textureData.size * FLOAT_SIZE_IN_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureData)

    init {
        vertexBuffer.position(0)
        textureBuffer.position(0)
    }

    private var avPosition = 0
    private var afPosition = 0
    private var textureId = 0

    private lateinit var program: OpenGLProgram

    private fun initialise(): BaseTexture {
        program = OpenGLProgram(
            context,
            R.raw.vertex_shader,
            R.raw.fragment_shader
        )
        if (program.programId > 0) {
            avPosition = GLES20.glGetAttribLocation(program.programId, "av_Position")
            afPosition = GLES20.glGetAttribLocation(program.programId, "af_Position")
            val textureIds = IntArray(1)
            GLES20.glGenTextures(1, textureIds, 0)
            if (textureIds[0] == 0) {
                return this
            }
            textureId = textureIds[0]
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, getBitmap(), 0)
        }
        return this
    }

    fun draw() {
        initialise()
        program.useProgram()
        GLES20.glEnableVertexAttribArray(avPosition)
        GLES20.glEnableVertexAttribArray(afPosition)
        //设置顶点位置值
        GLES20.glVertexAttribPointer(
            avPosition,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        GLES20.glVertexAttribPointer(
            afPosition,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            textureBuffer
        )
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        //transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        //绘制 GLES20.GL_TRIANGLE_STRIP:复用坐标
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)
        GLES20.glDisableVertexAttribArray(avPosition)
        GLES20.glDisableVertexAttribArray(afPosition)
    }

}