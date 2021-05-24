package com.tiparo.tripway.views.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ImagesGridItemDecorator(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val spanCount = getTotalSpanCount(parent)
        val position = parent.getChildAdapterPosition(view)
        val spanItemSize = getItemSpanSize(parent, position)

        if(spanItemSize == spanCount) return

        outRect.top = if(isInTheFirstRow(position,spanCount)) 0 else spacing
        outRect.left = if(isFirstInRow(position,spanCount)) 0 else spacing/2
        outRect.right = if(isLastInRow(position,spanCount)) 0 else spacing/2
        outRect.bottom = 0
    }

    private fun isInTheFirstRow(position: Int, spanCount: Int) = position < spanCount;

    private fun isFirstInRow(position: Int, spanCount: Int) = position % spanCount == 0

    private fun isLastInRow(position: Int, spanCount: Int) = isFirstInRow(position + 1, spanCount)


    private fun getTotalSpanCount(parent: RecyclerView): Int {
        val layoutManager = parent.layoutManager
        return if (layoutManager is GridLayoutManager)
            layoutManager.spanCount
        else 1
    }

    private fun getItemSpanSize(parent: RecyclerView, position: Int): Int {
        val layoutManager = parent.layoutManager
        return if (layoutManager is GridLayoutManager)
            layoutManager.spanSizeLookup.getSpanSize(position)
        else 1
    }
}