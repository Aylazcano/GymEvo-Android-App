package com.example.gymevo.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.gymevo.models.ExerciseInWorkout;
import com.example.gymevo.models.Workout;

import java.time.LocalDate;
import java.util.List;

public class WorkoutRepository {
    private WorkoutDao workoutDao;
    private ExerciseInWorkoutDao exerciseInWorkoutDao;
    private LiveData<List<Workout>> allWorkouts;

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

    public LiveData<List<ExerciseInWorkout>> getExercisesByWorkoutId(Long workoutId) {
        return exerciseInWorkoutDao.getExercisesByWorkoutId(workoutId);
    }

    public void insertWorkout(Workout workout) {
        AppDatabase.databaseWriteExecutor.execute(() -> workoutDao.insertWorkout(workout));
    }

    public void insertExerciseInWorkout(ExerciseInWorkout exerciseInWorkout) {
        AppDatabase.databaseWriteExecutor.execute(() -> exerciseInWorkoutDao.insertExerciseInWorkout(exerciseInWorkout));
    }
}
