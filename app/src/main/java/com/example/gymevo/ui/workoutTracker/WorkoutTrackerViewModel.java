package com.example.gymevo.ui.workoutTracker;

import androidx.lifecycle.ViewModel;

import com.example.gymevo.data.seed.ExerciceInWorkoutSeed;
import com.example.gymevo.models.ExerciseInWorkout;

import java.util.List;

public class WorkoutTrackerViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private List<ExerciseInWorkout> exerciseInWorkoutList;

    public WorkoutTrackerViewModel() {
        // Initialiser avec les données seed
        exerciseInWorkoutList = ExerciceInWorkoutSeed.getExercisesInWorkout();
    }

    public List<ExerciseInWorkout> getExercices() {
        return exerciseInWorkoutList;
    }
}