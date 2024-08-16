package com.example.gymevo.ui.workoutTracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gymevo.data.seed.ExerciceInWorkoutSeed;
import com.example.gymevo.models.ExerciseInWorkout;

import java.util.List;

public class WorkoutTrackerViewModel extends ViewModel {
    // Une liste privée d'objets ExerciseInWorkout qui représente les exercices dans un entraînement
    private List<ExerciseInWorkout> exerciseInWorkoutList;

    // LiveData qui contiendra une liste mutable d'ExerciseInWorkout
    private MutableLiveData<List<ExerciseInWorkout>> exercisesLiveData;

    // Constructeur du ViewModel
    public WorkoutTrackerViewModel() {
        // Initialise exercisesLiveData avec les données de seed provenant de ExerciceInWorkoutSeed
        exercisesLiveData = new MutableLiveData<>(ExerciceInWorkoutSeed.getExercisesInWorkout());
    }

    // Méthode publique pour obtenir la liste des exercices encapsulée dans un LiveData
    public LiveData<List<ExerciseInWorkout>> getExercises() {
        return exercisesLiveData;
    }
}
