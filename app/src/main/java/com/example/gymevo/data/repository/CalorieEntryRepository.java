package com.example.gymevo.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.gymevo.data.local.AppDatabase;
import com.example.gymevo.data.local.CalorieEntryDao;
import com.example.gymevo.model.CalorieEntry;

import java.util.List;

public class CalorieEntryRepository {

    private final CalorieEntryDao dao;

    public CalorieEntryRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        dao = db.calorieEntryDao();
    }

    public LiveData<List<CalorieEntry>> getEntriesForDate(long epochDay) {
        return dao.getEntriesForDate(epochDay);
    }

    public LiveData<Integer> getTotalCaloriesForDate(long epochDay) {
        return dao.getTotalCaloriesForDate(epochDay);
    }

    public LiveData<List<Long>> getAllDatesWithEntries() {
        return dao.getAllDatesWithEntries();
    }

    public void insert(CalorieEntry entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = dao.insert(entry);
            entry.setId(id);
        });
    }

    public void update(CalorieEntry entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> dao.update(entry));
    }

    public void delete(CalorieEntry entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> dao.delete(entry));
    }
}
