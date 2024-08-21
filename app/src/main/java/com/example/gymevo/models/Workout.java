package com.example.gymevo.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Workout {
    private Long id;
    private String name;
    private LocalDate date;
    private List<ExerciseInWorkout> exercisesList;

    // Constructeurs optimisés
    public Workout(Long id, String name, LocalDate date) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.exercisesList = new ArrayList<>();
    }

    public Workout(Long id, LocalDate date) {
        this(id, null, date);
    }

    public Workout() {
        this(null, null, null);
    }

    // Méthodes pour gérer les exercices
    public void addExercise(ExerciseInWorkout exercise) {
        if (exercise != null && !exercisesList.contains(exercise)) {
            exercisesList.add(exercise);
            exercise.setWorkout(this);
        }
    }

    public void removeExercise(ExerciseInWorkout exercise) {
        if (exercise != null && exercisesList.remove(exercise)) {
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
