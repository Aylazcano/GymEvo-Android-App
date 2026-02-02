package com.example.gymevo.data.seed;

import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.Workout;
import com.example.gymevo.model.Workout.WorkoutType;

import java.util.ArrayList;
import java.util.List;

public class WorkoutSeed {

    private static long nextExerciseInWorkoutId = 1;
    private static final int DEFAULT_SERIES = 3;
    private static final int DEFAULT_REPS = 10;
    private static final int DEFAULT_TIME = 60;

    private WorkoutSeed() {
    }

    public static List<Workout> generateWorkouts() {
        List<Workout> workouts = new ArrayList<>();
        List<Workout> starredWorkouts = generateTemplates();

        // Only keep curated starred workouts to avoid long numeric sequences
        workouts.addAll(starredWorkouts);
        return workouts;
    }

    private static List<Workout> generateTemplates() {
        List<Workout> templates = new ArrayList<>();
        List<Exercise> exercises = generateExercises();

        templates.add(createTemplate(1L, "Full Body Workout", true, exercises, new int[][]{
            {0, 3, 12, 60, 45, 100},
            {1, 3, 10, 70, 60, 110},
            {2, 3, 8, 90, 60, 120}
        }));

        templates.add(createTemplate(2L, "Leg Day", true, exercises, new int[][]{
            {0, 4, 10, 100, 60, 120},
            {8, 3, 12, 80, 90, 130},
            {9, 3, 12, 60, 60, 115}
        }));

        templates.add(createTemplate(3L, "Chest Day", true, exercises, new int[][]{
            {1, 3, 10, 70, 60, 110},
            {10, 3, 12, 50, 60, 105},
            {11, 3, 10, 60, 60, 112}
        }));

        templates.add(createTemplate(4L, "Back Day", true, exercises, new int[][]{
            {2, 3, 8, 100, 60, 125},
            {4, 3, 10, 0, 45, 115},
            {5, 3, 12, 35, 45, 105}
        }));

        templates.add(createTemplate(5L, "Shoulders & Arms", false, exercises, new int[][]{
            {3, 3, 10, 45, 60, 108},
            {5, 3, 12, 25, 45, 100},
            {6, 3, 12, 0, 45, 102}
        }));

        templates.add(createTemplate(6L, "Core & Cardio", false, exercises, new int[][]{
            {7, DEFAULT_SERIES, 1, 0, DEFAULT_TIME, 120}
        }));

        return templates;
    }

    public static List<Exercise> generateExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("Squat", "Legs",
            "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80",
            true));
        exercises.add(new Exercise("Bench Press", "Chest",
            "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=1200&q=80",
            true));
        exercises.add(new Exercise("Deadlift", "Back",
            "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Shoulder Press", "Shoulders",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Pull-up", "Back",
            "https://images.unsplash.com/photo-1483721310020-03333e577078?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1483721310020-03333e577078?auto=format&fit=crop&w=1200&q=80",
            true));
        exercises.add(new Exercise("Bicep Curl", "Arms",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Tricep Dip", "Arms",
            "https://images.unsplash.com/photo-1517832606294-6c5b7f4f49f9?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517832606294-6c5b7f4f49f9?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Plank", "Abs",
            "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0?auto=format&fit=crop&w=1200&q=80",
            true));
        exercises.add(new Exercise("Leg Press", "Legs",
            "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=1200&q=80",
            true));
        exercises.add(new Exercise("Lunge", "Legs",
            "https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Chest Fly", "Chest",
            "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80",
            true));
        exercises.add(new Exercise("Incline Bench Press", "Chest",
            "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=1200&q=80",
            true));
        exercises.add(new Exercise("Romanian Deadlift", "Hamstrings",
            "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Leg Extension", "Quadriceps",
            "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Leg Curl", "Hamstrings",
            "https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Calf Raise", "Calves",
            "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Hip Thrust", "Glutes",
            "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Glute Bridge", "Glutes",
            "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Lat Pulldown", "Lats",
            "https://images.unsplash.com/photo-1483721310020-03333e577078?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1483721310020-03333e577078?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Bent-Over Row", "Back",
            "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Seated Cable Row", "Middle back",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Face Pull", "Rear delts",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Lateral Raise", "Lateral delts",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Front Raise", "Front delts",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Shrug", "Traps",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Crunch", "Abs",
            "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Russian Twist", "Obliques",
            "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Hanging Leg Raise", "Abs",
            "https://images.unsplash.com/photo-1483721310020-03333e577078?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1483721310020-03333e577078?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Cable Woodchop", "Obliques",
            "https://images.unsplash.com/photo-1517832606294-6c5b7f4f49f9?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517832606294-6c5b7f4f49f9?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Tricep Pushdown", "Triceps",
            "https://images.unsplash.com/photo-1517832606294-6c5b7f4f49f9?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517832606294-6c5b7f4f49f9?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Overhead Tricep Extension", "Triceps",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Hammer Curl", "Biceps",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Preacher Curl", "Biceps",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Wrist Curl", "Forearms",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Farmer's Carry", "Forearms",
            "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Hip Abduction", "Abductors",
            "https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Hip Adduction", "Adductors",
            "https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1514996937319-344454492b37?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Good Morning", "Lower back",
            "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Back Extension", "Lower back",
            "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Push-up", "Chest",
            "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Incline Dumbbell Press", "Upper chest",
            "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Decline Press", "Lower chest",
            "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=1200&q=80",
            false));
        exercises.add(new Exercise("Neck Flexion", "Neck",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1517960413843-0aee8e2d471c?auto=format&fit=crop&w=1200&q=80",
            false));
        return exercises;
    }

    private static Workout createTemplate(long id, String name, boolean starred,
                                          List<Exercise> exercises, int[][] plan) {
        Workout workout = new Workout(id, name, null, starred, WorkoutType.TEMPLATE);
        if (plan == null || exercises == null) {
            return workout;
        }

        for (int[] spec : plan) {
            if (spec == null || spec.length < 6) {
                continue;
            }
            int exerciseIndex = spec[0];
            if (exerciseIndex < 0 || exerciseIndex >= exercises.size()) {
                continue;
            }
            workout.addExercise(createExerciseInWorkout(nextExerciseInWorkoutId++,
                    exercises.get(exerciseIndex),
                    spec[1],
                    spec[2],
                    spec[3],
                    spec[4],
                    spec[5],
                    workout.getId()));
        }

        return workout;
    }

    private static ExerciseInWorkout createExerciseInWorkout(long id, Exercise exercise, int series, int repetitions,
                                                             int weight, int time, int heartRates, Long workoutId) {
        return new ExerciseInWorkout(
                id,
                exercise.getName(),
            exercise.getTargetedMusclesLabel(),
                exercise.getImageA(),
                exercise.getImageB(),
                series,
                repetitions,
                weight,
                time,
                heartRates,
                exercise.getId(),
                workoutId
        );
    }

}
