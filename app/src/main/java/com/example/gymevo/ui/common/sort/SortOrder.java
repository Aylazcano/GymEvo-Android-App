package com.example.gymevo.ui.common.sort;

public enum SortOrder {
    ASC,
    DESC;

    public static SortOrder from(String value, SortOrder fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return SortOrder.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
