package com.example.gymevo.data.sync;

import android.text.TextUtils;

import com.example.gymevo.BuildConfig;

public final class ExerciseImageSyncConfig {

    private ExerciseImageSyncConfig() {
    }

    public static boolean isServerSyncEnabled() {
        return BuildConfig.IMAGE_SYNC_ENABLED
                && !TextUtils.isEmpty(BuildConfig.IMAGE_SYNC_BASE_URL);
    }

    public static boolean isValid() {
        return isServerSyncEnabled()
                && !TextUtils.isEmpty(BuildConfig.IMAGE_SYNC_UPLOAD_PATH)
                && BuildConfig.IMAGE_SYNC_CONNECT_TIMEOUT_MS > 0
                && BuildConfig.IMAGE_SYNC_READ_TIMEOUT_MS > 0;
    }

    public static String buildUploadUrl() {
        if (!isValid()) {
            return null;
        }
        String baseUrl = BuildConfig.IMAGE_SYNC_BASE_URL;
        String uploadPath = BuildConfig.IMAGE_SYNC_UPLOAD_PATH;
        String safeBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String safePath = uploadPath.startsWith("/") ? uploadPath : "/" + uploadPath;
        return safeBase + safePath;
    }

    public static int getConnectTimeoutMs() {
        return BuildConfig.IMAGE_SYNC_CONNECT_TIMEOUT_MS;
    }

    public static int getReadTimeoutMs() {
        return BuildConfig.IMAGE_SYNC_READ_TIMEOUT_MS;
    }

    public static String getAuthPrefsName() {
        return BuildConfig.IMAGE_SYNC_AUTH_PREFS_NAME;
    }

    public static String getAuthTokenKey() {
        return BuildConfig.IMAGE_SYNC_AUTH_TOKEN_KEY;
    }
}
