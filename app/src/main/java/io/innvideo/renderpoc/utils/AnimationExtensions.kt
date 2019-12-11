package io.innvideo.renderpoc.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.ImageView


fun AnimatorSet.animationCallbackVal(callback: () -> Unit): Animator.AnimatorListener {
    val animatorListener: Animator.AnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animator: Animator?) {
        }

        override fun onAnimationCancel(animator: Animator?) {
        }

        override fun onAnimationStart(animator: Animator?) {
        }

        override fun onAnimationEnd(animator: Animator?) {
            callback()
            animator?.removeAllListeners()
        }

    }
    this.addListener(animatorListener)
    return animatorListener
}

fun Animator.animationCallbackVal(callback: () -> Unit): Animator.AnimatorListener {
    val animatorListener: Animator.AnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animator: Animator?) {
        }

        override fun onAnimationCancel(animator: Animator?) {
        }

        override fun onAnimationStart(animator: Animator?) {
        }

        override fun onAnimationEnd(animator: Animator?) {
            callback()
            animator?.removeAllListeners()
        }

    }
    this.addListener(animatorListener)
    return animatorListener
}

fun View.getScaleDownToNormalAnimator(animDuration: Long): ObjectAnimator {
    val scaleAnimationX = PropertyValuesHolder.ofFloat(View.SCALE_X, 2f, 1f)
    val scaleAnimationY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 2f, 1f)
    return ObjectAnimator.ofPropertyValuesHolder(
        this, scaleAnimationX, scaleAnimationY
    ).setDuration(animDuration)
}

fun View.getShowWithAlphaAnimator(animDuration: Int): ObjectAnimator {
    val alphaAnimation = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, alphaAnimation)
    objectAnimator.interpolator = OvershootInterpolator(1f)
    objectAnimator.duration = animDuration.toLong()
    return objectAnimator
}

fun View.translateFromCurrentToTop(animDuration: Int, translateVal: Float): ObjectAnimator {
    val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 1f, -translateVal)
    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, translateY)
    objectAnimator.duration = animDuration.toLong()
    return objectAnimator
}

fun View.getSplashTextAnim(animDuration: Int, translateVal: Float): ObjectAnimator {
    val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, translateVal, 1f)
    val alphaAnimation = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, translateY, alphaAnimation)
    objectAnimator.duration = animDuration.toLong()
    return objectAnimator
}

fun View.getScaleWithAlphaAnimator(
    animDuration: Int,
    startValue: Float,
    endValue: Float,
    alphaFrom: Float,
    alphaTo: Float,
    isOvershoot: Boolean
): ObjectAnimator {
    val scaleAnimationX = PropertyValuesHolder.ofFloat(View.SCALE_X, startValue, endValue)
    val scaleAnimationY = PropertyValuesHolder.ofFloat(View.SCALE_Y, startValue, endValue)
    val alphaAnimation = PropertyValuesHolder.ofFloat(View.ALPHA, alphaFrom, alphaTo)
    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
        this, scaleAnimationX, scaleAnimationY, alphaAnimation
    ).setDuration(animDuration.toLong())
    if (isOvershoot) {
        objectAnimator.interpolator = OvershootInterpolator(2f)
    }
    return objectAnimator
}

fun View.getHomeFabAnimator(
    animDuration: Int
): ObjectAnimator {
    val scaleAnimationX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f)
    val scaleAnimationY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f)
    val alphaAnimation = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
    val rotateAnimation = PropertyValuesHolder.ofFloat(View.ROTATION, 0f, 360f)
    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
        this, scaleAnimationX, scaleAnimationY, alphaAnimation, rotateAnimation
    )
    objectAnimator.duration = animDuration.toLong()

    return objectAnimator
}

fun View.handleAlphaStartEndVal(
    view: View,
    animDuration: Int,
    startValue: Float,
    endValue: Float
): ObjectAnimator {
    val alphaAnimation = PropertyValuesHolder.ofFloat(View.ALPHA, startValue, endValue)
    //Animator set for scale animation
    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
        view, alphaAnimation
    ).setDuration(animDuration.toLong())
    objectAnimator.interpolator = OvershootInterpolator(1f)
    return objectAnimator
}

fun View.getYTranslationBasedOnStartEndVal(
    startValue: Float,
    endValue: Float,
    animDuration: Int,
    alphaFrom: Float,
    alphaTo: Float
): ObjectAnimator {
    val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, startValue, endValue)
    //Handle alpha for text while animating top to bottom
    val alphaAnimation = PropertyValuesHolder.ofFloat(View.ALPHA, alphaFrom, alphaTo)
    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, translateY, alphaAnimation)
    objectAnimator.duration = animDuration.toLong()
    return objectAnimator
}

fun View.getXTranslationBasedOnStartEndVal(
    startVal: Float,
    endVal: Float,
    animDuration: Int,
    alphaFrom: Float,
    alphaTo: Float
): ObjectAnimator {
    val translateX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, startVal, endVal)
    //Handle alpha for text while animating top to bottom
    val alphaAnimation = PropertyValuesHolder.ofFloat(View.ALPHA, alphaFrom, alphaTo)
    //Animator set
    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, translateX, alphaAnimation)
    objectAnimator.duration = animDuration.toLong()
    objectAnimator.interpolator = OvershootInterpolator(1f)
    return objectAnimator
}

fun View.reloadRotateAnimation(): ObjectAnimator {
    val rotationAnim = PropertyValuesHolder.ofFloat(View.ROTATION, 0f, -360f)
    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, rotationAnim)
    objectAnimator.repeatMode = ObjectAnimator.REVERSE
    objectAnimator.duration = 1000
    return objectAnimator
}

fun View.fetchOvershootFromNormal(animDuration: Long): ObjectAnimator {
    val scaleAnimationX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f)
    val scaleAnimationY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f)
    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
        this, scaleAnimationX, scaleAnimationY
    ).setDuration(animDuration)
    objectAnimator.interpolator = OvershootInterpolator(2f)
    objectAnimator.repeatMode = ObjectAnimator.REVERSE
    return objectAnimator
}

fun View.getXTranslationBasedOnStartEndValWithScaleFromZero(
    startVal: Float,
    endVal: Float,
    animDuration: Int,
    alphaFrom: Float,
    alphaTo: Float
): ObjectAnimator {
    val translateX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, startVal, endVal)
    //Handle alpha for text while animating top to bottom
    val alphaAnimation = PropertyValuesHolder.ofFloat(View.ALPHA, alphaFrom, alphaTo)
    //Animator set
    val scaleAnimationX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1.0f)
    val scaleAnimationY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1.0f)

    val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
        this,
        translateX,
        scaleAnimationX,
        scaleAnimationY,
        alphaAnimation
    )
    objectAnimator.duration = animDuration.toLong()
    return objectAnimator
}

fun EditText.setEditTextLineAnimation(line: View) {
    setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            line.animate().translationX(line.width.toFloat())
                .setDuration(300L)
                .start()
        } else {
            line.animate().translationX(0f).setDuration(300L).start()
        }
    }
}

fun EditText.setPasswordToggleAnimation(eyeIcon: ImageView, slash: ImageView) {
    var isPasswordMode = true
    eyeIcon.setOnClickListener {
        if (isPasswordMode) {
            isPasswordMode = false
            slash.pivotX = 0f
            slash.pivotY = slash.height.toFloat()
            slash.animate().scaleY(0f).scaleX(0f).setDuration(300)
                .setInterpolator(AccelerateInterpolator()).start()
            this.transformationMethod = SingleLineTransformationMethod()
        } else {
            isPasswordMode = true
            slash.pivotX = slash.width.toFloat()
            slash.pivotY = 0f
            slash.animate().scaleY(1f).scaleX(1f).setDuration(300)
                .setInterpolator(AccelerateInterpolator()).start()
            this.transformationMethod = PasswordTransformationMethod()
        }
    }
}


