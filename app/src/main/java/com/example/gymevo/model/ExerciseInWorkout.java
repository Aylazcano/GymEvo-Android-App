package com.example.gymevo.model;

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

    private Integer series;
    private Integer repetitions;
    private Float weight;
    private Integer time;
    private Integer heartRates;
    private Integer distance;
    private Integer calories;
    private boolean weightInKg;
    private int orderIndex;

    @ColumnInfo(index = true)
    private Long exerciseId;

    @ColumnInfo(index = true)
    private Long workoutId;

    public ExerciseInWorkout() {
        super("", "", null, null, false);
    }

    public ExerciseInWorkout(Long id, String name, String targetedMuscles, String imageA, String imageB,
                             Integer series, Integer repetitions, Float weight, Integer time, Integer heartRates,
                             Integer distance, Integer calories, Long exerciseId, Long workoutId) {
        super(name, targetedMuscles, imageA, imageB, false);
        this.id = id;
        this.series = series;
        this.repetitions = repetitions;
        this.weight = weight;
        this.time = time;
        this.heartRates = heartRates;
        this.distance = distance;
        this.calories = calories;
        this.exerciseId = exerciseId;
        this.workoutId = workoutId;
        this.weightInKg = false;
        this.orderIndex = 0;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        validatePositive(series, "Series");
        this.series = series;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(Integer repetitions) {
        validatePositive(repetitions, "Repetitions");
        this.repetitions = repetitions;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative.");
        }
        this.weight = weight;
    }

    public boolean isWeightInKg() {
        return weightInKg;
    }

    public void setWeightInKg(boolean weightInKg) {
        this.weightInKg = weightInKg;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        if (time != null && time < 0) {
            throw new IllegalArgumentException("Time cannot be negative.");
        }
        this.time = time;
    }

    public Integer getHeartRates() {
        return heartRates;
    }

    public void setHeartRates(Integer heartRates) {
        this.heartRates = heartRates;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        if (distance != null && distance < 0) {
            throw new IllegalArgumentException("Distance cannot be negative.");
        }
        this.distance = distance;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        if (calories != null && calories < 0) {
            throw new IllegalArgumentException("Calories cannot be negative.");
        }
        this.calories = calories;
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

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = Math.max(0, orderIndex);
    }

    private void validatePositive(Integer value, String label) {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0.");
        }
    }

}
