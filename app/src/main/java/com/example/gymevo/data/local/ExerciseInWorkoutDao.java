package com.example.gymevo.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.ExerciseType;
import com.example.gymevo.model.MuscleGroup;

import java.util.List;

@Dao
public interface ExerciseInWorkoutDao {
    @Insert
    void insertAll(List<ExerciseInWorkout> exercises);

    @Query("DELETE FROM exercise_in_workout WHERE workoutId = :workoutId")
    void deleteByWorkoutId(Long workoutId);

    @Query("UPDATE exercise_in_workout SET name = :name, targetedMuscles = :targetedMuscles, imageA = :imageA, imageB = :imageB, type = :type, showSeries = :showSeries, showRepetitions = :showRepetitions, showWeight = :showWeight, showTime = :showTime, showHeartRate = :showHeartRate, showDistance = :showDistance, showCalories= :showCalories WHERE exerciseId = :exerciseId")
    void updateExerciseDetailsByExerciseId(Long exerciseId,
                                           String name,
                                           MuscleGroup targetedMuscles,
                                           String imageA,
                                           String imageB,
                                           ExerciseType type,
                                           boolean showSeries,
                                           boolean showRepetitions,
                                           boolean showWeight,
                                           boolean showTime,
                                           boolean showHeartRate,
                                           boolean showDistance,
                                           boolean showCalories);

    @Query("UPDATE exercise_in_workout SET name = :name, targetedMuscles = :targetedMuscles, imageA = :imageA, imageB = :imageB, type = :type, showSeries = :showSeries, showRepetitions = :showRepetitions, showWeight = :showWeight, showTime = :showTime, showHeartRate = :showHeartRate, showDistance = :showDistance, showCalories= :showCalories  WHERE exerciseId IS NULL AND name = :oldName AND targetedMuscles IS :oldTargetedMuscles")
    void updateExerciseDetailsByLegacyKey(String oldName,
                                          MuscleGroup oldTargetedMuscles,
                                          String name,
                                          MuscleGroup targetedMuscles,
                                          String imageA,
                                          String imageB,
                                          ExerciseType type,
                                          boolean showSeries,
                                          boolean showRepetitions,
                                          boolean showWeight,
                                          boolean showTime,
                                          boolean showHeartRate,
                                          boolean showDistance,
                                          boolean showCalories);
}
