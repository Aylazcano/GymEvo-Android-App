package com.example.gymevo.ui.common.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Set;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private static final double MONTH_ROW_RATIO = 1.0 / 6.0;
    private static final int CELL_HEIGHT_DP = 40;
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private final LocalDate displayDate;
    private final LocalDate selectedDate;
    private final boolean isMonthView;
    private final LocalDate today = LocalDate.now();
    private final Set<LocalDate> datesWithExercises;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, LocalDate displayDate, boolean isMonthView, Set<LocalDate> datesWithExercises, LocalDate selectedDate) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.displayDate = displayDate;
        this.isMonthView = isMonthView;
        this.datesWithExercises = datesWithExercises;
        this.selectedDate = selectedDate;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_day_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * MONTH_ROW_RATIO);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String dayText = daysOfMonth.get(position);
        holder.setDayText(dayText);

        LocalDate cellDate = resolveCellDate(dayText, position);
        boolean isSelected = cellDate != null && selectedDate != null && cellDate.equals(selectedDate);
        boolean isToday = cellDate != null && cellDate.equals(today);
        boolean hasWorkout = cellDate != null && datesWithExercises != null && datesWithExercises.contains(cellDate);
        holder.setCellDate(cellDate);
        holder.setState(isSelected, isToday);
        holder.setIndicatorVisible(hasWorkout, isSelected);

        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        params.height = dpToPx(holder.itemView, CELL_HEIGHT_DP);
        holder.itemView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, @NonNull String dayText, @NonNull LocalDate date);
    }

    private LocalDate resolveCellDate(String dayText, int position) {
        if (displayDate == null || dayText == null || dayText.isEmpty()) {
            return null;
        }
        if (isMonthView) {
            int day = Integer.parseInt(dayText);
            return LocalDate.of(displayDate.getYear(), displayDate.getMonth(), day);
        }
        LocalDate startOfWeek = displayDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
        return startOfWeek.plusDays(position);
    }

    private int dpToPx(View view, int dp) {
        return Math.round(dp * view.getContext().getResources().getDisplayMetrics().density);
    }
}
