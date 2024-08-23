package com.example.gymevo.ui.workoutCreation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gymevo.models.ExerciseInWorkout;
import com.example.gymevo.models.Workout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkoutCreationViewModel extends ViewModel {
    private final MutableLiveData<List<Workout>> workoutsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Workout> currentWorkoutLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ExerciseInWorkout>> exercisesOnDateLiveData = new MutableLiveData<>();

    public WorkoutCreationViewModel() {
        loadData();
    }

    private void loadData() {
        List<Workout> seedWorkouts = new ArrayList<>(); // Replace with your actual data source
        workoutsLiveData.setValue(seedWorkouts != null ? seedWorkouts : new ArrayList<>());

        // Initialize currentWorkoutLiveData with an empty workout
        setCurrentWorkout(new Workout());
    }

    public LiveData<List<Workout>> getWorkoutsLiveData() {
        return workoutsLiveData;
    }

    public LiveData<Workout> getCurrentWorkoutLiveData() {
        return currentWorkoutLiveData;
    }

    public void setCurrentWorkout(Workout workout) {
        currentWorkoutLiveData.setValue(workout);
    }

    public void addExerciseToWorkout(ExerciseInWorkout exercise) {
        Workout currentWorkout = currentWorkoutLiveData.getValue();
        if (currentWorkout != null) {
            currentWorkout.addExercise(exercise);
            currentWorkoutLiveData.setValue(currentWorkout);
        }
    }

    public void removeExerciseFromWorkout(ExerciseInWorkout exercise) {
        Workout currentWorkout = currentWorkoutLiveData.getValue();
        if (currentWorkout != null) {
            currentWorkout.removeExercise(exercise);
            currentWorkoutLiveData.setValue(currentWorkout);
        }
    }

    public LiveData<List<ExerciseInWorkout>> getExercisesForWorkoutOnDate(LocalDate date) {
        List<Workout> workouts = workoutsLiveData.getValue();
        List<ExerciseInWorkout> exerciseInWorkout = new ArrayList<>();

        if (workouts != null) {
            Workout currentWorkout = workouts.stream()
                    .filter(w -> w.getDate().equals(date))
                    .findFirst()
                    .orElse(null);

            if (currentWorkout != null && currentWorkout.getExercises() != null) {
                exerciseInWorkout = new ArrayList<>(currentWorkout.getExercises());
                setCurrentWorkout(currentWorkout);
            } else {
                // No workout found for the selected date, create a new one
                Workout newWorkout = new Workout();
                newWorkout.setDate(date);
                setCurrentWorkout(newWorkout);
            }
        }

        exercisesOnDateLiveData.setValue(exerciseInWorkout);
        return exercisesOnDateLiveData;
    }

    public void saveWorkout() {
        Workout currentWorkout = currentWorkoutLiveData.getValue();
        if (currentWorkout != null) {
            List<Workout> workouts = workoutsLiveData.getValue();
            if (workouts == null) {
                workouts = new ArrayList<>();
            }

            // Check if workout already exists on the date
            boolean exists = workouts.stream()
                    .anyMatch(w -> w.getDate().equals(currentWorkout.getDate()));

            if (!exists) {
                workouts.add(currentWorkout);
            }

            workoutsLiveData.setValue(workouts);
        }
    }
}