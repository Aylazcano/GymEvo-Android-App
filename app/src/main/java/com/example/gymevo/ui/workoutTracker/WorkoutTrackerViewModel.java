package com.example.gymevo.ui.workoutTracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gymevo.data.repository.WorkoutRepository;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.Workout;
import com.example.gymevo.model.Workout.WorkoutType;
import com.example.gymevo.model.WorkoutWithExercises;
import com.example.gymevo.ui.common.WorkoutMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkoutTrackerViewModel extends AndroidViewModel {
    private final MutableLiveData<List<ExerciseInWorkout>> exercisesOnDateLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<LocalDate>> workoutDatesWithExercisesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<List<Workout>> templatesLiveData = new MediatorLiveData<>();
    private final WorkoutRepository workoutRepository;

    public WorkoutTrackerViewModel(@NonNull Application application) {
        super(application);
        workoutRepository = new WorkoutRepository(application);
        templatesLiveData.addSource(workoutRepository.getTemplateWorkouts(),
                items -> templatesLiveData.setValue(WorkoutMapper.mapToWorkouts(items)));
        loadWorkoutDatesWithExercises();
    }

    public LiveData<List<Workout>> getTemplatesLiveData() {
        return templatesLiveData;
    }

    public LiveData<List<ExerciseInWorkout>> getExercisesOnDateLiveData() {
        return exercisesOnDateLiveData;
    }

    public LiveData<List<LocalDate>> getWorkoutDatesWithExercisesLiveData() {
        return workoutDatesWithExercisesLiveData;
    }

    public void refreshWorkoutDatesWithExercises() {
        loadWorkoutDatesWithExercises();
    }

    private void loadWorkoutDatesWithExercises() {
        workoutRepository.loadSessionDatesWithExercises(dates -> {
            workoutDatesWithExercisesLiveData.postValue(safeDates(dates));
        });
    }

    public LiveData<List<ExerciseInWorkout>> getExercisesForWorkoutOnDate(LocalDate date) {
        loadSessionByDate(date, workout -> {
            exercisesOnDateLiveData.postValue(exercisesFromWorkout(workout));
        });
        return exercisesOnDateLiveData;
    }

    public void updateExerciseInWorkout(LocalDate date, ExerciseInWorkout updatedExercise) {
        updateExerciseInWorkout(date, null, updatedExercise);
    }

    public void updateExerciseInWorkout(LocalDate date,
                                        ExerciseInWorkout originalExercise,
                                        ExerciseInWorkout updatedExercise) {
        if (date == null || updatedExercise == null) {
            return;
        }
        loadSessionByDate(date, workout -> {
            Workout target = ensureSession(date, workout);
            List<ExerciseInWorkout> exercises = snapshotExercises(target);
            ExerciseInWorkout matcher = originalExercise != null ? originalExercise : updatedExercise;
            if (!replaceExercise(exercises, matcher, updatedExercise)) {
                exercises.add(updatedExercise);
            }
            saveSessionWithExercises(target, exercises);
        });
    }

    public void updateExerciseInWorkoutAt(LocalDate date, int index, ExerciseInWorkout updatedExercise) {
        if (date == null || updatedExercise == null) {
            return;
        }
        loadSessionByDate(date, workout -> {
            Workout target = ensureSession(date, workout);
            List<ExerciseInWorkout> exercises = snapshotExercises(target);
            if (index >= 0 && index < exercises.size()) {
                exercises.set(index, updatedExercise);
                saveSessionWithExercises(target, exercises);
            } else {
                updateExerciseInWorkout(date, null, updatedExercise);
            }
        });
    }

    public void removeExerciseFromWorkout(LocalDate date, ExerciseInWorkout exercise) {
        if (date == null || exercise == null) {
            return;
        }
        loadSessionByDate(date, workout -> {
            if (workout == null || workout.getExercises() == null) {
                return;
            }

            List<ExerciseInWorkout> exercises = snapshotExercises(workout);
            removeExercise(exercises, exercise);
            saveSessionWithExercises(workout, exercises);
        });
    }

    public void addExerciseToWorkout(LocalDate date, ExerciseInWorkout exercise) {
        if (date == null || exercise == null) {
            return;
        }
        loadSessionByDate(date, workout -> {
            Workout target = ensureSession(date, workout);
            List<ExerciseInWorkout> exercises = snapshotExercises(target);
            exercises.add(exercise);
            saveSessionWithExercises(target, exercises);
        });
    }

    public void replaceExercisesForDate(LocalDate date, List<ExerciseInWorkout> exercises) {
        if (date == null) {
            return;
        }
        loadSessionByDate(date, workout -> {
            Workout target = ensureSession(date, workout);
            List<ExerciseInWorkout> safe = exercises != null ? new ArrayList<>(exercises) : new ArrayList<>();
            saveSessionWithExercises(target, safe);
        });
    }

    public boolean exportWorkoutAsTemplate(String name, List<ExerciseInWorkout> exercises) {
        if (exercises == null || exercises.isEmpty()) {
            return false;
        }
        Workout template = new Workout();
        template.setType(WorkoutType.TEMPLATE);
        template.setDate(null);
        template.setStar(false);
        template.setName(name != null ? name.trim() : "");

        List<ExerciseInWorkout> cloned = new ArrayList<>();
        for (ExerciseInWorkout exercise : exercises) {
            if (exercise != null) {
                cloned.add(cloneExercise(exercise));
            }
        }
        if (cloned.isEmpty()) {
            return false;
        }
        template.setExercises(cloned);
        workoutRepository.saveTemplate(template);
        return true;
    }

    public boolean importExercisesFromWorkout(LocalDate targetDate, Workout template) {
        if (targetDate == null || template == null || template.getExercises() == null) {
            return false;
        }
        if (!template.isTemplate()) {
            return false;
        }
        loadSessionByDate(targetDate, workout -> {
            Workout target = ensureSession(targetDate, workout);
            List<ExerciseInWorkout> merged = snapshotExercises(target);

            for (ExerciseInWorkout exercise : template.getExercises()) {
                if (exercise == null) {
                    continue;
                }
                merged.add(cloneExercise(exercise));
            }

            if (target.getName() == null || target.getName().isEmpty()) {
                target.setName(template.getName());
            }

            saveSessionWithExercises(target, merged);
        });
        return true;
    }

    private List<ExerciseInWorkout> snapshotExercises(Workout workout) {
        List<ExerciseInWorkout> current = exercisesOnDateLiveData.getValue();
        if (current != null && !current.isEmpty()) {
            return new ArrayList<>(current);
        }
        if (workout != null && workout.getExercises() != null) {
            return new ArrayList<>(workout.getExercises());
        }
        return current != null ? new ArrayList<>(current) : new ArrayList<>();
    }

    private boolean replaceExercise(List<ExerciseInWorkout> exercises,
                                    ExerciseInWorkout original,
                                    ExerciseInWorkout updated) {
        int index = findExerciseIndex(exercises, original);
        if (index >= 0) {
            exercises.set(index, updated);
            return true;
        }
        return false;
    }

    private void removeExercise(List<ExerciseInWorkout> exercises, ExerciseInWorkout target) {
        int index = findExerciseIndex(exercises, target);
        if (index >= 0) {
            exercises.remove(index);
        }
    }

    private int findExerciseIndex(List<ExerciseInWorkout> exercises, ExerciseInWorkout target) {
        if (exercises == null || target == null) {
            return -1;
        }
        Long targetId = target.getId();
        for (int i = 0; i < exercises.size(); i++) {
            ExerciseInWorkout existing = exercises.get(i);
            if (existing == target) {
                return i;
            }
            if (existing != null && targetId != null && targetId.equals(existing.getId())) {
                return i;
            }
        }
        // Fallback: match by name + exerciseId (for imported exercises without DB IDs)
        String targetName = target.getName();
        Long targetExerciseId = target.getExerciseId();
        for (int i = 0; i < exercises.size(); i++) {
            ExerciseInWorkout existing = exercises.get(i);
            if (existing == null) continue;
            if (Objects.equals(targetName, existing.getName())
                    && Objects.equals(targetExerciseId, existing.getExerciseId())) {
                return i;
            }
        }
        return -1;
    }

    private void loadSessionByDate(LocalDate date, WorkoutRepository.OnWorkoutLoaded callback) {
        workoutRepository.loadSessionByDate(date, callback);
    }

    private Workout ensureSession(LocalDate date, Workout workout) {
        if (workout == null) {
            workout = new Workout();
            workout.setDate(date);
            workout.setType(WorkoutType.SESSION);
        }
        return workout;
    }

    private void saveSessionWithExercises(Workout workout, List<ExerciseInWorkout> exercises) {
        workout.setExercises(exercises);
        workoutRepository.saveSession(workout, this::refreshWorkoutDatesWithExercises);
        exercisesOnDateLiveData.postValue(exercises);
    }

    private List<ExerciseInWorkout> exercisesFromWorkout(Workout workout) {
        if (workout == null || workout.getExercises() == null) {
            return new ArrayList<>();
        }
        return WorkoutMapper.sortExercises(workout.getExercises());
    }

    private List<LocalDate> safeDates(List<LocalDate> dates) {
        return dates != null ? dates : new ArrayList<>();
    }

    private ExerciseInWorkout cloneExercise(ExerciseInWorkout source) {
        ExerciseInWorkout copy = new ExerciseInWorkout(
                null,
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
                null
        );
        copy.setType(source.getType());
        copy.copyCatalogMetadataFrom(source);
        copy.setShowSeries(source.isShowSeries());
        copy.setShowRepetitions(source.isShowRepetitions());
        copy.setShowWeight(source.isShowWeight());
        copy.setShowTime(source.isShowTime());
        copy.setShowHeartRate(source.isShowHeartRate());
        copy.setWeightInKg(source.isWeightInKg());
        copy.setOrderIndex(source.getOrderIndex());
        return copy;
    }
}
