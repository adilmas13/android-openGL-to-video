package io.innvideo.renderpoc.gles.utils

import android.content.Context
import android.opengl.GLES20

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
        val vertexShader = ELUtils.createVertexShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                vertexShaderResourceId
            )
        )
        val fragmentShader = ELUtils.createFragmentShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                fragmentShaderResourceId
            )
        )
        program = ELUtils.createProgram(
            vertexShader,
            fragmentShader
        )
    }

    fun useProgram() = GLES20.glUseProgram(program)
}