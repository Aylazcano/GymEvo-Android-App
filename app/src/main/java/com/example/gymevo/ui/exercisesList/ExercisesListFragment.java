package com.example.gymevo.ui.exercisesList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gymevo.databinding.FragmentExercisesListBinding;

public class ExercisesListFragment extends Fragment {

    private FragmentExercisesListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExercisesListViewModel exercicesListViewModel =
                new ViewModelProvider(this).get(ExercisesListViewModel.class);

        binding = FragmentExercisesListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textExercisesList;
        exercicesListViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}