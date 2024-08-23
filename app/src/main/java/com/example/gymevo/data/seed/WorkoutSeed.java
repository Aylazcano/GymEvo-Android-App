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
        List<Exercise> exercises = generateExercises();

        LocalDate startDate = LocalDate.parse("20240101", formatter);
        LocalDate endDate = LocalDate.parse("20240818", formatter);

        long workoutId = 1;
        long exerciseInWorkoutId = 1;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // Create a workout every 2-3 days
            if (random.nextInt(3) > 0) {
                Workout workout = new Workout(workoutId++, "Workout " + workoutId, date);

                // Add 3-5 exercises to each workout
                int exerciseCount = random.nextInt(3) + 3;
                for (int i = 0; i < exerciseCount; i++) {
                    Exercise exercise = exercises.get(random.nextInt(exercises.size()));
                    ExerciseInWorkout eiw = new ExerciseInWorkout(
                            exerciseInWorkoutId++,
                            random.nextInt(3) + 2, // 2-4 series
                            random.nextInt(8) + 8, // 8-15 repetitions
                            random.nextInt(81) + 20, // 20-100 lbs
                            random.nextInt(61) + 30, // 30-90 seconds
                            random.nextInt(41) + 80, // 80-120 bpm
                            exercise.getId(), // Set exercise ID
                            workout.getId() // Set workout ID
                    );
                    workout.addExercise(eiw);
                }

                workouts.add(workout);
            }
        }

        return workouts;
    }

    private static List<Exercise> generateExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise(1L, "Squat", "Legs", "squat_start.png", "squat_end.png"));
        exercises.add(new Exercise(2L, "Bench Press", "Chest", "bench_press_start.png", "bench_press_end.png"));
        exercises.add(new Exercise(3L, "Deadlift", "Back", "deadlift_start.png", "deadlift_end.png"));
        exercises.add(new Exercise(4L, "Shoulder Press", "Shoulders", "shoulder_press_start.png", "shoulder_press_end.png"));
        exercises.add(new Exercise(5L, "Pull-up", "Back", "pull_up_start.png", "pull_up_end.png"));
        return exercises;
    }
}
