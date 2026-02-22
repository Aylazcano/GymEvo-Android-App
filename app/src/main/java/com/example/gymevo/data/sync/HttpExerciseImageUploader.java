package com.example.gymevo.data.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.gymevo.model.ExerciseImageSyncQueueItem;

import java.io.File;

final class HttpExerciseImageUploader implements ExerciseImageUploader {

    private final Context appContext;

    HttpExerciseImageUploader(Context context) {
        this.appContext = context != null ? context.getApplicationContext() : null;
    }

    @Override
    public boolean upload(ExerciseImageSyncQueueItem item) {
        if (item == null || !ExerciseImageSyncConfig.isValid()) {
            return false;
        }

        String localPath = item.getLocalPath();
        if (TextUtils.isEmpty(localPath)) {
            return false;
        }
        File file = new File(localPath);
        if (!file.exists() || !file.isFile()) {
            return false;
        }

        String uploadUrl = ExerciseImageSyncConfig.buildUploadUrl();
        if (TextUtils.isEmpty(uploadUrl)) {
            return false;
        }

        String authorizationHeader = resolveAuthorizationHeader();

        // TODO(server-integration):
        // 1) build multipart/form-data request to uploadUrl with file + metadata
        //    (exerciseKey, slot, sourceUri)
        // 2) add authentication header from authorizationHeader when present
        // 3) execute request using ExerciseImageSyncConfig timeout values
        //    (connect/read) with retry-safe semantics
        // 4) return true only for HTTP 2xx; otherwise return false
        //
        // This skeleton intentionally returns false until backend contract and auth
        // flow are defined.
        return false;
    }

    private String resolveAuthorizationHeader() {
        if (appContext == null) {
            return null;
        }

        String prefsName = ExerciseImageSyncConfig.getAuthPrefsName();
        String tokenKey = ExerciseImageSyncConfig.getAuthTokenKey();
        if (TextUtils.isEmpty(prefsName) || TextUtils.isEmpty(tokenKey)) {
            return null;
        }

        SharedPreferences preferences = appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        String token = preferences.getString(tokenKey, null);
        if (TextUtils.isEmpty(token)) {
            return null;
        }

        String trimmed = token.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return "Bearer " + trimmed;
    }
}
