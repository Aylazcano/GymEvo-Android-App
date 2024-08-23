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
import java.util.stream.Collectors;

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
        if (seedWorkouts != null && !seedWorkouts.isEmpty()) {
            workoutsLiveData.setValue(seedWorkouts);
            currentWorkoutLiveData.setValue(seedWorkouts.get(0)); // Set the first workout as default
        } else {
            workoutsLiveData.setValue(new ArrayList<>());
            currentWorkoutLiveData.setValue(new Workout()); // Initialize with an empty workout if seed data is empty
        }
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
