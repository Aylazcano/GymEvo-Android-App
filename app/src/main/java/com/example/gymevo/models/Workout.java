package com.example.gymevo.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "workout")
public class Workout {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String name;
    private LocalDate date;

    @Ignore // This is not stored in the database, it's for runtime use
    private List<ExerciseInWorkout> exercisesList;

    public Workout(Long id, String name, LocalDate date) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.exercisesList = new ArrayList<>();
    }

    @Ignore
    public Workout() {
        this.id = null;
        this.name = null;
        this.date = null;
        this.exercisesList = new ArrayList<>();
    }

    // Methods to manage exercises (not stored in DB, used at runtime)
    public void addExercise(ExerciseInWorkout exercise) {
        if (!exercisesList.contains(exercise)) {
            exercisesList.add(exercise);
            exercise.setWorkoutId(this.id); // Set the workout ID for ExerciseInWorkout
        }
    }

    public void removeExercise(ExerciseInWorkout exercise) {
        exercisesList.remove(exercise);
        exercise.setWorkoutId(null); // Remove the workout ID for ExerciseInWorkout
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<ExerciseInWorkout> getExercisesList() {
        return exercisesList;
    }

    public void setExercisesList(List<ExerciseInWorkout> exercisesList) {
        this.exercisesList = exercisesList;
    }
}
