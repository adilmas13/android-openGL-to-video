package io.innvideo.renderpoc.editor.openGL.utils

import android.content.Context
import android.content.res.Resources
import androidx.annotation.RawRes
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class GLSLTextReader {
    companion object {
        fun readGlslFromRawRes(context: Context, @RawRes resourceId: Int): String {
            val body = StringBuilder()
            try {
                val inputStream = context.resources.openRawResource(resourceId)
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var nextLine: String?
                nextLine = bufferedReader.readLine()
                while (nextLine != null) {
                    body.append(nextLine)
                    body.append('\n')
                    nextLine = bufferedReader.readLine()
                }
                bufferedReader.close()
                inputStream.close()
                inputStreamReader.close()
            } catch (e: IOException) {
                throw RuntimeException("Could not open resource: $resourceId", e)
            } catch (nfe: Resources.NotFoundException) {
                throw RuntimeException("Resource not found: $resourceId", nfe)
            }
            OpenGLLogger.logIt("SHADER LOADED FROM RAW \n $body")
            return body.toString()
        }
    }
}