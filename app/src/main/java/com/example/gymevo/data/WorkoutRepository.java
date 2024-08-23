package com.example.gymevo.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.gymevo.models.ExerciseInWorkout;
import com.example.gymevo.models.Workout;

import java.time.LocalDate;
import java.util.List;

public class WorkoutRepository {
    private final WorkoutDao workoutDao;
    private final ExerciseInWorkoutDao exerciseInWorkoutDao;
    private final LiveData<List<Workout>> allWorkouts;

    public WorkoutRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        workoutDao = db.workoutDao();
        exerciseInWorkoutDao = db.exerciseInWorkoutDao();
        allWorkouts = workoutDao.getAllWorkouts();
    }

    public LiveData<List<Workout>> getAllWorkouts() {
        return allWorkouts;
    }

    public LiveData<List<Workout>> getWorkoutsByDate(LocalDate date) {
        return workoutDao.getWorkoutsByDate(date);
    }

    public LiveData<Workout> getWorkoutById(Long workoutId) {
        return workoutDao.getWorkoutById(workoutId);
    }

    public void insertWorkout(Workout workout) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                workoutDao.insertWorkout(workout);
            } catch (Exception e) {
                e.printStackTrace(); // Consider logging or notifying user
            }
        });
    }

    public void updateWorkout(Workout workout) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                workoutDao.updateWorkout(workout);
            } catch (Exception e) {
                e.printStackTrace(); // Consider logging or notifying user
            }
        });
    }

    public void deleteWorkout(Workout workout) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                workoutDao.deleteWorkout(workout);
            } catch (Exception e) {
                e.printStackTrace(); // Consider logging or notifying user
            }
        });
    }

    public void deleteWorkoutById(Long workoutId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                workoutDao.deleteWorkoutById(workoutId);
            } catch (Exception e) {
                e.printStackTrace(); // Consider logging or notifying user
            }
        });
    }

    public void deleteAllWorkouts() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                workoutDao.deleteAllWorkouts();
            } catch (Exception e) {
                e.printStackTrace(); // Consider logging or notifying user
            }
        });
    }
}
