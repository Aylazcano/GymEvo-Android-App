package com.example.gymevo.models;

import java.util.Date;

public class ExerciseInWorkout {
    private Long id;
    private int series;
    private int repetitions;
    private int weight;
    private int time; // assuming time is in seconds
    private int heartRates;
    private Date date;
    private Exercice exercice;

    // Constructeurs, getters et setters
    public ExerciseInWorkout(Long id, int series, int repetitions, int weight, int time, int heartRates, Date date, Exercice exercice) {
        this.id = id;
        this.series = series;
        this.repetitions = repetitions;
        this.weight = weight;
        this.time = time;
        this.heartRates = heartRates;
        this.date = date;
        this.exercice = exercice;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getSeries() { return series; }
    public void setSeries(int series) { this.series = series; }

    public int getRepetitions() { return repetitions; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public int getTime() { return time; }
    public void setTime(int time) { this.time = time; }

    public int getHeartRates() { return heartRates; }
    public void setHeartRates(int heartRates) { this.heartRates = heartRates; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Exercice getExercice() { return exercice; }
    public void setExercice(Exercice exercice) { this.exercice = exercice; }
}
