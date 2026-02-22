package com.example.gymevo.data.sync;

import com.example.gymevo.model.ExerciseImageSyncQueueItem;

final class StubExerciseImageUploader implements ExerciseImageUploader {

    @Override
    public boolean upload(ExerciseImageSyncQueueItem item) {
        // TODO(server-integration): replace this stub with a real HTTP uploader
        // (multipart + auth header + mark DONE on 2xx).
        // IMPORTANT:
        // Upload is intentionally a stub right now (returns false), so items
        // retry with backoff until retry budget is exhausted, then remain FAILED
        // for later real API integration.
        return false;
    }
}
