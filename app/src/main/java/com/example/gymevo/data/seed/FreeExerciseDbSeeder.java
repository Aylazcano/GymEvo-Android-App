package com.example.gymevo.data.seed;

import android.content.Context;
import android.util.Log;

import com.example.gymevo.data.local.ExerciseDao;
import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseType;
import com.example.gymevo.model.MuscleGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class FreeExerciseDbSeeder {

    private static final String TAG = "FreeExerciseDbSeeder";
    private static final String ASSET_PATH = "free_exercise_db/exercises.json";
    private static final String IMAGE_ASSET_PREFIX = "file:///android_asset/free_exercise_db/images/";

    private FreeExerciseDbSeeder() {
    }

    public static List<Exercise> ensureSeeded(Context context, ExerciseDao exerciseDao) {
        if (context == null || exerciseDao == null) {
            return new ArrayList<>();
        }

        List<Exercise> existing = safeList(exerciseDao.getAllExercisesNow());
        List<Exercise> imported = loadExercisesFromAsset(context);
        if (imported.isEmpty()) {
            return existing;
        }

        if (existing.isEmpty()) {
            exerciseDao.insertAll(imported);
            return imported;
        }

        Set<String> existingKeys = new HashSet<>();
        for (Exercise current : existing) {
            if (current != null) {
                existingKeys.add(buildKey(current.getSourceId(), current.getName(), current.getTargetedMusclesLabel()));
            }
        }

        List<Exercise> missing = new ArrayList<>();
        for (Exercise candidate : imported) {
            if (candidate == null) {
                continue;
            }
            String key = buildKey(candidate.getSourceId(), candidate.getName(), candidate.getTargetedMusclesLabel());
            if (!existingKeys.contains(key)) {
                missing.add(candidate);
            }
        }

        if (!missing.isEmpty()) {
            exerciseDao.insertAll(missing);
            existing.addAll(missing);
        }
        return existing;
    }

    private static List<Exercise> loadExercisesFromAsset(Context context) {
        List<Exercise> items = new ArrayList<>();
        try (InputStream inputStream = context.getAssets().open(ASSET_PATH)) {
            String json = readFully(inputStream);
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject node = array.optJSONObject(i);
                Exercise exercise = mapToExercise(node);
                if (exercise != null) {
                    items.add(exercise);
                }
            }
        } catch (Exception error) {
            Log.w(TAG, "Failed loading offline exercise db", error);
        }
        return items;
    }

    private static Exercise mapToExercise(JSONObject node) {
        if (node == null) {
            return null;
        }

        String name = trimToNull(node.optString("name", null));
        if (name == null) {
            return null;
        }

        String sourceId = trimToNull(node.optString("id", null));
        String force = trimToNull(node.optString("force", null));
        String level = trimToNull(node.optString("level", null));
        String mechanic = trimToNull(node.optString("mechanic", null));
        String equipment = trimToNull(node.optString("equipment", null));
        String category = trimToNull(node.optString("category", null));
        JSONArray primaryMusclesArray = node.optJSONArray("primaryMuscles");
        JSONArray secondaryMusclesArray = node.optJSONArray("secondaryMuscles");
        JSONArray instructionsArray = node.optJSONArray("instructions");
        JSONArray imagesArray = node.optJSONArray("images");

        String primaryMuscle = firstPrimaryMuscle(primaryMusclesArray);
        MuscleGroup muscleGroup = MuscleGroup.fromLabel(primaryMuscle);
        if (muscleGroup == null) {
            muscleGroup = MuscleGroup.OTHER;
        }

        ExerciseType type = resolveType(category);
        String imageA = toLocalAssetImagePath(imagesArray, 0);
        String imageB = toLocalAssetImagePath(imagesArray, 1);
        Exercise exercise = new Exercise(name, muscleGroup, imageA, imageB, false, type);
        exercise.setSourceId(sourceId);
        exercise.setForce(force);
        exercise.setLevel(level);
        exercise.setMechanic(mechanic);
        exercise.setEquipment(equipment);
        exercise.setCategory(category);
        exercise.setPrimaryMuscles(toJsonString(primaryMusclesArray));
        exercise.setSecondaryMuscles(toJsonString(secondaryMusclesArray));
        exercise.setInstructions(toJsonString(instructionsArray));
        exercise.setStar(false);
        return exercise;
    }

    private static String firstPrimaryMuscle(JSONArray muscles) {
        if (muscles == null || muscles.length() == 0) {
            return null;
        }
        return trimToNull(muscles.optString(0, null));
    }

    private static ExerciseType resolveType(String category) {
        String normalized = normalize(category);
        if ("cardio".equals(normalized)) {
            return ExerciseType.AEROBIC;
        }
        return ExerciseType.ANAEROBIC;
    }

    private static String toLocalAssetImagePath(JSONArray images, int index) {
        if (images == null || index < 0 || index >= images.length()) {
            return null;
        }
        String relativePath = trimToNull(images.optString(index, null));
        if (relativePath == null) {
            return null;
        }
        return IMAGE_ASSET_PREFIX + relativePath;
    }

    private static String buildKey(String sourceId, String name, String muscles) {
        String safeSourceId = normalize(sourceId);
        if (!safeSourceId.isEmpty()) {
            return "id:" + safeSourceId;
        }
        return "legacy:" + normalize(name) + "|" + normalize(muscles);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.US);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static List<Exercise> safeList(List<Exercise> value) {
        return value != null ? new ArrayList<>(value) : new ArrayList<>();
    }

    private static String toJsonString(JSONArray array) {
        return array != null ? array.toString() : null;
    }

    private static String readFully(InputStream inputStream) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }
}