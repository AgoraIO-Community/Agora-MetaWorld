package io.agora.metachat.example.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

import io.agora.metachat.example.R;
import io.agora.metachat.example.models.SkinInfo;

public class SkinGridViewAdapter extends BaseAdapter {
    private static final String TAG = SkinGridViewAdapter.class.getSimpleName();
    private Context mContext;
    private List<SkinInfo> mList;
    private int mIndex; // 页数下标，标示第几页，从0开始
    private int mPargerSize;// 每页显示的最大的数量
    private DisplayMetrics metrics;
    private SkinItemClick skinItemClick;

    public SkinGridViewAdapter(Context context, List<SkinInfo> list, int mIndex, int mPagerSize, SkinItemClick skinItemClick) {
        this.mContext = context;
        this.mList = list;
        this.mIndex = mIndex;
        this.mPargerSize = mPagerSize;
        this.skinItemClick = skinItemClick;

    }

    @Override
    public int getCount() {
        return mList.size() > (mIndex + 1) * mPargerSize ?
                mPargerSize : (mList.size() - mIndex * mPargerSize);
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position + mIndex * mPargerSize);
    }

    @Override
    public long getItemId(int position) {
        return position + (long) mIndex * mPargerSize;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        GridViewHolder holder;

        if (convertView == null) {
            holder = new GridViewHolder();
            convertView = View.inflate(mContext, R.layout.skin_grid_item, null);
            holder.ivItemImage = convertView.findViewById(R.id.gridview_item_im);
            holder.radioButton = convertView.findViewById(R.id.gridview_rb);
            convertView.setTag(holder);
        } else {
            holder = (GridViewHolder) convertView.getTag();
        }
        //重新确定position因为拿到的总是数据源，数据源是分页加载到每页的GridView上的
        final int pos = position + mIndex * mPargerSize;//假设mPageSiez
        //假设mPagerSize=8，假如点击的是第二页（即mIndex=1）上的第二个位置item(position=1),那么这个item的实际位置就是pos=9
        holder.ivItemImage.setImageResource(mList.get(pos).getIconId());

        holder.radioButton.setChecked(mList.get(pos).isCheck());
        holder.ivItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick(pos);
            }
        });
        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick(pos);
            }
        });

        return convertView;
    }

    private void itemClick(int pos) {
        for (int i = 0; i < mList.size(); i++) {
            mList.get(i).setCheck(false);
        }
        mList.get(pos).setCheck(true);

        if (null != skinItemClick) {
            skinItemClick.onSkinItemClick(pos);
        }
    }

    static class GridViewHolder {
        ImageView ivItemImage;
        RadioButton radioButton;
    }

    public interface SkinItemClick {
        void onSkinItemClick(int position);
    }
}