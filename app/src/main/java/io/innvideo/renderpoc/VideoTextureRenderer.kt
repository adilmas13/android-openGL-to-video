package io.innvideo.renderpoc

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import io.innvideo.renderpoc.utils.logIt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


class VideoTextureRenderer(
    private var context: Context,
    private var texture: SurfaceTexture,
    private var myWidth: Int,
    private var myHeight: Int
) : TextureSurfaceRenderer(texture, myWidth, myHeight), SurfaceTexture.OnFrameAvailableListener {

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "attribute vec4 vTexCoordinate;" +
            "uniform mat4 textureTransform;" +
            "varying vec2 v_TexCoordinate;" +
            "void main() {" +
            "   v_TexCoordinate = (textureTransform * vTexCoordinate).xy;" +
            "   gl_Position = vPosition;" +
            "}"

    private val fragmentShaderCode =
        "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "uniform samplerExternalOES texture;" +
                "varying vec2 v_TexCoordinate;" +
                "void main () {" +
                "    vec4 color = texture2D(texture, v_TexCoordinate);" +
                "    gl_FragColor = color;" +
                "}"


    private val squareSize = 1.0f
    private val squareCoords = floatArrayOf(
        -squareSize, squareSize, 0.0f,  // top left
        -squareSize, -squareSize, 0.0f,  // bottom left
        squareSize, -squareSize, 0.0f,  // bottom right
        squareSize, squareSize, 0.0f
    ) // top right


    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)

    // Texture to be shown in background
    private var textureBuffer: FloatBuffer? = null
    private val textureCoords = floatArrayOf(
        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 1.0f, 0.0f, 1.0f
    )
    private val textures = IntArray(1)

    private var vertexShaderHandle = 0
    private var fragmentShaderHandle = 0
    private var shaderProgram = 0
    private var vertexBuffer: FloatBuffer? = null
    private var drawListBuffer: ShortBuffer? = null

    private val videoTextureTransform = FloatArray(16)
    private var frameAvailable = false

    private var videoWidth = 0
    private var videoHeight = 0
    private var adjustViewport = false

    private fun loadShaders() {
        vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode)
        GLES20.glCompileShader(vertexShaderHandle)
        checkGlError("Vertex shader compile")
        fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCode)
        GLES20.glCompileShader(fragmentShaderHandle)
        checkGlError("Pixel shader compile")
        shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShaderHandle)
        GLES20.glAttachShader(shaderProgram, fragmentShaderHandle)
        GLES20.glLinkProgram(shaderProgram)
        checkGlError("Shader program compile")
        val status = IntArray(1)
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] != GLES20.GL_TRUE) {
            val error = GLES20.glGetProgramInfoLog(shaderProgram)
            logIt("Error while linking program:\n$error")
        }
    }

    private fun setupVertexBuffer() { // Draw list buffer
        val dlb: ByteBuffer = ByteBuffer.allocateDirect(drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer?.put(drawOrder)
        drawListBuffer?.position(0)
        // Initialize the texture holder
        val bb: ByteBuffer = ByteBuffer.allocateDirect(squareCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer?.put(squareCoords)
        vertexBuffer?.position(0)
    }

    private fun setupTexture(context: Context) {
        val texturebb: ByteBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4)
        texturebb.order(ByteOrder.nativeOrder())
        textureBuffer = texturebb.asFloatBuffer()
        textureBuffer?.put(textureCoords)
        textureBuffer?.position(0)
        // Generate the actual texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glGenTextures(1, textures, 0)
        checkGlError("Texture generate")
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
        checkGlError("Texture bind")
        texture = SurfaceTexture(textures[0])
        texture.setOnFrameAvailableListener(this)
    }

    override fun initGLComponents() {
        setupVertexBuffer()
        setupTexture(context)
        loadShaders()
    }

    override fun deInitGLComponents() {
        GLES20.glDeleteTextures(1, textures, 0)
        GLES20.glDeleteProgram(shaderProgram)
        texture.release()
        texture.setOnFrameAvailableListener(null)
    }

    override fun draw(): Boolean {
        synchronized(this) {
            frameAvailable = if (frameAvailable) {
                texture.updateTexImage()
                texture.getTransformMatrix(videoTextureTransform)
                false
            } else {
                return false
            }
        }

        if (adjustViewport) {
            adjustViewport()
        }

        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Draw texture
        // Draw texture
        GLES20.glUseProgram(shaderProgram)
        val textureParamHandle = GLES20.glGetUniformLocation(shaderProgram, "texture")
        val textureCoordinateHandle =
            GLES20.glGetAttribLocation(shaderProgram, "vTexCoordinate")
        val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        val textureTranformHandle =
            GLES20.glGetUniformLocation(shaderProgram, "textureTransform")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, vertexBuffer)

        GLES20.glBindTexture(GLES20.GL_TEXTURE0, textures[0])
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glUniform1i(textureParamHandle, 0)

        GLES20.glEnableVertexAttribArray(textureCoordinateHandle)
        GLES20.glVertexAttribPointer(
            textureCoordinateHandle,
            4,
            GLES20.GL_FLOAT,
            false,
            0,
            textureBuffer
        )

        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, videoTextureTransform, 0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            drawListBuffer
        )
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle)

        return true
    }

    private fun adjustViewport() {
        val surfaceAspect = myHeight / myWidth.toFloat()
        val videoAspect = videoHeight / videoWidth.toFloat()
        if (surfaceAspect > videoAspect) {
            val heightRatio = myHeight / videoHeight.toFloat()
            val newWidth = (myWidth * heightRatio).toInt()
            val xOffset = (newWidth - myWidth) / 2
            GLES20.glViewport(-xOffset, 0, newWidth, myHeight)
        } else {
            val widthRatio = myWidth / videoWidth.toFloat()
            val newHeight = (myHeight * widthRatio).toInt()
            val yOffset = (newHeight - myHeight) / 2
            GLES20.glViewport(0, -yOffset, myWidth, newHeight)
        }
        adjustViewport = false
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(this)
        {
            frameAvailable = true
        }
    }

    fun getVideoTexture(): SurfaceTexture? {
        return texture
    }

    fun setVideoSize(width: Int, height: Int) {
        videoWidth = width
        videoHeight = height
        adjustViewport = true
    }

    private fun checkGlError(op: String) {
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            logIt(op + ": glError " + GLUtils.getEGLErrorString(error))
        }
    }
}