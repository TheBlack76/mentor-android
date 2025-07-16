package com.mentor.application.utils

import android.content.Context
import android.graphics.Point
import android.view.WindowManager

/**
 * Created by Mukesh on 20/7/18.
 */
open class ScreenDimensions(private val mContext: Context) {

    companion object {
        private var screenWidth: Int = 0
        private var screenHeight: Int = 0
    }

    fun getScreenWidth(): Int {
        return if (0 == screenWidth) {
            val display = (mContext
                    .getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val size = Point()
            display.getSize(size)
            screenWidth = size.x
            screenWidth
        } else {
            screenWidth
        }
    }

    fun getScreenHeight(): Int {
        return if (0 == screenHeight) {
            val display = (mContext
                    .getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val size = Point()
            display.getSize(size)
            screenHeight = size.y
            screenHeight
        } else {
            screenHeight
        }
    }

    fun getHeightWithGivenPercentage(percentage: Int): Int {
        if (0 == screenHeight) {
            screenHeight = getScreenHeight()
        }
        return percentage * screenHeight / 100
    }

}
