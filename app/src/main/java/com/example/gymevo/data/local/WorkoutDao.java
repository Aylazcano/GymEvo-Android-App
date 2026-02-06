package com.example.gymevo.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.gymevo.model.Workout;
import com.example.gymevo.model.WorkoutWithExercises;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface WorkoutDao {
    @Insert
    long insertWorkout(Workout workout);

    @Update
    void updateWorkout(Workout workout);

    @Delete
    void deleteWorkout(Workout workout);

    @Query("UPDATE workout SET isStar = :isStar WHERE id = :id")
    void updateStar(Long id, boolean isStar);

    @Query("SELECT COUNT(*) FROM workout WHERE type = :type")
    int countByType(Workout.WorkoutType type);

    @Transaction
    @Query("SELECT * FROM workout WHERE type = :type")
    LiveData<List<WorkoutWithExercises>> getWorkoutsWithExercisesByType(Workout.WorkoutType type);

    @Transaction
    @Query("SELECT * FROM workout WHERE type = :type AND date = :date LIMIT 1")
    WorkoutWithExercises getWorkoutWithExercisesByTypeAndDateNow(Workout.WorkoutType type, LocalDate date);

    @Query("SELECT * FROM workout WHERE type = :type AND createdAt = :createdAt LIMIT 1")
    Workout getWorkoutByTypeAndCreatedAtNow(Workout.WorkoutType type, long createdAt);

    @Query("SELECT DISTINCT workout.date FROM workout INNER JOIN exercise_in_workout ON workout.id = exercise_in_workout.workoutId WHERE workout.type = :type")
    List<LocalDate> getWorkoutDatesWithExercisesByTypeNow(Workout.WorkoutType type);
}
