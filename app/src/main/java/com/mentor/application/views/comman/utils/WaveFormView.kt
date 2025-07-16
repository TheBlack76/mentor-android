package com.mentor.application.views.comman.utils

import android.os.Build
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View


  class WaveFormView : View {

    private var phase: Float = 0.toFloat()
    private var amplitude: Float = 0.toFloat()
    private var frequency: Float = 0.toFloat()
    private var idleAmplitude: Float = 0.toFloat()
    private var numberOfWaves: Float = 0.toFloat()
    private var phaseShift: Float = 0.toFloat()
    private var density: Float = 0.toFloat()
    private var primaryWaveLineWidth: Float = 0.toFloat()
    private var secondaryWaveLineWidth: Float = 0.toFloat()
    internal lateinit var mPaintColor: Paint
    internal lateinit var rect: Rect
    internal var isStraightLine = false

    constructor(context: Context) : super(context) {
        setUp()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setUp()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setUp()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        setUp()
    }

    private fun setUp() {
        this.frequency = defaultFrequency

        this.amplitude = defaultAmplitude
        this.idleAmplitude = defaultIdleAmplitude

        this.numberOfWaves = defaultNumberOfWaves
        this.phaseShift = defaultPhaseShift
        this.density = defaultDensity

        this.primaryWaveLineWidth = defaultPrimaryLineWidth
        this.secondaryWaveLineWidth = defaultSecondaryLineWidth
        mPaintColor = Paint()
        mPaintColor.setColor(Color.WHITE)
    }

  open  fun updateAmplitude(ampli: Float, isSpeaking: Boolean) {
        this.amplitude = Math.max(ampli, idleAmplitude)
        isStraightLine = isSpeaking
    }


    protected override fun onDraw(canvas: Canvas) {
        rect = Rect(0, 0, canvas.getWidth(), canvas.getWidth())
        canvas.drawColor(Color.BLUE)
        /*canvas.drawRect(rect, mPaintColor);*/
        if (isStraightLine) {
            var i = 0
            while (i < numberOfWaves) {
                mPaintColor.setStrokeWidth(if (i == 0) primaryWaveLineWidth else secondaryWaveLineWidth)
                val halfHeight = canvas.getHeight() / 2
                val width = canvas.getWidth()
                val mid = canvas.getWidth() / 2

                val maxAmplitude = halfHeight - 4.0f
                val progress = 1.0f - i.toFloat() / this.numberOfWaves
                val normedAmplitude = (1.5f * progress - 0.5f) * this.amplitude
                val path = Path()

                val multiplier = Math.min(1.0f, progress / 3.0f * 2.0f + 1.0f / 3.0f)

                var x = 0f
                while (x < width + density) {
                    // We use a parable to scale the sinus wave, that has its peak in the middle of the view.
                    val scaling = (-Math.pow((1 / mid * (x - mid)).toDouble(), 2.0) + 1).toFloat()

                    val y =
                        (scaling.toDouble() * maxAmplitude.toDouble() * normedAmplitude.toDouble() * Math.sin(
                            2.0 * Math.PI * (x / width).toDouble() * frequency.toDouble() + phase
                        ) + halfHeight).toFloat()

                    if (x == 0f) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    x += density
                }
                mPaintColor.setStyle(Paint.Style.STROKE)
                mPaintColor.setAntiAlias(true)
                canvas.drawPath(path, mPaintColor)
                i++

            }
        } else {
            canvas.drawLine(
                5F,
                (canvas.getHeight() / 2).toFloat(),
                canvas.getWidth().toFloat(),
                (canvas.getHeight() / 2).toFloat(),
                mPaintColor
            )
            canvas.drawLine(
                0F,
                (canvas.getHeight() / 2).toFloat(),
                canvas.getWidth().toFloat(),
                (canvas.getHeight() / 2).toFloat(),
                mPaintColor
            )
            canvas.drawLine(
                (-5).toFloat(),
                (canvas.getHeight() / 2).toFloat(),
                canvas.getWidth().toFloat(),
                (canvas.getHeight() / 2).toFloat(),
                mPaintColor
            )
        }
        this.phase += phaseShift
        invalidate()
    }

    companion object {

        private val defaultFrequency = 1.5f
        private val defaultAmplitude = 1.0f
        private val defaultIdleAmplitude = 0.01f
        private val defaultNumberOfWaves = 5.0f
        private val defaultPhaseShift = -0.15f
        private val defaultDensity = 5.0f
        private val defaultPrimaryLineWidth = 3.0f
        private val defaultSecondaryLineWidth = 1.0f
    }
}