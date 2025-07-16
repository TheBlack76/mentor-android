package com.mentor.application.views.comman.utils

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan


/**
 * Created by Mukesh on 15/10/18.
 */
class CustomTypefaceSpan(val font: Typeface?) : MetricAffectingSpan() {
    override fun updateMeasureState(textPaint: TextPaint) = update(textPaint)
    override fun updateDrawState(textPaint: TextPaint?) = update(textPaint)

    private fun update(textPaint: TextPaint?) {
        textPaint.apply {
            val old = this!!.typeface
            val oldStyle = old?.style ?: 0
            val font = Typeface.create(font, oldStyle)
            typeface = font
        }
    }
}