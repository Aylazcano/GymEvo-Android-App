package com.example.gymevo.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gymevo.model.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Insert
    void insert(Exercise exercise);

    @Insert
    void insertAll(List<Exercise> exercises);

    @Update
    void update(Exercise exercise);

    @Delete
    void delete(Exercise exercise);

    @Query("SELECT * FROM exercise")
    LiveData<List<Exercise>> getAllExercises();

    @Query("SELECT * FROM exercise")
    List<Exercise> getAllExercisesNow();

}
