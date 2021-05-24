package com.tiparo.tripway.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.tiparo.tripway.R

class StepProgressBar(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var barProgressPaint: Paint
    private var barProgressPaintEnd: Paint

    private var stepWidth: Int = 0
    private var currentBarWidth: Int = 0

    private var stepIndexChangedListener: ((Int) -> Unit)? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.StepProgressBar, 0, 0).apply {
            try {
                val colorBar = getColor(R.styleable.StepProgressBar_color, Color.BLACK)
                val colorBarEnd = getColor(R.styleable.StepProgressBar_colorEnd, colorBar)
                barProgressPaint = Paint().apply {
                    color = colorBar
                    style = Paint.Style.FILL
                    setShadowLayer(10F,8F,0F, Color.LTGRAY)
                }
                barProgressPaintEnd = Paint().apply {
                    color = colorBarEnd
                    style = Paint.Style.FILL
                    setShadowLayer(10F,8F,0F, Color.LTGRAY)
                }
            } finally {
                recycle()
            }
        }
    }

    fun setStepCount(count: Int) {
        stepWidth = width / count

    }

    fun updateStep(stepIndex: Int) {
        animateBar(stepIndex)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun setOnStepChangedListener(l: (Int) -> Unit) {
        stepIndexChangedListener = l
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val newStepIndex = getBarStepIndex(event.x)
                animateBar(newStepIndex)
                stepIndexChangedListener?.invoke(newStepIndex)

                //For blind people
                performClick()
            }
            MotionEvent.ACTION_MOVE -> {
                animateBarDragging(event.x)
            }
            MotionEvent.ACTION_UP -> {
                val newStepIndex = getBarStepIndex(event.x)
                animateBar(newStepIndex)
                stepIndexChangedListener?.invoke(newStepIndex)
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            if(currentBarWidth==width){
                drawRect(0F, 0F, currentBarWidth.toFloat(), height.toFloat(), barProgressPaintEnd)
            }
            else{
                drawRect(0F, 0F, currentBarWidth.toFloat(), height.toFloat(), barProgressPaint)
            }
        }

    }

    private fun getBarStepIndex(x: Float): Int {
        return (x / stepWidth).toInt()
    }

    private fun animateBarDragging(x: Float) {
        currentBarWidth = x.toInt()
        invalidate()
    }

    private fun animateBar(stepIndex: Int) {
        if (stepWidth == 0) return

        val newBarWidth = (stepWidth * (stepIndex + 1)).toFloat()

        val animator = ValueAnimator().apply {
            setIntValues(currentBarWidth, newBarWidth.toInt())
            interpolator = AccelerateDecelerateInterpolator()
            duration = 200
            addUpdateListener {
                currentBarWidth = it.animatedValue as Int
                invalidate()
            }
        }
        animator.start()
    }
}