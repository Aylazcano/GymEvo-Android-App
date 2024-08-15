package com.example.gymevo.ui.exerciceCreation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExerciceCreationViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ExerciceCreationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Exercice Creation fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}