package com.mentor.application.views.comman.utils.frescozoomabledraweeview.zoomable

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Matrix
import android.view.animation.DecelerateInterpolator
import com.facebook.common.internal.Preconditions
import com.facebook.common.logging.FLog
import com.mentor.application.views.comman.utils.frescozoomabledraweeview.gestures.TransformGestureDetector

/**
 * ZoomableController that adds animation capabilities to DefaultZoomableController using standard
 * Android animation classes
 */
class AnimatedZoomableController @SuppressLint("NewApi")
constructor(transformGestureDetector: TransformGestureDetector) :
        AbstractAnimatedZoomableController(transformGestureDetector) {

    private val mValueAnimator: ValueAnimator = ValueAnimator.ofFloat(0F, 1F)

    init {
        mValueAnimator.interpolator = DecelerateInterpolator() as TimeInterpolator?
    }

    @SuppressLint("NewApi")
    override fun setTransformAnimated(
            newTransform: Matrix,
            durationMs: Long,
            onAnimationComplete: Runnable?) {
        FLog.v(logTag, "setTransformAnimated: duration %d ms", durationMs)
        stopAnimation()
        Preconditions.checkArgument(durationMs > 0)
        Preconditions.checkState(!isAnimating)
        isAnimating = true
        mValueAnimator.duration = durationMs
        transform.getValues(startValues)
        newTransform.getValues(stopValues)
        mValueAnimator.addUpdateListener { valueAnimator ->
            calculateInterpolation(workingTransform, valueAnimator.animatedValue as Float)
            super@AnimatedZoomableController.transform = workingTransform
        }
        mValueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                FLog.v(logTag, "setTransformAnimated: animation cancelled")
                onAnimationStopped()
            }

            override fun onAnimationEnd(animation: Animator) {
                FLog.v(logTag, "setTransformAnimated: animation finished")
                onAnimationStopped()
            }

            private fun onAnimationStopped() {
                onAnimationComplete?.run()
                isAnimating = false
                detector.restartGesture()
            }
        })
        mValueAnimator.start()
    }

    @SuppressLint("NewApi")
    public override fun stopAnimation() {
        if (!isAnimating) {
            return
        }
        FLog.v(logTag, "stopAnimation")
        mValueAnimator.cancel()
        mValueAnimator.removeAllUpdateListeners()
        mValueAnimator.removeAllListeners()
    }

    override val logTag: Class<*>
        get() = TAG

    companion object {

        private val TAG = AnimatedZoomableController::class.java

        fun newInstance(): AnimatedZoomableController {
            return AnimatedZoomableController(TransformGestureDetector.newInstance())
        }
    }

}
