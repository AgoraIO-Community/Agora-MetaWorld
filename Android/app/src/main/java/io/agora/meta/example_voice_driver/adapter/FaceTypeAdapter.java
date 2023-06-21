package io.agora.meta.example_voice_driver.adapter;

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

import io.agora.meta.example_voice_driver.R;
import io.agora.meta.example_voice_driver.databinding.ItemFaceTypeListBinding;
import io.agora.meta.example_voice_driver.models.manifest.FaceBlendShape;


public class FaceTypeAdapter extends RecyclerView.Adapter<FaceTypeAdapter.ViewHolder> {

    private List<FaceBlendShape> mDataList;
    private Context mContext;
    private OnItemClickCallBack mOnItemClickCallBack;

    private int mCurrentPosition;

    public FaceTypeAdapter(Context context) {
        mDataList = new ArrayList<>();
        mContext = context;
        mCurrentPosition = 0;
    }

    public void setDataList(List<FaceBlendShape> list) {
        mDataList = list;
        mCurrentPosition = 0;
        notifyDataSetChanged();
    }

    public void setOnItemClickCallBack(OnItemClickCallBack callBack) {
        mOnItemClickCallBack = callBack;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemFaceTypeListBinding binding;

        public ViewHolder(ItemFaceTypeListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        return new ViewHolder(ItemFaceTypeListBinding.inflate(
                LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (null != mDataList) {
            if (mCurrentPosition == position) {
                viewHolder.binding.layout.setBackgroundResource(R.drawable.bg_dress_and_face_checked);
            } else {
                viewHolder.binding.layout.setBackground(new ColorDrawable(Color.TRANSPARENT));
            }

            FaceBlendShape faceBlendShape = mDataList.get(position);
            viewHolder.binding.faceTypeTv.setText(faceBlendShape.getType());
            viewHolder.binding.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnItemClickCallBack) {
                        notifyItemChanged(position);
                        notifyItemChanged(mCurrentPosition);
                        mCurrentPosition = position;
                        mOnItemClickCallBack.onItemClick(faceBlendShape);
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
        void onItemClick(FaceBlendShape faceBlendShape);
    }
}
