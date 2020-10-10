package openGlToVideo.openGL.program

import android.content.Context
import androidx.annotation.RawRes
import openGlToVideo.openGL.utils.GLSLTextReader.Companion.readGlslFromRawRes
import openGlToVideo.openGL.utils.OpenGLUtils
import openGlToVideo.openGL.utils.OpenGLUtils.Companion.createFragmentShader
import openGlToVideo.openGL.utils.OpenGLUtils.Companion.createProgram
import openGlToVideo.openGL.utils.OpenGLUtils.Companion.createVertexShader
import openGlToVideo.openGL.utils.OpenGLUtils.Companion.useProgram
import io.opengltovideo.R

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