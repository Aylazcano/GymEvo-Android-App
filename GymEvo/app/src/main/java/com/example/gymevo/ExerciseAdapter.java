package com.example.gymevo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.example.gymevo.models.ExerciseInWorkout;

import java.util.List;


public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
    private List<ExerciseInWorkout> exercisesList;
    private Context context;

    public ExerciseAdapter(Context context, List<ExerciseInWorkout> exercisesList) {
        this.context = context;
        this.exercisesList = exercisesList;
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exercice_item, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseViewHolder holder, int position) {
        ExerciseInWorkout exercise = exercisesList.get(position);
        holder.ExerciseNameTV.setText(exercise.getExercice().getName());
        holder.MusclesTargetsTV.setText(exercise.getExercice().getTargetedMuscle());
        holder.SeriesQtyTV.setText(String.valueOf(exercise.getSeries()));
        holder.RepetitionsQtyTV.setText(String.valueOf(exercise.getRepetitions()));
        holder.WeightNumTV.setText(String.valueOf(exercise.getWeight()));

        // Charger les images avec une bibliothèque comme Glide ou Picasso
        // Glide.with(context).load(exercise.getExercice().getStartImage()).into(holder.ExeciceIV);
    }

    @Override
    public int getItemCount() {
        return exercisesList.size();
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView ExerciseNameTV, MusclesTargetsTV, SeriesQtyTV, RepetitionsQtyTV, WeightNumTV;
        ImageView ExeciceIV;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            ExeciceIV = itemView.findViewById(R.id.ExeciceIV);
            ExerciseNameTV = itemView.findViewById(R.id.ExerciceNameTV);
            MusclesTargetsTV = itemView.findViewById(R.id.MusclesTargetsTV);
            SeriesQtyTV = itemView.findViewById(R.id.SeriesQtyTV);
            RepetitionsQtyTV = itemView.findViewById(R.id.RepetitionsQtyTV);
            WeightNumTV = itemView.findViewById(R.id.WeightNumTV);
        }
    }
}
