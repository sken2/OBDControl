package com.example.obdcontrol.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.lang.Integer.max

class CommandHistoryLayoutManager : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams (
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {

        recycler?.let{
            detachAndScrapAttachedViews(it)

            if (itemCount == 0) {
                return
            }
            var renderHeight = 0
            var renderWidth = 0
            var position = 0

            var view = recycler.getViewForPosition(position)
            measureChild(view, 0, 0)
            var maxHightInRow = getDecoratedMeasuredHeight(view)
            while ((renderHeight + maxHightInRow < paddingBottom - paddingTop)
                    and (position < itemCount)) {
                var itemWidth = getDecoratedMeasuredWidth(view)
                while (renderWidth + itemWidth < paddingRight - paddingLeft) {
                    addView(view)
                    position++
                    renderWidth += itemWidth
                    if (position > itemCount) break
                    view = recycler.getViewForPosition(position)
                    itemWidth = getDecoratedMeasuredWidth(view)
                    maxHightInRow = max(maxHightInRow, getDecoratedMeasuredHeight(view))
                }
                if (position > itemCount) break
                renderHeight =+ maxHightInRow
                maxHightInRow = getBottomDecorationHeight(view)
                renderWidth = 0
            }
        }
    }
}