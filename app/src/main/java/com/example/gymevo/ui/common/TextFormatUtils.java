package com.example.gymevo.ui.common;

import android.text.TextUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared text formatting utilities for JSON ↔ display text conversions.
 * Used by adapters, dialogs, and formatters across the app.
 */
public final class TextFormatUtils {

    private TextFormatUtils() {
        // no instantiation
    }

    /** Returns the trimmed string or empty string if null. */
    public static String safeText(String value) {
        return value != null ? value.trim() : "";
    }

    /** Returns null if the value is null or blank, otherwise the trimmed value. */
    public static String emptyToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Converts a JSON array string (e.g. {@code ["a","b","c"]}) to comma-separated text.
     * If the input is not a JSON array, returns it as-is (trimmed).
     */
    public static String toEditableList(String rawValue) {
        if (TextUtils.isEmpty(rawValue)) {
            return "";
        }
        String trimmed = rawValue.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return trimmed;
        }
        try {
            JSONArray array = new JSONArray(trimmed);
            List<String> values = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i, "").trim();
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
            return TextUtils.join(", ", values);
        } catch (Exception ignored) {
            return trimmed;
        }
    }

    /**
     * Converts a JSON array string to newline-separated text for display.
     * If the input is not a JSON array, returns it as-is (trimmed).
     */
    public static String toDisplayLines(String rawValue) {
        if (TextUtils.isEmpty(rawValue)) {
            return "";
        }
        String trimmed = rawValue.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return trimmed;
        }
        try {
            JSONArray array = new JSONArray(trimmed);
            List<String> values = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i, "").trim();
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
            return TextUtils.join("\n", values);
        } catch (Exception ignored) {
            return trimmed;
        }
    }

    /**
     * Converts comma-separated text to a JSON array string.
     * Returns null if the input produces an empty array.
     */
    public static String toJsonArrayString(String rawValue) {
        String trimmed = safeText(rawValue);
        if (trimmed.isEmpty()) {
            return null;
        }
        String[] parts = trimmed.split(",");
        JSONArray array = new JSONArray();
        for (String part : parts) {
            String value = safeText(part);
            if (!value.isEmpty()) {
                array.put(value);
            }
        }
        return array.length() > 0 ? array.toString() : null;
    }

    /**
     * Wraps a single value as a JSON array string (e.g. "Chest" → {@code ["Chest"]}).
     * Returns null if the input is empty.
     */
    public static String toSingleItemJsonArray(String value) {
        String trimmed = safeText(value);
        if (trimmed.isEmpty()) {
            return null;
        }
        JSONArray array = new JSONArray();
        array.put(trimmed);
        return array.toString();
    }
}
