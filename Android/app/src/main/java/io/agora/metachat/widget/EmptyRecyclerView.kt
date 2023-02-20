package io.agora.metachat.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author create by zhangwei03
 */
class EmptyRecyclerView : RecyclerView {

    private var emptyView: View? = null

    private var onEmptyCallback: ((error: Boolean) -> Unit)? = null

    private val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            checkIfEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            checkIfEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            checkIfEmpty()
        }
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**通过这个方法设置空布局 */
    fun setEmptyView(emptyView: View) {
        this.emptyView = emptyView
        checkIfEmpty()
    }

    /**列表数据空回调 */
    fun setOnEmptyCallback(onEmptyCallback: (error: Boolean) -> Unit) {
        this.onEmptyCallback = onEmptyCallback
    }

    override fun setAdapter(newAdapter: Adapter<*>?) {
        val oldAdapter = adapter
        oldAdapter?.unregisterAdapterDataObserver(emptyObserver)
        super.setAdapter(newAdapter)
        newAdapter?.registerAdapterDataObserver(emptyObserver)
        checkIfEmpty()
    }

    private fun checkIfEmpty() {
        adapter?.let { adapter ->
            val emptyViewVisible = adapter.itemCount == 0
            emptyView?.visibility = if (emptyViewVisible) View.VISIBLE else View.GONE
            onEmptyCallback?.invoke(emptyViewVisible)
        }
    }
}