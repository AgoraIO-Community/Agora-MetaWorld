package io.agora.metachat.widget

import android.view.View

/**
 * @author create by zhangwei03
 */
class OnIntervalClickListener constructor(
    private val action: View.OnClickListener,
    private val timeInterval: Long = DEFAULT_INTERVAL_TIME
) : View.OnClickListener {
    private var mLastClickTime: Long = 0

    companion object{
        private const val DEFAULT_INTERVAL_TIME = 500L
    }

    override fun onClick(v: View) {
        val nowTime = System.currentTimeMillis()
        if (nowTime - mLastClickTime > timeInterval) {
            //单次点击事件
            mLastClickTime = nowTime
            action.onClick(v)
        }
    }
}