package com.example.gymevo.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.google.android.material.color.MaterialColors;

public class SectionHeaderDecoration extends RecyclerView.ItemDecoration {

    public interface HeaderProvider {
        @Nullable
        String getHeaderLabel(int position);
    }

    private final HeaderProvider headerProvider;
    private final int headerHeight;
    private final int headerPaddingStart;
    private final int headerPaddingBottom;
    private final Paint backgroundPaint;
    private final TextPaint textPaint;

    public SectionHeaderDecoration(@NonNull Context context, @NonNull HeaderProvider headerProvider) {
        this.headerProvider = headerProvider;
        headerHeight = context.getResources().getDimensionPixelSize(R.dimen.section_header_height);
        headerPaddingStart = context.getResources().getDimensionPixelSize(R.dimen.section_header_padding_start);
        headerPaddingBottom = context.getResources().getDimensionPixelSize(R.dimen.section_header_padding_bottom);
        int textSize = context.getResources().getDimensionPixelSize(R.dimen.section_header_text_size);

        int surface = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, 0xFFFFFFFF);
        int onSurface = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, 0xFF000000);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(surface);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(onSurface);
        textPaint.setTextSize(textSize);
        textPaint.setFakeBoldText(true);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        if (isFirstInGroup(position)) {
            outRect.top = headerHeight;
        } else {
            outRect.top = 0;
        }
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent,
                           @NonNull RecyclerView.State state) {
        int childCount = parent.getChildCount();
        if (childCount == 0) {
            return;
        }
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (position == RecyclerView.NO_POSITION) {
                continue;
            }
            if (!isFirstInGroup(position)) {
                continue;
            }
            String header = headerProvider.getHeaderLabel(position);
            if (header == null || header.trim().isEmpty()) {
                continue;
            }
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            int bottom = child.getTop() + Math.round(child.getTranslationY());
            int top = bottom - headerHeight;
            if (top < 0) {
                top = 0;
            }
            canvas.drawRect(left, top, right, bottom, backgroundPaint);

            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float textX = left + headerPaddingStart;
            float textY = bottom - headerPaddingBottom - fontMetrics.descent;
            canvas.drawText(header, textX, textY, textPaint);
        }
    }

    private boolean isFirstInGroup(int position) {
        String current = headerProvider.getHeaderLabel(position);
        if (current == null || current.trim().isEmpty()) {
            return false;
        }
        if (position == 0) {
            return true;
        }
        String previous = headerProvider.getHeaderLabel(position - 1);
        if (previous == null) {
            return true;
        }
        return !current.equals(previous);
    }
}
