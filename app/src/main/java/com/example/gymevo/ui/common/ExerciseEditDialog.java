package com.example.gymevo.ui.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.example.gymevo.R;
import com.example.gymevo.data.local.AppDatabase;
import com.example.gymevo.data.seed.WorkoutSeed;
import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseInWorkout;

import java.util.ArrayList;
import java.util.List;

public final class ExerciseEditDialog {

    private static final int MIN_SERIES = 0;
    private static final int MAX_SERIES = 20;
    private static final int MIN_REPS = 0;
    private static final int MAX_REPS = 50;
    private static final int MIN_WEIGHT = 0;
    private static final int MAX_WEIGHT = 500;
    private static final int MIN_TIME = 0;
    private static final int MAX_TIME = 3600;
    private static final int MIN_HEART_RATE = 0;
    private static final int MAX_HEART_RATE = 220;

    public interface OnExerciseUpdated {
        void onUpdated(ExerciseInWorkout updatedExercise);
    }

    public interface OnExerciseDeleted {
        void onDeleted(ExerciseInWorkout exercise);
    }

    private ExerciseEditDialog() {
    }

    public static void show(Context context,
                            ExerciseInWorkout exercise,
                            OnExerciseUpdated onUpdated) {
        show(context, exercise, onUpdated, null);
    }

    public static void show(Context context,
                            ExerciseInWorkout exercise,
                            OnExerciseUpdated onUpdated,
                            OnExerciseDeleted onDeleted) {
        if (context == null || exercise == null) {
            return;
        }

        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_edit_exercise_in_workout, null, false);

        ImageView exercisePreview = dialogView.findViewById(R.id.image_exercise_preview);
        NumberPicker exercisePicker = dialogView.findViewById(R.id.numberPickerExercise);
        NumberPicker seriesPicker = dialogView.findViewById(R.id.numberPickerSeries);
        NumberPicker repetitionsPicker = dialogView.findViewById(R.id.numberPickerRepetitions);
        NumberPicker weightPicker = dialogView.findViewById(R.id.numberPickerWeight);
        NumberPicker timePicker = dialogView.findViewById(R.id.numberPickerTime);
        NumberPicker heartRatePicker = dialogView.findViewById(R.id.numberPickerHeartRate);

        List<Exercise> options = new ArrayList<>();
        configureExercisePicker(context, exercisePicker, options, exercise);
        configurePicker(seriesPicker, MIN_SERIES, MAX_SERIES, exercise.getSeries());
        configurePicker(repetitionsPicker, MIN_REPS, MAX_REPS, exercise.getRepetitions());
        configurePicker(weightPicker, MIN_WEIGHT, MAX_WEIGHT, exercise.getWeight());
        configurePicker(timePicker, MIN_TIME, MAX_TIME, exercise.getTime());
        configurePicker(heartRatePicker, MIN_HEART_RATE, MAX_HEART_RATE, exercise.getHeartRates());

        updateExercisePreview(exercisePreview, exercise);
        if (exercisePicker != null) {
            exercisePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                Exercise selected = getSelectedExercise(exercisePicker, options, exercise);
                updateExercisePreview(exercisePreview, selected);
            });
        }

        loadExercisesAsync(context, loadedExercises -> {
            options.clear();
            options.addAll(loadedExercises);
            if (shouldAddCurrentExercise(exercise, options, context)) {
                options.add(0, toExercise(exercise));
            }
            configureExercisePicker(context, exercisePicker, options, exercise);
            Exercise selected = getSelectedExercise(exercisePicker, options, exercise);
            updateExercisePreview(exercisePreview, selected);
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    try {
                        Exercise selected = getSelectedExercise(exercisePicker, options, exercise);
                        if (isPlaceholderSelection(context, selected)) {
                            Toast.makeText(context, R.string.exercise_select_required, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ExerciseInWorkout updatedExercise = createUpdatedExercise(exercise, selected,
                            seriesPicker, repetitionsPicker, weightPicker, timePicker, heartRatePicker);

                        if (onUpdated != null) {
                            onUpdated.onUpdated(updatedExercise);
                        }
                    } catch (IllegalArgumentException ex) {
                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .setOnDismissListener(dialog -> ImageUi.stopExerciseLoop(exercisePreview));

        if (onDeleted != null) {
            builder.setNeutralButton(R.string.action_delete, (dialog, which) -> onDeleted.onDeleted(exercise));
        }

        builder.show();
    }

    private static void configurePicker(NumberPicker picker, int min, int max, Integer value) {
        if (picker == null) {
            return;
        }
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setWrapSelectorWheel(false);
        int safeValue = value != null ? value : 0;
        picker.setValue(clamp(safeValue, min, max));
    }

    private interface OnExercisesLoaded {
        void onLoaded(List<Exercise> exercises);
    }

    private static void loadExercisesAsync(Context context, OnExercisesLoaded callback) {
        if (callback == null) {
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Exercise> exercises = new ArrayList<>();
            AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
            List<Exercise> stored = db.exerciseDao().getAllExercisesNow();
            if (stored != null && !stored.isEmpty()) {
                exercises.addAll(stored);
            } else {
                List<Exercise> seed = WorkoutSeed.generateExercises();
                if (!seed.isEmpty()) {
                    db.exerciseDao().insertAll(seed);
                    exercises.addAll(seed);
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> callback.onLoaded(exercises));
        });
    }

    private static void configureExercisePicker(Context context, NumberPicker picker,
                                                List<Exercise> options,
                                                ExerciseInWorkout current) {
        if (picker == null) {
            return;
        }
        List<Exercise> safeOptions = options;
        if (safeOptions == null || safeOptions.isEmpty()) {
            safeOptions = new ArrayList<>();
            if (current != null) {
                safeOptions.add(current);
            }
        }

        if (safeOptions.isEmpty()) {
            return;
        }

        String[] labels = new String[safeOptions.size()];
        int selectedIndex = 0;
        for (int i = 0; i < safeOptions.size(); i++) {
            Exercise option = safeOptions.get(i);
            labels[i] = buildExerciseLabel(context, option);
            if (isSameExercise(option, current)) {
                selectedIndex = i;
            }
        }

        picker.setDisplayedValues(null);
        picker.setMinValue(0);
        picker.setMaxValue(labels.length - 1);
        picker.setDisplayedValues(labels);
        picker.setWrapSelectorWheel(true);
        picker.setValue(selectedIndex);
    }

    private static Exercise getSelectedExercise(NumberPicker picker,
                                                List<Exercise> options,
                                                ExerciseInWorkout fallback) {
        if (picker == null || options == null || options.isEmpty()) {
            return fallback;
        }
        int index = clamp(picker.getValue(), 0, options.size() - 1);
        return options.get(index);
    }

    private static void applyPickerValues(ExerciseInWorkout exercise,
                                          NumberPicker seriesPicker,
                                          NumberPicker repetitionsPicker,
                                          NumberPicker weightPicker,
                                          NumberPicker timePicker,
                                          NumberPicker heartRatePicker) {
        if (exercise == null) {
            return;
        }
        exercise.setSeries(toNullableValue(seriesPicker));
        exercise.setRepetitions(toNullableValue(repetitionsPicker));
        exercise.setWeight(toNullableValue(weightPicker));
        exercise.setTime(toNullableValue(timePicker));
        exercise.setHeartRates(toNullableValue(heartRatePicker));
    }

    private static ExerciseInWorkout copyExercise(ExerciseInWorkout source) {
        if (source == null) {
            return null;
        }
        return new ExerciseInWorkout(
                source.getId(),
                source.getName(),
                source.getTargetedMusclesLabel(),
                source.getImageA(),
                source.getImageB(),
                source.getSeries(),
                source.getRepetitions(),
                source.getWeight(),
                source.getTime(),
                source.getHeartRates(),
                source.getExerciseId(),
                source.getWorkoutId()
        );
    }

    private static Integer toNullableValue(NumberPicker picker) {
        if (picker == null) {
            return null;
        }
        int value = picker.getValue();
        return value == 0 ? null : value;
    }

    private static void applyExerciseSelection(ExerciseInWorkout target, Exercise source) {
        if (target == null || source == null) {
            return;
        }
        target.setName(source.getName());
        target.setTargetedMusclesLabel(source.getTargetedMusclesLabel());
        target.setImageA(source.getImageA());
        target.setImageB(source.getImageB());
        target.setExerciseId(source.getId());
    }

    private static void updateExercisePreview(ImageView imageView, Exercise exercise) {
        if (imageView == null) {
            return;
        }
        if (exercise == null) {
            ImageUi.startExerciseLoop(imageView, null, null);
            return;
        }
        ImageUi.startExerciseLoop(imageView, exercise.getImageA(), exercise.getImageB());
    }

    private static boolean isSameExercise(Exercise left, ExerciseInWorkout right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.getId() != null && right.getExerciseId() != null) {
            return left.getId().equals(right.getExerciseId());
        }
        String leftKey = buildExerciseKey(left);
        String rightKey = buildExerciseKey(right);
        return leftKey.equals(rightKey);
    }

    private static boolean containsExercise(List<Exercise> options, ExerciseInWorkout current) {
        if (options == null || options.isEmpty() || current == null) {
            return false;
        }
        for (Exercise option : options) {
            if (isSameExercise(option, current)) {
                return true;
            }
        }
        return false;
    }

    private static Exercise toExercise(ExerciseInWorkout current) {
        if (current == null) {
            return new Exercise("", "", null, null, false);
        }
        String name = safeText(current.getName());
        String muscles = safeText(current.getTargetedMusclesLabel());
        Exercise exercise = new Exercise(name, muscles, current.getImageA(), current.getImageB(), false);
        exercise.setId(current.getExerciseId());
        return exercise;
    }

    private static String buildExerciseKey(ExerciseInWorkout exercise) {
        String name = safeText(exercise.getName());
        String muscles = safeText(exercise.getTargetedMusclesLabel());
        String idPart = exercise.getExerciseId() != null ? String.valueOf(exercise.getExerciseId()) : "";
        return idPart + "|" + name + "|" + muscles;
    }

    private static String buildExerciseKey(Exercise exercise) {
        String name = safeText(exercise.getName());
        String muscles = safeText(exercise.getTargetedMusclesLabel());
        String idPart = exercise.getId() != null ? String.valueOf(exercise.getId()) : "";
        return idPart + "|" + name + "|" + muscles;
    }

    private static String buildExerciseLabel(Context context, Exercise exercise) {
        if (exercise == null) {
            return "";
        }
        String name = safeText(exercise.getName());
        if (name.isEmpty() && context != null) {
            name = context.getString(R.string.exercise_select_placeholder);
        }
        String muscles = exercise.getTargetedMusclesLabel();
        if (muscles == null || muscles.isEmpty()) {
            return name;
        }
        return name + " • " + muscles;
    }


    private static boolean shouldAddCurrentExercise(ExerciseInWorkout current,
                                                    List<Exercise> options,
                                                    Context context) {
        if (current == null) {
            return false;
        }
        if (isPlaceholderSelection(context, toExercise(current))) {
            return false;
        }
        return !containsExercise(options, current);
    }

    private static boolean isPlaceholderSelection(Context context, Exercise selected) {
        if (selected == null || context == null) {
            return true;
        }
        String name = selected.getName() != null ? selected.getName().trim() : "";
        if (name.isEmpty()) {
            return true;
        }
        String placeholder = context.getString(R.string.exercise_select_placeholder);
        String legacy = context.getString(R.string.exercise_label);
        boolean matchesLabel = placeholder.equals(name) || legacy.equals(name);
        boolean noDetails = (selected.getId() == null)
                && (selected.getTargetedMusclesLabel() == null || selected.getTargetedMusclesLabel().isEmpty());
        return matchesLabel && noDetails;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static ExerciseInWorkout createUpdatedExercise(ExerciseInWorkout source,
                                                           Exercise selected,
                                                           NumberPicker seriesPicker,
                                                           NumberPicker repetitionsPicker,
                                                           NumberPicker weightPicker,
                                                           NumberPicker timePicker,
                                                           NumberPicker heartRatePicker) {
        ExerciseInWorkout updatedExercise = copyExercise(source);
        applyExerciseSelection(updatedExercise, selected);
        applyPickerValues(updatedExercise, seriesPicker, repetitionsPicker,
                weightPicker, timePicker, heartRatePicker);
        return updatedExercise;
    }

    private static String safeText(String value) {
        return value != null ? value : "";
    }
}
