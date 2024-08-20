package com.example.gymevo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gymevo.models.ExerciseInWorkout;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ExerciseViewHolder> {
    private List<ExerciseInWorkout> exerciseInWorkoutList;
    private Context context;

    public WorkoutAdapter(Context context, List<ExerciseInWorkout> exerciseInWorkoutList) {
        this.context = context;
        this.exerciseInWorkoutList = exerciseInWorkoutList;
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exercice_in_workout_item, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseViewHolder holder, int position) {
        ExerciseInWorkout exercise = exerciseInWorkoutList.get(position);
        holder.ExerciseNameTV.setText(exercise.getExercice().getName());
        holder.MusclesTargetsTV.setText(exercise.getExercice().getTargetedMuscle());
        holder.SeriesQtyTV.setText(String.valueOf(exercise.getSeries()));
        holder.RepetitionsQtyTV.setText(String.valueOf(exercise.getRepetitions()));
        holder.WeightNumTV.setText(String.valueOf(exercise.getWeight()));

        // Charger les images avec Glide
        Glide.with(context).load(exercise.getExercice().getStartImage()).into(holder.ExerciseIV);
    }

    // Méthode pour mettre à jour les données de l'adaptateur
    public void setExercises(List<ExerciseInWorkout> exercises) {
        this.exerciseInWorkoutList = exercises;
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
