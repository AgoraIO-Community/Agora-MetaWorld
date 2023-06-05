package io.agora.meta.example.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.agora.meta.example.R;
import io.agora.meta.example.databinding.ItemFaceShapesListBinding;
import io.agora.meta.example.models.FaceParameterItem;
import io.agora.meta.example.models.manifest.FaceBlendShapeItem;


public class FaceTypeShapesAdapter extends RecyclerView.Adapter<FaceTypeShapesAdapter.ViewHolder> {

    private List<FaceBlendShapeItem> mDataList;
    private final Context mContext;
    private OnShapeChangeCallBack mCallBack;

    private int mCurrentPosition;

    private Map<String, FaceParameterItem> mFaceParameters;

    public FaceTypeShapesAdapter(Context context) {
        mDataList = new ArrayList<>();
        mContext = context;
        mCurrentPosition = 0;
    }

    public void setDataList(List<FaceBlendShapeItem> list, Map<String, FaceParameterItem> faceParameters) {
        mDataList = list;
        mFaceParameters = faceParameters;
        mCurrentPosition = 0;
        notifyDataSetChanged();
    }

    public void setOnShapeChangeCallBack(OnShapeChangeCallBack callBack) {
        mCallBack = callBack;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemFaceShapesListBinding binding;
        private final OnShapeChangeCallBack mCallBack;
        private FaceBlendShapeItem mFaceBlendShapeItem;

        public ViewHolder(Context context, ItemFaceShapesListBinding binding, OnShapeChangeCallBack callBack) {
            super(binding.getRoot());
            this.binding = binding;
            mCallBack = callBack;
            binding.faceShapeSlider.addOnChangeListener(new Slider.OnChangeListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                    if (fromUser && null != mCallBack && null != mFaceBlendShapeItem) {
                        binding.faceTypeTv.setText(String.format(context.getResources().getString(R.string.face_shape_label), mFaceBlendShapeItem.getCh(), (int) value));
                        mCallBack.onShapeChange(mFaceBlendShapeItem, (int) value);
                    }
                }
            });
        }

        public void setFaceBlendShapeItem(FaceBlendShapeItem faceBlendShapeItem) {
            mFaceBlendShapeItem = faceBlendShapeItem;
        }
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        return new ViewHolder(mContext, ItemFaceShapesListBinding.inflate(
                LayoutInflater.from(viewGroup.getContext()), viewGroup, false), mCallBack);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (null != mDataList && null != mFaceParameters) {
            FaceBlendShapeItem faceBlendShapeItem = mDataList.get(position);
            viewHolder.setFaceBlendShapeItem(faceBlendShapeItem);
            viewHolder.binding.faceTypeTv.setText(String.format(mContext.getResources().getString(R.string.face_shape_label), faceBlendShapeItem.getCh(), Objects.requireNonNull(mFaceParameters.get(faceBlendShapeItem.getKey())).getValue()));
            viewHolder.binding.faceShapeSlider.setValue(Objects.requireNonNull(mFaceParameters.get(faceBlendShapeItem.getKey())).getValue());
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

    public interface OnShapeChangeCallBack {
        void onShapeChange(FaceBlendShapeItem faceBlendShapeItem, int value);
    }
}
