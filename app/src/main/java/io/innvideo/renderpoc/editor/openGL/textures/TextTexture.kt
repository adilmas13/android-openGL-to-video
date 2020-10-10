package io.innvideo.renderpoc.editor.openGL.textures

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import io.innvideo.renderpoc.editor.newModels.parsed_models.LayerData


class TextTexture(
    val context: Context,
    private val textData: LayerData.Text
) : BaseTexture(context, textData.coordinates) {

    override fun getBitmap() = textBitmap

    private var textBitmap: Bitmap

    init {
        val fontSize = 50
        val textPaint = Paint().apply {
            textSize = fontSize.toFloat()
            isFakeBoldText = false
            isAntiAlias = true
            setARGB(255, 255, 255, 255)
            isSubpixelText = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
            textData.fontFamily?.let { typeface -> setTypeface(typeface) }
        }
        val realTextWidth = textPaint.measureText(textData.text)
        val bitmapWidth = (realTextWidth + 2f).toInt()
        val bitmapHeight = fontSize + 30
        textBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        textBitmap.eraseColor(Color.argb(0, 255, 255, 255))
        val bitmapCanvas = Canvas(textBitmap)
        bitmapCanvas.drawText(textData.text, 1f, 1.0f + fontSize * 0.75f, textPaint)
        /*  val canvas = Canvas(bitmap)

          val textPaint = TextPaint()
          textPaint.isAntiAlias = true
          textPaint.bgColor = 255
          textPaint.textSize = 20f
          textPaint.setARGB(0xff, 0x00, 0x00, 0x00)
          canvas.drawText(text, 0f, 20f, textPaint)*/
    }
}