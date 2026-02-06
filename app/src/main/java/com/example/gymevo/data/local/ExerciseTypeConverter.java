package com.example.gymevo.data.local;

import androidx.room.TypeConverter;

import com.example.gymevo.model.ExerciseType;

public class ExerciseTypeConverter {

    @TypeConverter
    public static String fromType(ExerciseType type) {
        return type != null ? type.name() : null;
    }

    @TypeConverter
    public static ExerciseType toType(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ExerciseType.ANAEROBIC;
        }
        try {
            return ExerciseType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return ExerciseType.ANAEROBIC;
        }
    }
}
