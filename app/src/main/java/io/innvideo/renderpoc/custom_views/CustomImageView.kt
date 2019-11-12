package io.innvideo.renderpoc.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast


class CustomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ImageView(context) {

    private var dX: Float = 0.toFloat()
    private var dY: Float = 0.toFloat()
    private var lastAction: Int = 0

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dX = this.x - event.rawX
                dY = this.y - event.rawY
                lastAction = MotionEvent.ACTION_DOWN
            }

            MotionEvent.ACTION_MOVE -> {
                this.y = event.rawY + dY
                this.x = event.rawX + dX
                lastAction = MotionEvent.ACTION_MOVE
            }

            MotionEvent.ACTION_UP -> if (lastAction == MotionEvent.ACTION_DOWN)
                performClick()

            else -> return false
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        onImageViewClicked()
        return true
    }

    private fun onImageViewClicked() {
        Toast.makeText(context, "Clicked!", Toast.LENGTH_SHORT).show()
    }
}