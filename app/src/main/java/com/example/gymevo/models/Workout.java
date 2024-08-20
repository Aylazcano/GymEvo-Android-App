package com.example.gymevo.models;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Workout {
    private Long id;
    private String name;
    private LocalDate date;
    private List<ExerciseInWorkout> exercisesList;

    public Workout(Long id,String name, LocalDate date) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.exercisesList = new ArrayList<>();
    }
    public Workout(Long id, LocalDate date) {
        this.id = id;
        this.date = date;
        this.exercisesList = new ArrayList<>();
    }

    public Workout() {
        this.id = null;
        this.name = null;
        this.date = null;
        this.exercisesList = new ArrayList<>();
    }

    public void addExercise(ExerciseInWorkout exercise) {
        if (!exercisesList.contains(exercise)) {
            exercisesList.add(exercise);
            exercise.setWorkout(this);
        }
    }

    public void removeExercise(ExerciseInWorkout exercise) {
        if (exercisesList.remove(exercise)) {
            exercise.setWorkout(null);
        }
    }

    // Getters et setters...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<ExerciseInWorkout> getExercisesList() {
        return exercisesList;
    }

    public void setExercisesList(List<ExerciseInWorkout> exercisesList) {
        this.exercisesList = exercisesList;
    }

    public void observe(LifecycleOwner viewLifecycleOwner, Observer<Workout> observer) {

    }
}