package com.example.gymevo.data.repository;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;

import com.example.gymevo.data.local.AppDatabase;
import com.example.gymevo.data.local.ExerciseDao;
import com.example.gymevo.data.local.ExerciseImageSyncQueueDao;
import com.example.gymevo.data.local.ExerciseInWorkoutDao;
import com.example.gymevo.data.seed.FreeExerciseDbSeeder;
import com.example.gymevo.data.sync.ExerciseImageSyncConfig;
import com.example.gymevo.data.sync.ExerciseImageSyncScheduler;
import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseImageSyncQueueItem;
import com.example.gymevo.util.ExerciseImageStore;

import java.io.File;
import java.util.List;

public class ExerciseRepository {

    private final Application application;
    private final ExerciseDao exerciseDao;
    private final ExerciseInWorkoutDao exerciseInWorkoutDao;
    private final ExerciseImageSyncQueueDao exerciseImageSyncQueueDao;
    private final LiveData<List<Exercise>> allExercises;

    public ExerciseRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        exerciseDao = db.exerciseDao();
        exerciseInWorkoutDao = db.exerciseInWorkoutDao();
        exerciseImageSyncQueueDao = db.exerciseImageSyncQueueDao();
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
            persistExerciseImages(exercise);

            long now = System.currentTimeMillis();
            if (exercise.getCreatedAt() <= 0L) {
                exercise.setCreatedAt(now);
            }
            exercise.setUpdatedAt(now);
            long id = exerciseDao.insert(exercise);
            exercise.setId(id);
            queuePendingImageSync(exercise);
        });
    }

    public void updateExercise(Exercise exercise) {
        if (exercise == null) {
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            persistExerciseImages(exercise);

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
                    exercise.getSourceId(),
                    exercise.getForce(),
                    exercise.getLevel(),
                    exercise.getMechanic(),
                    exercise.getEquipment(),
                    exercise.getCategory(),
                    exercise.getPrimaryMuscles(),
                    exercise.getSecondaryMuscles(),
                    exercise.getInstructions(),
                        exercise.getImageA(),
                        exercise.getImageB(),
                        exercise.getType(),
                        exercise.isShowSeries(),
                        exercise.isShowRepetitions(),
                        exercise.isShowWeight(),
                        exercise.isShowTime(),
                        exercise.isShowHeartRate(),
                        exercise.isShowDistance(),
                        exercise.isShowCalories()
                );
            }
            if (existing != null) {
                exerciseInWorkoutDao.updateExerciseDetailsByLegacyKey(
                        existing.getName(),
                        existing.getTargetedMuscles(),
                        exercise.getName(),
                        exercise.getTargetedMuscles(),
                    exercise.getSourceId(),
                    exercise.getForce(),
                    exercise.getLevel(),
                    exercise.getMechanic(),
                    exercise.getEquipment(),
                    exercise.getCategory(),
                    exercise.getPrimaryMuscles(),
                    exercise.getSecondaryMuscles(),
                    exercise.getInstructions(),
                        exercise.getImageA(),
                        exercise.getImageB(),
                        exercise.getType(),
                        exercise.isShowSeries(),
                        exercise.isShowRepetitions(),
                        exercise.isShowWeight(),
                        exercise.isShowTime(),
                        exercise.isShowHeartRate(),
                        exercise.isShowDistance(),
                        exercise.isShowCalories()
                );
            }
            queuePendingImageSync(exercise);
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
            FreeExerciseDbSeeder.ensureSeeded(application.getApplicationContext(), exerciseDao);
        });
    }

    private void persistExerciseImages(Exercise exercise) {
        ExerciseImageStore.PersistResult result =
                ExerciseImageStore.persistIfNeeded(application.getApplicationContext(), exercise);
        exercise.setImageA(result.imageA);
        exercise.setImageB(result.imageB);
    }

    private void queuePendingImageSync(Exercise exercise) {
        if (exercise == null || exercise.getId() == null) {
            return;
        }

        exerciseImageSyncQueueDao.deleteByExerciseId(exercise.getId());

        long now = System.currentTimeMillis();
        upsertQueueItem(exercise, "imageA", exercise.getImageA(), now);
        upsertQueueItem(exercise, "imageB", exercise.getImageB(), now);

        if (ExerciseImageSyncConfig.isValid()) {
            ExerciseImageSyncScheduler.enqueue(application.getApplicationContext());
        }
    }

    private void upsertQueueItem(Exercise exercise, String slot, String localUri, long now) {
        File localFile = toFile(localUri);
        if (localFile == null || !localFile.exists() || !localFile.isFile()) {
            return;
        }

        ExerciseImageSyncQueueItem item = new ExerciseImageSyncQueueItem();
        item.setExerciseId(exercise.getId());
        item.setExerciseKey(exercise.getName());
        item.setSlot(slot);
        item.setSourceUri(localUri);
        item.setLocalUri(localUri);
        item.setLocalPath(localFile.getAbsolutePath());
        item.setLocalLastModified(localFile.lastModified());
        item.setStatus(ExerciseImageSyncQueueItem.STATUS_PENDING);
        item.setRetryCount(0);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        exerciseImageSyncQueueDao.upsert(item);
    }

    private File toFile(String uriOrPath) {
        if (TextUtils.isEmpty(uriOrPath)) {
            return null;
        }
        String value = uriOrPath.trim();
        if (value.startsWith("file://")) {
            Uri uri = Uri.parse(value);
            String path = uri.getPath();
            return path == null ? null : new File(path);
        }
        return new File(value);
    }
}
