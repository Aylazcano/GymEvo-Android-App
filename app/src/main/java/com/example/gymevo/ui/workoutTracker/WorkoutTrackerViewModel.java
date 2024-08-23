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
    private final MutableLiveData<List<Workout>> workoutsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Workout> currentWorkoutLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ExerciseInWorkout>> exercisesOnDateLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Exercise>> allExercisesLiveData = new MutableLiveData<>();

    public WorkoutTrackerViewModel() {
        loadSeedData();
    }

    private void loadSeedData() {
        List<Workout> seedWorkouts = WorkoutSeed.generateWorkouts();
        workoutsLiveData.setValue(seedWorkouts != null ? seedWorkouts : new ArrayList<>());

        // Initialize currentWorkoutLiveData with the first workout or an empty workout
        setCurrentWorkout(!seedWorkouts.isEmpty() ? seedWorkouts.get(0) : new Workout());
    }

    public LiveData<List<ExerciseInWorkout>> getExercisesForWorkoutOnDate(LocalDate date) {
        List<ExerciseInWorkout> exercisesForDate = new ArrayList<>();

        // Get all workouts and find the one matching the date
        Workout currentWorkout = workoutsLiveData.getValue().stream()
                .filter(workout -> workout.getDate().equals(date))
                .findFirst()
                .orElse(null);

        if (currentWorkout != null && currentWorkout.getExercises() != null) {
            exercisesForDate = new ArrayList<>(currentWorkout.getExercises());
        }

        exercisesOnDateLiveData.setValue(exercisesForDate);
        return exercisesOnDateLiveData;
    }
}
