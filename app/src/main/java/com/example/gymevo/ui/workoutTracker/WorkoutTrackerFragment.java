package com.example.gymevo.ui.workoutTracker;

import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.gymevo.databinding.CalendarHeaderBinding;
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
    private GestureDetector gestureDetector;
    private CalendarHeaderBinding calendarHeaderBinding;

    public static WorkoutTrackerFragment newInstance() {
        return new WorkoutTrackerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutTrackerBinding.inflate(inflater, container, false);
        selectedDate = LocalDate.now();

        // Access the included layout binding manually
        calendarHeaderBinding = CalendarHeaderBinding.bind(binding.getRoot().findViewById(R.id.calendarLayout));

        initializeViews(binding.getRoot());
        initializeViewModel();
        initializeGestureDetector();

        setupCalendar();

        return binding.getRoot();
    }

    private void initializeViews(View root) {
        calendarRecyclerView = root.findViewById(R.id.calendarRecyclerView);
        workoutRecyclerView = binding.WorkoutRecyclerView;
    }

    private void initializeViewModel() {
        workoutTrackerViewModel = new ViewModelProvider(this).get(WorkoutTrackerViewModel.class);
        updateWorkoutRecyclerView(selectedDate);
    }

    private void initializeGestureDetector() {
        gestureDetector = CalendarUtils.createGestureDetector(
                getContext(),
                this,
                selectedDate,
                calendarRecyclerView,
                isMonthView,
                calendarHeaderBinding.oneMonthText,
                calendarHeaderBinding.oneYearText
        );
        calendarRecyclerView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void setupCalendar() {
        Context context = getContext();
        if (context == null) return;

        CalendarUtils.setCalendarView(context, calendarRecyclerView, selectedDate, isMonthView, this);
        CalendarUtils.updateCalendarHeader(selectedDate, calendarHeaderBinding.oneMonthText, calendarHeaderBinding.oneYearText);
    }

    private void updateWorkoutRecyclerView(LocalDate selectedDate) {
        Context context = getContext();
        if (context == null) return;

        WorkoutAdapter workoutAdapter = new WorkoutAdapter(context, new ArrayList<>());
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        workoutRecyclerView.setAdapter(workoutAdapter);
        workoutRecyclerView.setItemAnimator(new DefaultItemAnimator());

        workoutTrackerViewModel.getExercisesForWorkoutOnDate(selectedDate)
                .observe(getViewLifecycleOwner(), exercises -> {
                    if (exercises != null && !exercises.isEmpty()) {
                        workoutAdapter.setExercises(exercises);
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

        showCustomToast(String.format("Selected date %d %s %d",
                day,
                selectedDate.getMonth(),
                selectedDate.getYear())
        );

        updateWorkoutRecyclerView(selectedDate);
    }

    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(
                R.layout.custom_toast,
                (ViewGroup) requireView().findViewById(R.id.custom_toast_container)
        );

        TextView text = layout.findViewById(R.id.text);
        text.setText(message);

        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
