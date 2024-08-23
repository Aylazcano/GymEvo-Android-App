package com.example.gymevo.ui.exercisesList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExercisesListViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ExercisesListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}