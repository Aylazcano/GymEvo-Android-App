package com.example.gymevo.data.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.gymevo.data.local.AppDatabase;
import com.example.gymevo.data.local.ExerciseImageSyncQueueDao;
import com.example.gymevo.model.ExerciseImageSyncQueueItem;

import java.util.List;

public final class ExerciseImageSyncWorker extends Worker {

    private static final int MAX_BATCH_SIZE = 5;
    private static final int MAX_RETRIES = 10;

    public ExerciseImageSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!ExerciseImageSyncConfig.isValid()) {
            return Result.success();
        }

        ExerciseImageUploader uploader = new HttpExerciseImageUploader(getApplicationContext());

        ExerciseImageSyncQueueDao dao = AppDatabase
                .getDatabase(getApplicationContext())
                .exerciseImageSyncQueueDao();

        List<ExerciseImageSyncQueueItem> batch = dao.getNextBatchForSync(
                ExerciseImageSyncQueueItem.STATUS_PENDING,
                ExerciseImageSyncQueueItem.STATUS_FAILED,
                MAX_RETRIES,
                MAX_BATCH_SIZE
        );

        if (batch == null || batch.isEmpty()) {
            return Result.success();
        }

        long now = System.currentTimeMillis();

        for (ExerciseImageSyncQueueItem item : batch) {
            if (item == null || item.getId() == null) {
                continue;
            }

            dao.updateStatus(
                    item.getId(),
                    ExerciseImageSyncQueueItem.STATUS_UPLOADING,
                    item.getRetryCount(),
                    now
            );

            boolean uploaded = uploader.upload(item);
            if (uploaded) {
                dao.updateStatus(
                        item.getId(),
                        ExerciseImageSyncQueueItem.STATUS_DONE,
                        item.getRetryCount(),
                        System.currentTimeMillis()
                );
            } else {
                int retries = Math.min(item.getRetryCount() + 1, MAX_RETRIES);
                dao.updateStatus(
                        item.getId(),
                        ExerciseImageSyncQueueItem.STATUS_FAILED,
                        retries,
                        System.currentTimeMillis()
                );
            }
        }

        int remaining = dao.countRetriable(
                ExerciseImageSyncQueueItem.STATUS_PENDING,
                ExerciseImageSyncQueueItem.STATUS_FAILED,
                MAX_RETRIES
        );
        if (remaining > 0) {
            return Result.retry();
        }

        return Result.success();
    }
}
