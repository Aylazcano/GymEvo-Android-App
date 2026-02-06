package com.example.gymevo.ui.common.sort;

public enum SortField {
    RECENT,
    POPULAR,
    NAME,
    MUSCLE,
    MUSCLE_GROUP,
    CREATED,
    UPDATED,
    CUSTOM;

    public static SortField from(String value, SortField fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return SortField.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
