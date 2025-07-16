package com.mentor.application.views.comman.utils

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class DefaultDividerItemDecoration(private val orientation: Int,
                                   private val divider: Drawable) : RecyclerView.ItemDecoration() {

    companion object {
        //        private val ATTRS = intArrayOf(android.R.attr.listDivider)
//        private const val HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL
        private const val VERTICAL_LIST = LinearLayoutManager.VERTICAL
    }


//    private fun setOrientation(orientation: Int) {
//        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
//            throw IllegalArgumentException("invalid orientation")
//        }
//    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (orientation == VERTICAL_LIST) {
            drawVertical(canvas, parent)
        } else {
            drawHorizontal(canvas, parent)
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin +
                    Math.round(ViewCompat.getTranslationY(child))
            val bottom = top + divider.intrinsicHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin +
                    Math.round(ViewCompat.getTranslationX(child))
            val right = left + divider.intrinsicHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        if (orientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, divider.intrinsicHeight)
        } else {
            outRect.set(0, 0, divider.intrinsicWidth, 0)
        }
    }
}