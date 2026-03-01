package com.example.gymevo.ui.common;

import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.Workout;
import com.example.gymevo.model.WorkoutWithExercises;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared mapping/sorting utilities for Workout → display data conversions.
 * Used by ViewModels and Repository to avoid duplicated mapping logic.
 */
public final class WorkoutMapper {

    private WorkoutMapper() {
        // no instantiation
    }

    /** Sorts exercises by their order index ascending. */
    public static List<ExerciseInWorkout> sortExercises(List<ExerciseInWorkout> exercises) {
        if (exercises == null || exercises.isEmpty()) {
            return exercises != null ? exercises : new ArrayList<>();
        }
        List<ExerciseInWorkout> sorted = new ArrayList<>(exercises);
        sorted.sort((a, b) -> Integer.compare(
                a != null ? a.getOrderIndex() : 0,
                b != null ? b.getOrderIndex() : 0));
        return sorted;
    }

    /** Returns the list as-is if non-null, otherwise an empty list. */
    public static List<ExerciseInWorkout> safeExercises(List<ExerciseInWorkout> exercises) {
        return exercises != null ? exercises : new ArrayList<>();
    }

    /** Maps a single WorkoutWithExercises to a Workout with sorted exercises. */
    public static Workout mapToWorkout(WorkoutWithExercises data) {
        if (data == null || data.workout == null) {
            return null;
        }
        Workout workout = data.workout;
        workout.setExercises(sortExercises(safeExercises(data.exercises)));
        return workout;
    }

    /** Maps a list of WorkoutWithExercises to a list of Workouts with sorted exercises. */
    public static List<Workout> mapToWorkouts(List<WorkoutWithExercises> items) {
        List<Workout> workouts = new ArrayList<>();
        if (items == null) {
            return workouts;
        }
        for (WorkoutWithExercises item : items) {
            Workout workout = mapToWorkout(item);
            if (workout != null) {
                workouts.add(workout);
            }
        }
        return workouts;
    }
}
