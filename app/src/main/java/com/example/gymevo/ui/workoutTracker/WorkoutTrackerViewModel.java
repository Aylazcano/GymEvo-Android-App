package com.example.gymevo.ui.workoutTracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gymevo.data.seed.WorkoutSeed;
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
            currentWorkoutLiveData.setValue(currentWorkout); // Notify observers of the change
        }
    }

    public void removeExerciseFromWorkout(ExerciseInWorkout exercise) {
        Workout currentWorkout = currentWorkoutLiveData.getValue();
        if (currentWorkout != null) {
            currentWorkout.removeExercise(exercise);
            currentWorkoutLiveData.setValue(currentWorkout); // Notify observers of the change
        }
    }

    public LiveData<List<ExerciseInWorkout>> getExercisesForWorkoutOnDate(LocalDate date) {
        List<Workout> workouts = workoutsLiveData.getValue();
        if (workouts != null) {
            Workout filteredWorkout = workouts.stream()
                    .filter(w -> w.getDate().equals(date))
                    .findFirst()
                    .orElse(new Workout());
            exercisesOnDateLiveData.setValue(filteredWorkout.getExercisesList());
        } else {
            exercisesOnDateLiveData.setValue(new ArrayList<>());
        }
        return exercisesOnDateLiveData;
    }
}
