package io.agora.meta.example.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.agora.meta.example.models.DressItemResource;
import io.agora.meta.example.R;
import io.agora.meta.example.databinding.ItemDressTypeListBinding;


public class DressTypeAdapter extends RecyclerView.Adapter<DressTypeAdapter.ViewHolder> {

    private List<DressItemResource> mDataList;
    private Context mContext;
    private OnItemClickCallBack mOnItemClickCallBack;

    private int mCurrentPosition;

    public DressTypeAdapter(Context context) {
        mDataList = new ArrayList<>();
        mContext = context;
        mCurrentPosition = 0;
    }

    public void setDataList(List<DressItemResource> list) {
        mDataList = list;
        mCurrentPosition = 0;
    }

    public void setOnItemClickCallBack(OnItemClickCallBack callBack) {
        mOnItemClickCallBack = callBack;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemDressTypeListBinding binding;

        public ViewHolder(ItemDressTypeListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        return new ViewHolder(ItemDressTypeListBinding.inflate(
                LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (null != mDataList) {
            if (mCurrentPosition == position) {
                viewHolder.binding.layout.setBackgroundResource(R.drawable.bg_dress_type);
            } else {
                viewHolder.binding.layout.setBackground(new ColorDrawable(Color.TRANSPARENT));
            }

            DressItemResource dressItemResource = mDataList.get(position);
            viewHolder.binding.dressTypeTv.setText(dressItemResource.getName());
            viewHolder.binding.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnItemClickCallBack) {
                        mCurrentPosition = position;
                        mOnItemClickCallBack.onItemClick(dressItemResource);
                        notifyItemRangeChanged(0, mDataList.size());
                    }
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (null == mDataList) {
            return 0;
        }
        return mDataList.size();
    }

    public interface OnItemClickCallBack {
        void onItemClick(DressItemResource dressItemResource);
    }
}
