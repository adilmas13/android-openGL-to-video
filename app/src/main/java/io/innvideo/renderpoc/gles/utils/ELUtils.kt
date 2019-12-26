package io.innvideo.renderpoc.gles.utils

import android.opengl.GLES20.GL_COMPILE_STATUS
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_LINK_STATUS
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glDeleteProgram
import android.opengl.GLES20.glDeleteShader
import android.opengl.GLES20.glGetProgramInfoLog
import android.opengl.GLES20.glGetProgramiv
import android.opengl.GLES20.glGetShaderInfoLog
import android.opengl.GLES20.glGetShaderiv
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glShaderSource

class ELUtils {

    companion object {
        fun compileVertexShader(shaderCode: String) =
            compileShader(GL_VERTEX_SHADER, shaderCode)

        fun compileFragmentShader(shaderCode: String) =
            compileShader(GL_FRAGMENT_SHADER, shaderCode)

        private fun compileShader(type: Int, shaderCode: String): Int {
            val shaderName = getShaderNameUsingType(type)
            MyLogger.logIt("====== $shaderName STARTED ======")
            MyLogger.logIt("CREATING SHADER OBJECT =>")
            val shaderObjectId = glCreateShader(type) // GLES to create a new shader object
            if (shaderObjectId == 0) {
                MyLogger.logIt("UNABLE TO CREATE SHADER OBJECT")
            }
            // getting shader source : LINKING received shader object with shader code
            MyLogger.logIt("GETTING SHADER SOURCE")
            glShaderSource(shaderObjectId, shaderCode)          // GLES
            // compiling shader
            MyLogger.logIt("COMPILING SHADER")
            glCompileShader(shaderObjectId)                     // GLES
            // checking compile status
            MyLogger.logIt("GETTING COMPILATION STATUS")
            val compileStatus = IntArray(1)
            glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0)  // GLES
            val info = glGetShaderInfoLog(shaderObjectId)
            MyLogger.logIt("SHADER COMPILED INFO => $info")
            if (compileStatus[0] == 0) {
                MyLogger.logIt("SHADER COMPILATION FAILED")
                MyLogger.logIt("DELETING FAILED SHADER OBJECT")
                glDeleteShader(shaderObjectId)
                return 0
            } else {
                MyLogger.logIt("SHADER COMPILATION SUCCESS")
            }
            MyLogger.logIt("====== $shaderName ENDED ======")
            return shaderObjectId
        }

        private fun getShaderNameUsingType(type: Int) =
            when (type) {
                GL_VERTEX_SHADER -> "GL_VERTEX_SHADER"
                else -> "GL_FRAGMENT_SHADER"
            }


         fun createProgram(vararg shaders: Int): Int {
            MyLogger.logIt("====== Started Program creation ======")
            val programObjectId = glCreateProgram()
            if (programObjectId == 0) {
                MyLogger.logIt("Could not create program")
            }

            shaders.forEach {
                MyLogger.logIt("Attaching shader")
                glAttachShader(programObjectId, it)
            }
            MyLogger.logIt("JOINING shaders to program")
            glLinkProgram(programObjectId)

            MyLogger.logIt("checking program linking status")
            val linkStatus = IntArray(1)
            glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0)
            val info = glGetProgramInfoLog(programObjectId)
            MyLogger.logIt("Program linking status => $info")
            if (linkStatus[0] == 0) {
                MyLogger.logIt("Program linking failed")
                MyLogger.logIt("Deleting Failed program")
                glDeleteProgram(programObjectId)
            }
            return programObjectId
        }
    }
}