package com.example.gymevo.ui.common.calendar;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Set;

public class CalendarUtils {

    private static final int DAYS_IN_WEEK = 7;
    private static final int MONTH_VIEW_HEIGHT_DP = 200;
    private static final int WEEK_VIEW_HEIGHT_DP = 36;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM.");

    private CalendarUtils() {
    }

    public static String monthFromDateString(LocalDate date) {
        return date.format(MONTH_FORMATTER);
    }

    public static ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysArray = new ArrayList<>();
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());

        int leadingBlanks = firstDayOfMonth.getDayOfWeek().getValue() % DAYS_IN_WEEK; // Sunday = 0
        for (int i = 0; i < leadingBlanks; i++) {
            daysArray.add("");
        }

        while (!firstDayOfMonth.isAfter(lastDayOfMonth)) {
            daysArray.add(String.valueOf(firstDayOfMonth.getDayOfMonth()));
            firstDayOfMonth = firstDayOfMonth.plusDays(1);
        }

        while (daysArray.size() % DAYS_IN_WEEK != 0) {
            daysArray.add("");
        }

        return daysArray;
    }

    public static ArrayList<String> daysInWeekArray(LocalDate date) {
        ArrayList<String> daysArray = new ArrayList<>();
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            daysArray.add(String.valueOf(startOfWeek.plusDays(i).getDayOfMonth()));
        }

        return daysArray;
    }

    public static void setCalendarView(Context context, RecyclerView calendarRecyclerView, LocalDate displayDate, boolean isMonthView, CalendarAdapter.OnItemListener listener, Set<LocalDate> datesWithExercises, LocalDate selectedDate) {
        ArrayList<String> daysArray = isMonthView ? daysInMonthArray(displayDate) : daysInWeekArray(displayDate);
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysArray, listener, displayDate, isMonthView, datesWithExercises, selectedDate);
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(context, DAYS_IN_WEEK));
        calendarRecyclerView.setAdapter(calendarAdapter);

        ViewGroup.LayoutParams params = calendarRecyclerView.getLayoutParams();
        params.height = dpToPx(context, isMonthView ? MONTH_VIEW_HEIGHT_DP : WEEK_VIEW_HEIGHT_DP);
        calendarRecyclerView.setLayoutParams(params);
    }

    public static void updateCalendarHeader(LocalDate selectedDate, TextView monthText, TextView yearText) {
        if (monthText != null) {
            monthText.setText(monthFromDateString(selectedDate));
        }
        if (yearText != null) {
            yearText.setText(String.valueOf(selectedDate.getYear()));
        }
    }

    public static GestureDetector createGestureDetector(Context context, CalendarAdapter.OnItemListener listener, LocalDate displayDate, RecyclerView calendarRecyclerView, boolean isMonthView, TextView monthText, TextView yearText, ViewModeListener viewModeListener, Set<LocalDate> datesWithExercises, DateChangeListener dateChangeListener, SelectedDateProvider selectedDateProvider, DisplayDateProvider displayDateProvider) {
        return new GestureDetector(context, new GestureListener(context, listener, displayDate, calendarRecyclerView, isMonthView, monthText, yearText, viewModeListener, datesWithExercises, dateChangeListener, selectedDateProvider, displayDateProvider));
    }

    public interface ViewModeListener {
        void onViewModeChanged(boolean isMonthView);
    }

    public interface DateChangeListener {
        void onDateChanged(LocalDate selectedDate);
    }

    public interface SelectedDateProvider {
        LocalDate getSelectedDate();
    }

    public interface DisplayDateProvider {
        LocalDate getDisplayDate();
    }

    public static class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 60;
        private static final int SWIPE_VELOCITY_THRESHOLD = 60;
        private final Context context;
        private final CalendarAdapter.OnItemListener listener;
        private LocalDate displayDate;
        private final RecyclerView calendarRecyclerView;
        private boolean isMonthView;
        private final TextView monthText;
        private final TextView yearText;

        private final ViewModeListener viewModeListener;
        private final Set<LocalDate> datesWithExercises;
        private final DateChangeListener dateChangeListener;
        private final SelectedDateProvider selectedDateProvider;
        private final DisplayDateProvider displayDateProvider;

        public GestureListener(Context context, CalendarAdapter.OnItemListener listener, LocalDate displayDate, RecyclerView calendarRecyclerView, boolean isMonthView, TextView monthText, TextView yearText, ViewModeListener viewModeListener, Set<LocalDate> datesWithExercises, DateChangeListener dateChangeListener, SelectedDateProvider selectedDateProvider, DisplayDateProvider displayDateProvider) {
            this.context = context;
            this.listener = listener;
            this.displayDate = displayDate;
            this.calendarRecyclerView = calendarRecyclerView;
            this.isMonthView = isMonthView;
            this.monthText = monthText;
            this.yearText = yearText;
            this.viewModeListener = viewModeListener;
            this.datesWithExercises = datesWithExercises;
            this.dateChangeListener = dateChangeListener;
            this.selectedDateProvider = selectedDateProvider;
            this.displayDateProvider = displayDateProvider;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            if (displayDateProvider != null) {
                LocalDate latest = displayDateProvider.getDisplayDate();
                if (latest != null) {
                    displayDate = latest;
                }
            }

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) onSwipeRight();
                    else onSwipeLeft();
                    return true;
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) onSwipeDown();
                    else onSwipeUp();
                    return true;
                }
            }
            return false;
        }

        private void onSwipeLeft() {
            displayDate = isMonthView ? displayDate.plusMonths(1) : displayDate.plusWeeks(1);
            updateCalendarView();
        }

        private void onSwipeRight() {
            displayDate = isMonthView ? displayDate.minusMonths(1) : displayDate.minusWeeks(1);
            updateCalendarView();
        }

        private void onSwipeUp() {
            if (isMonthView) {
                isMonthView = false;
                notifyViewModeChanged();
                updateCalendarView();
            }
        }

        private void onSwipeDown() {
            if (!isMonthView) {
                isMonthView = true;
                notifyViewModeChanged();
                updateCalendarView();
            }
        }

        private void notifyViewModeChanged() {
            if (viewModeListener != null) {
                viewModeListener.onViewModeChanged(isMonthView);
            }
        }

        private void updateCalendarView() {
            LocalDate selectedDate = selectedDateProvider != null ? selectedDateProvider.getSelectedDate() : null;
            CalendarUtils.setCalendarView(context, calendarRecyclerView, displayDate, isMonthView, listener, datesWithExercises, selectedDate);
            CalendarUtils.updateCalendarHeader(displayDate, monthText, yearText);
            if (dateChangeListener != null) {
                dateChangeListener.onDateChanged(displayDate);
            }
        }
    }

    private static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
