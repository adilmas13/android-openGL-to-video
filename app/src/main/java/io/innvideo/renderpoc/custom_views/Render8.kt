package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.gles.utils.OpenGLUtils
import io.innvideo.renderpoc.gles.utils.GLSLTextReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/*
* Square
*
* */
class Render8(val context: Context) : GLSurfaceView.Renderer {

    private var programId = 0
    private var fragmentShaderId = 0
    private var vertexShaderId = 0

    companion object {
        private const val COORDS_PER_VERTEX = 3 // 3 vertices for a triangle
        private const val BYTES_PER_FLOAT = 4 // bytes per float is universal, can never change :P
        private const val BYTES_PER_SHORT = 2 // bytes per short is universal, can never change :P

        private var vertices = floatArrayOf(
            // x,y,z
            -0.5f,  0.5f,   0.0f,      // top left
            -0.5f,  -0.5f,  0.0f,      // bottom left
            0.5f,   -0.5f,  0.0f,      // bottom right
            0.5f,   0.5f,   0.0f       // top right
        )

        private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

        // initialize byte buffer for the draw list
        private val drawListBuffer: ShortBuffer =
            // (# of coordinate values * 2 bytes per short)
            ByteBuffer.allocateDirect(drawOrder.size * BYTES_PER_SHORT).run {
                order(ByteOrder.nativeOrder())
                asShortBuffer().apply {
                    put(drawOrder)
                    position(0)
                }
            }

        private val vertexCount = vertices.size / COORDS_PER_VERTEX // 9/3 = 3

        private const val vertexStride =
            COORDS_PER_VERTEX * BYTES_PER_FLOAT // 4 bytes per vertex // 3x4 = 12

    }

    private var vertexBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(vertices.size * BYTES_PER_FLOAT).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(vertices)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    override fun onDrawFrame(gl: GL10) {
        OpenGLUtils.validateProgram(programId)
        // Add program to OpenGL ES environment
        OpenGLUtils.useProgram(programId)

        GLES20.glEnableVertexAttribArray(0)

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
            0,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        // Draw the triangle
       // GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            drawListBuffer
        )

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        vertexShaderId = OpenGLUtils.createVertexShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                R.raw.sample_vertex_shader
            )
        )
        fragmentShaderId = OpenGLUtils.createFragmentShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                R.raw.sample_fragment_shader
            )
        )

        // create empty OpenGL ES Program
        programId = OpenGLUtils.createProgram(vertexShaderId, fragmentShaderId)

        if (programId > 0) {
            // delete the shaders since link is formed
            OpenGLUtils.deleteShader(vertexShaderId)
            OpenGLUtils.deleteShader(fragmentShaderId)
        }
    }

}