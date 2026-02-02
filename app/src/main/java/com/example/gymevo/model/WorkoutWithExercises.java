package com.example.gymevo.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class WorkoutWithExercises {
    @Embedded
    public Workout workout;

    @Relation(parentColumn = "id", entityColumn = "workoutId")
    public List<ExerciseInWorkout> exercises = new ArrayList<>();
}
