package com.example.gymevo.ui.workoutTracker;

import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.CalendarAdapter;
import com.example.gymevo.R;
import com.example.gymevo.WorkoutAdapter;
import com.example.gymevo.databinding.FragmentWorkoutTrackerBinding;
import com.example.gymevo.models.ExerciseInWorkout;
import com.example.gymevo.utils.CalendarUtils;

import java.time.LocalDate;
import java.util.ArrayList;

public class WorkoutTrackerFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private FragmentWorkoutTrackerBinding binding;
    private WorkoutTrackerViewModel workoutTrackerViewModel;
    private RecyclerView calendarRecyclerView;
    private RecyclerView workoutRecyclerView;
    private LocalDate selectedDate;
    private boolean isMonthView = true;
    public TextView oneMonthText;
    public TextView oneYearText;
    private GestureDetector gestureDetector;

    public static WorkoutTrackerFragment newInstance() {
        return new WorkoutTrackerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        selectedDate = LocalDate.now();
        binding = FragmentWorkoutTrackerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initWidgets(root);
        workoutTrackerViewModel = new ViewModelProvider(this).get(WorkoutTrackerViewModel.class);
        initWorkoutRecyclerView(selectedDate);

        // Initialize GestureDetector for swipe detection
        gestureDetector = CalendarUtils.createGestureDetector(getContext(), this, selectedDate, calendarRecyclerView, isMonthView, this);

        // Attach the OnTouchListener to detect gestures
        calendarRecyclerView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Set the calendar view
        CalendarUtils.setCalendarView(getContext(), calendarRecyclerView, selectedDate, true, this);

        // TODO: Refactor this to use the new CalendarUtils class
        // Set the month and year text
        oneMonthText.setText(CalendarUtils.monthFromDateString(selectedDate));
        oneYearText.setText(String.valueOf(selectedDate.getYear()));

        return root;
    }

    private void initWidgets(View root) {
        calendarRecyclerView = root.findViewById(R.id.calendarRecyclerView);
        workoutRecyclerView = binding.WorkoutRecyclerView;

        // TODO: Refactor this to use the new CalendarUtils class
        // Set the month and year text
        oneMonthText = root.findViewById(R.id.oneMonthText);
        oneYearText = root.findViewById(R.id.oneYearText);
    }

    public void initWorkoutRecyclerView(LocalDate selectedDate) {
        Context context = getContext();
        if (context == null) return;

        WorkoutAdapter workoutAdapter = new WorkoutAdapter(context, new ArrayList<>());
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        workoutRecyclerView.setAdapter(workoutAdapter);
        workoutRecyclerView.setItemAnimator(new DefaultItemAnimator());

        workoutTrackerViewModel.getExercisesForWorkoutOnDate(selectedDate).observe(getViewLifecycleOwner(), exercisesInWorkout -> {
            if (exercisesInWorkout != null && !exercisesInWorkout.isEmpty()) {
                workoutAdapter.setExercises(exercisesInWorkout);
            } else {
                workoutAdapter.setExercises(new ArrayList<>());
            }
        });
    }

    @Override
    public void onItemClick(int position, @NonNull String dayText) {
        if (dayText.isEmpty()) return;

        int day = Integer.parseInt(dayText);
        selectedDate = LocalDate.of(selectedDate.getYear(), selectedDate.getMonthValue(), day);

        initWorkoutRecyclerView(selectedDate);
    }
}