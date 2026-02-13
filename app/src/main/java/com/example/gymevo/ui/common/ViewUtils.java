package com.example.gymevo.ui.common;

import android.view.View;
import android.view.animation.DecelerateInterpolator;

public final class ViewUtils {

    private ViewUtils() { /* no instances */ }

    /**
     * Subtle fade + scale animation intended for small overlay icons (drag/remove).
     * - show: sets VISIBLE then animates alpha & scale to 1
     * - hide: animates alpha & scale down then sets GONE
     */
    public static void animateOverlayVisibility(final View view, boolean show) {
        if (view == null) return;

        view.animate().cancel();

        final long duration = 160L;
        final float startScale = 0.88f;

        if (show) {
            if (view.getVisibility() == View.VISIBLE && view.getAlpha() == 1f) return;
            view.setScaleX(startScale);
            view.setScaleY(startScale);
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
            view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        } else {
            if (view.getVisibility() != View.VISIBLE) return;
            view.animate()
                .alpha(0f)
                .scaleX(startScale)
                .scaleY(startScale)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    view.setVisibility(View.GONE);
                    // reset to default state so next show starts clean
                    view.setAlpha(1f);
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                })
                .start();
        }
    }
}
