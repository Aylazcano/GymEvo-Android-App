package com.example.gymevo.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.Relation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "workout")
public class Workout {
    public enum WorkoutType {
        TEMPLATE,
        SESSION
    }

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String name;
    private LocalDate date;

    private boolean isStar;

    private WorkoutType type;
    private long createdAt;
    private long updatedAt;

    @Ignore
    @Relation(parentColumn = "id", entityColumn = "workoutId")
    private List<ExerciseInWorkout> exercises = new ArrayList<>();

    public Workout(Long id, String name, LocalDate date, boolean isStar, WorkoutType type) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.isStar = isStar;
        this.type = type != null ? type : WorkoutType.TEMPLATE;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    @Ignore
    public Workout() {
        this(null, "", null, false, WorkoutType.TEMPLATE);
    }

    @Ignore
    public Workout(String name, LocalDate date, boolean isStar) {
        this(null, name, date, isStar, WorkoutType.TEMPLATE);
    }

    @Ignore
    public Workout(String name, LocalDate date, boolean isStar, WorkoutType type) {
        this(null, name, date, isStar, type);
    }

    public void addExercise(ExerciseInWorkout exercise) {
        if (exercise != null && !exercises.contains(exercise)) {
            exercises.add(exercise);
            exercise.setWorkoutId(this.id);
        }
    }

    public void removeExercise(ExerciseInWorkout exercise) {
        if (exercise == null) {
            return;
        }
        exercises.remove(exercise);
        exercise.setWorkoutId(null);
    }

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

    public boolean isStar() {
        return isStar;
    }

    public void setStar(boolean isStar) {
        this.isStar = isStar;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public WorkoutType getType() {
        return type != null ? type : WorkoutType.TEMPLATE;
    }

    public void setType(WorkoutType type) {
        this.type = type != null ? type : WorkoutType.TEMPLATE;
    }

    public boolean isTemplate() {
        return getType() == WorkoutType.TEMPLATE;
    }

    public boolean isSession() {
        return getType() == WorkoutType.SESSION;
    }

    public List<ExerciseInWorkout> getExercises() {
        return exercises;
    }

    public void setExercises(List<ExerciseInWorkout> exercisesList) {
        this.exercises = exercisesList != null ? exercisesList : new ArrayList<>();
    }
}
