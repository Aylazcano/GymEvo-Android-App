package com.example.gymevo.ui.common;

import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.ImageView;

import com.example.gymevo.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ImageUi {

    private static final long LOOP_INTERVAL_MS = 1000L;
    private static final Map<String, String> LEGACY_IMAGE_MAP = createLegacyImageMap();
    private static final Map<String, Integer> DRAWABLE_NAME_MAP = createDrawableNameMap();

    // Abstraction layer — can be swapped for testing or another library later.
    private static ImageLoader imageLoader = new GlideImageLoader();

    private ImageUi() {
    }

    public static void setImageLoader(ImageLoader loader) {
        imageLoader = loader != null ? loader : new GlideImageLoader();
    }

    public static void loadExerciseImage(ImageView imageView, String imagePath) {
        if (imageView == null) {
            return;
        }

        ImageSource source = resolveImageSource(imagePath);
        if (source == null) {
            imageLoader.cancel(imageView);
            imageView.setImageResource(R.drawable.ic_menu_gallery);
            return;
        }

        if (source.drawableResId != 0) {
            imageLoader.cancel(imageView);
            imageView.setImageResource(source.drawableResId);
            return;
        }

        imageLoader.load(imageView, source.model);
    }

    public static void startExerciseLoop(ImageView imageView, String startImage, String endImage) {
        if (imageView == null) {
            return;
        }
        stopExerciseLoop(imageView);

        String startValue = mapLegacyName(trimOrNull(startImage));
        String endValue = mapLegacyName(trimOrNull(endImage));

        if (TextUtils.isEmpty(startValue) && TextUtils.isEmpty(endValue)) {
            imageView.setImageResource(R.drawable.ic_menu_gallery);
            return;
        }

        if (TextUtils.isEmpty(endValue) || TextUtils.equals(startValue, endValue)) {
            loadExerciseImage(imageView, startValue);
            return;
        }

        LoopState state = new LoopState(startValue, endValue);
        imageView.setTag(R.id.tag_exercise_loop, state);

        // Show A immediately, then alternate every second.
        loadExerciseImage(imageView, startValue);
        state.nextIsStart = false;
        state.lastSwitchAt = SystemClock.uptimeMillis();

        state.runnable = new Runnable() {
            @Override
            public void run() {
                LoopState currentState = getLoopState(imageView);
                if (currentState == null) {
                    return;
                }
                long now = SystemClock.uptimeMillis();
                long elapsed = now - currentState.lastSwitchAt;
                if (elapsed < LOOP_INTERVAL_MS) {
                    imageView.postDelayed(this, LOOP_INTERVAL_MS - elapsed);
                    return;
                }
                // Defensive: if the view is not attached to a window anymore, stop the loop to avoid
                // calling Glide.with(...) on a destroyed context which can throw and crash the app.
                if (imageView.getWindowToken() == null) {
                    stopExerciseLoop(imageView);
                    return;
                }
                String next = currentState.nextIsStart ? currentState.start : currentState.end;
                currentState.nextIsStart = !currentState.nextIsStart;
                currentState.lastSwitchAt = now;
                loadExerciseImage(imageView, next);
                imageView.postDelayed(this, LOOP_INTERVAL_MS);
            }
        };

        imageView.postDelayed(state.runnable, LOOP_INTERVAL_MS);
    }

    public static void stopExerciseLoop(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        LoopState state = getLoopState(imageView);
        if (state != null && state.runnable != null) {
            imageView.removeCallbacks(state.runnable);
        }
        imageView.setTag(R.id.tag_exercise_loop, null);
    }

    private static String mapLegacyName(String rawValue) {
        if (TextUtils.isEmpty(rawValue)) {
            return rawValue;
        }
        String clean = normalize(rawValue);
        int extensionIndex = clean.lastIndexOf('.');
        if (extensionIndex > 0) {
            clean = clean.substring(0, extensionIndex);
        }
        String mapped = LEGACY_IMAGE_MAP.get(clean);
        return mapped != null ? mapped : rawValue;
    }

    private static ImageSource resolveImageSource(String rawPath) {
        if (TextUtils.isEmpty(rawPath)) {
            return null;
        }
        String mapped = mapLegacyName(rawPath.trim());
        if (TextUtils.isEmpty(mapped)) {
            return null;
        }

        int drawableResId = resolveDrawableId(mapped);
        if (drawableResId != 0) {
            return ImageSource.fromDrawable(drawableResId);
        }

        return ImageSource.fromModel(normalizePathModel(mapped));
    }

    private static int resolveDrawableId(String rawValue) {
        if (TextUtils.isEmpty(rawValue)) {
            return 0;
        }
        String clean = normalize(rawValue);
        int extensionIndex = clean.lastIndexOf('.');
        if (extensionIndex > 0) {
            clean = clean.substring(0, extensionIndex);
        }
        clean = clean.replace('-', '_');
        Integer resId = DRAWABLE_NAME_MAP.get(clean);
        return resId != null ? resId : 0;
    }

    private static String normalizePathModel(String value) {
        String normalized = value != null ? value.trim() : null;
        if (TextUtils.isEmpty(normalized)) {
            return normalized;
        }
        if (isRemoteOrUri(normalized)) {
            return normalized;
        }
        if (normalized.startsWith("/")) {
            return "file://" + normalized;
        }
        return normalized;
    }

    private static LoopState getLoopState(ImageView imageView) {
        if (imageView == null) {
            return null;
        }
        Object tag = imageView.getTag(R.id.tag_exercise_loop);
        if (tag instanceof LoopState) {
            return (LoopState) tag;
        }
        return null;
    }

    private static boolean isRemoteOrUri(String value) {
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("content://") || lower.startsWith("file://")
                || lower.startsWith("android.resource://");
    }

    private static String trimOrNull(String value) {
        return value != null ? value.trim() : null;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static Map<String, String> createLegacyImageMap() {
        Map<String, String> map = new HashMap<>();
        map.put("squat_start", "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80");
        map.put("squat_end", "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80");
        map.put("bench_press_start", "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80");
        map.put("bench_press_end", "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80");
        map.put("deadlift_start", "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=800&q=80");
        map.put("deadlift_end", "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=800&q=80");
        map.put("shoulder_press_start", "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=800&q=80");
        map.put("shoulder_press_end", "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=800&q=80");
        map.put("pull_up_start", "https://images.unsplash.com/photo-1483721310020-03333e577078?auto=format&fit=crop&w=800&q=80");
        map.put("pull_up_end", "https://images.unsplash.com/photo-1483721310020-03333e577078?auto=format&fit=crop&w=800&q=80");
        map.put("bicep_curl_start", "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=800&q=80");
        map.put("bicep_curl_end", "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=800&q=80");
        map.put("tricep_dip_start", "https://images.unsplash.com/photo-1517832606294-6c5b7f4f49f9?auto=format&fit=crop&w=800&q=80");
        map.put("tricep_dip_end", "https://images.unsplash.com/photo-1517832606294-6c5b7f4f49f9?auto=format&fit=crop&w=800&q=80");
        map.put("plank_start", "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0?auto=format&fit=crop&w=800&q=80");
        map.put("plank_end", "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0?auto=format&fit=crop&w=800&q=80");
        map.put("leg_press_start", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=800&q=80");
        map.put("leg_press_end", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=800&q=80");
        map.put("lunge_start", "https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=800&q=80");
        map.put("lunge_end", "https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=800&q=80");
        map.put("chest_fly_start", "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80");
        map.put("chest_fly_end", "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80");
        map.put("incline_bench_press_start", "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80");
        map.put("incline_bench_press_end", "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80");
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, Integer> createDrawableNameMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("ic_menu_gallery", R.drawable.ic_menu_gallery);
        map.put("ic_menu_camera", R.drawable.ic_menu_camera);
        map.put("ic_drag_handle_24", R.drawable.ic_drag_handle_24);
        map.put("ic_filter_all", R.drawable.ic_filter_all);
        map.put("ic_filter_anaerobic", R.drawable.ic_filter_anaerobic);
        map.put("ic_filter_aerobic", R.drawable.ic_filter_aerobic);
        return Collections.unmodifiableMap(map);
    }

    private static final class ImageSource {
        private final String model;
        private final int drawableResId;

        private ImageSource(String model, int drawableResId) {
            this.model = model;
            this.drawableResId = drawableResId;
        }

        private static ImageSource fromDrawable(int drawableResId) {
            return new ImageSource(null, drawableResId);
        }

        private static ImageSource fromModel(String model) {
            return new ImageSource(model, 0);
        }
    }

    private static final class LoopState {
        private final String start;
        private final String end;
        private boolean nextIsStart = true;
        private long lastSwitchAt;
        private Runnable runnable;

        private LoopState(String start, String end) {
            this.start = start;
            this.end = end;
        }
    }
}
