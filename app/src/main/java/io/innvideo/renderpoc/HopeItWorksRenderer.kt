package io.innvideo.renderpoc

import android.graphics.SurfaceTexture
import android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION
import android.opengl.EGL14.EGL_OPENGL_ES2_BIT
import android.opengl.GLUtils
import io.innvideo.renderpoc.utils.logIt
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface


abstract class HopeItWorksRenderer(
    private val texture: SurfaceTexture,
    private var width: Int,
    private var height: Int
) : Runnable {

    protected abstract fun initGLComponents()
    protected abstract fun deinitGLComponents()
    protected abstract fun draw(): Boolean
    private var running = false
    private lateinit var egl: EGL10
    private lateinit var eglDisplay: EGLDisplay
    private lateinit var eglContext: EGLContext
    private lateinit var eglSurface: EGLSurface
    private var step = 0

    private var thread: Thread? = null

    fun letsRun() {
        running = true
        thread = Thread(this)
        thread?.start()
    }

    fun stopIt() {
        running = false
        thread?.interrupt()
    }

    override fun run() {
        logIt("${++step} Started running Thread")
        initGL()
        initGLComponents()
        while (running) {
            val loopStart = System.currentTimeMillis()
            if (draw()) {
                egl.eglSwapBuffers(eglDisplay, eglSurface)
            }
            val waitDelta =
                16 - (System.currentTimeMillis() - loopStart) // Targeting 60 fps, no need for faster
            if (waitDelta > 0) {
                try {
                    Thread.sleep(waitDelta)
                } catch (e: InterruptedException) {
                    continue
                }
            }
        }

        deinitGLComponents()
        deinitGL()
    }

    private fun deinitGL() {
        egl.eglMakeCurrent(
            eglDisplay,
            EGL10.EGL_NO_SURFACE,
            EGL10.EGL_NO_SURFACE,
            EGL10.EGL_NO_CONTEXT
        )
        egl.eglDestroySurface(eglDisplay, eglSurface)
        egl.eglDestroyContext(eglDisplay, eglContext)
        egl.eglTerminate(eglDisplay)
        logIt("${++step} deinitGL")
    }

    private fun initGL() {
        logIt("${++step} initGL")
        egl = EGLContext.getEGL() as EGL10
        eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        egl.eglInitialize(eglDisplay, version)
        val eglConfig = chooseEglConfig()
        eglContext = createContext(egl, eglDisplay, eglConfig!!)!!
        eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, texture, null)
        if (eglSurface == null || eglSurface === EGL10.EGL_NO_SURFACE) {
            throw RuntimeException("GL Error: " + GLUtils.getEGLErrorString(egl.eglGetError()))
        }
        if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw RuntimeException(
                "GL Make current error: " + GLUtils.getEGLErrorString(
                    egl.eglGetError()
                )
            )
        }
    }

    private fun createContext(
        egl: EGL10,
        eglDisplay: EGLDisplay,
        eglConfig: EGLConfig
    ): EGLContext? {
        val attribList = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
        return egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attribList)
    }

    private fun chooseEglConfig(): EGLConfig? {
        val configsCount = IntArray(1)
        val configs: Array<EGLConfig?> = arrayOfNulls<EGLConfig>(1)
        val configSpec = getConfig()
        require(egl.eglChooseConfig(eglDisplay, configSpec, configs, 1, configsCount)) {
            "Failed to choose config: " + GLUtils.getEGLErrorString(
                egl.eglGetError()
            )
        }
        return if (configsCount[0] > 0) {
            configs[0]
        } else null
    }

    private fun getConfig(): IntArray {
        return intArrayOf(
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_DEPTH_SIZE, 0,
            EGL10.EGL_STENCIL_SIZE, 0,
            EGL10.EGL_NONE
        )
    }

    fun onPause() {
        running = false
    }


}