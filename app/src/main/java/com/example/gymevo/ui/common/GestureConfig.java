package com.example.gymevo.ui.common;

/**
 * Central place for gesture-related configuration constants.
 * Keeps navigation order and other static config out of activities.
 */
public final class GestureConfig {
    public static final int[] DEFAULT_NAV_ORDER = new int[] {
            com.example.gymevo.R.id.nav_workout_tracker,
            com.example.gymevo.R.id.nav_statistics,
            com.example.gymevo.R.id.nav_exercises_list,
            com.example.gymevo.R.id.nav_workout_list,
            com.example.gymevo.R.id.nav_notes,
            com.example.gymevo.R.id.nav_body_status,
            com.example.gymevo.R.id.nav_calorie_tracker
    };

    private GestureConfig() {
        // no instances
    }
}
