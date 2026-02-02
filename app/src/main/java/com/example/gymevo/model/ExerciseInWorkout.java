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
    private Integer weight;
    private Integer time;
    private Integer heartRates;

    @ColumnInfo(index = true)
    private Long exerciseId;

    @ColumnInfo(index = true)
    private Long workoutId;

    public ExerciseInWorkout() {
        super("", "", null, null, false);
    }

    public ExerciseInWorkout(Long id, String name, String targetedMuscles, String imageA, String imageB,
                             Integer series, Integer repetitions, Integer weight, Integer time, Integer heartRates,
                             Long exerciseId, Long workoutId) {
        super(name, targetedMuscles, imageA, imageB, false);
        this.id = id;
        this.series = series;
        this.repetitions = repetitions;
        this.weight = weight;
        this.time = time;
        this.heartRates = heartRates;
        this.exerciseId = exerciseId;
        this.workoutId = workoutId;
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

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative.");
        }
        this.weight = weight;
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

    private void validatePositive(Integer value, String label) {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0.");
        }
    }

}
