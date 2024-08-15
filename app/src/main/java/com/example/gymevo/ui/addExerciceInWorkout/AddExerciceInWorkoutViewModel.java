package com.example.gymevo.ui.addExerciceInWorkout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddExerciceInWorkoutViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AddExerciceInWorkoutViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Workout Creation fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}