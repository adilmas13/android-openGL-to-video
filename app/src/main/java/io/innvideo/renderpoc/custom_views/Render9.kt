package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.editor.openGL.utils.OpenGLUtils
import io.innvideo.renderpoc.editor.openGL.utils.GLSLTextReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/*
* Texture
*
* */
class Render9(val context: Context) : GLSurfaceView.Renderer {

    private var programId = 0
    private var fragmentShaderId = 0
    private var vertexShaderId = 0
    private var textureId = 0

    private var programId2 = 0
    private var fragmentShaderId2 = 0
    private var vertexShaderId2 = 0
    private var textureId2 = 0

    companion object {
        private const val COORDS_PER_VERTEX = 3
        // bytes per float is universal, can never change :P
        private const val BYTES_PER_FLOAT = 4

        /*private var vertices = floatArrayOf(
            // positions
            // positions
            0.5f, -0.5f, 0.0f,                        // bottom right
            -0.5f, -0.5f, 0.0f,                    // bottom left
            0.0f, 0.5f, 0.0f
        )*/

        private var vertices = floatArrayOf(
            -1.0f, -1.0f, 0.0f,  // bottom left
            1f, -1.0f, 0.0f,  // bottom right
            -1.0f, 1.0f, 0.0f,  // top left
            1.0f, 1.0f, 0.0f // top right
        )

        private var colors = floatArrayOf(
            1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f
        )

        var textureData = floatArrayOf(
            0f, 1f, 0.0f,   // bottom left
            1f, 1f, 0.0f,   // bottom right
            0f, 0f, 0.0f,   // top left
            1f, 0f, 0.0f    // top right
        )

        var secondVertices = floatArrayOf(
            -0.5f, -0.5f, 0.0f,  // bottom left
            0.5f, -0.5f, 0.0f,  // bottom right
            -0.5f, 0.5f, 0.0f,  // top left
            0.5f, 0.5f, 0.0f // top right
        )
        private val vertexCount = vertices.size / COORDS_PER_VERTEX // 9/3 = 3

        private const val vertexStride =
            COORDS_PER_VERTEX * BYTES_PER_FLOAT // 4 bytes per vertex // 6x4 = 24

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

    private var vertex2Buffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(secondVertices.size * BYTES_PER_FLOAT).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(secondVertices)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private var textureBuffer =
        ByteBuffer.allocateDirect(textureData.size * BYTES_PER_FLOAT).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(textureData)
                position(0)
            }
        }

    private var textureBuffer2 =
        ByteBuffer.allocateDirect(textureData.size * BYTES_PER_FLOAT).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(textureData)
                position(0)
            }
        }

    private var colorBuffer2 =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(colors.size * BYTES_PER_FLOAT).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(colors)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private var colorBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(colors.size * BYTES_PER_FLOAT).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(colors)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    override fun onDrawFrame(gl: GL10) {
        OpenGLUtils.validateProgram(programId)

        // Add program to OpenGL ES environment
        OpenGLUtils.useProgram(programId)


        textureId = OpenGLUtils.createTexture(context, R.drawable.wall)
        OpenGLUtils.getAttributeIndex(programId, "position") { index ->
            GLES20.glEnableVertexAttribArray(index)
            GLES20.glVertexAttribPointer(
                index,
                3,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )
        }
        OpenGLUtils.getAttributeIndex(programId, "texCoord") { index ->
            GLES20.glEnableVertexAttribArray(index)
            GLES20.glVertexAttribPointer(
                index,
                3,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                textureBuffer
            )
        }
        OpenGLUtils.getAttributeIndex(programId, "inputColor") { index ->
            GLES20.glEnableVertexAttribArray(index)
            GLES20.glVertexAttribPointer(
                index,
                3,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                colorBuffer
            )
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)
        OpenGLUtils.deleteProgram(programId)


        OpenGLUtils.validateProgram(programId2)

        // Add program to OpenGL ES environment
        OpenGLUtils.useProgram(programId2)
        textureId2 = OpenGLUtils.createTexture(context, R.mipmap.ic_launcher)
        OpenGLUtils.getAttributeIndex(programId2, "position") { index ->
            GLES20.glEnableVertexAttribArray(index)
            GLES20.glVertexAttribPointer(
                index,
                3,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertex2Buffer
            )
        }
        OpenGLUtils.getAttributeIndex(programId2, "texCoord") { index ->
            GLES20.glEnableVertexAttribArray(index)
            GLES20.glVertexAttribPointer(
                index,
                3,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                textureBuffer2
            )
        }
        OpenGLUtils.getAttributeIndex(programId2, "inputColor") { index ->
            GLES20.glEnableVertexAttribArray(index)
            GLES20.glVertexAttribPointer(
                index,
                3,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                colorBuffer2
            )
        }
        // Prepare the triangle color coordinate data
        /*
  */
        // Prepare the triangle coordinate data
        /* GLES20.glVertexAttribPointer(
             0,
             3,
             GLES20.GL_FLOAT,
             false,
             vertexStride,
             vertexBuffer
         )*/
        // Draw the triangle
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 0, vertexCount)
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)
        // Disable vertex array
        //   GLES20.glDisableVertexAttribArray(0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        vertexShaderId = OpenGLUtils.createVertexShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                R.raw.sample_texture_vertex_shader
            )
        )
        fragmentShaderId = OpenGLUtils.createFragmentShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                R.raw.sample_texture_fragment_shader
            )
        )

        vertexShaderId2 = OpenGLUtils.createVertexShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                R.raw.sample_texture_vertex_shader
            )
        )
        fragmentShaderId2 = OpenGLUtils.createFragmentShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                R.raw.sample_texture_fragment_shader
            )
        )

        // create empty OpenGL ES Program
        programId = OpenGLUtils.createProgram(vertexShaderId, fragmentShaderId)
        programId2 = OpenGLUtils.createProgram(vertexShaderId2, fragmentShaderId2)

        if (programId > 0) {

        }
    }

}