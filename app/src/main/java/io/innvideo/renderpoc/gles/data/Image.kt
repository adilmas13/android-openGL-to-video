package io.innvideo.renderpoc.gles.data

import android.opengl.GLES20.GL_TRIANGLE_FAN
import android.opengl.GLES20.glDrawArrays
import io.innvideo.renderpoc.gles.utils.TextureShaderProgram

class Image {

    private val POSITION_COMPONENT_COUNT = 2
    private val TEXTURE_COORDINATES_COMPONENT_COUNT: Int = 2
    private val STRIDE: Int =
        (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * 4 // FLOAT BYTE COUNT
    private val VERTEX_DATA = floatArrayOf(
        // Triangle Fan
        // Order of coordinates:
        // X,   Y,      S,      T
        0.0f, 0.0f, -0.5f, -0.8f,
        0.5f, -0.8f, 0.5f, 0.8f,
        -0.5f, 0.8f, -0.5f, -0.8f,
        0.5f, 0.5f, 0f, 0.9f,
        1f, 0.9f, 1f, 0.1f,
        0f, 0.1f, 0f, 0.9f
    )
    private var vertexArray: VertexArray

    init {

        vertexArray = VertexArray(VERTEX_DATA)
    }

    fun bindData(textureProgram: TextureShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            textureProgram.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT,
            STRIDE
        )
    }

    fun draw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6)
    }
}