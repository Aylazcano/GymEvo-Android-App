package com.example.gymevo.ui.WorkoutCreation;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.WorkoutAdapter;
import com.example.gymevo.databinding.FragmentWorkoutCreationBinding;
import com.example.gymevo.models.Workout;
import com.example.gymevo.models.ExerciseInWorkout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class WorkoutCreationFragment extends Fragment {

    private WorkoutCreationViewModel viewModel;
    private FragmentWorkoutCreationBinding binding;
    private RecyclerView workoutRecyclerView;
    private Spinner workoutSpinner;
    private LocalDate selectedDate = LocalDate.now(); // Initialize with today's date or some default value

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialize ViewBinding
        binding = FragmentWorkoutCreationBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(WorkoutCreationViewModel.class);

        // Initialize UI components
        setupUI();

        return binding.getRoot();
    }

    private void setupUI() {
        Context context = getContext();
        if (context == null) return;

        // Initialize RecyclerView and Spinner
        workoutRecyclerView = binding.recyclerViewExercises;
        workoutSpinner = binding.spinnerWorkoutSelection;

        initWorkoutRecyclerView();
        setupWorkoutSpinner();

        // Handle "Pick Date" button click
        binding.buttonPickDate.setOnClickListener(v -> viewModel.pickDate(requireContext()));

        // Handle "Add Exercise" button click
        binding.addExerciseButton.setOnClickListener(v -> {
            // Logic to add an exercise
        });

        // Handle "Save Workout" button click
        binding.saveButton.setOnClickListener(v -> {
            viewModel.saveWorkout();
            requireActivity().onBackPressed(); // Go back after saving
        });
    }

    private void setupWorkoutSpinner() {
        // Load workouts and set up the Spinner
        viewModel.getWorkoutsLiveData().observe(getViewLifecycleOwner(), workouts -> {
            if (workouts != null) {
                List<String> workoutNames = new ArrayList<>();
                for (Workout workout : workouts) {
                    workoutNames.add(workout.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, workoutNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                workoutSpinner.setAdapter(adapter);

                // Handle Spinner item selection
                workoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Workout selectedWorkout = workouts.get(position);
                        viewModel.setCurrentWorkout(selectedWorkout);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Handle case when nothing is selected
                    }
                });
            }
        });
    }

    private void initWorkoutRecyclerView() {
        Context context = getContext();
        if (context == null) return;

        // Create adapter with an empty list
        WorkoutAdapter workoutAdapter = new WorkoutAdapter(context, new ArrayList<>());

        // Configure RecyclerView
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        workoutRecyclerView.setAdapter(workoutAdapter);
        workoutRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Observe LiveData to update the adapter
        viewModel.getExercisesForWorkoutOnDate(selectedDate).observe(getViewLifecycleOwner(), exercisesInWorkout -> {
            if (exercisesInWorkout != null) {
                workoutAdapter.setExercises(exercisesInWorkout);
            } else {
                workoutAdapter.setExercises(new ArrayList<>());
            }
        });
    }
}
