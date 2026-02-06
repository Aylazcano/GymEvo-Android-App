package com.example.gymevo.ui.exercisesList;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.gymevo.data.repository.ExerciseRepository;
import com.example.gymevo.model.Exercise;

import java.util.List;

public class ExerciseListViewModel extends AndroidViewModel {

    private final ExerciseRepository repository;
    private final LiveData<List<Exercise>> exercises;

    public ExerciseListViewModel(@NonNull Application application) {
        super(application);
        repository = new ExerciseRepository(application);
        exercises = repository.getAllExercises();
    }

    public LiveData<List<Exercise>> getExercises() {
        return exercises;
    }

    public void addExercise(Exercise exercise) {
        if (exercise != null) {
            repository.insertExercise(exercise);
        }
    }

    public void updateExercise(Exercise exercise) {
        if (exercise != null) {
            repository.updateExercise(exercise);
        }
    }

    public void deleteExercise(Exercise exercise) {
        if (exercise != null) {
            repository.deleteExercise(exercise);
        }
    }

    public void toggleExerciseStar(Exercise exercise) {
        if (exercise == null) {
            return;
        }
        repository.updateExerciseStar(exercise);
    }
}
