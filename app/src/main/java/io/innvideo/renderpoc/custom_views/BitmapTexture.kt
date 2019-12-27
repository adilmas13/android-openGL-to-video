package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import androidx.core.content.ContextCompat
import io.innvideo.renderpoc.R
import io.innvideo.renderpoc.gles.utils.ELUtils
import io.innvideo.renderpoc.gles.utils.GLSLTextReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class BitmapTexture(val context: Context) {
    private val vertexCount =
        vertexData.size / COORDS_PER_VERTEX
    //每一次取的总的点 大小
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    //位置
    private val vertexBuffer: FloatBuffer
    //纹理
    private val textureBuffer: FloatBuffer
    private var program = 0
    private var avPosition = 0
    //纹理位置
    private var afPosition = 0
    //纹理id
    private var textureId = 0

    fun onSurfaceCreated(): BitmapTexture {
        val vertexSource: String = GLSLTextReader.readGlslFromRawRes(context, R.raw.vertex_shader)
        val fragmentSource: String = GLSLTextReader.readGlslFromRawRes(context, R.raw.fragment_shader)
        program = createProgram(vertexSource, fragmentSource)
        if (program > 0) { //获取顶点坐标字段
            avPosition = GLES20.glGetAttribLocation(program, "av_Position")
            //获取纹理坐标字段
            afPosition = GLES20.glGetAttribLocation(program, "af_Position")
            val textureIds = IntArray(1)
            //创建纹理
            GLES20.glGenTextures(1, textureIds, 0)
            if (textureIds[0] == 0) {
                return this
            }
            textureId = textureIds[0]
            //绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
            //过滤（纹理像素映射到坐标点）  （缩小、放大：GL_LINEAR线性）
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            val bitmap = ELUtils.drawableToBitmap(ContextCompat.getDrawable(context, R.mipmap.ic_launcher)!!)
            //设置纹理为2d图片
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }
        return this
    }

    fun draw() { //使用程序
        GLES20.glUseProgram(program)
        GLES20.glEnableVertexAttribArray(avPosition)
        GLES20.glEnableVertexAttribArray(afPosition)
        //设置顶点位置值
        GLES20.glVertexAttribPointer(
            avPosition,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        //设置纹理位置值
        GLES20.glVertexAttribPointer(
            afPosition,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            textureBuffer
        )
        //绘制 GLES20.GL_TRIANGLE_STRIP:复用坐标
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)
        GLES20.glDisableVertexAttribArray(avPosition)
        GLES20.glDisableVertexAttribArray(afPosition)
    }

    companion object {
        //顶点坐标
        var vertexData = floatArrayOf( // in counterclockwise order:
            0.0f, 0.0f, 0.0f,  // bottom left
            1f, 0.0f, 0.0f,  // bottom right
            0.0f, 1.0f, 0.0f,  // top left
            1.0f, 1.0f, 0.0f // top right
        )
        //纹理坐标  对应顶点坐标  与之映射
        var textureData = floatArrayOf( // in counterclockwise order:
            0f, 1f, 0.0f,  // bottom left
            1f, 1f, 0.0f,  // bottom right
            0f, 0f, 0.0f,  // top left
            1f, 0f, 0.0f
        )
        //每一次取点的时候取几个点
        const val COORDS_PER_VERTEX = 3
    }

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        vertexBuffer.position(0)
        textureBuffer = ByteBuffer.allocateDirect(textureData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureData)
        textureBuffer.position(0)
    }

    fun loadShader(
        shaderType: Int,
        source: String?
    ): Int {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        var shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) { //添加代码到shader
            GLES20.glShaderSource(shader, source)
            //编译shader
            GLES20.glCompileShader(shader)
            val compile = IntArray(1)
            //检测是否编译成功
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compile, 0)
            if (compile[0] != GLES20.GL_TRUE) {
                Log.d("LOG_IT", "shader compile error")
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    fun createProgram(
        vertexSource: String?,
        fragmentSource: String?
    ): Int { //获取vertex shader
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        //获取fragment shader
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == 0) {
            return 0
        }
        //创建一个空的渲染程序
        var program = GLES20.glCreateProgram()
        if (program != 0) { //添加vertexShader到渲染程序
            GLES20.glAttachShader(program, vertexShader)
            //添加fragmentShader到渲染程序
            GLES20.glAttachShader(program, fragmentShader)
            //关联为可执行渲染程序
            GLES20.glLinkProgram(program)
            val linsStatus = IntArray(1)
            //检测是否关联成功
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linsStatus, 0)
            if (linsStatus[0] != GLES20.GL_TRUE) {
                Log.d("LOG_IT", "link program error")
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }
}