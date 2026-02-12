package com.example.gymevo.data.seed;

import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.ExerciseType;
import com.example.gymevo.model.MuscleGroup;
import com.example.gymevo.model.Workout;
import com.example.gymevo.model.Workout.WorkoutType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutSeed {

    private static long nextExerciseInWorkoutId = 1;
    private static final int DEFAULT_SERIES = 3;
    private static final int DEFAULT_REPS = 10;
    private static final int DEFAULT_TIME = 60;
    private static final int CARDIO_30_MINS_TIME_SECONDS = 1800;

    private WorkoutSeed() {
    }

    public static List<Workout> generateWorkouts() {
        List<Workout> workouts = new ArrayList<>();
        List<Workout> starredWorkouts = generateTemplates();
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

        templates.add(createTemplate(7L, "Dumbbell Day", false, exercises, new int[][]{
                {43, 3, 10, 20, 60, 110},
                {44, 3, 10, 20, 60, 110},
                {45, 3, 10, 18, 60, 110},
                {49, 3, 10, 16, 60, 110},
                {50, 3, 10, 18, 60, 110}
        }));

        templates.add(createTemplate(8L, "Cardio Endurance", false, exercises, new int[][]{
                {76, 1, 1, 0, 900, 140},
                {78, 1, 1, 0, 900, 135},
                {79, 1, 1, 0, 900, 145}
        }));

        templates.add(createTemplate(9L, "Machine Only", false, exercises, new int[][]{
                {findExerciseIndex(exercises, "Leg Press"), 3, 12, 90, 60, 120},
                {findExerciseIndex(exercises, "Chest Press Machine"), 3, 10, 60, 60, 110},
                {findExerciseIndex(exercises, "Seated Row Machine"), 3, 12, 55, 60, 110},
                {findExerciseIndex(exercises, "Shoulder Press Machine"), 3, 10, 45, 60, 110},
                {findExerciseIndex(exercises, "Leg Extension"), 3, 12, 50, 60, 110},
                {findExerciseIndex(exercises, "Seated Leg Curl Machine"), 3, 12, 45, 60, 110},
                {findExerciseIndex(exercises, "Pec Deck Machine"), 3, 12, 45, 60, 110}
        }));

        templates.add(createTemplate(10L, "Non-Machine Strength", false, exercises, new int[][]{
                {findExerciseIndex(exercises, "Squat"), 3, 10, 90, 60, 120},
                {findExerciseIndex(exercises, "Bench Press"), 3, 10, 70, 60, 110},
                {findExerciseIndex(exercises, "Deadlift"), 3, 8, 100, 60, 125},
                {findExerciseIndex(exercises, "Pull-up"), 3, 8, 0, 60, 110},
                {findExerciseIndex(exercises, "Push-up"), 3, 12, 0, 60, 110},
                {findExerciseIndex(exercises, "Plank"), 3, 1, 0, 60, 0}
        }));

        // --- NEW WORKOUTS (ID 11 - 20) ---
        // 11L: PHASE 1 - Dumbbell Chest & Free Weight Biceps
        templates.add(createTemplate(11L, "Ph1: Dumbbell Chest & Free Biceps", true, exercises, new int[][]{
                {findExerciseIndex(exercises, "Flat Dumbbell Bench Press"), 3, 10, 30, 60, 110},
                {findExerciseIndex(exercises, "Incline Dumbbell Press"), 3, 10, 26, 60, 110},
                {findExerciseIndex(exercises, "Dumbbell Pullover"), 3, 12, 24, 60, 110},
                {findExerciseIndex(exercises, "Push-up"), 3, 15, 0, 45, 105},
                {findExerciseIndex(exercises, "Barbell Curl"), 3, 10, 30, 45, 105},
                {findExerciseIndex(exercises, "Hammer Curl"), 3, 12, 16, 45, 105},
                {findExerciseIndex(exercises, "Bicep Curl"), 3, 12, 14, 45, 105},
                {findExerciseIndex(exercises, "Plank"), 3, 1, 0, DEFAULT_TIME, 0},
                {findExerciseIndex(exercises, "Russian Twist"), 3, 20, 0, 45, 100},
                {findExerciseIndex(exercises, "Treadmill Run"), 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        // 12L: PHASE 2 - Barbell Chest & Machine Biceps
        templates.add(createTemplate(12L, "Ph2: Barbell Chest & Machine Biceps", false, exercises, new int[][]{
                {findExerciseIndex(exercises, "Bench Press"), 3, 8, 70, 90, 115},
                {findExerciseIndex(exercises, "Incline Bench Press"), 3, 10, 60, 90, 115},
                {findExerciseIndex(exercises, "Decline Press"), 3, 10, 60, 90, 115},
                {findExerciseIndex(exercises, "Cable Chest Fly"), 3, 15, 15, 45, 105},
                {findExerciseIndex(exercises, "Cable Biceps Curl"), 3, 12, 25, 45, 105},
                {findExerciseIndex(exercises, "Preacher Curl"), 3, 12, 25, 45, 105},
                {findExerciseIndex(exercises, "Assisted Pull-up Machine"), 3, 10, 0, 60, 110},
                {findExerciseIndex(exercises, "Plank"), 3, 1, 0, DEFAULT_TIME, 0},
                {findExerciseIndex(exercises, "Cable Woodchop"), 3, 15, 20, 45, 100},
                {findExerciseIndex(exercises, "Treadmill Run"), 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        // 13L: Dumbbell/Barbell/Bodyweight
        templates.add(createTemplate(13L, "Barbell/Dumbbell Back & Triceps", true, exercises, new int[][]{
                {findExerciseIndex(exercises, "Deadlift"), 3, 8, 110, 90, 125},
                {findExerciseIndex(exercises, "Pull-up"), 3, 8, 0, 60, 110},
                {findExerciseIndex(exercises, "Barbell Row"), 3, 10, 70, 60, 110},
                {findExerciseIndex(exercises, "One-Arm Dumbbell Row"), 3, 10, 35, 60, 110},
                {findExerciseIndex(exercises, "Skull Crusher"), 3, 10, 20, 45, 105},
                {findExerciseIndex(exercises, "Tricep Dip"), 3, 12, 0, 45, 105},
                {findExerciseIndex(exercises, "Overhead Tricep Extension"), 3, 12, 15, 45, 105},
                {findExerciseIndex(exercises, "Plank"), 3, 1, 0, DEFAULT_TIME, 0},
                {findExerciseIndex(exercises, "Treadmill Run"), 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        // 14L: Machine/Cable Only
        templates.add(createTemplate(14L, "Machine Back & Triceps", false, exercises, new int[][]{
                {findExerciseIndex(exercises, "Lat Pulldown"), 3, 10, 60, 60, 110},
                {findExerciseIndex(exercises, "Seated Row Machine"), 3, 10, 55, 60, 110},
                {findExerciseIndex(exercises, "T-Bar Row"), 3, 10, 65, 60, 110},
                {findExerciseIndex(exercises, "Assisted Pull-up Machine"), 3, 10, 0, 60, 110},
                {findExerciseIndex(exercises, "Tricep Pushdown"), 3, 12, 30, 45, 105},
                {findExerciseIndex(exercises, "Cable Overhead Triceps Extension"), 3, 12, 20, 45, 105},
                {findExerciseIndex(exercises, "Assisted Dip Machine"), 3, 12, 0, 45, 105},
                {findExerciseIndex(exercises, "Plank"), 3, 1, 0, DEFAULT_TIME, 0},
                {findExerciseIndex(exercises, "Treadmill Run"), 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        // 15L: Dumbbell/Barbell/Bodyweight
        templates.add(createTemplate(15L, "Barbell/Dumbbell Legs & Shoulders", true, exercises, new int[][]{
                {findExerciseIndex(exercises, "Squat"), 4, 10, 95, 60, 120},
                {findExerciseIndex(exercises, "Lunge"), 3, 10, 15, 60, 115},
                {findExerciseIndex(exercises, "Front Squat"), 3, 8, 80, 60, 120},
                {findExerciseIndex(exercises, "Romanian Deadlift"), 3, 10, 70, 60, 115},
                {findExerciseIndex(exercises, "Shoulder Press"), 3, 10, 40, 60, 110},
                {findExerciseIndex(exercises, "Lateral Raise"), 3, 12, 10, 45, 105},
                {findExerciseIndex(exercises, "Front Raise"), 3, 12, 10, 45, 105},
                {findExerciseIndex(exercises, "Plank"), 3, 1, 0, DEFAULT_TIME, 0},
                {findExerciseIndex(exercises, "Cable Woodchop"), 3, 15, 20, 45, 100},
                {findExerciseIndex(exercises, "Treadmill Run"), 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        // 16L: Machine/Cable Only
        templates.add(createTemplate(16L, "Machine Legs & Shoulders", false, exercises, new int[][]{
                {findExerciseIndex(exercises, "Leg Press"), 4, 12, 120, 60, 120},
                {findExerciseIndex(exercises, "Leg Extension"), 3, 12, 50, 60, 115},
                {findExerciseIndex(exercises, "Seated Leg Curl Machine"), 3, 12, 45, 60, 115},
                {findExerciseIndex(exercises, "Hack Squat Machine"), 3, 10, 80, 60, 120},
                {findExerciseIndex(exercises, "Shoulder Press Machine"), 3, 10, 45, 60, 110},
                {findExerciseIndex(exercises, "Cable Lateral Raise"), 3, 12, 10, 45, 105},
                {findExerciseIndex(exercises, "Reverse Fly Machine"), 3, 12, 40, 45, 105},
                {findExerciseIndex(exercises, "Plank"), 3, 1, 0, DEFAULT_TIME, 0},
                {findExerciseIndex(exercises, "Russian Twist"), 3, 15, 0, 45, 100},
                {findExerciseIndex(exercises, "Treadmill Run"), 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        // 17L: Dumbbell/Barbell/Bodyweight
        templates.add(createTemplate(17L, "Barbell/Dumbbell Glutes & Biceps", true, exercises, new int[][]{
                {findExerciseIndex(exercises, "Hip Thrust"), 3, 10, 80, 60, 115},
                {findExerciseIndex(exercises, "Lunge"), 3, 10, 15, 60, 110},
                {findExerciseIndex(exercises, "Dumbbell Romanian Deadlift"), 3, 10, 20, 60, 110},
                {findExerciseIndex(exercises, "Barbell Curl"), 3, 10, 30, 45, 105},
                {findExerciseIndex(exercises, "Hammer Curl"), 3, 12, 15, 45, 105},
                {findExerciseIndex(exercises, "Bicep Curl"), 3, 12, 10, 45, 105},
                {findExerciseIndex(exercises, "Plank"), 3, 1, 0, DEFAULT_TIME, 0},
                {findExerciseIndex(exercises, "Russian Twist"), 3, 15, 0, 45, 100},
                {findExerciseIndex(exercises, "Treadmill Run"), 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        // 18L: Machine/Cable Only
        templates.add(createTemplate(18L, "Machine Glutes & Biceps", false, exercises, new int[][]{
                {findExerciseIndex(exercises, "Hip Thrust"), 3, 10, 80, 60, 115},
                {findExerciseIndex(exercises, "Cable Pull-through"), 3, 12, 40, 60, 110},
                {findExerciseIndex(exercises, "Cable Glute Kickback"), 3, 15, 20, 45, 105},
                {findExerciseIndex(exercises, "Cable Biceps Curl"), 3, 12, 25, 45, 105},
                {findExerciseIndex(exercises, "Preacher Curl"), 3, 10, 30, 45, 105},
                {findExerciseIndex(exercises, "Assisted Pull-up Machine"), 3, 10, 0, 60, 105},
                {findExerciseIndex(exercises, "Plank"), 3, 1, 0, DEFAULT_TIME, 0},
                {findExerciseIndex(exercises, "Cable Woodchop"), 3, 15, 20, 45, 100},
                {findExerciseIndex(exercises, "Treadmill Run"), 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        // 19L: Dumbbell/Barbell/Bodyweight
        templates.add(createTemplate(19L, "Barbell/Dumbbell Glutes & Shoulders", true, exercises, new int[][]{
                {findExerciseIndex(exercises, "Hip Thrust"), 3, 10, 80, 60, 115},
                {findExerciseIndex(exercises, "Lunge"), 3, 10, 15, 60, 110},
                {findExerciseIndex(exercises, "Dumbbell Romanian Deadlift"), 3, 10, 20, 60, 110},
                {findExerciseIndex(exercises, "Shoulder Press"), 3, 10, 40, 60, 110},
                {findExerciseIndex(exercises, "Lateral Raise"), 3, 12, 10, 45, 105},
                {findExerciseIndex(exercises, "Front Raise"), 3, 12, 10, 45, 105},
                {findExerciseIndex(exercises, "Plank"), 3, 1, 0, DEFAULT_TIME, 0},
                {findExerciseIndex(exercises, "Cable Woodchop"), 3, 15, 20, 45, 100},
                {findExerciseIndex(exercises, "Treadmill Run"), 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        // 20L: Machine/Cable Only
        templates.add(createTemplate(20L, "Machine Glutes & Shoulders", false, exercises, new int[][]{
                {findExerciseIndex(exercises, "Hip Thrust"), 3, 10, 80, 60, 115},
                {findExerciseIndex(exercises, "Cable Pull-through"), 3, 12, 40, 60, 110},
                {findExerciseIndex(exercises, "Cable Glute Kickback"), 3, 15, 20, 45, 105},
                {findExerciseIndex(exercises, "Shoulder Press Machine"), 3, 10, 45, 60, 110},
                {findExerciseIndex(exercises, "Cable Lateral Raise"), 3, 12, 10, 45, 105},
                {findExerciseIndex(exercises, "Reverse Fly Machine"), 3, 12, 40, 45, 105},
                {findExerciseIndex(exercises, "Plank"), 3, 1, 0, DEFAULT_TIME, 0},
                {findExerciseIndex(exercises, "Russian Twist"), 3, 15, 0, 45, 100},
                {findExerciseIndex(exercises, "Treadmill Run"), 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        return templates;
    }

    public static List<Exercise> generateExercises() {
        List<Exercise> exercises = new ArrayList<>();

        // --- LEGS ---
        exercises.add(new Exercise("Squat", "Legs",
                "https://images.unsplash.com/photo-1574680096141-1cddd32e04ca?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1574680096141-1cddd32e04ca?auto=format&fit=crop&w=1200&q=80",
                true));
        exercises.add(new Exercise("Leg Press", "Legs",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=1200&q=80",
                true));
        exercises.add(new Exercise("Lunge", "Legs",
                "https://images.unsplash.com/photo-1575052814086-f385e2e2ad1b?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1575052814086-f385e2e2ad1b?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Leg Extension", "Quadriceps",
                "https://images.pexels.com/photos/1552242/pexels-photo-1552242.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/1552242/pexels-photo-1552242.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Leg Curl", "Hamstrings",
                "https://images.pexels.com/photos/1954524/pexels-photo-1954524.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/1954524/pexels-photo-1954524.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Calf Raise", "Calves",
                "https://images.pexels.com/photos/3837781/pexels-photo-3837781.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/3837781/pexels-photo-3837781.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Goblet Squat", "Legs",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Bulgarian Split Squat", "Legs",
                "https://images.pexels.com/photos/1954524/pexels-photo-1954524.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/1954524/pexels-photo-1954524.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Front Squat", "Quadriceps",
                "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Hack Squat Machine", "Quadriceps",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Smith Machine Squat", "Legs",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Seated Leg Curl Machine", "Hamstrings",
                "https://images.pexels.com/photos/1954524/pexels-photo-1954524.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/1954524/pexels-photo-1954524.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Step-up", "Legs",
                "https://images.unsplash.com/photo-1575052814086-f385e2e2ad1b?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1575052814086-f385e2e2ad1b?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Hip Abduction", "Abductors",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Hip Adduction", "Adductors",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=1200&q=80",
                false));

        // --- CHEST ---
        exercises.add(new Exercise("Bench Press", "Chest",
                "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&w=1200&q=80",
                true));
        exercises.add(new Exercise("Chest Fly", "Chest",
                "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?auto=format&fit=crop&w=1200&q=80",
                true));
        exercises.add(new Exercise("Incline Bench Press", "Chest",
                "https://images.pexels.com/photos/3838389/pexels-photo-3838389.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/3838389/pexels-photo-3838389.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                true));
        exercises.add(new Exercise("Push-up", "Chest",
                "https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Incline Dumbbell Press", "Upper chest",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Decline Press", "Lower chest",
                "https://images.pexels.com/photos/3838389/pexels-photo-3838389.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/3838389/pexels-photo-3838389.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Flat Dumbbell Bench Press", "Chest",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Dumbbell Pullover", "Chest",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Cable Chest Fly", "Chest",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Pec Deck Machine", "Chest",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Chest Press Machine", "Chest",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=1200&q=80",
                false));

        // --- BACK ---
        exercises.add(new Exercise("Deadlift", "Back",
                "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Pull-up", "Back",
                "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?auto=format&fit=crop&w=1200&q=80",
                true));
        exercises.add(new Exercise("Lat Pulldown", "Lats",
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Bent-Over Row", "Back",
                "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Seated Cable Row", "Middle back",
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("One-Arm Dumbbell Row", "Lats",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Barbell Row", "Back",
                "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("T-Bar Row", "Middle back",
                "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Seated Row Machine", "Middle back",
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Assisted Pull-up Machine", "Back",
                "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Good Morning", "Lower back",
                "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Back Extension", "Lower back",
                "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1517838277536-f5f99be50135?auto=format&fit=crop&w=1200&q=80",
                false));

        // --- SHOULDERS ---
        exercises.add(new Exercise("Shoulder Press", "Shoulders",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Face Pull", "Rear delts",
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Lateral Raise", "Lateral delts",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Front Raise", "Front delts",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Shrug", "Traps",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Dumbbell Shoulder Press", "Shoulders",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Arnold Press", "Front delts",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1532029837206-abbe2b7a4bdd?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Cable Lateral Raise", "Lateral delts",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Shoulder Press Machine", "Shoulders",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Reverse Fly Machine", "Rear delts",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Neck Flexion", "Neck",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));

        // --- ARMS (BICEPS/TRICEPS/FOREARMS) ---
        exercises.add(new Exercise("Bicep Curl", "Arms",
                "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Tricep Dip", "Arms",
                "https://images.pexels.com/photos/3838389/pexels-photo-3838389.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/3838389/pexels-photo-3838389.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Tricep Pushdown", "Triceps",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Overhead Tricep Extension", "Triceps",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Hammer Curl", "Biceps",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Preacher Curl", "Biceps",
                "https://images.pexels.com/photos/3838389/pexels-photo-3838389.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/3838389/pexels-photo-3838389.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Wrist Curl", "Forearms",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Farmer's Carry", "Forearms",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Barbell Curl", "Biceps",
                "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Skull Crusher", "Triceps",
                "https://images.pexels.com/photos/3838389/pexels-photo-3838389.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/3838389/pexels-photo-3838389.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Cable Biceps Curl", "Biceps",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Assisted Dip Machine", "Triceps",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Cable Overhead Triceps Extension", "Triceps",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));

        // --- GLUTES / HIPS ---
        // User requested this specific Unsplash image for Hip Thrust
        exercises.add(new Exercise("Hip Thrust", "Glutes",
                "https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Glute Bridge", "Glutes",
                "https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Romanian Deadlift", "Hamstrings",
                "https://images.unsplash.com/photo-1567598008481-a639b07d3c96?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1567598008481-a639b07d3c96?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Dumbbell Romanian Deadlift", "Hamstrings",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164761/pexels-photo-4164761.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Cable Pull-through", "Glutes",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Cable Glute Kickback", "Glutes",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4164766/pexels-photo-4164766.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));

        // --- CORE ---
        exercises.add(new Exercise("Plank", "Abs",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                true));
        exercises.add(new Exercise("Crunch", "Abs",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Russian Twist", "Obliques",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Hanging Leg Raise", "Abs",
                "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Cable Woodchop", "Obliques",
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Machine Ab Crunch", "Abs",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=1200&q=80",
                false));
        exercises.add(new Exercise("Lying Leg Raise", "Abs",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));
        exercises.add(new Exercise("Ab Wheel Rollout", "Abs",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=800",
                "https://images.pexels.com/photos/4162451/pexels-photo-4162451.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                false));

        // --- CARDIO ---
        exercises.add(new Exercise("Treadmill Run", MuscleGroup.FULL_BODY,
                "https://images.unsplash.com/photo-1576678927484-cc907957088c?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1576678927484-cc907957088c?auto=format&fit=crop&w=1200&q=80",
                false, ExerciseType.AEROBIC));
        exercises.add(new Exercise("Outdoor Run", MuscleGroup.FULL_BODY,
                "https://images.unsplash.com/photo-1552674605-46d536d2e681?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1552674605-46d536d2e681?auto=format&fit=crop&w=1200&q=80",
                false, ExerciseType.AEROBIC));
        exercises.add(new Exercise("Stationary Bike", MuscleGroup.LEGS,
                "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=1200&q=80",
                false, ExerciseType.AEROBIC));
        exercises.add(new Exercise("Rowing Machine", MuscleGroup.FULL_BODY,
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1598289431512-b97b0917cdd1?auto=format&fit=crop&w=1200&q=80",
                false, ExerciseType.AEROBIC));
        exercises.add(new Exercise("Elliptical Trainer", MuscleGroup.FULL_BODY,
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=1200&q=80",
                false, ExerciseType.AEROBIC));
        exercises.add(new Exercise("Stair Climber", MuscleGroup.LEGS,
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1571388208497-71bedc66e932?auto=format&fit=crop&w=1200&q=80",
                false, ExerciseType.AEROBIC));
        exercises.add(new Exercise("Jump Rope", MuscleGroup.FULL_BODY,
                "https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?auto=format&fit=crop&w=1200&q=80",
                false, ExerciseType.AEROBIC));
        exercises.add(new Exercise("Swimming", MuscleGroup.FULL_BODY,
                "https://images.unsplash.com/photo-1530549387789-4c1017266635?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1530549387789-4c1017266635?auto=format&fit=crop&w=1200&q=80",
                false, ExerciseType.AEROBIC));
        exercises.add(new Exercise("Brisk Walk", MuscleGroup.FULL_BODY,
                "https://images.unsplash.com/photo-1552674605-46d536d2e681?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1552674605-46d536d2e681?auto=format&fit=crop&w=1200&q=80",
                false, ExerciseType.AEROBIC));
        exercises.add(new Exercise("Air Bike", MuscleGroup.FULL_BODY,
                "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=1200&q=80",
                false, ExerciseType.AEROBIC));

        for (Exercise exercise : exercises) {
            applyMetricsForExercise(exercise);
        }
        return exercises;
    }

    private static void applyMetricsForExercise(Exercise exercise) {
        if (exercise == null) {
            return;
        }
        ExerciseType type = exercise.getType();
        if (type == ExerciseType.AEROBIC) {
            exercise.setShowSeries(false);
            exercise.setShowRepetitions(false);
            exercise.setShowWeight(false);
            exercise.setShowTime(true);
            exercise.setShowHeartRate(true);
            return;
        }

        String name = exercise.getName();
        if (name == null) {
            return;
        }
        String normalized = name.toLowerCase(Locale.ROOT);

        if (containsAny(normalized, "plank")) {
            exercise.setShowSeries(true);
            exercise.setShowRepetitions(false);
            exercise.setShowWeight(false);
            exercise.setShowTime(true);
            exercise.setShowHeartRate(false);
            return;
        }

        if (containsAny(normalized, "carry")) {
            exercise.setShowSeries(true);
            exercise.setShowRepetitions(false);
            exercise.setShowWeight(true);
            exercise.setShowTime(true);
            exercise.setShowHeartRate(false);
            return;
        }

        if (containsAny(normalized,
                "push-up",
                "pull-up",
                "dip",
                "crunch",
                "russian twist",
                "leg raise",
                "ab wheel",
                "step-up")) {
            exercise.setShowSeries(true);
            exercise.setShowRepetitions(true);
            exercise.setShowWeight(false);
            exercise.setShowTime(false);
            exercise.setShowHeartRate(false);
            return;
        }

        exercise.applyDefaultMetricsForType(ExerciseType.ANAEROBIC);
    }

    private static boolean containsAny(String value, String... tokens) {
        if (value == null || tokens == null) {
            return false;
        }
        for (String token : tokens) {
            if (token != null && !token.isEmpty() && value.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static int findExerciseIndex(List<Exercise> exercises, String name) {
        if (exercises == null || name == null) {
            return -1;
        }
        for (int i = 0; i < exercises.size(); i++) {
            Exercise exercise = exercises.get(i);
            if (exercise != null && name.equalsIgnoreCase(exercise.getName())) {
                return i;
            }
        }
        return -1;
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
        ExerciseInWorkout exerciseInWorkout = new ExerciseInWorkout(
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
                null,
                null,
                exercise.getId(),
                workoutId
        );
        exerciseInWorkout.setType(exercise.getType());
        exerciseInWorkout.setShowSeries(exercise.isShowSeries());
        exerciseInWorkout.setShowRepetitions(exercise.isShowRepetitions());
        exerciseInWorkout.setShowWeight(exercise.isShowWeight());
        exerciseInWorkout.setShowTime(exercise.isShowTime());
        exerciseInWorkout.setShowHeartRate(exercise.isShowHeartRate());
        return exerciseInWorkout;
    }

}