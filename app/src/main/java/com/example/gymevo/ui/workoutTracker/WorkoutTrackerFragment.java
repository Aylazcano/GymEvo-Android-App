package com.example.gymevo.ui.workoutTracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.CalendarAdapter;
import com.example.gymevo.R;
import com.example.gymevo.WorkoutAdapter;
import com.example.gymevo.databinding.FragmentWorkoutTrackerBinding;
import com.example.gymevo.models.Exercise;
import com.example.gymevo.models.ExerciseInWorkout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class WorkoutTrackerFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private FragmentWorkoutTrackerBinding binding;
    private WorkoutTrackerViewModel workoutTrackerViewModel;
    private RecyclerView calendarRecyclerView;
    private RecyclerView workoutRecyclerView;
    private LocalDate selectedDate;
    private GestureDetector gestureDetector;
    private boolean isMonthView = true;
    private TextView oneMonthText;
    private TextView oneYearText;

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
        initGestureDetector();

        setMonthView();

        return root;
    }

    private void initWidgets(View root) {
        calendarRecyclerView = root.findViewById(R.id.calendarRecyclerView);
        workoutRecyclerView = binding.WorkoutRecyclerView;
        oneMonthText = root.findViewById(R.id.oneMonthText);
        oneYearText = root.findViewById(R.id.oneYearText);

        // Initialize other widgets as needed
    }

    private void initWorkoutRecyclerView(LocalDate selectedDate) {
        Context context = getContext();
        if (context == null) return;

        // Create an empty adapter with placeholder lists
        WorkoutAdapter workoutAdapter = new WorkoutAdapter(context, new ArrayList<>(), new ArrayList<>());
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        workoutRecyclerView.setAdapter(workoutAdapter);
        workoutRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Observe exercises for the selected date
        workoutTrackerViewModel.getExercisesForWorkoutOnDate(selectedDate).observe(getViewLifecycleOwner(), exercisesInWorkout -> {
            if (exercisesInWorkout != null && !exercisesInWorkout.isEmpty()) {
                // Mettre à jour les données de l'adaptateur
                workoutAdapter.setExercises(exercisesInWorkout);
            } else {
                // Gérer le cas où il n'y a pas d'exercices pour la date sélectionnée
                workoutAdapter.setExercises(new ArrayList<>());
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new GestureListener());

        binding.calendarLayout.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        calendarRecyclerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    @Override
    public void onItemClick(int position, @NonNull String dayText) {
        if (dayText.isEmpty()) return;

        int day = Integer.parseInt(dayText);
        selectedDate = LocalDate.of(selectedDate.getYear(), selectedDate.getMonthValue(), day);

        String message = String.format("Selected date %d %s %d", day, selectedDate.getMonth().toString(), selectedDate.getYear());
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

        initWorkoutRecyclerView(selectedDate);
    }

    private void onSwipeUp() {
        if (isMonthView) {
            Toast.makeText(getContext(), "Swipe Up - Switch to Week View", Toast.LENGTH_SHORT).show();
            setWeekView();
        }
    }

    private void onSwipeDown() {
        if (!isMonthView) {
            Toast.makeText(getContext(), "Swipe Down - Switch to Month View", Toast.LENGTH_SHORT).show();
            setMonthView();
        }
    }

    private void onSwipeLeft() {
        if (isMonthView) {
            nextMonthAction();
        } else {
            nextWeekAction();
        }
    }

    private void onSwipeRight() {
        if (isMonthView) {
            previousMonthAction();
        } else {
            previousWeekAction();
        }
    }

    private void nextWeekAction() {
        selectedDate = selectedDate.plusWeeks(1);
        setWeekView();
    }

    private void previousWeekAction() {
        selectedDate = selectedDate.minusWeeks(1);
        setWeekView();
    }

    private void nextMonthAction() {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    private void previousMonthAction() {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    private void setMonthView() {
        CalendarAdapter calendarAdapter = new CalendarAdapter(getContext(), CalendarAdapter.daysInMonthArray(selectedDate), this);
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);
        calendarRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ViewGroup.LayoutParams params = calendarRecyclerView.getLayoutParams();
        params.height = (int) (240 * getResources().getDisplayMetrics().density);
        calendarRecyclerView.setLayoutParams(params);

        // Update month and year texts
        oneMonthText.setText(monthFromDateString(selectedDate));
        oneYearText.setText(String.valueOf(selectedDate.getYear()));

        isMonthView = true;
    }

    private void setWeekView() {
        CalendarAdapter calendarAdapter = new CalendarAdapter(getContext(), CalendarAdapter.daysInWeekArray(selectedDate), this);
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);
        calendarRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ViewGroup.LayoutParams params = calendarRecyclerView.getLayoutParams();
        params.height = (int) (40 * getResources().getDisplayMetrics().density);
        calendarRecyclerView.setLayoutParams(params);

        // You may also want to update the TextViews for week view if applicable
        oneMonthText.setText(monthFromDateString(selectedDate));
        oneYearText.setText(String.valueOf(selectedDate.getYear()));

        isMonthView = false;
    }

    private String monthFromDateString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM.");
        return date.format(formatter);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    return true;
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeDown();
                    } else {
                        onSwipeUp();
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
