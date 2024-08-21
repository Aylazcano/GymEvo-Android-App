package com.example.gymevo.ui.WorkoutCreation;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gymevo.models.ExerciseInWorkout;
import com.example.gymevo.models.Workout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class WorkoutCreationViewModel extends ViewModel {
    private final MutableLiveData<List<Workout>> workoutsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Workout> currentWorkoutLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ExerciseInWorkout>> exercisesOnDateLiveData = new MutableLiveData<>();

    public WorkoutCreationViewModel() {
        loadSeedData();
    }

    private void loadSeedData() {
        List<Workout> seedWorkouts = WorkoutSeed.generateWorkouts();
        workoutsLiveData.setValue(seedWorkouts != null ? seedWorkouts : new ArrayList<>());

        // Initialize currentWorkoutLiveData with the first workout or an empty workout
        setCurrentWorkout(!seedWorkouts.isEmpty() ? seedWorkouts.get(0) : new Workout());
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
        List<ExerciseInWorkout> exercises = new ArrayList<>();

        if (workouts != null) {
            Workout filteredWorkout = workouts.stream()
                    .filter(w -> w.getDate().equals(date))
                    .findFirst()
                    .orElse(null);

            if (filteredWorkout != null) {
                exercises = filteredWorkout.getExercisesList();
            }
        }

        exercisesOnDateLiveData.setValue(exercises);
        return exercisesOnDateLiveData;
    }
}
