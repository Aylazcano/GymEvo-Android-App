package com.example.gymevo.ui.workoutTracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gymevo.data.seed.WorkoutSeed;
import com.example.gymevo.models.Exercise;
import com.example.gymevo.models.ExerciseInWorkout;
import com.example.gymevo.models.Workout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkoutTrackerViewModel extends ViewModel {
    private final MutableLiveData<List<Workout>> workoutsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Workout> currentWorkoutLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ExerciseInWorkout>> exercisesOnDateLiveData = new MutableLiveData<>(new ArrayList<>());

    public WorkoutTrackerViewModel() {
        loadData();
    }

    private void loadData() {
        // Effacer tous les workouts existants
        workoutsLiveData.setValue(new ArrayList<>());  // Vider la liste des workouts

        // Générer de nouveaux entraînements de seed
        List<Workout> seedWorkouts = WorkoutSeed.generateWorkouts();

        // Si des données de seed valides sont générées, les appliquer
        if (seedWorkouts != null && !seedWorkouts.isEmpty()) {
            workoutsLiveData.setValue(seedWorkouts);  // Remplacer par les nouveaux workouts générés
            currentWorkoutLiveData.setValue(seedWorkouts.get(0));  // Mettre à jour l'entraînement actuel
        }
    }


    public LiveData<List<ExerciseInWorkout>> getExercisesForWorkoutOnDate(LocalDate date) {
        List<Workout> workouts = workoutsLiveData.getValue();
        List<ExerciseInWorkout> exerciseInWorkout = new ArrayList<>();

        Workout currentWorkout = workouts.stream()
                .filter(w -> w.getDate() != null && w.getDate().equals(date))
                .findFirst()
                .orElse(null);

        if (currentWorkout != null && currentWorkout.getExercises() != null) {
            exerciseInWorkout = new ArrayList<>(currentWorkout.getExercises());
        }

        exercisesOnDateLiveData.setValue(exerciseInWorkout);
        return exercisesOnDateLiveData;
    }
}
