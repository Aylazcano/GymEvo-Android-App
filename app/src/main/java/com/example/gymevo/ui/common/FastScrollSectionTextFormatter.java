package com.example.gymevo.ui.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.Workout;
import com.example.gymevo.ui.common.sort.SortField;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

public final class FastScrollSectionTextFormatter {

    private FastScrollSectionTextFormatter() {
    }

    @NonNull
    public static String forWorkout(@Nullable Workout workout, @Nullable SortField sortField) {
        if (workout == null) {
            return "";
        }
        SortField safeSortField = sortField != null ? sortField : SortField.RECENT;
        switch (safeSortField) {
            case CUSTOM:
                return "";
            case RECENT:
                return "";
            case POPULAR:
                return "";
            case MUSCLE:
            case MUSCLE_GROUP:
                return buildMuscleIndexText(firstWorkoutMuscle(workout), workout.getName());
            case NAME:
            default:
                return firstLetter(workout.getName());
        }
    }

    @NonNull
    public static String forExercise(@Nullable Exercise exercise, @Nullable SortField sortField) {
        if (exercise == null) {
            return "";
        }
        SortField safeSortField = sortField != null ? sortField : SortField.RECENT;
        switch (safeSortField) {
            case CUSTOM:
                return "";
            case RECENT:
                return "";
            case POPULAR:
                return "";
            case MUSCLE:
            case MUSCLE_GROUP:
                return buildMuscleIndexText(exercise.getTargetedMusclesLabel(), exercise.getName());
            case NAME:
            default:
                return firstLetter(exercise.getName());
        }
    }

    public static int workoutPopularityPoints(@Nullable Workout workout) {
        if (workout == null) {
            return 0;
        }
        List<ExerciseInWorkout> exercises = workout.getExercises();
        return exercises != null ? exercises.size() : 0;
    }

    public static int exercisePopularityPoints(@Nullable Exercise exercise) {
        if (exercise == null) {
            return 0;
        }
        return exercise.isStar() ? 1 : 0;
    }

    @NonNull
    public static String normalizeMuscleLabel(@Nullable String muscleLabel) {
        return TextFormatUtils.safeText(muscleLabel).toLowerCase(Locale.ROOT);
    }

    @NonNull
    private static String firstWorkoutMuscle(@NonNull Workout workout) {
        List<ExerciseInWorkout> exercises = workout.getExercises();
        if (exercises == null || exercises.isEmpty()) {
            return "";
        }
        for (ExerciseInWorkout exercise : exercises) {
            if (exercise != null && exercise.getTargetedMusclesLabel() != null
                    && !exercise.getTargetedMusclesLabel().trim().isEmpty()) {
                return exercise.getTargetedMusclesLabel();
            }
        }
        return "";
    }

    @NonNull
    private static String firstLetter(@Nullable String value) {
        String safe = TextFormatUtils.safeText(value);
        if (safe.isEmpty()) {
            return "";
        }
        return safe.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    @NonNull
    private static String buildMuscleIndexText(@Nullable String muscleLabel, @Nullable String itemName) {
        String muscle = TextFormatUtils.safeText(muscleLabel);
        String letter = firstLetter(itemName);
        if (muscle.isEmpty()) {
            return letter;
        }
        if (letter.isEmpty()) {
            return muscle;
        }
        return muscle + " - " + letter;
    }

    @NonNull
    private static String formatDate(long epochMillis) {
        if (epochMillis <= 0L) {
            return "";
        }
        LocalDate date = Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return date.toString();
    }
}