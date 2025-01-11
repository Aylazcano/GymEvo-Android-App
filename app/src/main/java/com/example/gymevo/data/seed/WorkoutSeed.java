package com.example.gymevo.data.seed;

import com.example.gymevo.models.Exercise;
import com.example.gymevo.models.ExerciseInWorkout;
import com.example.gymevo.models.Workout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorkoutSeed {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Random random = new Random();

    public static List<Workout> generateWorkouts() {
        List<Workout> workouts = new ArrayList<>();
        List<Workout> starredWorkouts = generateStarredWorkouts();
        List<Exercise> exercises = generateExercises();

        LocalDate startDate = LocalDate.parse("20240101", formatter);
        LocalDate endDate = LocalDate.now();

        long workoutId = 1;
        long exerciseInWorkoutId = 1;

        // Add starred workouts
        workouts.addAll(starredWorkouts);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            // Create a regular workout every 2-3 days
            if (random.nextInt(3) > 0) {
                Workout workout = new Workout(workoutId++, "Workout " + workoutId, date, false);

                // Add 3-5 exercises to each workout
                int exerciseCount = random.nextInt(3) + 3;
                for (int i = 0; i < exerciseCount; i++) {
                    Exercise exercise = exercises.get(random.nextInt(exercises.size()));
                    ExerciseInWorkout eiw = new ExerciseInWorkout(
                            exerciseInWorkoutId++,
                            exercise.getName(), // Name of the exercise
                            exercise.getTargetedMuscles(), // Targeted muscles from the exercise
                            exercise.getStartImage(), // Using exercise start image
                            exercise.getEndImage(), // Using exercise end image
                            random.nextInt(3) + 2, // 2-4 sets
                            random.nextInt(8) + 8, // 8-15 reps
                            random.nextInt(81) + 20, // 20-100 lbs
                            random.nextInt(61) + 30, // 30-90 seconds of exercise
                            random.nextInt(41) + 80, // 80-120 bpm
                            exercise.getId(), // Exercise ID
                            workout.getId() // Workout ID
                    );
                    workout.addExercise(eiw); // Add exercise to the workout
                }
                workouts.add(workout);
            }
        }
        return workouts;
    }

    private static List<Workout> generateStarredWorkouts() {
        List<Workout> starredWorkouts = new ArrayList<>();
        List<Exercise> exercises = generateExercises();

        // Full Body Workout
        Workout fullBodyWorkout = new Workout(1L, "Full Body Workout", LocalDate.now(), true);
        fullBodyWorkout.addExercise(new ExerciseInWorkout(1L,
                exercises.get(0).getName(),
                exercises.get(1).getTargetedMuscles(),
                exercises.get(0).getStartImage(),
                exercises.get(0).getEndImage(),
                3, 12, 60, 45, 100,
                exercises.get(0).getId(),
                fullBodyWorkout.getId()));
        fullBodyWorkout.addExercise(new ExerciseInWorkout(2L,
                exercises.get(1).getName(),
                exercises.get(1).getTargetedMuscles(),
                exercises.get(1).getStartImage(),
                exercises.get(1).getEndImage(),
                3, 10, 70, 60, 110,
                exercises.get(1).getId(),
                fullBodyWorkout.getId()));
        starredWorkouts.add(fullBodyWorkout);

        // Leg Day Workout
        Workout legDayWorkout = new Workout(2L, "Leg Day", LocalDate.now(), true);
        legDayWorkout.addExercise(new ExerciseInWorkout(3L,
                exercises.get(0).getName(),
                exercises.get(1).getTargetedMuscles(),
                exercises.get(0).getStartImage(),
                exercises.get(0).getEndImage(),
                3, 12, 100, 60, 120,
                exercises.get(0).getId(),
                legDayWorkout.getId()));
        legDayWorkout.addExercise(new ExerciseInWorkout(4L,
                exercises.get(9).getName(),
                exercises.get(1).getTargetedMuscles(),
                exercises.get(9).getStartImage(),
                exercises.get(9).getEndImage(),
                3, 12, 80, 90, 130,
                exercises.get(9).getId(),
                legDayWorkout.getId()));
        starredWorkouts.add(legDayWorkout);

        // Chest Day Workout
        Workout chestDayWorkout = new Workout(3L, "Chest Day", LocalDate.now(), true);
        chestDayWorkout.addExercise(new ExerciseInWorkout(5L,
                exercises.get(1).getName(),
                exercises.get(1).getTargetedMuscles(),
                exercises.get(1).getStartImage(),
                exercises.get(1).getEndImage(),
                3, 10, 70, 60, 110,
                exercises.get(1).getId(),
                chestDayWorkout.getId()));
        chestDayWorkout.addExercise(new ExerciseInWorkout(6L,
                exercises.get(10).getName(),
                exercises.get(10).getTargetedMuscles(),
                exercises.get(10).getStartImage(),
                exercises.get(10).getEndImage(),
                3, 10, 70, 60, 120,
                exercises.get(10).getId(),
                chestDayWorkout.getId()));
        starredWorkouts.add(chestDayWorkout);

        // Back Day Workout
        Workout backDayWorkout = new Workout(4L, "Back Day", LocalDate.now(), true);
        backDayWorkout.addExercise(new ExerciseInWorkout(7L,
                exercises.get(2).getName(),
                exercises.get(2).getTargetedMuscles(),
                exercises.get(2).getStartImage(),
                exercises.get(2).getEndImage(),
                3, 12, 100, 60, 120,
                exercises.get(2).getId(),
                backDayWorkout.getId()));
        backDayWorkout.addExercise(new ExerciseInWorkout(8L,
                exercises.get(5).getName(),
                exercises.get(5).getTargetedMuscles(),
                exercises.get(5).getStartImage(),
                exercises.get(5).getEndImage(),
                3, 10, 70, 90, 110,
                exercises.get(5).getId(),
                backDayWorkout.getId()));
        starredWorkouts.add(backDayWorkout);

        return starredWorkouts;
    }

    private static List<Exercise> generateExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("Squat", "Legs", "squat_start.png", "squat_end.png", true));
        exercises.add(new Exercise("Bench Press", "Chest", "bench_press_start.png", "bench_press_end.png", true));
        exercises.add(new Exercise("Deadlift", "Back", "deadlift_start.png", "deadlift_end.png", false));
        exercises.add(new Exercise("Shoulder Press", "Shoulders", "shoulder_press_start.png", "shoulder_press_end.png", false));
        exercises.add(new Exercise("Pull-up", "Back", "pull_up_start.png", "pull_up_end.png", true));
        exercises.add(new Exercise("Bicep Curl", "Arms", "bicep_curl_start.png", "bicep_curl_end.png", false));
        exercises.add(new Exercise("Tricep Dip", "Arms", "tricep_dip_start.png", "tricep_dip_end.png", false));
        exercises.add(new Exercise("Plank", "Abs", "plank_start.png", "plank_end.png", true));
        exercises.add(new Exercise("Leg Press", "Legs", "leg_press_start.png", "leg_press_end.png", true));
        exercises.add(new Exercise("Lunge", "Legs", "lunge_start.png", "lunge_end.png", false));
        exercises.add(new Exercise("Chest Fly", "Chest", "chest_fly_start.png", "chest_fly_end.png", true));
        exercises.add(new Exercise("Incline Bench Press", "Chest", "incline_bench_press_start.png", "incline_bench_press_end.png", true));
        return exercises;
    }
}
