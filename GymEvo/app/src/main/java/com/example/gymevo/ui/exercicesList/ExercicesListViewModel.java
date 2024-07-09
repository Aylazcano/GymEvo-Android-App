package com.example.gymevo.ui.exercicesList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExercicesListViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ExercicesListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}