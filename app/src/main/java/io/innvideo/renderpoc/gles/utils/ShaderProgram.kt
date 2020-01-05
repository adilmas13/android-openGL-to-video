package io.innvideo.renderpoc.gles.utils

import android.content.Context
import android.opengl.GLES20
import io.innvideo.renderpoc.editor.openGL.utils.GLSLTextReader
import io.innvideo.renderpoc.editor.openGL.utils.OpenGLUtils

open class ShaderProgram(
    context: Context,
    vertexShaderResourceId: Int,
    fragmentShaderResourceId: Int
) {

    // Uniform constants
    protected val U_MATRIX = "u_Matrix"
    protected val U_TEXTURE_UNIT = "u_TextureUnit"
    // Attribute constants
    protected val A_POSITION = "a_Position"
    protected val A_COLOR = "a_Color"
    protected val A_TEXTURE_COORDINATES = "a_TextureCoordinates"
    // Shader program
    protected var program = 0

    init {
        val vertexShader = OpenGLUtils.createVertexShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                vertexShaderResourceId
            )
        )
        val fragmentShader = OpenGLUtils.createFragmentShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                fragmentShaderResourceId
            )
        )
        program = OpenGLUtils.createProgram(
            vertexShader,
            fragmentShader
        )
    }

    fun useProgram() = GLES20.glUseProgram(program)
}