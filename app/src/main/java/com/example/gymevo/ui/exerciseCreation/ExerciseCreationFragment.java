package com.example.gymevo.ui.exerciseCreation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gymevo.databinding.FragmentExerciseCreationBinding;

public class ExerciseCreationFragment extends Fragment {

    private FragmentExerciseCreationBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExerciceCreationViewModel galleryViewModel =
                new ViewModelProvider(this).get(ExerciceCreationViewModel.class);

        binding = FragmentExerciseCreationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textviewExerciseCreation;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}