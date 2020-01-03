package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.gles.utils.ELUtils
import io.innvideo.renderpoc.gles.utils.GLSLTextReader
import io.innvideo.renderpoc.gles.utils.MyLogger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/*
* Triangle
* */
class Render7(val context: Context) : GLSurfaceView.Renderer {

    private var programId = 0
    private var fragmentShaderId = 0
    private var vertexShaderId = 0

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    // Use to access and set the view transformation
    private var vPMatrixHandle: Int = 0

    companion object {
        private const val COORDS_PER_VERTEX = 3
        private const val BYTES_PER_FLOAT =
            4           // bytes per float is universal, can never change :P
        private var vertices = floatArrayOf(
            // positions
            0.5f, -0.5f, 0.0f,                        // bottom right
            -0.5f, -0.5f, 0.0f,                    // bottom left
            0.0f, 0.5f, 0.0f                          // top
        )

        private var colors = floatArrayOf(
            1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f
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
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)


        ELUtils.validateProgram(programId)
        // Add program to OpenGL ES environment
        ELUtils.useProgram(programId)

        ELUtils.getAttributeIndex(programId, "position") { index ->
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
        ELUtils.getAttributeIndex(programId, "inputColor") { index ->
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
        // get handle to shape's transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, vPMatrix, 0)

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(0)
//        GLES20.glDisableVertexAttribArray(1)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        MyLogger.logIt("onSurfaceChanged => width $width = height $height")
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        GLES20.glClearColor(1.0f, 0f, 0f, 1f)
        vertexShaderId = ELUtils.createVertexShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                R.raw.sample_vertex_shader
            )
        )
        fragmentShaderId = ELUtils.createFragmentShader(
            GLSLTextReader.readGlslFromRawRes(
                context,
                R.raw.sample_fragment_shader
            )
        )

        // create empty OpenGL ES Program
        programId = ELUtils.createProgram(vertexShaderId, fragmentShaderId)

        if (programId > 0) {
            // delete the shaders since link is formed
            ELUtils.deleteShader(vertexShaderId)
            ELUtils.deleteShader(fragmentShaderId)
        }
    }

}