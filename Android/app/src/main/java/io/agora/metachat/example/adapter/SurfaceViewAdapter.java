package io.agora.metachat.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.agora.metachat.example.R;
import io.agora.metachat.example.databinding.ItemSurfaceViewListBinding;
import io.agora.metachat.example.models.SurfaceViewInfo;

public class SurfaceViewAdapter extends RecyclerView.Adapter<SurfaceViewAdapter.ViewHolder> {

    private List<SurfaceViewInfo> mViewLists;
    private Context mContext;

    public SurfaceViewAdapter(Context context) {
        mViewLists = new ArrayList<>();
        mContext = context;
    }

    public void setSurfaceViewData(List<SurfaceViewInfo> list) {
        mViewLists = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemSurfaceViewListBinding binding;

        public ViewHolder(ItemSurfaceViewListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        return new ViewHolder(ItemSurfaceViewListBinding.inflate(
                LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (null != mViewLists) {
            if (viewHolder.binding.surfaceLayout.getChildCount() > 0) {
                viewHolder.binding.surfaceLayout.removeAllViews();
            }
            View view = mViewLists.get(position).getView();
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
            viewHolder.binding.surfaceLayout.addView(view, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            int uid = mViewLists.get(position).getUid();
            if (uid != 0) {
                viewHolder.binding.uidTv.setText(String.format(mContext.getResources().getString(R.string.uid_label), uid));
                viewHolder.binding.uidTv.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.uidTv.setVisibility(View.GONE);
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mViewLists.size();
    }

}
