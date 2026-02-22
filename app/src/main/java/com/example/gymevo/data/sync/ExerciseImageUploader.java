package com.example.gymevo.data.sync;

import com.example.gymevo.model.ExerciseImageSyncQueueItem;

public interface ExerciseImageUploader {
    boolean upload(ExerciseImageSyncQueueItem item);
}
