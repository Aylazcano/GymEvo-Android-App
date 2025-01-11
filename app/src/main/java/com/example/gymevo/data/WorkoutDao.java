package com.example.gymevo.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gymevo.models.Workout;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface WorkoutDao {
    @Insert
    void insertWorkout(Workout workout);

    @Update
    void updateWorkout(Workout workout);

    @Delete
    void deleteWorkout(Workout workout);

    @Query("SELECT * FROM workout WHERE date = :date")
    LiveData<List<Workout>> getWorkoutsByDate(LocalDate date);

    @Query("SELECT * FROM workout")
    LiveData<List<Workout>> getAllWorkouts();

    @Query("SELECT * FROM workout WHERE id = :workoutId")
    LiveData<Workout> getWorkoutById(Long workoutId);

    @Query("DELETE FROM workout WHERE id = :workoutId")
    void deleteWorkoutById(Long workoutId);

    @Query("DELETE FROM workout")
    void deleteAllWorkouts();  // Cette méthode supprime tous les workouts
}
