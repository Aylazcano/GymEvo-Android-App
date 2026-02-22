package com.example.gymevo.ui.common;

import android.content.Context;
import android.graphics.Color;
import com.google.android.material.color.MaterialColors;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.TextKeyListener;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.example.gymevo.data.repository.UserPreferencesRepository;
import com.example.gymevo.ui.common.FilterUi;
import com.example.gymevo.ui.common.sort.SortBottomSheet;
import com.example.gymevo.ui.common.sort.SortField;
import com.example.gymevo.ui.common.sort.SortOrder;

import com.example.gymevo.R;
import com.example.gymevo.data.local.AppDatabase;
import com.example.gymevo.data.seed.FreeExerciseDbSeeder;
import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.ExerciseType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ExerciseEditDialog {

    private static final int MIN_SERIES = 0;
    private static final int MAX_SERIES = 20;
    private static final int MIN_REPS = 0;
    private static final int MAX_REPS = 50;
    private static final int MIN_WEIGHT = 0;
    private static final int MAX_WEIGHT = 500;
    private static final int MIN_MINUTES = 0;
    private static final int MAX_MINUTES = 59;
    private static final int MIN_SECONDS = 0;
    private static final int MAX_SECONDS = 59;
    private static final int MIN_HEART_RATE = 0;
    private static final int MAX_HEART_RATE = 220;

    private static final int MIN_DISTANCE = 0;            // meters
    private static final int MAX_DISTANCE = 100000;       // up to 100 km (as meters)
    private static final int MIN_CALORIES = 0;            // kcal
    private static final int MAX_CALORIES = 5000;

    private static final double LBS_TO_KG = 0.45359237d;
    private static final double KG_TO_LBS = 2.20462262d;

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
        ImageView filterAllImage = dialogView.findViewById(R.id.image_filter_all);
        ImageView filterAnaerobicImage = dialogView.findViewById(R.id.image_filter_anaerobic);
        ImageView filterAerobicImage = dialogView.findViewById(R.id.image_filter_aerobic);
        NumberPicker seriesPicker = dialogView.findViewById(R.id.numberPickerSeries);
        NumberPicker repetitionsPicker = dialogView.findViewById(R.id.numberPickerRepetitions);
        NumberPicker weightPicker = dialogView.findViewById(R.id.numberPickerWeight);
        NumberPicker timeMinutesPicker = dialogView.findViewById(R.id.numberPickerTimeMin);
        NumberPicker timeSecondsPicker = dialogView.findViewById(R.id.numberPickerTimeSec);
        NumberPicker heartRatePicker = dialogView.findViewById(R.id.numberPickerHeartRate);
        NumberPicker distancePicker = dialogView.findViewById(R.id.numberPickerDistance);
        NumberPicker caloriesPicker = dialogView.findViewById(R.id.numberPickerCalories);
        ImageView sortImage = dialogView.findViewById(R.id.image_sort);
        ImageView starPriorityImage = dialogView.findViewById(R.id.image_star_priority);
        TextView weightLabel = dialogView.findViewById(R.id.text_weight_label);
        View metricSeries = dialogView.findViewById(R.id.layout_metric_series);
        View metricReps = dialogView.findViewById(R.id.layout_metric_reps);
        View metricWeight = dialogView.findViewById(R.id.layout_metric_weight);
        View metricTime = dialogView.findViewById(R.id.layout_metric_time);
        View metricHr = dialogView.findViewById(R.id.layout_metric_hr);
        View metricDistance = dialogView.findViewById(R.id.layout_metric_distance);
        View metricCalories = dialogView.findViewById(R.id.layout_metric_calories);

        final WeightUnit[] weightUnit = {exercise.isWeightInKg() ? WeightUnit.KG : WeightUnit.LBS};
        final Exercise[] selectedExercise = {exercise};
        final ExerciseType[] typeFilter = {null};
        final boolean[] suppressFilter = {false};
        final SortField[] sortField = {SortField.RECENT};
        final SortOrder[] sortOrder = {SortOrder.DESC};
        final boolean[] starPriorityEnabled = {false};
        final List<Long> customOrderIds = new ArrayList<>();
        final ImageView[] selectedFilterImage = {null};

        List<Exercise> options = new ArrayList<>();
        List<Exercise> filteredOptions = new ArrayList<>();
        configureExercisePicker(context, exercisePicker, options, toExercise(exercise), selectedExercise);
        configurePicker(seriesPicker, MIN_SERIES, MAX_SERIES, exercise.getSeries());
        configurePicker(repetitionsPicker, MIN_REPS, MAX_REPS, exercise.getRepetitions());
        configurePicker(weightPicker, MIN_WEIGHT, MAX_WEIGHT, exercise.getWeight());
        int timeSeconds = exercise.getTime() != null ? exercise.getTime() : 0;
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        configurePicker(timeMinutesPicker, MIN_MINUTES, MAX_MINUTES, minutes);
        configurePicker(timeSecondsPicker, MIN_SECONDS, MAX_SECONDS, seconds);
        configurePicker(heartRatePicker, MIN_HEART_RATE, MAX_HEART_RATE, exercise.getHeartRates());
        configurePicker(distancePicker, MIN_DISTANCE, MAX_DISTANCE, exercise.getDistance());
        configurePicker(caloriesPicker, MIN_CALORIES, MAX_CALORIES, exercise.getCalories());

        setupDirectInput(context, seriesPicker);
        setupDirectInput(context, repetitionsPicker);
        setupDirectInput(context, weightPicker);
        setupDirectInput(context, timeMinutesPicker);
        setupDirectInput(context, timeSecondsPicker);
        setupDirectInput(context, heartRatePicker);
        setupDirectInput(context, distancePicker);
        setupDirectInput(context, caloriesPicker);
        setupExerciseInput(context, exercisePicker, options, filteredOptions,
            selectedExercise, exercise, typeFilter, suppressFilter,
            exercisePreview, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);

        applyWeightLabel(context, weightLabel, weightUnit[0]);
        if (weightLabel != null && weightPicker != null) {
            weightLabel.setOnClickListener(v -> {
                weightUnit[0] = weightUnit[0].toggle();
                int current = weightPicker.getValue();
                int converted = weightUnit[0] == WeightUnit.KG
                        ? toKg(current)
                        : toLbs(current);
                weightPicker.setValue(clamp(converted, MIN_WEIGHT, MAX_WEIGHT));
                applyWeightLabel(context, weightLabel, weightUnit[0]);
            });
        }

        applyMetricVisibility(exercise, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);

        updateExercisePreview(exercisePreview, exercise);
        if (exercisePicker != null) {
            exercisePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                Exercise selected = getSelectedExercise(exercisePicker,
                        filteredOptions.isEmpty() ? options : filteredOptions,
                        exercise);
                selectedExercise[0] = selected != null ? selected : exercise;
                updateExercisePreview(exercisePreview, selectedExercise[0]);
                applyMetricVisibility(selectedExercise[0], metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);
            });
        }

        // Set up filter image click listeners
        setupFilterImageClickListener(filterAllImage, null, selectedFilterImage, typeFilter, suppressFilter, context, exercisePicker, options, filteredOptions, selectedExercise, exercise, exercisePreview, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);
        setupFilterImageClickListener(filterAnaerobicImage, ExerciseType.ANAEROBIC, selectedFilterImage, typeFilter, suppressFilter, context, exercisePicker, options, filteredOptions, selectedExercise, exercise, exercisePreview, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);
        setupFilterImageClickListener(filterAerobicImage, ExerciseType.AEROBIC, selectedFilterImage, typeFilter, suppressFilter, context, exercisePicker, options, filteredOptions, selectedExercise, exercise, exercisePreview, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);

        // Initially select all filter
        selectedFilterImage[0] = filterAllImage;
        if (selectedFilterImage[0] != null) {
            // use theme accent (colorSecondary) instead of hard-coded pink
            int accent = MaterialColors.getColor(selectedFilterImage[0], com.google.android.material.R.attr.colorSecondary);
            selectedFilterImage[0].setColorFilter(accent);
        }

        // Load exercise list preferences for sorting
        UserPreferencesRepository preferencesRepo = UserPreferencesRepository.getInstance(context.getApplicationContext());
        //noinspection CheckResult
        preferencesRepo.getExerciseListPreferences().subscribe(preferences -> {
            if (preferences != null) {
                sortField[0] = SortField.from(preferences.sortField, SortField.RECENT);
                sortOrder[0] = SortOrder.from(preferences.sortOrder, SortOrder.DESC);
                starPriorityEnabled[0] = preferences.starPriorityEnabled;
                customOrderIds.clear();
                customOrderIds.addAll(parseIdList(preferences.customOrder));
                updateStarPriorityImage(starPriorityImage, starPriorityEnabled[0]);
            }
        }, throwable -> {
            // Use defaults
        });

        loadExercisesAsync(context, loadedExercises -> {
            // Sort exercises based on preferences
            List<Exercise> sortedExercises = new ArrayList<>(loadedExercises);
            Comparator<Exercise> comparator = buildExerciseComparator(sortField[0], sortOrder[0], starPriorityEnabled[0], customOrderIds);
            sortedExercises.sort(comparator);

            options.clear();
            options.addAll(sortedExercises);
            if (shouldAddCurrentExercise(exercise, options, context)) {
                options.add(0, toExercise(exercise));
            }
                applyExerciseFilter(context, exercisePicker, options, filteredOptions, exercise,
                    selectedExercise, getPickerQuery(context, exercisePicker), typeFilter[0], suppressFilter);
            Exercise selected = selectedExercise[0];
            updateExercisePreview(exercisePreview, selected);
            applyMetricVisibility(selected, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);
        });

        if (sortImage != null) {
            sortImage.setOnClickListener(v -> showSortDialog(context, sortField, sortOrder, starPriorityEnabled, customOrderIds, preferencesRepo, options, filteredOptions, exercisePicker, exercise, selectedExercise, suppressFilter, exercisePreview, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories));
        }

        if (starPriorityImage != null) {
            updateStarPriorityImage(starPriorityImage, starPriorityEnabled[0]);
            starPriorityImage.setOnClickListener(v -> {
                starPriorityEnabled[0] = !starPriorityEnabled[0];
                updateStarPriorityImage(starPriorityImage, starPriorityEnabled[0]);
                // Re-sort options
                List<Exercise> sortedExercises = new ArrayList<>(options);
                Comparator<Exercise> comparator = buildExerciseComparator(sortField[0], sortOrder[0], starPriorityEnabled[0], customOrderIds);
                sortedExercises.sort(comparator);
                options.clear();
                options.addAll(sortedExercises);
                if (shouldAddCurrentExercise(exercise, options, context)) {
                    options.add(0, toExercise(exercise));
                }
                applyExerciseFilter(context, exercisePicker, options, filteredOptions, exercise,
                    selectedExercise, getPickerQuery(context, exercisePicker), null, suppressFilter);
                Exercise selected = selectedExercise[0];
                updateExercisePreview(exercisePreview, selected);
                applyMetricVisibility(selected, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);
                // Persist preferences
                persistExerciseListPreferences(preferencesRepo, sortField[0], sortOrder[0], starPriorityEnabled[0], customOrderIds);
            });
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    try {
                        Exercise selected = selectedExercise[0] != null ? selectedExercise[0]
                                : getSelectedExercise(exercisePicker, options, exercise);
                        if (isPlaceholderSelection(context, selected)) {
                            Toast.makeText(context, R.string.exercise_select_required, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ExerciseInWorkout updatedExercise = createUpdatedExercise(exercise, selected,
                            seriesPicker, repetitionsPicker, weightPicker,
                            timeMinutesPicker, timeSecondsPicker, heartRatePicker, distancePicker, caloriesPicker, weightUnit[0]);

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
        picker.setFocusable(true);
        picker.setFocusableInTouchMode(true);
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setWrapSelectorWheel(true);
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
            AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
            List<Exercise> exercises = FreeExerciseDbSeeder.ensureSeeded(
                    context.getApplicationContext(),
                    db.exerciseDao()
            );

            new Handler(Looper.getMainLooper()).post(() -> callback.onLoaded(exercises));
        });
    }

    private static void configureExercisePicker(Context context, NumberPicker picker,
                                                List<Exercise> options,
                                                Exercise current,
                                                Exercise[] selectedOut) {
        if (picker == null) {
            return;
        }
        picker.setFocusable(true);
        picker.setFocusableInTouchMode(true);
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

        // Clear any pending SetSelectionCommand from the NumberPicker's internal EditText
        // to prevent crashes when the displayed values change to shorter text
        EditText innerEditText = findNumberPickerEditText(picker);
        if (innerEditText != null && innerEditText.getHandler() != null) {
            innerEditText.getHandler().removeCallbacksAndMessages(null);
        }

        picker.setDisplayedValues(null);
        picker.setMinValue(0);
        picker.setMaxValue(labels.length - 1);
        picker.setDisplayedValues(labels);
        picker.setWrapSelectorWheel(true);
        picker.setValue(selectedIndex);
        if (selectedOut != null && selectedOut.length > 0) {
            selectedOut[0] = safeOptions.get(selectedIndex);
        }
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
                                          NumberPicker timeMinutesPicker,
                                          NumberPicker timeSecondsPicker,
                                          NumberPicker heartRatePicker,
                                          NumberPicker distancePicker,
                                          NumberPicker caloriesPicker,
                                          WeightUnit weightUnit) {
        if (exercise == null) {
            return;
        }
        exercise.setSeries(toNullableValue(seriesPicker));
        exercise.setRepetitions(toNullableValue(repetitionsPicker));
        exercise.setWeight(toNullableValue(weightPicker));
        exercise.setWeightInKg(weightUnit == WeightUnit.KG);
        exercise.setTime(toTimeSeconds(timeMinutesPicker, timeSecondsPicker));
        exercise.setHeartRates(toNullableValue(heartRatePicker));
        exercise.setDistance(toNullableValue(distancePicker));
        exercise.setCalories(toNullableValue(caloriesPicker));
    }

    private static ExerciseInWorkout copyExercise(ExerciseInWorkout source) {
        if (source == null) {
            return null;
        }
        ExerciseInWorkout copy = new ExerciseInWorkout(
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
                source.getDistance(),
                source.getCalories(),
                source.getExerciseId(),
                source.getWorkoutId()
        );
        copy.setType(source.getType());
        copy.copyCatalogMetadataFrom(source);
        copy.setWeightInKg(source.isWeightInKg());
        return copy;
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
        target.setType(source.getType());
        target.copyCatalogMetadataFrom(source);
        target.setShowSeries(source.isShowSeries());
        target.setShowRepetitions(source.isShowRepetitions());
        target.setShowWeight(source.isShowWeight());
        target.setShowTime(source.isShowTime());
        target.setShowHeartRate(source.isShowHeartRate());
        target.setShowDistance(source.isShowDistance());
        target.setShowCalories(source.isShowCalories());
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

    private static boolean isSameExercise(Exercise left, Exercise right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.getId() != null && right.getId() != null) {
            return left.getId().equals(right.getId());
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
        exercise.setType(current.getType());
        exercise.copyCatalogMetadataFrom(current);
        exercise.setShowSeries(current.isShowSeries());
        exercise.setShowRepetitions(current.isShowRepetitions());
        exercise.setShowWeight(current.isShowWeight());
        exercise.setShowTime(current.isShowTime());
        exercise.setShowHeartRate(current.isShowHeartRate());
        exercise.setShowDistance(current.isShowDistance());
        exercise.setShowCalories(current.isShowCalories());
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
        String star = exercise.isStar() ? "★ " : "";
        String name = safeText(exercise.getName());
        if (name.isEmpty() && context != null) {
            name = context.getString(R.string.exercise_select_placeholder);
        }
        String muscles = exercise.getTargetedMusclesLabel();
        if (muscles == null || muscles.isEmpty()) {
            return star + name;
        }
        return star + name + " • " + muscles;
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
                                                           NumberPicker timeMinutesPicker,
                                                           NumberPicker timeSecondsPicker,
                                                           NumberPicker heartRatePicker,
                                                           NumberPicker distancePicker,
                                                           NumberPicker caloriesPicker,
                                                           WeightUnit weightUnit) {
        ExerciseInWorkout updatedExercise = copyExercise(source);
        applyExerciseSelection(updatedExercise, selected);
        applyPickerValues(updatedExercise, seriesPicker, repetitionsPicker,
            weightPicker, timeMinutesPicker, timeSecondsPicker, heartRatePicker, distancePicker, caloriesPicker, weightUnit);
        return updatedExercise;
    }


    private static void applyWeightLabel(Context context, TextView label, WeightUnit unit) {
        if (context == null || label == null) {
            return;
        }
        int resId = unit == WeightUnit.KG ? R.string.exercise_weight_kg : R.string.exercise_weight_lbs;
        label.setText(context.getString(resId));
    }

    private static void applyMetricVisibility(Exercise exercise,
                                              View series,
                                              View reps,
                                              View weight,
                                              View time,
                                              View hr,
                                              View distance,
                                              View calories) {
        if (exercise == null) {
            return;
        }
        if (!exercise.hasAnyDisplayMetric()) {
            exercise.applyDefaultMetricsForType(exercise.getType());
        }
        if (series != null) series.setVisibility(exercise.isShowSeries() ? View.VISIBLE : View.GONE);
        if (reps != null) reps.setVisibility(exercise.isShowRepetitions() ? View.VISIBLE : View.GONE);
        if (weight != null) weight.setVisibility(exercise.isShowWeight() ? View.VISIBLE : View.GONE);
        if (time != null) time.setVisibility(exercise.isShowTime() ? View.VISIBLE : View.GONE);
        if (hr != null) hr.setVisibility(exercise.isShowHeartRate() ? View.VISIBLE : View.GONE);
        if (distance != null) distance.setVisibility(exercise.isShowDistance() ? View.VISIBLE : View.GONE);
        if (calories != null) calories.setVisibility(exercise.isShowCalories() ? View.VISIBLE : View.GONE);
    }

    private static int toKg(int lbs) {
        return (int) Math.round(lbs * LBS_TO_KG);
    }

    private static int toLbs(int kg) {
        return (int) Math.round(kg * KG_TO_LBS);
    }

    private static void setupDirectInput(Context context, NumberPicker picker) {
        if (context == null || picker == null) {
            return;
        }
        picker.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        EditText editText = findNumberPickerEditText(picker);
        if (editText == null) {
            return;
        }
        final boolean[] suppress = {false};
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setSelectAllOnFocus(true);
        editText.setCursorVisible(true);
        editText.setShowSoftInputOnFocus(true);
        editText.setOnClickListener(v -> showKeyboard(context, editText));
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard(context, editText);
            }
        });
        attachPickerTapToFocus(context, picker, editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (suppress[0]) {
                    return;
                }
                String value = s != null ? s.toString().trim() : "";
                if (value.isEmpty()) {
                    return;
                }
                try {
                    int parsed = Integer.parseInt(value);
                    int clamped = clamp(parsed, picker.getMinValue(), picker.getMaxValue());
                    if (clamped != picker.getValue()) {
                        suppress[0] = true;
                        picker.setValue(clamped);
                        suppress[0] = false;
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    int value = Integer.parseInt(v.getText().toString());
                    picker.setValue(clamp(value, picker.getMinValue(), picker.getMaxValue()));
                } catch (NumberFormatException ignored) {
                }
                hideKeyboard(context, editText);
                return true;
            }
            return false;
        });
    }

    private static void setupExerciseInput(Context context,
                                           NumberPicker picker,
                                           List<Exercise> options,
                                           List<Exercise> filtered,
                                           Exercise[] selectedOut,
                                           ExerciseInWorkout fallback,
                                           ExerciseType[] typeFilter,
                                           boolean[] suppressFilter,
                                           ImageView exercisePreview,
                                           View metricSeries,
                                           View metricReps,
                                           View metricWeight,
                                           View metricTime,
                                           View metricHr,
                                           View metricDistance,
                                           View metricCalories) {
        if (context == null || picker == null) {
            return;
        }
        picker.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        EditText editText = findNumberPickerEditText(picker);
        if (editText == null) {
            return;
        }
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setKeyListener(TextKeyListener.getInstance());
        editText.setFilters(new InputFilter[]{});
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setSelectAllOnFocus(true);
        editText.setCursorVisible(true);
        editText.setShowSoftInputOnFocus(true);
        editText.setOnClickListener(v -> showKeyboard(context, editText));
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard(context, editText);
            }
        });
        attachPickerTapToFocus(context, picker, editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (suppressFilter != null && suppressFilter.length > 0 && suppressFilter[0]) {
                    return;
                }
                if (!editText.hasFocus()) {
                    return;
                }
                applyExerciseFilter(context, picker, options, filtered, fallback,
                    selectedOut, s != null ? s.toString() : "",
                    typeFilter != null && typeFilter.length > 0 ? typeFilter[0] : null,
                    suppressFilter);
                Exercise selected = selectedOut != null && selectedOut.length > 0
                    ? selectedOut[0]
                    : getSelectedExercise(picker, filtered != null && !filtered.isEmpty() ? filtered : options, fallback);
                updateExercisePreview(exercisePreview, selected);
                applyMetricVisibility(selected, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Exercise selected = getFirstExercise(filtered, options, fallback);
                if (selected != null) {
                    int index = findExerciseIndexFromList(filtered, options, selected);
                    if (index >= 0) {
                        picker.setValue(clamp(index, picker.getMinValue(), picker.getMaxValue()));
                    }
                    if (selectedOut != null && selectedOut.length > 0) {
                        selectedOut[0] = selected;
                    }
                }
                updateExercisePreview(exercisePreview, selected);
                applyMetricVisibility(selected, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);
                hideKeyboard(context, editText);
                return true;
            }
            return false;
        });
    }

    private static void applyExerciseFilter(Context context,
                                            NumberPicker picker,
                                            List<Exercise> allOptions,
                                            List<Exercise> filteredOptions,
                                            ExerciseInWorkout fallback,
                                            Exercise[] selectedOut,
                                            String query,
                                            ExerciseType typeFilter,
                                            boolean[] suppressFilter) {
        if (allOptions == null) {
            return;
        }
        EditText editText = findNumberPickerEditText(picker);
        if (filteredOptions != null) {
            filteredOptions.clear();
        }
        String normalized = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        for (Exercise option : allOptions) {
            if (option == null) {
                continue;
            }
            if (typeFilter != null && option.getType() != typeFilter) {
                continue;
            }
            if (normalized.isEmpty()) {
                if (filteredOptions != null) {
                    filteredOptions.add(option);
                }
                continue;
            }
                String name = option.getName() != null ? option.getName().toLowerCase(Locale.ROOT) : "";
            String muscles = option.getTargetedMusclesLabel() != null
                    ? option.getTargetedMusclesLabel().toLowerCase(Locale.ROOT) : "";
            if (name.contains(normalized) || muscles.contains(normalized)) {
                if (filteredOptions != null) {
                    filteredOptions.add(option);
                }
            }
        }

        List<Exercise> target = filteredOptions != null ? filteredOptions : allOptions;
        if (target.isEmpty() && fallback != null) {
            target.add(toExercise(fallback));
        }
        if (suppressFilter != null && suppressFilter.length > 0) {
            suppressFilter[0] = true;
        }
        Exercise current = resolveCurrentExercise(target, fallback);
        configureExercisePicker(context, picker, target, current != null ? current : toExercise(fallback), selectedOut);
        if (editText != null && editText.hasFocus() && query != null && !query.isEmpty()) {
            final String finalQuery = query;
            editText.post(() -> {
                if (editText.hasFocus()) {
                    if (suppressFilter != null && suppressFilter.length > 0) {
                        suppressFilter[0] = true;
                    }
                    try {
                        editText.setText(finalQuery);
                        int length = editText.getText() != null ? editText.getText().length() : 0;
                        if (length > 0) {
                            editText.setSelection(length);
                        }
                    } finally {
                        if (suppressFilter != null && suppressFilter.length > 0) {
                            suppressFilter[0] = false;
                        }
                    }
                }
            });
        } else if (suppressFilter != null && suppressFilter.length > 0) {
            suppressFilter[0] = false;
        }
    }

    private static Exercise resolveCurrentExercise(List<Exercise> options, ExerciseInWorkout fallback) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        Exercise preferred = fallback != null ? toExercise(fallback) : null;
        if (preferred != null) {
            for (Exercise option : options) {
                if (isSameExercise(option, preferred)) {
                    return option;
                }
            }
            String preferredName = preferred.getName() != null ? preferred.getName().trim() : "";
            if (!preferredName.isEmpty()) {
                for (Exercise option : options) {
                    if (option != null && preferredName.equalsIgnoreCase(safeText(option.getName()).trim())) {
                        return option;
                    }
                }
            }
        }
        return options.get(0);
    }

    private static Exercise getFirstExercise(List<Exercise> filtered,
                                             List<Exercise> all,
                                             ExerciseInWorkout fallback) {
        if (filtered != null && !filtered.isEmpty()) {
            return filtered.get(0);
        }
        if (all != null && !all.isEmpty()) {
            return all.get(0);
        }
        return fallback != null ? toExercise(fallback) : null;
    }

    private static int findExerciseIndexFromList(List<Exercise> filtered,
                                                 List<Exercise> all,
                                                 Exercise target) {
        if (target == null) {
            return -1;
        }
        List<Exercise> source = filtered != null && !filtered.isEmpty() ? filtered : all;
        if (source == null) {
            return -1;
        }
        for (int i = 0; i < source.size(); i++) {
            Exercise option = source.get(i);
            if (option == null) {
                continue;
            }
            if (option.getId() != null && target.getId() != null) {
                if (option.getId().equals(target.getId())) {
                    return i;
                }
            } else if (safeText(option.getName()).equalsIgnoreCase(safeText(target.getName()))) {
                return i;
            }
        }
        return -1;
    }

    private static String getPickerQuery(Context context, NumberPicker picker) {
        EditText editText = findNumberPickerEditText(picker);
        if (editText == null || editText.getText() == null) {
            return "";
        }
        if (!editText.hasFocus()) {
            return "";
        }
        String value = editText.getText().toString();
        if (context != null) {
            String placeholder = context.getString(R.string.exercise_select_placeholder);
            String legacy = context.getString(R.string.exercise_label);
            if (value.equals(placeholder) || value.equals(legacy)) {
                return "";
            }
        }
        return value;
    }

    private static Integer toTimeSeconds(NumberPicker minutesPicker, NumberPicker secondsPicker) {
        int minutes = minutesPicker != null ? minutesPicker.getValue() : 0;
        int seconds = secondsPicker != null ? secondsPicker.getValue() : 0;
        int total = Math.max(0, minutes) * 60 + Math.max(0, seconds);
        return total == 0 ? null : total;
    }

    private static EditText findNumberPickerEditText(NumberPicker picker) {
        if (picker == null) {
            return null;
        }
        for (int i = 0; i < picker.getChildCount(); i++) {
            View child = picker.getChildAt(i);
            if (child instanceof EditText) {
                return (EditText) child;
            }
        }
        return null;
    }

    private static void attachPickerTapToFocus(Context context, NumberPicker picker, EditText editText) {
        if (context == null || picker == null || editText == null) {
            return;
        }
        int slop = ViewConfiguration.get(context).getScaledTouchSlop();
        final float[] startX = {0f};
        final float[] startY = {0f};
        final boolean[] moved = {false};
        picker.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX[0] = event.getX();
                    startY[0] = event.getY();
                    moved[0] = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = Math.abs(event.getX() - startX[0]);
                    float dy = Math.abs(event.getY() - startY[0]);
                    if (dx > slop || dy > slop) {
                        moved[0] = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (!moved[0]) {
                        v.performClick();
                        editText.requestFocus();
                        showKeyboard(context, editText);
                    }
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    private static void showKeyboard(Context context, View view) {
        if (context == null || view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private static void hideKeyboard(Context context, View view) {
        if (context == null || view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private static Comparator<Exercise> buildExerciseComparator(SortField sortField, SortOrder sortOrder, boolean starPriorityEnabled, List<Long> customOrderIds) {
        Comparator<Exercise> base;
        switch (sortField) {
            case RECENT:
                base = Comparator.comparingLong(Exercise::getUpdatedAt)
                        .thenComparingLong(Exercise::getCreatedAt)
                        .reversed();
                break;
            case POPULAR:
                base = Comparator.comparing(Exercise::isStar).reversed()
                        .thenComparingLong(Exercise::getUpdatedAt).reversed()
                        .thenComparing(exercise -> FilterUi.normalize(exercise.getName()));
                break;
            case CUSTOM:
                base = buildCustomExerciseComparator(starPriorityEnabled, customOrderIds);
                break;
            case MUSCLE:
            case MUSCLE_GROUP:
                base = Comparator.comparing(
                    (Exercise exercise) -> FilterUi.normalize(exercise.getTargetedMusclesLabel())
                ).thenComparing(exercise -> FilterUi.normalize(exercise.getName()));
                break;
            case CREATED:
                base = Comparator.comparingLong(Exercise::getCreatedAt);
                break;
            case UPDATED:
                base = Comparator.comparingLong(Exercise::getUpdatedAt);
                break;
            case NAME:
            default:
                base = Comparator.comparing(exercise -> FilterUi.normalize(exercise.getName()));
                break;
        }

        if (sortField != SortField.RECENT
                && sortField != SortField.POPULAR
                && sortField != SortField.CUSTOM
                && sortOrder == SortOrder.DESC) {
            base = base.reversed();
        }
        if (starPriorityEnabled && sortField != SortField.CUSTOM) {
            base = Comparator.comparing(Exercise::isStar).reversed().thenComparing(base);
        }
        return base;
    }

    private static Comparator<Exercise> buildCustomExerciseComparator(boolean starPriorityEnabled, List<Long> customOrderIds) {
        java.util.Map<Long, Integer> orderMap = buildOrderMap(customOrderIds);
        return (a, b) -> {
            // First priority: star status (starred items first)
            if (starPriorityEnabled) {
                boolean starA = a != null && a.isStar();
                boolean starB = b != null && b.isStar();
                if (starA != starB) {
                    return starA ? -1 : 1; // starred first
                }
            }
            // Second priority: custom order
            int indexA = orderMap.getOrDefault(a != null ? a.getId() : null, Integer.MAX_VALUE);
            int indexB = orderMap.getOrDefault(b != null ? b.getId() : null, Integer.MAX_VALUE);
            if (indexA != indexB) {
                return Integer.compare(indexA, indexB);
            }
            // Third priority: name as tiebreaker
            String nameA = a != null ? FilterUi.normalize(a.getName()) : "";
            String nameB = b != null ? FilterUi.normalize(b.getName()) : "";
            return nameA.compareTo(nameB);
        };
    }

    private static java.util.Map<Long, Integer> buildOrderMap(List<Long> ids) {
        java.util.Map<Long, Integer> map = new java.util.HashMap<>();
        if (ids == null) {
            return map;
        }
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            if (id != null && !map.containsKey(id)) {
                map.put(id, i);
            }
        }
        return map;
    }

    private static java.util.List<Long> parseIdList(String value) {
        java.util.List<Long> ids = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return ids;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            try {
                ids.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    private enum WeightUnit {
        LBS,
        KG;

        WeightUnit toggle() {
            return this == LBS ? KG : LBS;
        }
    }

    private static void showSortDialog(Context context, SortField[] sortField, SortOrder[] sortOrder, boolean[] starPriorityEnabled, List<Long> customOrderIds, UserPreferencesRepository preferencesRepo, List<Exercise> options, List<Exercise> filteredOptions, NumberPicker exercisePicker, ExerciseInWorkout exercise, Exercise[] selectedExercise, boolean[] suppressFilter, ImageView exercisePreview, View metricSeries, View metricReps, View metricWeight, View metricTime, View metricHr, View metricDistance, View metricCalories) {
        if (context == null) {
            return;
        }
        SortBottomSheet sheet = new SortBottomSheet();
        List<SortBottomSheet.SortGroup> groups = buildSortGroups();
        sheet.setOptions(groups, sortField[0], sortOrder[0], (field, order) -> {
            sortField[0] = field;
            sortOrder[0] = order;
            // Re-sort options
            List<Exercise> sortedExercises = new ArrayList<>(options);
            Comparator<Exercise> comparator = buildExerciseComparator(sortField[0], sortOrder[0], starPriorityEnabled[0], customOrderIds);
            sortedExercises.sort(comparator);
            options.clear();
            options.addAll(sortedExercises);
            if (shouldAddCurrentExercise(exercise, options, context)) {
                options.add(0, toExercise(exercise));
            }
            applyExerciseFilter(context, exercisePicker, options, filteredOptions, exercise,
                selectedExercise, getPickerQuery(context, exercisePicker), null, suppressFilter);
            Exercise selected = selectedExercise[0];
            updateExercisePreview(exercisePreview, selected);
            applyMetricVisibility(selected, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);
            // Persist preferences
            persistExerciseListPreferences(preferencesRepo, sortField[0], sortOrder[0], starPriorityEnabled[0], customOrderIds);
        });
        if (context instanceof androidx.fragment.app.FragmentActivity) {
            sheet.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "SortBottomSheet");
        }
    }

    private static List<SortBottomSheet.SortGroup> buildSortGroups() {
        List<SortBottomSheet.SortGroup> groups = new ArrayList<>();
        List<SortBottomSheet.SortOption> smart = new ArrayList<>();
        smart.add(new SortBottomSheet.SortOption(R.string.sort_recent, SortField.RECENT, SortOrder.DESC));
        smart.add(new SortBottomSheet.SortOption(R.string.sort_popular, SortField.POPULAR, SortOrder.DESC));
        groups.add(new SortBottomSheet.SortGroup(R.string.sort_group_smart, smart));

        List<SortBottomSheet.SortOption> anatomical = new ArrayList<>();
        anatomical.add(new SortBottomSheet.SortOption(R.string.sort_muscle_asc, SortField.MUSCLE_GROUP, SortOrder.ASC));
        groups.add(new SortBottomSheet.SortGroup(R.string.sort_group_anatomical, anatomical));

        List<SortBottomSheet.SortOption> basic = new ArrayList<>();
        basic.add(new SortBottomSheet.SortOption(R.string.sort_name_asc, SortField.NAME, SortOrder.ASC));
        basic.add(new SortBottomSheet.SortOption(R.string.sort_name_desc, SortField.NAME, SortOrder.DESC));
        basic.add(new SortBottomSheet.SortOption(R.string.sort_custom, SortField.CUSTOM, SortOrder.ASC));
        groups.add(new SortBottomSheet.SortGroup(R.string.sort_group_basic, basic));

        return groups;
    }

    private static void persistExerciseListPreferences(UserPreferencesRepository repo, SortField sortField, SortOrder sortOrder, boolean starPriorityEnabled, List<Long> customOrderIds) {
        if (repo == null) {
            return;
        }
        String customOrder = encodeIdList(customOrderIds);
        UserPreferencesRepository.ExerciseListPreferences preferences =
                new UserPreferencesRepository.ExerciseListPreferences(
                        null, null, sortField.name(), sortOrder.name(), starPriorityEnabled, customOrder);
        repo.setExerciseListPreferences(preferences).subscribe();
    }

    private static String encodeIdList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            if (id == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(id);
        }
        return builder.toString();
    }

    private static String safeText(String value) {
        return value != null ? value : "";
    }

    private static void setupFilterImageClickListener(ImageView imageView, ExerciseType filterType, ImageView[] selectedFilterImage, ExerciseType[] typeFilter, boolean[] suppressFilter, Context context, NumberPicker exercisePicker, List<Exercise> options, List<Exercise> filteredOptions, Exercise[] selectedExercise, ExerciseInWorkout exercise, ImageView exercisePreview, View metricSeries, View metricReps, View metricWeight, View metricTime, View metricHr, View metricDistance, View metricCalories) {
        if (imageView == null) return;
        imageView.setOnClickListener(v -> {
            if (suppressFilter[0]) return;
            // Clear previous selection
            if (selectedFilterImage[0] != null) {
                selectedFilterImage[0].setColorFilter(null);
            }
            selectedFilterImage[0] = imageView;
            // theme-driven tint for selection
            int accent = MaterialColors.getColor(selectedFilterImage[0], com.google.android.material.R.attr.colorSecondary);
            selectedFilterImage[0].setColorFilter(accent);
            typeFilter[0] = filterType;
            applyExerciseFilter(context, exercisePicker, options, filteredOptions, exercise,
                selectedExercise, getPickerQuery(context, exercisePicker), typeFilter[0], suppressFilter);
            Exercise selected = selectedExercise[0];
            updateExercisePreview(exercisePreview, selected);
            applyMetricVisibility(selected, metricSeries, metricReps, metricWeight, metricTime, metricHr, metricDistance, metricCalories);
        });
    }

    private static void updateStarPriorityImage(ImageView imageView, boolean enabled) {
        if (imageView == null) return;
        int resId = enabled ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off;
        imageView.setImageResource(resId);
    }
}
