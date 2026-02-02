package com.example.gymevo.ui.common;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemSpacingDecoration extends RecyclerView.ItemDecoration {

    private final int spacingPx;
    private final boolean includeTop;

    public ItemSpacingDecoration(int spacingPx, boolean includeTop) {
        this.spacingPx = Math.max(spacingPx, 0);
        this.includeTop = includeTop;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
                               @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        if (includeTop && position == 0) {
            outRect.top = spacingPx;
        }
        outRect.bottom = spacingPx;
    }
}
