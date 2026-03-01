package com.example.gymevo.ui.calories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.gymevo.data.repository.CalorieEntryRepository;
import com.example.gymevo.model.CalorieEntry;

import java.time.LocalDate;
import java.util.List;

public class CalorieViewModel extends AndroidViewModel {

    private final CalorieEntryRepository repository;
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now());

    public final LiveData<List<CalorieEntry>> entries;
    public final LiveData<Integer> totalCalories;
    public final LiveData<List<Long>> datesWithEntries;

    public CalorieViewModel(@NonNull Application application) {
        super(application);
        repository = new CalorieEntryRepository(application);

        entries = Transformations.switchMap(selectedDate, date ->
                repository.getEntriesForDate(date.toEpochDay()));

        totalCalories = Transformations.switchMap(selectedDate, date ->
                repository.getTotalCaloriesForDate(date.toEpochDay()));

        datesWithEntries = repository.getAllDatesWithEntries();
    }

    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate.setValue(date);
    }

    public void insert(CalorieEntry entry) {
        repository.insert(entry);
    }

    public void update(CalorieEntry entry) {
        repository.update(entry);
    }

    public void delete(CalorieEntry entry) {
        repository.delete(entry);
    }
}
