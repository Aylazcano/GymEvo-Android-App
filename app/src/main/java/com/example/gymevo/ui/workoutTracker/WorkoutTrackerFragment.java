package com.example.gymevo.ui.workoutTracker;

import static java.time.LocalDate.now;

import android.annotation.SuppressLint;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.CalendarAdapter;
import com.example.gymevo.R;
import com.example.gymevo.databinding.FragmentWorkoutTrackerBinding;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class WorkoutTrackerFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private FragmentWorkoutTrackerBinding binding;
    private WorkoutTrackerViewModel mViewModel;
    private TextView oneMonthText;
    private TextView oneYearText;
    private RecyclerView calendarRecyclerView;
    private RecyclerView workoutRecyclerView;
    private LocalDate selectedDate;
    private GestureDetector gestureDetector;
    private boolean isMonthView = true; // Track current view mode

    public static WorkoutTrackerFragment newInstance() {
        return new WorkoutTrackerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutTrackerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize widgets
        initWidgets(root);

        // Initialize GestureDetector
        InitGestureDetector(root);

        selectedDate = LocalDate.now();
        setMonthView();

        WorkoutTrackerViewModel workoutTrackerVM =
                new ViewModelProvider(this).get(WorkoutTrackerViewModel.class);

        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void InitGestureDetector(View root){
        // Set up the gesture detector
        gestureDetector = new GestureDetector(getContext(), new GestureListener());

        // Ensure the root view handles touch events
        root.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;  // Return true to indicate the event was handled
        });

        // Ensure the RecyclerView handles touch events
        calendarRecyclerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;  // Return true to indicate the event was handled
        });
    }

    private void initWidgets(View root) {
        calendarRecyclerView = root.findViewById(R.id.calendarRecyclerView);
        oneMonthText = root.findViewById(R.id.oneMonthText);
        oneYearText = root.findViewById(R.id.oneYearText);

        workoutRecyclerView = binding.WorkoutRecyclerView;
    }

    private String monthFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM.");
        return date.format(formatter);
    }

    private String yearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return date.format(formatter);
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    private void setMonthView() {
        if (oneMonthText != null && oneYearText != null) {
            oneMonthText.setText(monthFromDate(selectedDate));
            oneYearText.setText(yearFromDate(selectedDate));
            ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

            CalendarAdapter calendarAdapter = new CalendarAdapter(getContext(), daysInMonth, this);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext().getApplicationContext(), 7);
            calendarRecyclerView.setLayoutManager(layoutManager);
            calendarRecyclerView.setAdapter(calendarAdapter);

            // Set item animator for smooth animations
            calendarRecyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set the height of the calendarFrame to 240dp
            ViewGroup.LayoutParams params = calendarRecyclerView.getLayoutParams();
            params.height = (int) (240 * getResources().getDisplayMetrics().density);
            calendarRecyclerView.setLayoutParams(params);

            isMonthView = true; // Track current view mode
        } else {
            // Handle case when TextViews are null
            Toast.makeText(getContext(), "Error: TextViews not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> daysInWeekArray(LocalDate date) {
        ArrayList<String> daysInWeekArray = new ArrayList<>();
        LocalDate startOfWeek = date.with(java.time.DayOfWeek.MONDAY);

        for (int i = 0; i < 7; i++) {
            daysInWeekArray.add(String.valueOf(startOfWeek.plusDays(i).getDayOfMonth()));
        }
        return daysInWeekArray;
    }

    private void setWeekView() {
        if (oneMonthText != null && oneYearText != null) {
            oneMonthText.setText(monthFromDate(selectedDate));
            oneYearText.setText(yearFromDate(selectedDate));
            ArrayList<String> daysInWeek = daysInWeekArray(selectedDate);

            CalendarAdapter calendarAdapter = new CalendarAdapter(getContext(), daysInWeek, this);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext().getApplicationContext(), 7);
            calendarRecyclerView.setLayoutManager(layoutManager);
            calendarRecyclerView.setAdapter(calendarAdapter);

            // Set item animator for smooth animations
            calendarRecyclerView.setItemAnimator(new DefaultItemAnimator());

            // Adjust height for week view
            ViewGroup.LayoutParams params = calendarRecyclerView.getLayoutParams();
            params.height = (int) (40 * getResources().getDisplayMetrics().density);
            calendarRecyclerView.setLayoutParams(params);

            isMonthView = false; // Track current view mode
        } else {
            Toast.makeText(getContext(), "Error: TextViews not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    public void previousMonthAction(View view) {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    public void previousWeekAction(View view) {
        selectedDate = selectedDate.minusWeeks(1);
        setWeekView();
    }

    public void nextWeekAction(View view) {
        selectedDate = selectedDate.plusWeeks(1);
        setWeekView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WorkoutTrackerViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            String message = "Selected date " + dayText + " " + monthFromDate(selectedDate) + " "
                    + yearFromDate(selectedDate);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // Gesture listener class
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeDown();
                    } else {
                        onSwipeUp();
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
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
            nextMonthAction(null);
        } else {
            nextWeekAction(null);
        }
    }

    private void onSwipeRight() {
        if (isMonthView) {
            previousMonthAction(null);
        } else {
            previousWeekAction(null);
        }
    }
}
