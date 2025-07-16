package com.mentor.application.views.comman.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mentor.application.R

class GradientDotLineView : View {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f

        val path = Path()
        path.moveTo(0f, height / 2f)
        path.lineTo(width.toFloat(), height / 2f)

        val intervals = floatArrayOf(33f, 12f)
        val phase = 0f

        val dashPathEffect = DashPathEffect(intervals, phase)
        paint.pathEffect = dashPathEffect

        val shader: Shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(
                ContextCompat.getColor(context, R.color.colorWhiteTransparent_10),
                ContextCompat.getColor(context, R.color.colorWhite),
                ContextCompat.getColor(context, R.color.colorWhiteTransparent_10)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        canvas.drawPath(path, paint)
    }
}