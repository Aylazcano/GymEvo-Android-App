package com.example.gymevo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.models.Exercise;
import com.example.gymevo.models.ExerciseInWorkout;
import com.example.gymevo.models.Workout;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WorkoutAdapter adapter;
    private Workout workout;
    private List<Exercise> allExercises; // List of all exercises

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_fragment_workout_detail);

        recyclerView = findViewById(R.id.taskRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize workout and allExercises with your data
        // For example, get workout from intent or database and load all exercises
        workout = getWorkoutFromIntent();
        allExercises = getAllExercises(); // Load all exercises

        // Pass both lists to the adapter
        adapter = new WorkoutAdapter(this, workout.getExercises(), allExercises);
        recyclerView.setAdapter(adapter);
    }

    private Workout getWorkoutFromIntent() {
        // Code to get workout from Intent or other data source
        // For example:
        // return (Workout) getIntent().getSerializableExtra("WORKOUT_KEY");
        return new Workout(); // Example
    }

    private List<Exercise> getAllExercises() {
        // Code to get all exercises from data source
        // For example, from database or ViewModel
        return new ArrayList<>(); // Example
    }
}
