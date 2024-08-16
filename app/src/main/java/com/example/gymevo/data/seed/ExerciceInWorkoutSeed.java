package com.example.gymevo.data.seed;

import com.example.gymevo.models.Exercice;
import com.example.gymevo.models.ExerciseInWorkout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExerciceInWorkoutSeed {

    public static List<ExerciseInWorkout> getExercisesInWorkout() {
        List<ExerciseInWorkout> exercisesInWorkout = new ArrayList<>();

        // Création des exercices
        Exercice benchPress = new Exercice(1L, "Bench Press", "Pectorales > Triceps > Épaules",
                "url_to_start_image_bench_press", "url_to_end_image_bench_press");
        Exercice squat = new Exercice(2L, "Squat", "Cuisses > Fessiers",
                "url_to_start_image_squat", "url_to_end_image_squat");
        Exercice deadlift = new Exercice(3L, "Deadlift", "Dos > Fessiers > Cuisses",
                "url_to_start_image_deadlift", "url_to_end_image_deadlift");
        Exercice pullUp = new Exercice(4L, "Pull-up", "Dos > Biceps",
                "url_to_start_image_pull_up", "url_to_end_image_pull_up");
        Exercice lunge = new Exercice(5L, "Lunge", "Cuisses > Fessiers",
                "url_to_start_image_lunge", "url_to_end_image_lunge");

        // Ajout des exercices dans les workouts
        exercisesInWorkout.add(new ExerciseInWorkout(1L, 3, 10, 45, 60, 120, new Date(), benchPress));
        exercisesInWorkout.add(new ExerciseInWorkout(2L, 4, 12, 60, 90, 130, new Date(), squat));
        exercisesInWorkout.add(new ExerciseInWorkout(3L, 5, 8, 70, 120, 140, new Date(), deadlift));
        exercisesInWorkout.add(new ExerciseInWorkout(4L, 4, 15, 50, 60, 110, new Date(), pullUp));
        exercisesInWorkout.add(new ExerciseInWorkout(5L, 3, 20, 40, 70, 100, new Date(), lunge));

        // Ajoutez autant d'exercices que nécessaire

        return exercisesInWorkout;
    }
}
