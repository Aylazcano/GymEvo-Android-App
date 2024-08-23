package com.example.gymevo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.models.Workout;


public class WorkoutDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WorkoutAdapter adapter;
    private Workout workout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_fragment_workout_detail);

        recyclerView = findViewById(R.id.taskRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialiser le workout avec les données (par exemple, en le récupérant à partir de la base de données ou d'un Intent)
//        workout = getWorkoutFromIntent();

        adapter = new WorkoutAdapter(this, workout.getExercises());
        recyclerView.setAdapter(adapter);
    }

//    private Workout getWorkoutFromIntent() {
//        // Code pour récupérer le workout à partir de l'Intent ou d'une autre source de données
//        // Par exemple :
//        // return (Workout) getIntent().getSerializableExtra("WORKOUT_KEY");
//        return new Workout(1L, new Date(), new ArrayList<>()); // Exemple statique
//    }
}
