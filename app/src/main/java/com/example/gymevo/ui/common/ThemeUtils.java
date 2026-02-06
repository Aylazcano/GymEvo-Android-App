package com.example.gymevo.ui.common;

import com.example.gymevo.R;

public final class ThemeUtils {

    public static final String THEME_DEFAULT = "default";
    public static final String THEME_RED = "red";
    public static final String THEME_GREEN = "green";
    public static final String THEME_BLUE = "blue";

    private ThemeUtils() {
    }

    public static int getThemeResId(String themeName) {
        if (THEME_RED.equalsIgnoreCase(themeName)) {
            return R.style.Theme_GymEvo_Red;
        }
        if (THEME_GREEN.equalsIgnoreCase(themeName)) {
            return R.style.Theme_GymEvo_Green;
        }
        if (THEME_BLUE.equalsIgnoreCase(themeName)) {
            return R.style.Theme_GymEvo_Blue;
        }
        return R.style.Theme_GymEvo;
    }
}
