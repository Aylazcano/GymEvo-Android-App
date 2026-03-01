package com.example.gymevo.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gymevo.model.CalorieEntry;

import java.util.List;

@Dao
public interface CalorieEntryDao {

    @Insert
    long insert(CalorieEntry entry);

    @Update
    void update(CalorieEntry entry);

    @Delete
    void delete(CalorieEntry entry);

    @Query("SELECT * FROM calorie_entry WHERE date = :epochDay ORDER BY createdAt ASC")
    LiveData<List<CalorieEntry>> getEntriesForDate(long epochDay);

    @Query("SELECT COALESCE(SUM(calories),0) FROM calorie_entry WHERE date = :epochDay")
    LiveData<Integer> getTotalCaloriesForDate(long epochDay);

    @Query("SELECT * FROM calorie_entry ORDER BY date DESC, createdAt DESC")
    LiveData<List<CalorieEntry>> getAllEntries();

    @Query("SELECT DISTINCT date FROM calorie_entry ORDER BY date DESC")
    LiveData<List<Long>> getAllDatesWithEntries();
}
