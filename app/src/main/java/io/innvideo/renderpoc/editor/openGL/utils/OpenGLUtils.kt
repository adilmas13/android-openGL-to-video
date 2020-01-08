package io.innvideo.renderpoc.editor.openGL.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.opengl.EGL14
import android.opengl.GLES20.GL_COMPILE_STATUS
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_LINEAR
import android.opengl.GLES20.GL_LINK_STATUS
import android.opengl.GLES20.GL_REPEAT
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.GL_TEXTURE_WRAP_S
import android.opengl.GLES20.GL_TEXTURE_WRAP_T
import android.opengl.GLES20.GL_VALIDATE_STATUS
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glDeleteProgram
import android.opengl.GLES20.glDeleteShader
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetProgramInfoLog
import android.opengl.GLES20.glGetProgramiv
import android.opengl.GLES20.glGetShaderInfoLog
import android.opengl.GLES20.glGetShaderiv
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glTexParameteri
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glValidateProgram
import android.opengl.GLUtils
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

class OpenGLUtils {

    companion object {
        fun createVertexShader(shaderCode: String) =
            compileShader(
                GL_VERTEX_SHADER,
                shaderCode
            )

        fun createFragmentShader(shaderCode: String) =
            compileShader(
                GL_FRAGMENT_SHADER,
                shaderCode
            )

        private fun compileShader(type: Int, shaderCode: String): Int {
            val shaderName =
                getShaderNameUsingType(
                    type
                )
            OpenGLLogger.logIt("====== $shaderName STARTED ======")
            OpenGLLogger.logIt("CREATING SHADER OBJECT =>")
            val shaderObjectId = glCreateShader(type) // GLES to create a new shader object
            if (shaderObjectId == 0) {
                OpenGLLogger.logIt("UNABLE TO CREATE SHADER OBJECT")
            }
            // getting shader source : LINKING received shader object with shader code
            OpenGLLogger.logIt("GETTING SHADER SOURCE")
            glShaderSource(shaderObjectId, shaderCode)          // GLES
            // compiling shader
            OpenGLLogger.logIt("COMPILING SHADER")
            glCompileShader(shaderObjectId)                     // GLES
            // checking compile status
            OpenGLLogger.logIt("GETTING COMPILATION STATUS")
            val compileStatus = IntArray(1)
            glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0)  // GLES
            val info = glGetShaderInfoLog(shaderObjectId)
            OpenGLLogger.logIt("SHADER COMPILED INFO => $info")
            if (compileStatus[0] == 0) {
                OpenGLLogger.logIt("SHADER COMPILATION FAILED")
                OpenGLLogger.logIt("DELETING FAILED SHADER OBJECT")
                glDeleteShader(shaderObjectId)
                return 0
            } else {
                OpenGLLogger.logIt("SHADER COMPILATION SUCCESS")
            }
            OpenGLLogger.logIt("====== $shaderName ENDED ======")
            return shaderObjectId
        }

        private fun getShaderNameUsingType(type: Int) =
            when (type) {
                GL_VERTEX_SHADER -> "GL_VERTEX_SHADER"
                else -> "GL_FRAGMENT_SHADER"
            }

        fun deleteShader(shaderId: Int) {
            glDeleteShader(shaderId)
        }

        fun createProgram(vararg shaders: Int): Int {
            OpenGLLogger.logIt("====== Started Program creation ======")
            val programObjectId = glCreateProgram()
            if (programObjectId == 0) {
                OpenGLLogger.logIt("Could not create program")
            }

            shaders.forEach {
                OpenGLLogger.logIt("Attaching shader")
                glAttachShader(programObjectId, it)
            }
            OpenGLLogger.logIt("JOINING shaders to program")
            glLinkProgram(programObjectId)

            OpenGLLogger.logIt("checking program linking status")
            val linkStatus = IntArray(1)
            glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0)
            val info = glGetProgramInfoLog(programObjectId)
            OpenGLLogger.logIt("Program linking status => $info")
            if (linkStatus[0] == 0) {
                OpenGLLogger.logIt("Program linking failed")
                OpenGLLogger.logIt("Deleting Failed program")
                glDeleteProgram(programObjectId)
            }
            return programObjectId
        }

        fun validateProgram(programObjectId: Int): Boolean {
            OpenGLLogger.logIt("===== Validating program ======")
            glValidateProgram(programObjectId)
            val validateStatus = IntArray(1)
            glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0)
            val info = glGetProgramInfoLog(programObjectId)
            OpenGLLogger.logIt("Validation Status => $info")
            return if (validateStatus[0] == 0) {
                OpenGLLogger.logIt("Program is not Valid")
                false
            } else {
                OpenGLLogger.logIt("Program is Valid")
                true
            }
        }

        fun useProgram(programObjectId: Int) {
            OpenGLLogger.logIt("===== Using program ======")
            glUseProgram(programObjectId)
        }

        fun deleteProgram(programObjectId: Int) {
            OpenGLLogger.logIt("===== Deleting program ======")
            glDeleteProgram(programObjectId)
        }

        fun createTexture(context: Context, @DrawableRes resourceId: Int): Int {
            OpenGLLogger.logIt("====== Started texture creation ======")
            val textureObjectIds = IntArray(1)
            glGenTextures(
                1,
                textureObjectIds,
                0
            ) // we pass one since we want to create only one texture
            if (textureObjectIds[0] == 0) {
                OpenGLLogger.logIt("texture creation Failed")
            } else {
                OpenGLLogger.logIt("texture created successfully")
            }
            OpenGLLogger.logIt("binding texture")
            glBindTexture(GL_TEXTURE_2D, textureObjectIds[0])

            val bitmap =
                drawableToBitmap(
                    ContextCompat.getDrawable(context, resourceId)!!
                )
                    ?: throw RuntimeException("Bitmap is null")
            OpenGLLogger.logIt("Bitmap is not null")

            OpenGLLogger.logIt("Adding texture filtering")
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            OpenGLLogger.logIt("Adding Bitmap to openGL")
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
            /* MyLogger.logIt("Generating mipmap")
             glGenerateMipmap(GL_TEXTURE_2D)*/
            /*  MyLogger.logIt("Unbinding texture")
              glBindTexture(GL_TEXTURE_2D, 0)*/
            return textureObjectIds[0]

        }

        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        private fun checkEglError(msg: String) {
            var error: Int
            if (EGL14.eglGetError().also { error = it } != EGL14.EGL_SUCCESS) {
                throw RuntimeException(
                    "$msg: EGL error: 0x" + Integer.toHexString(
                        error
                    )
                )
            }
        }

        fun getAttributeIndex(programId: Int, attribute: String, callback: (Int) -> Unit) {
            val index = glGetAttribLocation(programId, attribute)
            if (index > -1) {
                callback(index)
            } else {
                OpenGLLogger.logIt("Attribute not found => $attribute")
            }
        }
    }
}