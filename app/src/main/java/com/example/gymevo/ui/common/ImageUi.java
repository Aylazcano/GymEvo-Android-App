package com.example.gymevo.ui.common;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.gymevo.R;

public final class ImageUi {

    private static final long LOOP_INTERVAL_MS = 1000L;

    private ImageUi() {
    }

    public static void loadExerciseImage(ImageView imageView, String imagePath) {
        if (imageView == null) {
            return;
        }
        if (TextUtils.isEmpty(imagePath)) {
            imageView.setImageResource(R.drawable.ic_menu_gallery);
            return;
        }

        String value = mapLegacyName(imagePath.trim());
        boolean isRemote = isRemoteUri(value);

        if (!isRemote) {
            int drawableId = resolveDrawableId(imageView.getContext(), value);
            if (drawableId != 0) {
                Glide.with(imageView)
                        .load(drawableId)
                        .placeholder(R.drawable.ic_menu_gallery)
                        .error(R.drawable.ic_menu_gallery)
                        .centerCrop()
                        .into(imageView);
                return;
            }
        }

        Glide.with(imageView)
                .load(value)
                .placeholder(R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(imageView);
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

    private static int resolveDrawableId(Context context, String rawValue) {
        if (context == null || TextUtils.isEmpty(rawValue)) {
            return 0;
        }
        String clean = normalize(rawValue);
        int extensionIndex = clean.lastIndexOf('.');
        if (extensionIndex > 0) {
            clean = clean.substring(0, extensionIndex);
        }
        clean = clean.replace('-', '_');
        return context.getResources().getIdentifier(clean, "drawable", context.getPackageName());
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
        switch (clean) {
            case "squat_start":
            case "squat_end":
                return "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80";
            case "bench_press_start":
            case "bench_press_end":
                return "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80";
            case "deadlift_start":
            case "deadlift_end":
                return "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=800&q=80";
            case "shoulder_press_start":
            case "shoulder_press_end":
                return "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=800&q=80";
            case "pull_up_start":
            case "pull_up_end":
                return "https://images.unsplash.com/photo-1483721310020-03333e577078?auto=format&fit=crop&w=800&q=80";
            case "bicep_curl_start":
            case "bicep_curl_end":
                return "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=800&q=80";
            case "tricep_dip_start":
            case "tricep_dip_end":
                return "https://images.unsplash.com/photo-1517832606294-6c5b7f4f49f9?auto=format&fit=crop&w=800&q=80";
            case "plank_start":
            case "plank_end":
                return "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0?auto=format&fit=crop&w=800&q=80";
            case "leg_press_start":
            case "leg_press_end":
                return "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=800&q=80";
            case "lunge_start":
            case "lunge_end":
                return "https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=800&q=80";
            case "chest_fly_start":
            case "chest_fly_end":
                return "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80";
            case "incline_bench_press_start":
            case "incline_bench_press_end":
                return "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80";
            default:
                return rawValue;
        }
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

    private static boolean isRemoteUri(String value) {
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        String lower = value.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("content://") || lower.startsWith("file://")
                || lower.startsWith("android.resource://");
    }

    private static String trimOrNull(String value) {
        return value != null ? value.trim() : null;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase();
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
