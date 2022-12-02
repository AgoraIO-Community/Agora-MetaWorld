package io.agora.metachat.example.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

/**
 * 衣服、裤子、鞋子等底部滑动的viewpager
 */
public class ViewPagerAdapter extends PagerAdapter {

    List<View> mViewList;//View就是GridView所显示的内容
    Context mContext;

    public ViewPagerAdapter(Context context, List<View> viewList) {
        this.mContext = context;
        this.mViewList = viewList;
    }

    @Override
    public int getCount() {
        if (mViewList != null && mViewList.size() > 0) {
            return mViewList.size();
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        container.addView(mViewList.get(position));
        return mViewList.get(position);
    }

    /**
     * 将当前的View添加到ViewGroup容器中
     * 这个方法，return一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPage上
     */
    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}