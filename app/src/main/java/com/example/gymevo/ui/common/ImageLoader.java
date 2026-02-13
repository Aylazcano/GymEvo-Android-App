package com.example.gymevo.ui.common;

import android.widget.ImageView;

/**
 * Minimal image-loading abstraction. Keeps call-sites decoupled from a specific library (Glide/Picasso/Coil).
 * Implementations should be lightweight and lifecycle-aware if possible.
 */
public interface ImageLoader {
    /**
     * Load image into the provided ImageView. Implementations should handle null/empty targets gracefully.
     */
    void load(ImageView target, String url);

    /**
     * Cancel any pending load for the provided ImageView and clear resources.
     */
    void cancel(ImageView target);
}
