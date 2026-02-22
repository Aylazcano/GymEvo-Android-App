package com.example.gymevo.data.seed;

import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.Workout;
import com.example.gymevo.model.Workout.WorkoutType;

import java.util.ArrayList;
import java.util.List;

public final class WorkoutSeed {

    private static long nextExerciseInWorkoutId = 1;
    private static final int DEFAULT_TIME = 60;
    private static final int CARDIO_30_MINS_TIME_SECONDS = 1800;

    private WorkoutSeed() {
    }

    public static List<Workout> generateWorkouts(List<Exercise> exercises) {
        return generateTemplates(exercises);
    }

    private static List<Workout> generateTemplates(List<Exercise> exercises) {
        List<Workout> templates = new ArrayList<>();
        if (exercises == null || exercises.isEmpty()) {
            return templates;
        }

        templates.add(createTemplate(1L, "Full Body Workout", true, exercises, new Object[][]{
                {"Bodyweight Squat", 3, 12, 60, 45, 100},
                {"Bench Press", 3, 10, 70, 60, 110},
                {"Deadlift", 3, 8, 90, 60, 120}
        }));

        templates.add(createTemplate(2L, "Leg Day", true, exercises, new Object[][]{
                {"Bodyweight Squat", 4, 10, 100, 60, 120},
                {"Leg Press", 3, 12, 80, 90, 130},
                {"Leg Extension", 3, 12, 60, 60, 115}
        }));

        templates.add(createTemplate(3L, "Chest Day", true, exercises, new Object[][]{
                {"Bench Press", 3, 10, 70, 60, 110},
                {"Incline Bench Press", 3, 12, 50, 60, 105},
                {"Chest Fly", 3, 10, 60, 60, 112}
        }));

        templates.add(createTemplate(4L, "Back Day", true, exercises, new Object[][]{
                {"Deadlift", 3, 8, 100, 60, 125},
                {"Pull-up", 3, 10, 0, 45, 115},
                {"Lat Pulldown", 3, 12, 35, 45, 105}
        }));

        templates.add(createTemplate(5L, "Shoulders & Arms", false, exercises, new Object[][]{
                {"Shoulder Press", 3, 10, 45, 60, 108},
                {"Bicep Curl", 3, 12, 25, 45, 100},
                {"Tricep Dip", 3, 12, 0, 45, 102}
        }));

        templates.add(createTemplate(6L, "Core & Cardio", false, exercises, new Object[][]{
                {"Plank", 3, 1, 0, DEFAULT_TIME, 120}
        }));

        templates.add(createTemplate(7L, "Dumbbell Day", false, exercises, new Object[][]{
                {"Flat Dumbbell Bench Press", 3, 10, 20, 60, 110},
                {"Incline Dumbbell Press", 3, 10, 20, 60, 110},
                {"One-Arm Dumbbell Row", 3, 10, 18, 60, 110},
                {"Lateral Raise", 3, 10, 16, 60, 110},
                {"Hammer Curl", 3, 10, 18, 60, 110}
        }));

        templates.add(createTemplate(8L, "Cardio Endurance", false, exercises, new Object[][]{
                {"Jogging, Treadmill", 1, 1, 0, 900, 140},
                {"Bicycling, Stationary", 1, 1, 0, 900, 135},
                {"Rowing, Stationary", 1, 1, 0, 900, 145},
                {"Swimming", 1, 1, 0, 900, 130}
        }));

        templates.add(createTemplate(9L, "Machine Only", false, exercises, new Object[][]{
                {"Leg Press", 3, 12, 90, 60, 120},
                {"Chest Press Machine", 3, 10, 60, 60, 110},
                {"Seated Row Machine", 3, 12, 55, 60, 110},
                {"Shoulder Press Machine", 3, 10, 45, 60, 110},
                {"Leg Extension", 3, 12, 50, 60, 110},
                {"Seated Leg Curl Machine", 3, 12, 45, 60, 110},
                {"Pec Deck Machine", 3, 12, 45, 60, 110}
        }));

        templates.add(createTemplate(10L, "Non-Machine Strength", false, exercises, new Object[][]{
                {"Bodyweight Squat", 3, 10, 90, 60, 120},
                {"Bench Press", 3, 10, 70, 60, 110},
                {"Deadlift", 3, 8, 100, 60, 125},
                {"Pull-up", 3, 8, 0, 60, 110},
                {"Push-Up", 3, 12, 0, 60, 110},
                {"Plank", 3, 1, 0, 60, 0}
        }));

        templates.add(createTemplate(11L, "Ph1: Dumbbell Chest & Free Biceps", true, exercises, new Object[][]{
                {"Flat Dumbbell Bench Press", 3, 10, 30, 60, 110},
                {"Incline Dumbbell Press", 3, 10, 26, 60, 110},
                {"Dumbbell Pullover", 3, 12, 24, 60, 110},
                {"Push-Up", 3, 15, 0, 45, 105},
                {"Barbell Curl", 3, 10, 30, 45, 105},
                {"Hammer Curl", 3, 12, 16, 45, 105},
                {"Bicep Curl", 3, 12, 14, 45, 105},
                {"Plank", 3, 1, 0, DEFAULT_TIME, 0},
                {"Russian Twist", 3, 20, 0, 45, 100},
                {"Treadmill Run", 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        templates.add(createTemplate(12L, "Ph2: Barbell Chest & Machine Biceps", false, exercises, new Object[][]{
                {"Bench Press", 3, 8, 70, 90, 115},
                {"Incline Bench Press", 3, 10, 60, 90, 115},
                {"Decline Press", 3, 10, 60, 90, 115},
                {"Cable Chest Fly", 3, 15, 15, 45, 105},
                {"Cable Biceps Curl", 3, 12, 25, 45, 105},
                {"Preacher Curl", 3, 12, 25, 45, 105},
                {"Assisted Pull-up Machine", 3, 10, 0, 60, 110},
                {"Plank", 3, 1, 0, DEFAULT_TIME, 0},
                {"Cable Woodchop", 3, 15, 20, 45, 100},
                {"Treadmill Run", 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        templates.add(createTemplate(13L, "Barbell/Dumbbell Back & Triceps", true, exercises, new Object[][]{
                {"Deadlift", 3, 8, 110, 90, 125},
                {"Pull-up", 3, 8, 0, 60, 110},
                {"Barbell Row", 3, 10, 70, 60, 110},
                {"One-Arm Dumbbell Row", 3, 10, 35, 60, 110},
                {"Skull Crusher", 3, 10, 20, 45, 105},
                {"Tricep Dip", 3, 12, 0, 45, 105},
                {"Overhead Tricep Extension", 3, 12, 15, 45, 105},
                {"Plank", 3, 1, 0, DEFAULT_TIME, 0},
                {"Treadmill Run", 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        templates.add(createTemplate(14L, "Machine Back & Triceps", false, exercises, new Object[][]{
                {"Lat Pulldown", 3, 10, 60, 60, 110},
                {"Seated Row Machine", 3, 10, 55, 60, 110},
                {"T-Bar Row", 3, 10, 65, 60, 110},
                {"Assisted Pull-up Machine", 3, 10, 0, 60, 110},
                {"Tricep Pushdown", 3, 12, 30, 45, 105},
                {"Cable Overhead Triceps Extension", 3, 12, 20, 45, 105},
                {"Assisted Dip Machine", 3, 12, 0, 45, 105},
                {"Plank", 3, 1, 0, DEFAULT_TIME, 0},
                {"Treadmill Run", 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        templates.add(createTemplate(15L, "Barbell/Dumbbell Legs & Shoulders", true, exercises, new Object[][]{
                {"Bodyweight Squat", 4, 10, 95, 60, 120},
                {"Lunge", 3, 10, 15, 60, 115},
                {"Front Squat", 3, 8, 80, 60, 120},
                {"Romanian Deadlift", 3, 10, 70, 60, 115},
                {"Shoulder Press", 3, 10, 40, 60, 110},
                {"Lateral Raise", 3, 12, 10, 45, 105},
                {"Front Raise", 3, 12, 10, 45, 105},
                {"Plank", 3, 1, 0, DEFAULT_TIME, 0},
                {"Cable Woodchop", 3, 15, 20, 45, 100},
                {"Treadmill Run", 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        templates.add(createTemplate(16L, "Machine Legs & Shoulders", false, exercises, new Object[][]{
                {"Leg Press", 4, 12, 120, 60, 120},
                {"Leg Extension", 3, 12, 50, 60, 115},
                {"Seated Leg Curl Machine", 3, 12, 45, 60, 115},
                {"Hack Squat Machine", 3, 10, 80, 60, 120},
                {"Shoulder Press Machine", 3, 10, 45, 60, 110},
                {"Cable Lateral Raise", 3, 12, 10, 45, 105},
                {"Reverse Fly Machine", 3, 12, 40, 45, 105},
                {"Plank", 3, 1, 0, DEFAULT_TIME, 0},
                {"Russian Twist", 3, 15, 0, 45, 100},
                {"Treadmill Run", 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        templates.add(createTemplate(17L, "Barbell/Dumbbell Glutes & Biceps", true, exercises, new Object[][]{
                {"Hip Thrust", 3, 10, 80, 60, 115},
                {"Lunge", 3, 10, 15, 60, 110},
                {"Dumbbell Romanian Deadlift", 3, 10, 20, 60, 110},
                {"Barbell Curl", 3, 10, 30, 45, 105},
                {"Hammer Curl", 3, 12, 15, 45, 105},
                {"Bicep Curl", 3, 12, 10, 45, 105},
                {"Plank", 3, 1, 0, DEFAULT_TIME, 0},
                {"Russian Twist", 3, 15, 0, 45, 100},
                {"Treadmill Run", 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        templates.add(createTemplate(18L, "Machine Glutes & Biceps", false, exercises, new Object[][]{
                {"Hip Thrust", 3, 10, 80, 60, 115},
                {"Cable Pull-through", 3, 12, 40, 60, 110},
                {"Cable Glute Kickback", 3, 15, 20, 45, 105},
                {"Cable Biceps Curl", 3, 12, 25, 45, 105},
                {"Preacher Curl", 3, 10, 30, 45, 105},
                {"Assisted Pull-up Machine", 3, 10, 0, 60, 105},
                {"Plank", 3, 1, 0, DEFAULT_TIME, 0},
                {"Cable Woodchop", 3, 15, 20, 45, 100},
                {"Treadmill Run", 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        templates.add(createTemplate(19L, "Barbell/Dumbbell Glutes & Shoulders", true, exercises, new Object[][]{
                {"Hip Thrust", 3, 10, 80, 60, 115},
                {"Lunge", 3, 10, 15, 60, 110},
                {"Dumbbell Romanian Deadlift", 3, 10, 20, 60, 110},
                {"Shoulder Press", 3, 10, 40, 60, 110},
                {"Lateral Raise", 3, 12, 10, 45, 105},
                {"Front Raise", 3, 12, 10, 45, 105},
                {"Plank", 3, 1, 0, DEFAULT_TIME, 0},
                {"Cable Woodchop", 3, 15, 20, 45, 100},
                {"Treadmill Run", 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        templates.add(createTemplate(20L, "Machine Glutes & Shoulders", false, exercises, new Object[][]{
                {"Hip Thrust", 3, 10, 80, 60, 115},
                {"Cable Pull-through", 3, 12, 40, 60, 110},
                {"Cable Glute Kickback", 3, 15, 20, 45, 105},
                {"Shoulder Press Machine", 3, 10, 45, 60, 110},
                {"Cable Lateral Raise", 3, 12, 10, 45, 105},
                {"Reverse Fly Machine", 3, 12, 40, 45, 105},
                {"Plank", 3, 1, 0, DEFAULT_TIME, 0},
                {"Russian Twist", 3, 15, 0, 45, 100},
                {"Treadmill Run", 1, 1, 0, CARDIO_30_MINS_TIME_SECONDS, 140}
        }));

        return templates;
    }

    private static Workout createTemplate(long id,
                                          String name,
                                          boolean starred,
                                          List<Exercise> exercises,
                                          Object[][] plan) {
        Workout workout = new Workout(id, name, null, starred, WorkoutType.TEMPLATE);
        if (plan == null || exercises == null || exercises.isEmpty()) {
            return workout;
        }

        for (Object[] spec : plan) {
            if (spec == null || spec.length < 6) {
                continue;
            }
            String exerciseName = (String) spec[0];
            Exercise exercise = findExerciseByName(exercises, exerciseName);
            if (exercise == null) {
                continue;
            }
            workout.addExercise(createExerciseInWorkout(
                    nextExerciseInWorkoutId++,
                    exercise,
                    (int) spec[1],
                    (int) spec[2],
                    (int) spec[3],
                    (int) spec[4],
                    (int) spec[5],
                    workout.getId()
            ));
        }

        return workout;
    }

    private static Exercise findExerciseByName(List<Exercise> exercises, String name) {
        if (exercises == null || name == null) {
            return null;
        }

        // first try exact case-insensitive match
        String lowerName = name.toLowerCase();
        for (Exercise exercise : exercises) {
            if (exercise != null && lowerName.equals(exercise.getName().toLowerCase())) {
                return exercise;
            }
        }

        // fallback: try to match by tokens (handles variations like "Treadmill Run" vs "Running, Treadmill")
        String[] queryTokens = lowerName.split("[\\s,\\-]+");
        for (Exercise exercise : exercises) {
            if (exercise == null) {
                continue;
            }
            String lowerEx = exercise.getName().toLowerCase();
            String[] exTokens = lowerEx.split("[\\s,\\-]+");
            boolean allMatch = true;
            for (String token : queryTokens) {
                if (token.isEmpty()) {
                    continue;
                }
                boolean tokenFound = false;
                for (String exToken : exTokens) {
                    if (exToken.contains(token)) {
                        tokenFound = true;
                        break;
                    }
                }
                if (!tokenFound) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                return exercise;
            }
        }

        // last resort, try simple contains
        for (Exercise exercise : exercises) {
            if (exercise != null && exercise.getName().toLowerCase().contains(lowerName)) {
                return exercise;
            }
        }
        return null;
    }

    private static ExerciseInWorkout createExerciseInWorkout(long id,
                                                             Exercise exercise,
                                                             int series,
                                                             int repetitions,
                                                             int weight,
                                                             int time,
                                                             int heartRates,
                                                             Long workoutId) {
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
        exerciseInWorkout.copyCatalogMetadataFrom(exercise);
        exerciseInWorkout.setShowSeries(exercise.isShowSeries());
        exerciseInWorkout.setShowRepetitions(exercise.isShowRepetitions());
        exerciseInWorkout.setShowWeight(exercise.isShowWeight());
        exerciseInWorkout.setShowTime(exercise.isShowTime());
        exerciseInWorkout.setShowHeartRate(exercise.isShowHeartRate());
        exerciseInWorkout.setShowDistance(exercise.isShowDistance());
        exerciseInWorkout.setShowCalories(exercise.isShowCalories());
        return exerciseInWorkout;
    }
}
