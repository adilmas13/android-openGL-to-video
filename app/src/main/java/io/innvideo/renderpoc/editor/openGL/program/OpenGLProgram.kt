package io.innvideo.renderpoc.editor.openGL.program

import android.content.Context
import androidx.annotation.RawRes
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.editor.openGL.utils.GLSLTextReader.Companion.readGlslFromRawRes
import io.innvideo.renderpoc.editor.openGL.utils.OpenGLUtils
import io.innvideo.renderpoc.editor.openGL.utils.OpenGLUtils.Companion.createFragmentShader
import io.innvideo.renderpoc.editor.openGL.utils.OpenGLUtils.Companion.createProgram
import io.innvideo.renderpoc.editor.openGL.utils.OpenGLUtils.Companion.createVertexShader
import io.innvideo.renderpoc.editor.openGL.utils.OpenGLUtils.Companion.useProgram

class OpenGLProgram(
    context: Context,
    @RawRes vertexShaderResId: Int = R.raw.vertex_shader,
    @RawRes fragmentShaderResId: Int = R.raw.fragment_shader
) {

    var programId = 0

    init {
        val vertexSource = readGlslFromRawRes(context, vertexShaderResId)
        val fragmentSource = readGlslFromRawRes(context, fragmentShaderResId)

        val vertexShaderId = createVertexShader(vertexSource)
        val fragmentShaderId = createFragmentShader(fragmentSource)

        programId = createProgram(vertexShaderId, fragmentShaderId)
    }

    fun useProgram() = useProgram(programId)

    fun clear() = OpenGLUtils.deleteProgram(programId)
}