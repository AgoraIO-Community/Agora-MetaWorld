package io.agora.metachat.example.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

import io.agora.metachat.example.R;
import io.agora.metachat.example.models.SkinInfo;

/**
 * 衣服、裤子、鞋子等底部gridview
 * 底部每项是viewpager加gridview，每个gridview有8个数据，如果数据多余8个则再次添加一个gridview到viewpager中
 */
public class SkinGridViewAdapter extends BaseAdapter {
    private static final String TAG = "GridViewAdapter";
    Context mContext;
    List<SkinInfo> mList = new ArrayList<>();
    int mIndex; // 页数下标，标示第几页，从0开始
    int mPargerSize;// 每页显示的最大的数量
    DisplayMetrics metrics;

    public SkinGridViewAdapter(Context context, List<SkinInfo> list, int mIndex, int mPagerSize) {
        this.mContext = context;
        this.mList = list;
        this.mIndex = mIndex;
        this.mPargerSize = mPagerSize;

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
        metrics = mContext.getResources().getDisplayMetrics();
//        int widthPixels = metrics.widthPixels;
//        int width = widthPixels / 5;

        int height = Dp2Px(mContext, 80);
        if (convertView == null) {
            holder = new GridViewHolder();
            convertView = View.inflate(mContext, R.layout.skin_grid_item, null);
            holder.ivItemImage = convertView.findViewById(R.id.gridview_item_im);
            holder.radioButton = convertView.findViewById(R.id.gridview_rb);
            AbsListView.LayoutParams params = (AbsListView.LayoutParams) convertView.getLayoutParams();
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
//                Toast.makeText(mContext,"position = "+position, Toast.LENGTH_SHORT).show();
                /*for(int i = 0;i<mList.size();i++){
                    mList.get(i).setCheck(false);
                }
                for (int i = 0;i< GameActivity.adapterArrayList.size();i++){
                    GameActivity.adapterArrayList.get(i).notifyDataSetChanged();
                }
//                notifyDataSetChanged();
                if (holder.radioButton.isChecked()) {
                    holder.radioButton.setChecked(false);
                } else {
                    holder.radioButton.setChecked(true);
                }

                ChangeClothes changeClothes;
                Gson gson = new Gson();

                if(!"".equals(Utils.changeClothes)){
                    changeClothes = gson.fromJson(Utils.changeClothes, ChangeClothes.class);
                }else{
                    String dataStr = Utils.getPreferences(mContext,Utils.changeData);
                    changeClothes = gson.fromJson(dataStr, ChangeClothes.class);
                }

                for (int i = 0;i<changeClothes.getChangeClothingList().size();i++){

                    if(changeClothes.getChangeClothingList().get(i).getType() == mList.get(pos).getType()){
                        mList.get(pos).setCheck(true);
                        changeClothes.getChangeClothingList().set(i,mList.get(pos));
                        String jsonObject = gson.toJson(changeClothes);
                        Utils.changeClothes = jsonObject;
//                        Utils.setPreferences(mContext,jsonObject,Utils.changeData);
                        MetaChatContext.getInstance().sendSceneMessage(jsonObject);
                    }
                }*/
            }
        });
        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  for(int i = 0;i<mList.size();i++){
                    mList.get(i).setCheck(false);
                }
                for (int i = 0;i< GameActivity.adapterArrayList.size();i++){
                    GameActivity.adapterArrayList.get(i).notifyDataSetChanged();
                }
                if (holder.radioButton.isChecked()) {
                    holder.radioButton.setChecked(false);
                } else {
                    holder.radioButton.setChecked(true);
                }
                ChangeClothes changeClothes;
                Gson gson = new Gson();
                if(!"".equals(Utils.changeClothes)){
                    changeClothes = gson.fromJson(Utils.changeClothes, ChangeClothes.class);
                }else{
                    String dataStr = Utils.getPreferences(mContext,Utils.changeData);
                    changeClothes = gson.fromJson(dataStr, ChangeClothes.class);
                }

                for (int i = 0;i<changeClothes.getChangeClothingList().size();i++){

                    if(changeClothes.getChangeClothingList().get(i).getType() == mList.get(pos).getType()){
                        mList.get(pos).setCheck(true);
                        changeClothes.getChangeClothingList().set(i,mList.get(pos));
                        String jsonObject = gson.toJson(changeClothes);
                        Utils.changeClothes = jsonObject;
//                        Utils.setPreferences(mContext,jsonObject,Utils.changeData);
                        MetaChatContext.getInstance().sendSceneMessage(jsonObject);
                    }
                }*/
            }
        });
        return convertView;
    }

    static class GridViewHolder {
        ImageView ivItemImage;
        RadioButton radioButton;
    }

    // dp转px
    public static int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}