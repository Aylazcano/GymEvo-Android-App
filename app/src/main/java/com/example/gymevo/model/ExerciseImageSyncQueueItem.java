package com.example.gymevo.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "exercise_image_sync_queue",
    foreignKeys = @ForeignKey(
        entity = Exercise.class,
        parentColumns = "id",
        childColumns = "exerciseId",
        onDelete = ForeignKey.CASCADE
    ),
        indices = {
        @Index(value = {"exerciseId", "slot", "localPath"}, unique = true),
        @Index(value = {"exerciseId"}),
                @Index(value = {"status", "updatedAt"})
        }
)
public class ExerciseImageSyncQueueItem {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_UPLOADING = "UPLOADING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAILED = "FAILED";

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private Long exerciseId;
    private String exerciseKey;
    private String slot;
    private String sourceUri;
    private String localUri;
    private String localPath;
    private long localLastModified;
    private String status;
    private int retryCount;
    private long createdAt;
    private long updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseKey() {
        return exerciseKey;
    }

    public void setExerciseKey(String exerciseKey) {
        this.exerciseKey = exerciseKey;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getSourceUri() {
        return sourceUri;
    }

    public void setSourceUri(String sourceUri) {
        this.sourceUri = sourceUri;
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public long getLocalLastModified() {
        return localLastModified;
    }

    public void setLocalLastModified(long localLastModified) {
        this.localLastModified = localLastModified;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
