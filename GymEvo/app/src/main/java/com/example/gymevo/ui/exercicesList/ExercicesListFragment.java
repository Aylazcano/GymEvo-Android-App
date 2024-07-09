package com.example.gymevo.ui.exercicesList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gymevo.databinding.FragmentAddExerciceInWorkoutBinding;

public class ExercicesListFragment extends Fragment {

    private FragmentAddExerciceInWorkoutBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExercicesListViewModel exercicesListViewModel =
                new ViewModelProvider(this).get(ExercicesListViewModel.class);

        binding = FragmentAddExerciceInWorkoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textviewAddExerciceInWorkout;
        exercicesListViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}