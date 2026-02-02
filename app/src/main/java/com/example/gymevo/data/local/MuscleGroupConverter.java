package com.example.gymevo.data.local;

import androidx.room.TypeConverter;

import com.example.gymevo.model.MuscleGroup;

public class MuscleGroupConverter {
    private MuscleGroupConverter() {
    }

    @TypeConverter
    public static MuscleGroup fromString(String value) {
        if (value == null) {
            return null;
        }
        MuscleGroup direct = tryValueOf(value);
        if (direct != null) {
            return direct;
        }
        MuscleGroup group = MuscleGroup.fromLabel(value);
        return group != null ? group : MuscleGroup.OTHER;
    }

    @TypeConverter
    public static String toString(MuscleGroup group) {
        return group == null ? null : group.name();
    }

    private static MuscleGroup tryValueOf(String value) {
        try {
            return MuscleGroup.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
