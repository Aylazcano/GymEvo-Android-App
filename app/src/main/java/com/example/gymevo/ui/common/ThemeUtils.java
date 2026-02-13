package com.example.gymevo.ui.common;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.gymevo.R;

import java.util.Locale;

public final class ThemeUtils {

    private ThemeUtils() {
    }

    /**
     * Maps persisted theme name to a concrete style resource.
     */
    public static int getThemeResId(@NonNull Context context, @NonNull String themeName) {
        if (themeName == null) {
            return R.style.Theme_GymEvo;
        }

        switch (themeName.trim().toLowerCase(Locale.ROOT)) {
            case "red":
                return R.style.Theme_GymEvo_Red;
            case "green":
                return R.style.Theme_GymEvo_Green;
            case "blue":
                return R.style.Theme_GymEvo_Blue;
            case "gray":
                return R.style.Theme_GymEvo_Gray;
            case "default":
            default:
                return R.style.Theme_GymEvo;
        }
    }
}
