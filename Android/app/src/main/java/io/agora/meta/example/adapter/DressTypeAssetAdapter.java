package io.agora.meta.example.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.agora.meta.example.R;
import io.agora.meta.example.databinding.ItemDressAssetListBinding;


public class DressTypeAssetAdapter extends RecyclerView.Adapter<DressTypeAssetAdapter.ViewHolder> {

    private List<Integer> mDataList;
    private final Context mContext;
    private OnItemClickCallBack mOnItemClickCallBack;

    private Map<Integer, String> mAssetMap;
    private int mCurrentResId;
    private int mCurrentPosition;

    public DressTypeAssetAdapter(Context context) {
        mDataList = new ArrayList<>();
        mContext = context;
        mCurrentPosition = 0;
    }

    public void setAssetMap(Map<Integer, String> assetMap) {
        mAssetMap = assetMap;
    }

    public void setDataList(List<Integer> list, int resId) {
        mDataList = list;
        mCurrentPosition = mDataList.indexOf(resId);
        mCurrentResId = resId;
        notifyDataSetChanged();
    }

    public void setOnItemClickCallBack(OnItemClickCallBack callBack) {
        mOnItemClickCallBack = callBack;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemDressAssetListBinding binding;

        public ViewHolder(ItemDressAssetListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        return new ViewHolder(ItemDressAssetListBinding.inflate(
                LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (null != mDataList && null != mAssetMap) {
            int resId = mDataList.get(position);

            boolean currentImageChecked = false;
            if (-1 != mCurrentResId) {
                currentImageChecked = mCurrentResId == resId;
            } else {
                mCurrentPosition = 0;
                currentImageChecked = mCurrentPosition == position;
            }

            viewHolder.binding.assetImg.setStrokeWidthResource(R.dimen.select_border_width);
            if (currentImageChecked) {
                viewHolder.binding.assetImg.setStrokeColorResource(R.color.select_border_color);
                viewHolder.binding.checkedIcon.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.assetImg.setStrokeColorResource(R.color.select_border_color_null);
                viewHolder.binding.checkedIcon.setVisibility(View.GONE);
            }

            Glide.with(mContext)
                    .load(new File(Objects.requireNonNull(mAssetMap.get(resId))))
                    .into(viewHolder.binding.assetImg);

            viewHolder.binding.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnItemClickCallBack) {
                        mCurrentResId = resId;

                        notifyItemChanged(position);
                        notifyItemChanged(mCurrentPosition);

                        mCurrentPosition = position;
                        mOnItemClickCallBack.onItemClick(resId);
                    }
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public interface OnItemClickCallBack {
        void onItemClick(int resId);
    }
}
