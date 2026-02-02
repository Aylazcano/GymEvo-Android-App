package com.example.gymevo.ui.common;

import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.example.gymevo.R;

public final class StarUi {

    private static final int STAR_ON = android.R.drawable.btn_star_big_on;
    private static final int STAR_OFF = android.R.drawable.btn_star_big_off;

    private StarUi() {
    }

    public static void apply(ImageView starIcon, boolean isStarred) {
        if (starIcon == null) {
            return;
        }
        starIcon.setVisibility(android.view.View.VISIBLE);
        int iconRes = isStarred ? STAR_ON : STAR_OFF;
        starIcon.setImageResource(iconRes);
        int tint = ContextCompat.getColor(starIcon.getContext(),
                isStarred ? R.color.star_on : R.color.star_off);
        starIcon.setColorFilter(tint, android.graphics.PorterDuff.Mode.SRC_IN);
    }
}
