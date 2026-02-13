package com.example.gymevo.ui.common;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gymevo.R;

/**
 * Glide-based implementation of ImageLoader. Keeps try/catch around Glide calls to be defensive
 * (used previously in ImageUi).
 */
public class GlideImageLoader implements ImageLoader {

    @Override
    public void load(ImageView target, String url) {
        if (target == null) return;
        String safe = url == null ? null : url.trim();
        try {
            RequestOptions opts = new RequestOptions()
                    .placeholder(R.drawable.ic_menu_gallery)
                    .error(R.drawable.ic_menu_gallery)
                    .centerCrop();
            if (safe == null || safe.isEmpty()) {
                // explicit placeholder for empty url
                Glide.with(target).clear(target);
                target.setImageResource(R.drawable.ic_menu_gallery);
                return;
            }
            Glide.with(target)
                    .load(safe)
                    .apply(opts)
                    .into(target);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // swallow and show fallback image — defensive against host context being invalid
            target.setImageResource(R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public void cancel(ImageView target) {
        if (target == null) return;
        try {
            Glide.with(target).clear(target);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            // context may be invalid; ignore
        }
    }
}
