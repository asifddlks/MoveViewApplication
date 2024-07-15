package com.asifddlks.moveviewapplication

import android.animation.ObjectAnimator
import android.content.res.Resources.getSystem
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.slider.RangeSlider
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var layoutContent: ConstraintLayout
    private lateinit var textSpendingAmountMax: TextView
    private lateinit var textSpendingAmountMin: TextView
    private lateinit var rangeSlider: RangeSlider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        layoutContent = findViewById(R.id.layoutContent)
        textSpendingAmountMax = findViewById(R.id.textSpendingAmountMax)
        textSpendingAmountMin = findViewById(R.id.textSpendingAmountMin)
        rangeSlider = findViewById(R.id.rangeSlider)

        prepareRangeSlider()


    }

    private fun prepareRangeSlider() {
        rangeSlider.setCustomThumbDrawable(R.drawable.ic_range_slider_thumb)
        rangeSlider.thumbWidth = 33.px
        rangeSlider.thumbHeight = 32.px

        // Set initial values
        rangeSlider.values = listOf(0.0f, 2000000.0f)
        rangeSlider.valueFrom = 0.0f
        rangeSlider.valueTo = 2000000.0f
        rangeSlider.stepSize = 50000.0f

        rangeSlider.setLabelFormatter { value: Float ->
            "৳${value.roundToInt()}"
        }

        // Initial update to position the TextViews correctly
        rangeSlider.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rangeSlider.viewTreeObserver.removeOnGlobalLayoutListener(this)
                updateTextViews(rangeSlider)
            }
        })

        // Set value change listener
        rangeSlider.addOnChangeListener { slider, value, fromUser ->
            val values = slider.values
            if (values[1] - values[0] < 50000) {
                if (fromUser) {
                    // Enforce the minimum gap and keep within bounds
                    if (value == values[0]) {
                        slider.values = listOf(values[0], (values[0] + 50000).coerceAtMost(slider.valueTo))
                    } else {
                        slider.values = listOf((values[1] - 50000).coerceAtLeast(slider.valueFrom), values[1])
                    }
                }
            }
            updateTextViews(slider)
        }

        // Set value change listener to prevent overlap
        rangeSlider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                // Do something when the touch starts
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                val values = slider.values
                if (values[1] - values[0] < 50000) {
                    // Enforce the minimum gap and keep within bounds
                    if (values[0] + 50000 <= slider.valueTo) {
                        slider.values = listOf(values[0], (values[0] + 50000).coerceAtMost(slider.valueTo))
                    } else {
                        slider.values = listOf((values[1] - 50000).coerceAtLeast(slider.valueFrom), values[1])
                    }
                }
            }
        })
    }

    private fun updateTextViews(slider: RangeSlider) {
        textSpendingAmountMin.text = "৳${slider.values.first().roundToInt()}"
        textSpendingAmountMax.text = "৳${slider.values.last().roundToInt()}"

        positionTextViews(slider)
    }

    private fun positionTextViews(slider: RangeSlider) {
        val minSliderValue = slider.valueFrom
        val maxSliderValue = slider.valueTo
        val rangeSliderWidth = slider.width - slider.paddingLeft - slider.paddingRight

        // Calculate the proportional position of each thumb relative to the slider range
        val leftProportion = (slider.values.first() - minSliderValue) / (maxSliderValue - minSliderValue)
        val rightProportion = (slider.values.last() - minSliderValue) / (maxSliderValue - minSliderValue)

        // Calculate exact positions for textSpendingAmountMin and textSpendingAmountMax
        val leftThumbX = slider.paddingLeft + leftProportion * rangeSliderWidth
        val rightThumbX = slider.paddingLeft + rightProportion * rangeSliderWidth

        // Define padding
        val minPadding = 20f // Adjust this value as needed

        // Adjust TextView positions to align with thumbs
        val textMinX = leftThumbX - textSpendingAmountMin.width / 2 + minPadding
        val textMaxX = rightThumbX - textSpendingAmountMax.width / 2

        // Ensure text views do not go out of bounds
        textSpendingAmountMin.x = when {
            textMinX < minPadding -> minPadding
            textMinX + textSpendingAmountMin.width > rangeSliderWidth -> (rangeSliderWidth - textSpendingAmountMin.width).toFloat()
            else -> textMinX
        }

        textSpendingAmountMax.x = when {
            textMaxX < 0 -> 0f
            textMaxX + textSpendingAmountMax.width > rangeSliderWidth -> (rangeSliderWidth - textSpendingAmountMax.width).toFloat()
            else -> textMaxX
        }
    }
}

val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()