package com.example.gymevo.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.gymevo.models.ExerciseInWorkout;

import java.util.List;

@Dao
public interface ExerciseInWorkoutDao {
    @Insert
    void insertExerciseInWorkout(ExerciseInWorkout exerciseInWorkout);

    @Query("SELECT * FROM exercise_in_workout WHERE workoutId = :workoutId")
    LiveData<List<ExerciseInWorkout>> getExercisesByWorkoutId(Long workoutId);
}
