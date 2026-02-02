package com.example.gymevo.data.local;

import androidx.room.TypeConverter;

import com.example.gymevo.model.Workout;

public class WorkoutTypeConverter {
    private WorkoutTypeConverter() {
    }

    @TypeConverter
    public static Workout.WorkoutType fromString(String value) {
        return value == null ? null : Workout.WorkoutType.valueOf(value);
    }

    @TypeConverter
    public static String toString(Workout.WorkoutType type) {
        return type == null ? null : type.name();
    }
}
