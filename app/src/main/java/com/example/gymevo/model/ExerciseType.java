package com.example.gymevo.model;

import java.util.Locale;

public enum ExerciseType {
    ANAEROBIC("Anaerobic"),
    AEROBIC("Aerobic");

    private final String label;

    ExerciseType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ExerciseType fromLabel(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.US);
        for (ExerciseType type : values()) {
            if (type.label.toLowerCase(Locale.US).equals(normalized)) {
                return type;
            }
        }
        return null;
    }
}
