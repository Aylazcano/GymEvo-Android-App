package com.example.gymevo.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.gymevo.models.Workout;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface WorkoutDao {
    @Insert
    void insertWorkout(Workout workout);

    @Query("SELECT * FROM workout WHERE date = :date")
    LiveData<List<Workout>> getWorkoutsByDate(LocalDate date);

    @Query("SELECT * FROM workout")
    LiveData<List<Workout>> getAllWorkouts();
}
