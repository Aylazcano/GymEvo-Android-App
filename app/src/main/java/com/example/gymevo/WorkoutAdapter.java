package com.example.gymevo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;


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

        // Charger les images avec une bibliothèque comme Glide ou Picasso
        // Glide.with(context).load(exercise.getExercice().getStartImage()).into(holder.ExeciceIV);
    }

    @Override
    public int getItemCount() {
        return exerciseInWorkoutList.size();
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

    public static class WeekViewActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }
}
