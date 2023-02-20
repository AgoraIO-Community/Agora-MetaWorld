package io.agora.metachat.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration

/**
 * @author create by zhangwei03
 */
class MChatItemDecoration : MaterialDividerItemDecoration {

    constructor(context: Context, orientation: Int) : this(context, null, orientation)

    constructor(context: Context, attrs: AttributeSet?, orientation: Int) : this(context, attrs, 0, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, orientation: Int) : super(
        context, attrs, defStyleAttr, orientation
    )

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect[0, 0, 0] = 0
//        if (orientation == VERTICAL) {
//            outRect.bottom = dividerDrawable.intrinsicHeight + thickness
//        } else {
//            outRect.right = dividerDrawable.intrinsicWidth + thickness
//        }

    }

    private fun isLastRow(itemPosition: Int, parent: RecyclerView): Boolean {
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            val spanCount = layoutManager.spanCount
            if ((itemPosition + 1) % spanCount == 0) {
                return true
            }
        }
        return false
    }

    private fun isLastColum(itemPosition: Int, parent: RecyclerView): Boolean {
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            val childCount = parent.adapter?.itemCount ?: 0
            val spanCount = layoutManager.spanCount
            val lastRowCount = childCount % spanCount
            if (lastRowCount == 0 || lastRowCount < spanCount)
                return true
        }
        return false
    }
}