package com.example.gymevo.models;

import java.util.Date;
import java.util.List;

public class Workout {
    private Long id;
    private Date date;
    private List<ExerciseInWorkout> exercisesList;

    // Constructeurs, getters et setters
    public Workout(Long id, Date date, List<ExerciseInWorkout> exercisesList) {
        this.id = id;
        this.date = date;
        this.exercisesList = exercisesList;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public List<ExerciseInWorkout> getExercisesList() { return exercisesList; }
    public void setExercisesList(List<ExerciseInWorkout> exercisesList) { this.exercisesList = exercisesList; }
}
