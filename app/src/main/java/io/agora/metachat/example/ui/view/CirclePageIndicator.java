package io.agora.metachat.example.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * Created by
 */

public class CirclePageIndicator extends View {

    //点的半径
    private final int mDotRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
    //点与点的间隔
    private final int mDotGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());


    private ViewPager mViewPager;
    private Paint mDotPaint;

    private int mPosition;
    private float mPositionOffset;

    //不动点的颜色
    private int mNormalColor;
    //动点颜色
    private int mSelectedColor;

    public CirclePageIndicator(Context context) {
        this(context, null);
    }

    public CirclePageIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mDotPaint = new Paint();
        mDotPaint.setAntiAlias(true);

        mNormalColor = Color.BLACK;
        mSelectedColor = Color.RED;
    }

    /**
     * 测量CirclePageIndicator的高宽，不去使用在布局中的配置的宽高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mViewPager.getAdapter() == null) {
            return;
        }
        int count = mViewPager.getAdapter().getCount();
        //宽度 = 点的直径 * 点的个数 + 点与点间隔 * (点的个数 - 1)
        int width = 2 * mDotRadius * count + (count - 1) * mDotGap;
        //高度 = 点的直径
        int height = 2 * mDotRadius;
        setMeasuredDimension(width, height);
    }

    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
    }

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            mPosition = position;
            mPositionOffset = positionOffset;
            invalidate();
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        if (mViewPager.getAdapter() == null) {
            return;
        }
        //点与点之间圆心的距离
        int dotDistance = mDotGap + 2 * mDotRadius;
        //循环遍历不动点
        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            float cx = mDotRadius + i * dotDistance;
            mDotPaint.setColor(mNormalColor);
            canvas.drawCircle(cx, (float) mDotRadius, mDotRadius, mDotPaint);
        }
        //绘制动点
        mDotPaint.setColor(mSelectedColor);
        //计算动点x轴的位置
        float mMoveCx = mDotRadius + dotDistance * mPositionOffset + mPosition * dotDistance;
        canvas.drawCircle(mMoveCx, (float) mDotRadius, mDotRadius, mDotPaint);
    }
}