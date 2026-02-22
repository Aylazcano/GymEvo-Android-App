package com.example.gymevo.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.gymevo.model.ExerciseImageSyncQueueItem;

import java.util.List;

@Dao
public interface ExerciseImageSyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ExerciseImageSyncQueueItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ExerciseImageSyncQueueItem> items);

    @Query("SELECT * FROM exercise_image_sync_queue WHERE status = :status ORDER BY updatedAt ASC")
    LiveData<List<ExerciseImageSyncQueueItem>> getByStatus(String status);

        @Query("SELECT * FROM exercise_image_sync_queue "
            + "WHERE (status = :pendingStatus OR status = :failedStatus) "
            + "AND retryCount < :maxRetries "
            + "ORDER BY updatedAt ASC LIMIT :limit")
        List<ExerciseImageSyncQueueItem> getNextBatchForSync(String pendingStatus,
                                 String failedStatus,
                                 int maxRetries,
                                 int limit);

        @Query("SELECT COUNT(*) FROM exercise_image_sync_queue "
            + "WHERE (status = :pendingStatus OR status = :failedStatus) "
            + "AND retryCount < :maxRetries")
        int countRetriable(String pendingStatus, String failedStatus, int maxRetries);

    @Query("UPDATE exercise_image_sync_queue SET status = :status, retryCount = :retryCount, updatedAt = :updatedAt WHERE id = :id")
    void updateStatus(Long id, String status, int retryCount, long updatedAt);

    @Query("DELETE FROM exercise_image_sync_queue WHERE exerciseId = :exerciseId")
    void deleteByExerciseId(Long exerciseId);

    @Query("DELETE FROM exercise_image_sync_queue WHERE status = :status")
    void deleteByStatus(String status);
}
