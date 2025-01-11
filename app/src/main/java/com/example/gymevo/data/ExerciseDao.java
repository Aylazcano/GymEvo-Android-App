package com.example.gymevo.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gymevo.models.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Insert
    void insert(Exercise exercise);

    @Update
    void update(Exercise exercise);

    @Delete
    void delete(Exercise exercise);

    @Query("SELECT * FROM exercise WHERE id = :id LIMIT 1")
    LiveData<Exercise> getExerciseById(Long id);

    @Query("SELECT * FROM exercise WHERE name = :name LIMIT 1")
    LiveData<Exercise> getExerciseByName(String name);

    @Query("SELECT * FROM exercise")
    LiveData<List<Exercise>> getAllExercises();

    // Nouvelle méthode pour récupérer les exercices favoris (marqués comme 'isStar' = true)
    @Query("SELECT * FROM exercise WHERE isStar = 1")
    LiveData<List<Exercise>> getStarredExercises();
}
