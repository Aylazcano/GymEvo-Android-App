package com.example.gymevo.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.gymevo.data.local.AppDatabase;
import com.example.gymevo.data.local.ExerciseDao;
import com.example.gymevo.data.local.ExerciseInWorkoutDao;
import com.example.gymevo.data.seed.WorkoutSeed;
import com.example.gymevo.model.Exercise;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExerciseRepository {

    private final ExerciseDao exerciseDao;
    private final ExerciseInWorkoutDao exerciseInWorkoutDao;
    private final LiveData<List<Exercise>> allExercises;

    public ExerciseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        exerciseDao = db.exerciseDao();
        exerciseInWorkoutDao = db.exerciseInWorkoutDao();
        allExercises = exerciseDao.getAllExercises();
        seedMissingExercises();
    }

    public LiveData<List<Exercise>> getAllExercises() {
        return allExercises;
    }

    public void insertExercise(Exercise exercise) {
        if (exercise == null) {
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            if (exercise.getCreatedAt() <= 0L) {
                exercise.setCreatedAt(now);
            }
            exercise.setUpdatedAt(now);
            exerciseDao.insert(exercise);
        });
    }

    public void updateExercise(Exercise exercise) {
        if (exercise == null) {
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Exercise existing = null;
            if (exercise.getId() != null) {
                existing = exerciseDao.getByIdNow(exercise.getId());
            }
            exercise.setUpdatedAt(System.currentTimeMillis());
            exerciseDao.update(exercise);
            if (exercise.getId() != null) {
                exerciseInWorkoutDao.updateExerciseDetailsByExerciseId(
                        exercise.getId(),
                        exercise.getName(),
                        exercise.getTargetedMuscles(),
                        exercise.getImageA(),
                        exercise.getImageB(),
                        exercise.getType(),
                        exercise.isShowSeries(),
                        exercise.isShowRepetitions(),
                        exercise.isShowWeight(),
                        exercise.isShowTime(),
                        exercise.isShowHeartRate()
                );
            }
            if (existing != null) {
                exerciseInWorkoutDao.updateExerciseDetailsByLegacyKey(
                        existing.getName(),
                        existing.getTargetedMuscles(),
                        exercise.getName(),
                        exercise.getTargetedMuscles(),
                        exercise.getImageA(),
                        exercise.getImageB(),
                        exercise.getType(),
                        exercise.isShowSeries(),
                        exercise.isShowRepetitions(),
                        exercise.isShowWeight(),
                        exercise.isShowTime(),
                        exercise.isShowHeartRate()
                );
            }
        });
    }

    public void updateExerciseStar(Exercise exercise) {
        if (exercise == null || exercise.getId() == null) {
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() ->
                exerciseDao.updateStar(exercise.getId(), exercise.isStar()));
    }

    public void deleteExercise(Exercise exercise) {
        AppDatabase.databaseWriteExecutor.execute(() -> exerciseDao.delete(exercise));
    }

    private void seedMissingExercises() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Exercise> existing = exerciseDao.getAllExercisesNow();
            List<Exercise> seed = WorkoutSeed.generateExercises();
            if (seed == null || seed.isEmpty()) {
                return;
            }
            if (existing == null || existing.isEmpty()) {
                exerciseDao.insertAll(seed);
                return;
            }

            Set<String> existingKeys = new HashSet<>();
            for (Exercise current : existing) {
                if (current != null) {
                    existingKeys.add(buildKey(current.getName(), current.getTargetedMusclesLabel()));
                }
            }

            List<Exercise> missing = new ArrayList<>();
            for (Exercise candidate : seed) {
                if (candidate == null) {
                    continue;
                }
                String key = buildKey(candidate.getName(), candidate.getTargetedMusclesLabel());
                if (!existingKeys.contains(key)) {
                    missing.add(candidate);
                }
            }

            if (!missing.isEmpty()) {
                exerciseDao.insertAll(missing);
            }
        });
    }

    private static String buildKey(String name, String muscles) {
        return normalize(name) + "|" + normalize(muscles);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.US);
    }
}
