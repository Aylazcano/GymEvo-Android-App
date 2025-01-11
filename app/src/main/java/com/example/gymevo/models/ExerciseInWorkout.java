package com.example.gymevo.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "exercise_in_workout",
        foreignKeys = {
                @ForeignKey(entity = Exercise.class,
                        parentColumns = "id",
                        childColumns = "exerciseId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Workout.class,
                        parentColumns = "id",
                        childColumns = "workoutId",
                        onDelete = ForeignKey.CASCADE)
        })
public class ExerciseInWorkout extends Exercise {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private int series;
    private int repetitions;
    private int weight;
    private int time; // in seconds
    private int heartRates;

    @ColumnInfo(index = true)
    private Long exerciseId;

    @ColumnInfo(index = true)
    private Long workoutId;

    // Constructor without the logic for fetching the exercise
    public ExerciseInWorkout(Long id, String name, String targetedMuscles, String startImage, String endImage,
                             int series, int repetitions, int weight, int time, int heartRates,
                             Long exerciseId, Long workoutId) {
        super(name, targetedMuscles, startImage, endImage, false); // Call the superclass constructor
        this.id = id;
        this.series = series;
        this.repetitions = repetitions;
        this.weight = weight;
        this.time = time;
        this.heartRates = heartRates;
        this.exerciseId = exerciseId;
        this.workoutId = workoutId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSeries() {
        return series;
    }

    public void setSeries(int series) {
        if (series <= 0) {
            throw new IllegalArgumentException("Series must be greater than 0.");
        }
        this.series = series;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        if (repetitions <= 0) {
            throw new IllegalArgumentException("Repetitions must be greater than 0.");
        }
        this.repetitions = repetitions;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative.");
        }
        this.weight = weight;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        if (time < 0) {
            throw new IllegalArgumentException("Time cannot be negative.");
        }
        this.time = time;
    }

    public int getHeartRates() {
        return heartRates;
    }

    public void setHeartRates(int heartRates) {
        this.heartRates = heartRates;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(Long workoutId) {
        this.workoutId = workoutId;
    }

}
