package com.example.gymevo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.models.ExerciseInWorkout;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutViewHolder> {

    private Context context;
    private List<ExerciseInWorkout> exercises;

    public WorkoutAdapter(Context context, List<ExerciseInWorkout> exercises) {
        this.context = context;
        this.exercises = exercises;
    }

    @Override
    public WorkoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise_in_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WorkoutViewHolder holder, int position) {
        // Get the exercise at the current position
        ExerciseInWorkout exercise = exercises.get(position);

        // Bind the data to the views
        holder.bindData(exercise);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void setExercises(List<ExerciseInWorkout> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }
}
