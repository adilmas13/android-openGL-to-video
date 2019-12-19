package io.innvideo.renderpoc

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLUtils
import android.os.Bundle
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import io.innvideo.renderpoc.utils.logIt
import io.innvideo.renderpoc.utils.onSurfaceTextureAvailable
import io.innvideo.renderpoc.utils.toastIt
import kotlinx.android.synthetic.main.activity_hope_it_combines.*
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface

class HopeItCombinesActivity : AppCompatActivity() {

    private lateinit var egl: EGL10
    private lateinit var eglDisplay: EGLDisplay
    private lateinit var eglContext: EGLContext
    private lateinit var eglConfig: EGLConfig
    private lateinit var eglSurface: EGLSurface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hope_it_combines)
        init()
    }

    private fun init() {
        textureView.onSurfaceTextureAvailable { surfaceTexture, _, _ ->
            surfaceTexture.setOnFrameAvailableListener { logIt("FRAME AVAILABLE") }
            bhaiChal(surfaceTexture)
        }
    }

    private fun bhaiChal(surfaceTexture: SurfaceTexture) {
        // STEP 1: INITIALIZE GL
        initializeGL()
        // STEP 2 : Bind Surface with OpenGL
        bindSurfaceTextureToGL(surfaceTexture)
    }

    /*  1. Create a window surface by binding the textureView with GL
        2. Activate the new surface for drawing
    * reference - https://www.androidcookbook.info/opengl-3d/associating-a-drawing-surface-with-opengl-es-through-the-egl-context.html
    * */
    private fun bindSurfaceTextureToGL(surfaceTexture: SurfaceTexture) {
        // Step 1 : Bind GL with the surface texture to enable drawing
        eglSurface =
            egl.eglCreateWindowSurface(eglDisplay, eglConfig, Surface(surfaceTexture), null)
        // Step 2 : To activate drawing
        val boolean = egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
        if (boolean) {
            toastIt("Activated to draw")
        } else {
            toastIt("Something went wrong")
        }
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
        // Step 2: get instance of the egl object
        eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        // Step 3 : get initialize the display
        egl.eglInitialize(eglDisplay, IntArray(2))
        // Step 4 : Specify the config
        eglConfig = chooseEglConfig()!!
        // Step 5 : Create the context
        eglContext = egl.eglCreateContext(
            eglDisplay,
            eglConfig,
            EGL10.EGL_NO_CONTEXT,
            intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
        )
    }


    private fun release() {
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
            EGL10.EGL_RENDERABLE_TYPE,
            EGL14.EGL_OPENGL_ES2_BIT,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_DEPTH_SIZE, 0,
            EGL10.EGL_STENCIL_SIZE, 0,
            EGL10.EGL_NONE
        )
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
    }
}
