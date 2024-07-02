package com.example.gymevo.ui.workoutCreation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WorkoutCreationViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public WorkoutCreationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Workout Creation fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}