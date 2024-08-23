package com.example.gymevo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gymevo.models.Exercise;
import com.example.gymevo.models.ExerciseInWorkout;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ExerciseViewHolder> {
    private List<ExerciseInWorkout> exerciseInWorkoutList;
    private List<Exercise> exerciseList; // List of all exercises to get details
    private Context context;

    public WorkoutAdapter(Context context, List<ExerciseInWorkout> exerciseInWorkoutList, List<Exercise> exerciseList) {
        this.context = context;
        this.exerciseInWorkoutList = exerciseInWorkoutList;
        this.exerciseList = exerciseList;
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exercice_in_workout_item, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseViewHolder holder, int position) {
        ExerciseInWorkout exerciseInWorkout = exerciseInWorkoutList.get(position);
        Exercise exercise = findExerciseById(exerciseInWorkout.getExerciseId());

        if (exercise != null) {
            holder.ExerciseNameTV.setText(exercise.getName());
            holder.MusclesTargetsTV.setText(exercise.getTargetedMuscle());
            holder.SeriesQtyTV.setText(String.valueOf(exerciseInWorkout.getSeries()));
            holder.RepetitionsQtyTV.setText(String.valueOf(exerciseInWorkout.getRepetitions()));
            holder.WeightNumTV.setText(String.valueOf(exerciseInWorkout.getWeight()));

            // Charger les images avec Glide
            Glide.with(context).load(exercise.getStartImage()).into(holder.ExerciseIV);
        }
    }

    // Find exercise by ID from the list
    private Exercise findExerciseById(Long exerciseId) {
        for (Exercise exercise : exerciseList) {
            if (exercise.getId().equals(exerciseId)) {
                return exercise;
            }
        }
        return null; // Exercise not found
    }

    // Méthode pour mettre à jour les données de l'adaptateur
    public void setExercises(List<ExerciseInWorkout> exercises, List<Exercise> exerciseList) {
        this.exerciseInWorkoutList = exercises;
        this.exerciseList = exerciseList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return exerciseInWorkoutList.size();
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView ExerciseNameTV, MusclesTargetsTV, SeriesQtyTV, RepetitionsQtyTV, WeightNumTV;
        ImageView ExerciseIV;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            ExerciseIV = itemView.findViewById(R.id.ExerciseIV);
            ExerciseNameTV = itemView.findViewById(R.id.ExerciseNameTV);
            MusclesTargetsTV = itemView.findViewById(R.id.MusclesTargetsTV);
            SeriesQtyTV = itemView.findViewById(R.id.SeriesQtyTV);
            RepetitionsQtyTV = itemView.findViewById(R.id.RepetitionsQtyTV);
            WeightNumTV = itemView.findViewById(R.id.WeightNumTV);
        }
    }
}
