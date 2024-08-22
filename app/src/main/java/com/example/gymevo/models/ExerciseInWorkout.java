package com.example.gymevo.models;

import java.time.LocalDate;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(tableName = "exercise_in_workout")
public class ExerciseInWorkout {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private int series;
    private int repetitions;
    private int weight;
    private int time; // en secondes
    private int heartRates;
    private Exercice exercice;
    private Workout workout;

    public ExerciseInWorkout(Long id, int series, int repetitions, int weight, int time, int heartRates, Exercice exercice, Workout workout, LocalDate date) {
        this.id = id;
        this.series = series;
        this.repetitions = repetitions;
        this.weight = weight;
        this.time = time;
        this.heartRates = heartRates;
        this.exercice = exercice;
        this.workout = workout;
        if (workout != null) {
            workout.addExercise(this);
        }
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

    public Exercice getExercice() { return exercice; }
    public void setExercice(Exercice exercice) { this.exercice = exercice; }

    public Workout getWorkout() {
        return workout;
    }

    public void setWorkout(Workout workout) {
        if (this.workout != workout) {
            if (this.workout != null) {
                this.workout.removeExercise(this);
            }
            this.workout = workout;
            if (workout != null) {
                workout.addExercise(this);
            }
        }
    }

//    public Date getDate(LocalDate date) { return this.date; } // Add this getter
//    public void setDate(Date date) { this.date = date; } // Add this setter
}