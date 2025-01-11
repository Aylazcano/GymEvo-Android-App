package com.example.gymevo;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.models.ExerciseInWorkout;

public class WorkoutViewHolder extends RecyclerView.ViewHolder {

    // Declarations for the views
    private ImageView exerciseImageView;
    private TextView exerciseNameTextView;
    private TextView targetedMusclesTextView;
    private TextView seriesTextView;
    private TextView seriesQtyTextView;
    private TextView repetitionsTextView;
    private TextView repetitionsQtyTextView;
    private TextView weightTextView;
    private TextView weightQtyTextView;

    public WorkoutViewHolder(View itemView) {
        super(itemView);

        // Initialize the views
        exerciseImageView = itemView.findViewById(R.id.ExerciseIV);
        exerciseNameTextView = itemView.findViewById(R.id.ExerciseNameTV);
        targetedMusclesTextView = itemView.findViewById(R.id.TargetedMusclesTV);
        seriesTextView = itemView.findViewById(R.id.SeriesTV);
        seriesQtyTextView = itemView.findViewById(R.id.SeriesQtyTV);
        repetitionsTextView = itemView.findViewById(R.id.RepetitionsTV);
        repetitionsQtyTextView = itemView.findViewById(R.id.RepetitionsQtyTV);
        weightTextView = itemView.findViewById(R.id.WeightTV);
        weightQtyTextView = itemView.findViewById(R.id.WeightNumTV);
    }

    public void bindData(ExerciseInWorkout exercise) {
        Log.d("WorkoutViewHolder", "Targeted Muscles: " + exercise.getTargetedMuscles());

        exerciseNameTextView.setText(exercise.getName());
        targetedMusclesTextView.setText(exercise.getTargetedMuscles());
        seriesQtyTextView.setText(String.valueOf(exercise.getSeries()));
        repetitionsQtyTextView.setText(String.valueOf(exercise.getRepetitions()));
        weightQtyTextView.setText(String.valueOf(exercise.getWeight()));

        // Vérifiez si une image existe pour l'illustration
        if (exercise.getStartImage() != null) {
            // Utilisez Glide ou Picasso pour charger l'image
        } else {
            exerciseImageView.setImageResource(R.drawable.ic_menu_gallery); // Placez ici une image par défaut si nécessaire
        }
    }

}
