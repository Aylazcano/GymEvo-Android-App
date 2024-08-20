package com.example.gymevo.data.seed;

import com.example.gymevo.models.Exercice;
import com.example.gymevo.models.ExerciseInWorkout;
import com.example.gymevo.models.Workout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class WorkoutSeed {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Random random = new Random();

    public static List<Workout> generateWorkouts() {
        List<Workout> workouts = new ArrayList<>();
        List<Exercice> exercises = generateExercises();

        LocalDate startDate = LocalDate.parse("20240101", formatter);
        LocalDate endDate = LocalDate.parse("20240818", formatter);

        long workoutId = 1;
        long exerciseInWorkoutId = 1;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // Créer un workout tous les 2-3 jours
            if (random.nextInt(3) > 0) {
                Workout workout = new Workout(workoutId++, date);

                // Ajouter 3-5 exercices à chaque workout
                int exerciseCount = random.nextInt(3) + 3;
                for (int i = 0; i < exerciseCount; i++) {
                    Exercice exercice = exercises.get(random.nextInt(exercises.size()));
                    ExerciseInWorkout eiw = new ExerciseInWorkout(
                            exerciseInWorkoutId++,
                            random.nextInt(3) + 2, // 2-4 séries
                            random.nextInt(8) + 8, // 8-15 répétitions
                            random.nextInt(81) + 20, // 20-100 lbs
                            random.nextInt(61) + 30, // 30-90 secondes
                            random.nextInt(41) + 80, // 80-120 bpm
                            exercice,
                            workout,
                            date // Add the date field here
                    );
                    workout.addExercise(eiw);
                }

                workouts.add(workout);
            }
        }

        return workouts;
    }

    private static List<Exercice> generateExercises() {
        List<Exercice> exercises = new ArrayList<>();
        exercises.add(new Exercice(1L, "Squat", "Legs", "squat_start.png", "squat_end.png"));
        exercises.add(new Exercice(2L, "Bench Press", "Chest", "bench_press_start.png", "bench_press_end.png"));
        exercises.add(new Exercice(3L, "Deadlift", "Back", "deadlift_start.png", "deadlift_end.png"));
        exercises.add(new Exercice(4L, "Shoulder Press", "Shoulders", "shoulder_press_start.png", "shoulder_press_end.png"));
        exercises.add(new Exercice(5L, "Pull-up", "Back", "pull_up_start.png", "pull_up_end.png"));
        return exercises;
    }
}
