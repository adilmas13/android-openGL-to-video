package io.innvideo.renderpoc

import android.opengl.EGL14
import android.opengl.GLUtils
import android.view.Surface
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface

class EglCore(private val surface: Any) {
    private lateinit var egl: EGL10
    private lateinit var eglDisplay: EGLDisplay
    private lateinit var eglContext: EGLContext
    private lateinit var eglConfig: EGLConfig
    private lateinit var eglSurface: EGLSurface

    fun init() {
        // 1: initialise GL
        this.initializeGL()
        // 2: bind Surface to GL
        this.bindSurfaceTextureToGL()
    }

    /*  1. Create a window surface by binding the textureView with GL
        2. Activate the new surface for drawing
    *   reference - https://www.androidcookbook.info/opengl-3d/associating-a-drawing-surface-with-opengl-es-through-the-egl-context.html
    * */
    private fun bindSurfaceTextureToGL() {
        // Step 1 : Bind GL with the surface texture to enable drawing
        eglSurface =
            egl.eglCreateWindowSurface(eglDisplay, eglConfig, this.surface, null)
        // Step 2 : To activate drawing
        val activated = egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
        EglUtil.logIt("=== ACTIVE STATUE ===> ${activated}")
    }

    /*
    *   1. Get an implementation of EGL10.
        2. Get a display to use.
        3. Initialize the display.
        4. Specify a device-specific configuration to EGL.
        5. Use an initialized display and a configuration to get an EGL context.
        * Reference - https://www.androidcookbook.info/opengl-3d/getting-an-egl-context.html
        * */
    private fun initializeGL() {
        // Step 1: get Egl Object
        egl = EGLContext.getEGL() as EGL10
        // Step 2: get instance of the egl display object
        eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        // Step 3 : get initialize the display
        val minor = IntArray(2)
        egl.eglInitialize(eglDisplay, minor)
        // Step 4 : Specify the config
        eglConfig = chooseEglConfig()!!
        // Step 5 : Create the context
        val contextAttributes = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL10.EGL_NONE
        )
        eglContext = egl.eglCreateContext(
            eglDisplay,
            eglConfig,
            EGL10.EGL_NO_CONTEXT,
            contextAttributes
        )
        EglUtil.logIt("=== Initialised ===")
    }

    fun release() {
        EglUtil.logIt("=== RELEASING ===>")
        egl.eglMakeCurrent(
            eglDisplay,
            EGL10.EGL_NO_SURFACE,
            EGL10.EGL_NO_SURFACE,
            EGL10.EGL_NO_CONTEXT
        )
        egl.eglDestroyContext(eglDisplay, eglContext)
        egl.eglTerminate(eglDisplay)
        eglDisplay = EGL10.EGL_NO_DISPLAY
        eglContext = EGL10.EGL_NO_CONTEXT
    }

    private fun chooseEglConfig(): EGLConfig? {
        val configsCount = IntArray(1)
        val configs: Array<EGLConfig?> = arrayOfNulls(1)
        val configSpec = getConfig()
        require(egl.eglChooseConfig(eglDisplay, configSpec, configs, 1, configsCount)) {
            "Failed to choose config: " + GLUtils.getEGLErrorString(
                egl.eglGetError()
            )
        }
        return if (configsCount[0] > 0) {
            configs[0]
        } else
            null
    }

    private fun getConfig(): IntArray {
        return intArrayOf(
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,      //前台渲染
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
            EGL10.EGL_NONE
        )
    }

    fun swapBuffer() {
        egl.eglSwapBuffers(eglDisplay, eglSurface)
    }
}