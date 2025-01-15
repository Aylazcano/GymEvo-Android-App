package com.example.gymevo.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.CalendarAdapter;
import com.example.gymevo.ui.workoutTracker.WorkoutTrackerFragment;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarUtils {

    public static String monthFromDateString(LocalDate date) {
        return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM."));
    }

    public static ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysArray = new ArrayList<>();
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());

        while (!firstDayOfMonth.isAfter(lastDayOfMonth)) {
            daysArray.add(String.valueOf(firstDayOfMonth.getDayOfMonth()));
            firstDayOfMonth = firstDayOfMonth.plusDays(1);
        }

        return daysArray;
    }

    public static ArrayList<String> daysInWeekArray(LocalDate date) {
        ArrayList<String> daysArray = new ArrayList<>();
        LocalDate startOfWeek = date.minusDays(date.getDayOfWeek().getValue() - 1);

        for (int i = 0; i < 7; i++) {
            daysArray.add(String.valueOf(startOfWeek.plusDays(i).getDayOfMonth()));
        }

        return daysArray;
    }

    public static void setCalendarView(Context context, RecyclerView calendarRecyclerView, LocalDate selectedDate, boolean isMonthView, CalendarAdapter.OnItemListener listener) {
        ArrayList<String> daysArray = isMonthView ? daysInMonthArray(selectedDate) : daysInWeekArray(selectedDate);
        CalendarAdapter calendarAdapter = new CalendarAdapter(context, daysArray, listener);
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(context, 7));
        calendarRecyclerView.setAdapter(calendarAdapter);

        ViewGroup.LayoutParams params = calendarRecyclerView.getLayoutParams();
        params.height = isMonthView
                ? (int) (240 * context.getResources().getDisplayMetrics().density)
                : (int) (40 * context.getResources().getDisplayMetrics().density);
        calendarRecyclerView.setLayoutParams(params);
    }

    public static GestureDetector createGestureDetector(Context context, CalendarAdapter.OnItemListener listener, LocalDate selectedDate, RecyclerView calendarRecyclerView, boolean isMonthView, WorkoutTrackerFragment workoutTrackerFragment) {
        return new GestureDetector(context, new GestureListener(context, listener, selectedDate, calendarRecyclerView, isMonthView, workoutTrackerFragment));
    }

    public static class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        private final Context context;
        private final CalendarAdapter.OnItemListener listener;
        private LocalDate selectedDate;
        private final RecyclerView calendarRecyclerView;
        private boolean isMonthView;
        private final WorkoutTrackerFragment workoutTrackerFragment;

        public GestureListener(Context context, CalendarAdapter.OnItemListener listener, LocalDate selectedDate, RecyclerView calendarRecyclerView, boolean isMonthView, WorkoutTrackerFragment workoutTrackerFragment) {
            this.context = context;
            this.listener = listener;
            this.selectedDate = selectedDate;
            this.calendarRecyclerView = calendarRecyclerView;
            this.isMonthView = isMonthView;
            this.workoutTrackerFragment = workoutTrackerFragment;
        }

        public void onItemClick(int position, @NonNull String dayText) {
            if (dayText.isEmpty()) return;

            int day = Integer.parseInt(dayText);
            selectedDate = LocalDate.of(selectedDate.getYear(), selectedDate.getMonthValue(), day);

            String message = String.format("Selected date %d %s %d", day, selectedDate.getMonth().toString(), selectedDate.getYear());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

            workoutTrackerFragment.initWorkoutRecyclerView(selectedDate);
        }

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

        private void onSwipeLeft() {
            if (isMonthView) {
                selectedDate = selectedDate.plusMonths(1);
            } else {
                selectedDate = selectedDate.plusWeeks(1);
            }
            updateView();
        }

        private void onSwipeRight() {
            if (isMonthView) {
                selectedDate = selectedDate.minusMonths(1);
            } else {
                selectedDate = selectedDate.minusWeeks(1);
            }
            updateView();
        }

        private void onSwipeUp() {
            if (isMonthView) {
                isMonthView = false;
                setCalendarView(context, calendarRecyclerView, selectedDate, isMonthView, listener);
                Toast.makeText(context, "Swipe Up - Switch to Week View", Toast.LENGTH_SHORT).show();
            }
        }

        private void onSwipeDown() {
            if (!isMonthView) {
                isMonthView = true;
                setCalendarView(context, calendarRecyclerView, selectedDate, isMonthView, listener);
                Toast.makeText(context,"Swipe Down - Switch to Month View", Toast.LENGTH_SHORT).show();
            }
        }

        private void updateView() {
            CalendarUtils.setCalendarView(context, calendarRecyclerView, selectedDate, isMonthView, listener);
            workoutTrackerFragment.oneMonthText.setText(monthFromDateString(selectedDate));
            workoutTrackerFragment.oneYearText.setText(String.valueOf(selectedDate.getYear()));
        }
    }
}