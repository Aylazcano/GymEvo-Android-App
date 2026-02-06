package com.example.gymevo.ui.common.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.ui.common.ImageUi;

public class WorkoutExerciseViewHolder extends RecyclerView.ViewHolder {

    private static final String EMPTY_VALUE = "-";

    private final ImageView exerciseImageView;
    private final TextView exerciseNameTextView;
    private final TextView targetedMusclesTextView;
    private final TextView seriesQtyTextView;
    private final TextView repetitionsQtyTextView;
    private final TextView weightQtyTextView;
    private final TextView weightLabelTextView;
    private final TextView timeQtyTextView;
    private final TextView heartRateQtyTextView;
    private final View seriesLayout;
    private final View repsLayout;
    private final View weightLayout;
    private final View timeLayout;
    private final View heartRateLayout;
    private final ImageView dragHandle;
    private final ImageView removeIcon;

    public WorkoutExerciseViewHolder(View itemView) {
        super(itemView);

        exerciseImageView = itemView.findViewById(R.id.ExerciseIV);
        exerciseNameTextView = itemView.findViewById(R.id.ExerciseNameTV);
        targetedMusclesTextView = itemView.findViewById(R.id.TargetedMusclesTV);
        seriesQtyTextView = itemView.findViewById(R.id.SeriesQtyTV);
        repetitionsQtyTextView = itemView.findViewById(R.id.RepetitionsQtyTV);
        weightQtyTextView = itemView.findViewById(R.id.WeightNumTV);
        weightLabelTextView = itemView.findViewById(R.id.WeightTV);
        timeQtyTextView = itemView.findViewById(R.id.TimeQtyTV);
        heartRateQtyTextView = itemView.findViewById(R.id.HeartRateQtyTV);
        seriesLayout = itemView.findViewById(R.id.layout_metric_series);
        repsLayout = itemView.findViewById(R.id.layout_metric_reps);
        weightLayout = itemView.findViewById(R.id.layout_metric_weight);
        timeLayout = itemView.findViewById(R.id.layout_metric_time);
        heartRateLayout = itemView.findViewById(R.id.layout_metric_hr);
        dragHandle = itemView.findViewById(R.id.image_drag_handle);
        removeIcon = itemView.findViewById(R.id.image_remove);
    }

    public ImageView getDragHandle() {
        return dragHandle;
    }

    public ImageView getRemoveIcon() {
        return removeIcon;
    }

    public void bind(ExerciseInWorkout exercise) {
        if (exercise == null) {
            bindEmptyState();
            return;
        }
        exerciseNameTextView.setText(exercise.getName());
        targetedMusclesTextView.setText(exercise.getTargetedMusclesLabel());
        seriesQtyTextView.setText(formatValue(exercise.getSeries()));
        repetitionsQtyTextView.setText(formatValue(exercise.getRepetitions()));
        weightQtyTextView.setText(formatValue(exercise.getWeight()));
        if (weightLabelTextView != null) {
            int labelRes = exercise.isWeightInKg()
                ? R.string.exercise_weight_kg
                : R.string.exercise_weight_lbs;
            weightLabelTextView.setText(labelRes);
        }
        timeQtyTextView.setText(formatDuration(exercise.getTime()));
        heartRateQtyTextView.setText(formatValue(exercise.getHeartRates()));
        applyMetricVisibility(exercise);
        ImageUi.startExerciseLoop(exerciseImageView, exercise.getImageA(), exercise.getImageB());
    }

    public void clearImageLoop() {
        ImageUi.stopExerciseLoop(exerciseImageView);
    }

    private String formatValue(Integer value) {
        return value == null ? EMPTY_VALUE : String.valueOf(value);
    }

    private String formatDuration(Integer seconds) {
        if (seconds == null) {
            return EMPTY_VALUE;
        }
        int safe = Math.max(0, seconds);
        int minutes = safe / 60;
        int remaining = safe % 60;
        return String.format("%02d:%02d", minutes, remaining);
    }

    private void applyMetricVisibility(ExerciseInWorkout exercise) {
        if (exercise == null) {
            return;
        }
        if (!exercise.hasAnyDisplayMetric()) {
            exercise.applyDefaultMetricsForType(exercise.getType());
        }
        setVisible(seriesLayout, exercise.isShowSeries());
        setVisible(repsLayout, exercise.isShowRepetitions());
        setVisible(weightLayout, exercise.isShowWeight());
        setVisible(timeLayout, exercise.isShowTime());
        setVisible(heartRateLayout, exercise.isShowHeartRate());
    }

    private void setVisible(View view, boolean visible) {
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void bindEmptyState() {
        exerciseNameTextView.setText("");
        targetedMusclesTextView.setText("");
        seriesQtyTextView.setText(EMPTY_VALUE);
        repetitionsQtyTextView.setText(EMPTY_VALUE);
        weightQtyTextView.setText(EMPTY_VALUE);
        timeQtyTextView.setText(EMPTY_VALUE);
        heartRateQtyTextView.setText(EMPTY_VALUE);
        exerciseImageView.setImageResource(R.drawable.ic_menu_gallery);
    }
}
