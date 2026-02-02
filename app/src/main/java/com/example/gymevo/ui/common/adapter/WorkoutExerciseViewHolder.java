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
        ImageUi.startExerciseLoop(exerciseImageView, exercise.getImageA(), exercise.getImageB());
    }

    public void clearImageLoop() {
        ImageUi.stopExerciseLoop(exerciseImageView);
    }

    private String formatValue(Integer value) {
        return value == null ? EMPTY_VALUE : String.valueOf(value);
    }

    private void bindEmptyState() {
        exerciseNameTextView.setText("");
        targetedMusclesTextView.setText("");
        seriesQtyTextView.setText(EMPTY_VALUE);
        repetitionsQtyTextView.setText(EMPTY_VALUE);
        weightQtyTextView.setText(EMPTY_VALUE);
        exerciseImageView.setImageResource(R.drawable.ic_menu_gallery);
    }
}
