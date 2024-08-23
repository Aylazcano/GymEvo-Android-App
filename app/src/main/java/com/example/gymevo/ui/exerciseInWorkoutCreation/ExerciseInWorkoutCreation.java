package com.example.gymevo.ui.exerciseInWorkoutCreation;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gymevo.R;

public class ExerciseInWorkoutCreation extends Fragment {

    private ExerciseInWorkoutCreationViewModel mViewModel;

    public static ExerciseInWorkoutCreation newInstance() {
        return new ExerciseInWorkoutCreation();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercise_in_workout_creation, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ExerciseInWorkoutCreationViewModel.class);
        // TODO: Use the ViewModel
    }

}