package com.example.obdcontrol.adapters

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.obdcontrol.Const
import java.lang.Integer.max

class CommandHistoryLayoutManager : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        Log.v(Const.TAG, "CommandHistoryLayoutManager::generateDefaultLayoutParams")
        return RecyclerView.LayoutParams (
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        Log.v(Const.TAG, "CommandHistoryLayoutManager::onLayoutChildlen")
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
            fillForY@ while (renderHeight + maxHightInRow < height - paddingBottom - paddingTop) {
                var itemWidth = getDecoratedMeasuredWidth(view)
                while (renderWidth + itemWidth < width - paddingRight - paddingLeft) {
                    addView(view)
                    layoutDecorated(view, renderWidth, renderHeight, renderWidth + itemWidth, renderHeight + getDecoratedMeasuredHeight(view) )
                    renderWidth += itemWidth
                    position++
                    if (position >= itemCount) break@fillForY   // its mean complete rendering
                    view = recycler.getViewForPosition(position)
                    measureChild(view, 0, 0)
                    itemWidth = getDecoratedMeasuredWidth(view)
                    maxHightInRow = max(maxHightInRow, getDecoratedMeasuredHeight(view))
                }
                addView(view)
                renderWidth = 0
                renderHeight += maxHightInRow
                if (itemWidth >= width - paddingLeft - paddingRight) { // item is larger than view
                    layoutDecorated(view, renderWidth, renderHeight, renderWidth + itemWidth, renderHeight + getDecoratedMeasuredHeight(view) )
                } else {
                    layoutDecorated(view, renderWidth, renderHeight, renderWidth + itemWidth, renderHeight + getDecoratedMeasuredHeight(view) )
                    renderWidth = itemWidth
                }
                position++
                if (position >= itemCount) break
                view = recycler.getViewForPosition(position)
                measureChild(view, 0, 0)
                itemWidth = getDecoratedMeasuredWidth(view)
                maxHightInRow = getDecoratedMeasuredHeight(view)
            }
        }
    }

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        Log.v(Const.TAG, "CommandHistoryLayoutManager::onItemRemoved itemCount = $itemCount")
        super.onItemsRemoved(recyclerView, positionStart, itemCount)
    }

    override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        Log.v(Const.TAG, "CommandHistoryLayoutManager::onItemsAdd itemCount = $itemCount")
        super.onItemsAdded(recyclerView, positionStart, itemCount)
    }
}