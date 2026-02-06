package com.example.gymevo.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.gymevo.R;
import com.example.gymevo.data.local.AppDatabase;
import com.example.gymevo.data.local.ExerciseInWorkoutDao;
import com.example.gymevo.data.local.WorkoutDao;
import com.example.gymevo.data.seed.WorkoutSeed;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.Workout;
import com.example.gymevo.model.WorkoutWithExercises;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkoutRepository {
    public interface OnWorkoutLoaded {
        void onLoaded(Workout workout);
    }

    public interface OnWorkoutDatesLoaded {
        void onLoaded(List<LocalDate> dates);
    }

    private final WorkoutDao workoutDao;
    private final ExerciseInWorkoutDao exerciseInWorkoutDao;
    private final LiveData<List<WorkoutWithExercises>> templatesLiveData;
    private final Application application;

    public WorkoutRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        workoutDao = db.workoutDao();
        exerciseInWorkoutDao = db.exerciseInWorkoutDao();
        templatesLiveData = workoutDao.getWorkoutsWithExercisesByType(Workout.WorkoutType.TEMPLATE);
        seedTemplatesIfEmpty();
    }

    public LiveData<List<WorkoutWithExercises>> getTemplateWorkouts() {
        return templatesLiveData;
    }

    public LiveData<List<WorkoutWithExercises>> getSessionWorkouts() {
        return workoutDao.getWorkoutsWithExercisesByType(Workout.WorkoutType.SESSION);
    }

    public void loadSessionByDate(LocalDate date, OnWorkoutLoaded callback) {
        if (callback == null) {
            return;
        }
        runOnDbThread(() -> {
            WorkoutWithExercises result = workoutDao
                    .getWorkoutWithExercisesByTypeAndDateNow(Workout.WorkoutType.SESSION, date);
            Workout workout = mapToWorkout(result);
            callback.onLoaded(workout);
        });
    }

    public void loadSessionDatesWithExercises(OnWorkoutDatesLoaded callback) {
        if (callback == null) {
            return;
        }
        runOnDbThread(() -> {
            List<LocalDate> dates = workoutDao
                    .getWorkoutDatesWithExercisesByTypeNow(Workout.WorkoutType.SESSION);
            callback.onLoaded(dates);
        });
    }

    public void saveTemplate(Workout workout) {
        saveWorkoutInternal(workout, Workout.WorkoutType.TEMPLATE);
    }

    public void saveSession(Workout workout) {
        saveWorkoutInternal(workout, Workout.WorkoutType.SESSION, null);
    }

    public void saveSession(Workout workout, Runnable onSaved) {
        saveWorkoutInternal(workout, Workout.WorkoutType.SESSION, onSaved);
    }

    public void deleteTemplate(Workout workout) {
        deleteWorkoutInternal(workout);
    }

    public void deleteSession(Workout workout) {
        deleteWorkoutInternal(workout);
    }

    public void updateWorkoutStar(Workout workout) {
        if (workout == null || workout.getId() == null) {
            return;
        }
        runOnDbThread(() -> workoutDao.updateStar(workout.getId(), workout.isStar()));
    }

    private void saveWorkoutInternal(Workout workout, Workout.WorkoutType type) {
        saveWorkoutInternal(workout, type, null);
    }

    private void saveWorkoutInternal(Workout workout, Workout.WorkoutType type, Runnable onSaved) {
        if (workout == null) {
            return;
        }
        applyType(workout, type);
        runOnDbThread(() -> {
            long now = System.currentTimeMillis();
            Long workoutId = workout.getId();
            if (workoutId == null) {
                if (workout.getCreatedAt() <= 0L) {
                    workout.setCreatedAt(now);
                }
                Workout existing = workoutDao.getWorkoutByTypeAndCreatedAtNow(type, workout.getCreatedAt());
                if (existing != null && existing.getId() != null) {
                    workoutId = existing.getId();
                    workout.setId(workoutId);
                    workout.setUpdatedAt(now);
                    workoutDao.updateWorkout(workout);
                } else {
                    workout.setUpdatedAt(now);
                    workoutId = workoutDao.insertWorkout(workout);
                    workout.setId(workoutId);
                }
            } else {
                workout.setUpdatedAt(now);
                workoutDao.updateWorkout(workout);
            }

            applyDefaultNameIfMissing(workout, workoutId, now, type);

            exerciseInWorkoutDao.deleteByWorkoutId(workoutId);
            List<ExerciseInWorkout> exercises = safeExercises(workout.getExercises());
            if (!exercises.isEmpty()) {
                applyExerciseOrder(exercises);
                for (ExerciseInWorkout exercise : exercises) {
                    if (exercise != null) {
                        exercise.setWorkoutId(workoutId);
                        exercise.setId(null); // Clear ID so it gets auto-generated on insert
                    }
                }
                exerciseInWorkoutDao.insertAll(exercises);
            }
            if (onSaved != null) {
                onSaved.run();
            }
        });
    }

    private void deleteWorkoutInternal(Workout workout) {
        if (workout == null || workout.getId() == null) {
            return;
        }
        runOnDbThread(() -> {
            exerciseInWorkoutDao.deleteByWorkoutId(workout.getId());
            workoutDao.deleteWorkout(workout);
        });
    }

    private void seedTemplatesIfEmpty() {
        runOnDbThread(() -> {
            int count = workoutDao.countByType(Workout.WorkoutType.TEMPLATE);
            if (count > 0) {
                return;
            }
            List<Workout> seed = WorkoutSeed.generateWorkouts();
            if (seed == null || seed.isEmpty()) {
                return;
            }

            for (Workout workout : seed) {
                workout.setId(null);
                applyType(workout, Workout.WorkoutType.TEMPLATE);
                Long workoutId = workoutDao.insertWorkout(workout);
                workout.setId(workoutId);
                List<ExerciseInWorkout> exercises = safeExercises(workout.getExercises());
                if (!exercises.isEmpty()) {
                    List<ExerciseInWorkout> inserts = new ArrayList<>();
                    for (ExerciseInWorkout exercise : exercises) {
                        if (exercise == null) {
                            continue;
                        }
                        exercise.setId(null);
                        exercise.setWorkoutId(workoutId);
                        inserts.add(exercise);
                    }
                    if (!inserts.isEmpty()) {
                        applyExerciseOrder(inserts);
                        exerciseInWorkoutDao.insertAll(inserts);
                    }
                }
            }
        });
    }

    private Workout mapToWorkout(WorkoutWithExercises data) {
        if (data == null || data.workout == null) {
            return null;
        }
        Workout workout = data.workout;
        workout.setExercises(sortExercises(safeExercises(data.exercises)));
        return workout;
    }

    private void applyExerciseOrder(List<ExerciseInWorkout> exercises) {
        if (exercises == null) {
            return;
        }
        for (int i = 0; i < exercises.size(); i++) {
            ExerciseInWorkout exercise = exercises.get(i);
            if (exercise != null) {
                exercise.setOrderIndex(i);
            }
        }
    }

    private List<ExerciseInWorkout> sortExercises(List<ExerciseInWorkout> exercises) {
        if (exercises == null || exercises.isEmpty()) {
            return exercises != null ? exercises : new ArrayList<>();
        }
        List<ExerciseInWorkout> sorted = new ArrayList<>(exercises);
        sorted.sort((a, b) -> Integer.compare(
                a != null ? a.getOrderIndex() : 0,
                b != null ? b.getOrderIndex() : 0));
        return sorted;
    }

    private void runOnDbThread(Runnable action) {
        AppDatabase.databaseWriteExecutor.execute(action);
    }

    private void applyType(Workout workout, Workout.WorkoutType type) {
        if (workout != null) {
            workout.setType(type);
        }
    }

    private void applyDefaultNameIfMissing(Workout workout, Long workoutId, long now,
                                           Workout.WorkoutType type) {
        if (workout == null || workoutId == null || type != Workout.WorkoutType.TEMPLATE) {
            return;
        }
        String name = workout.getName() != null ? workout.getName().trim() : "";
        if (!name.isEmpty()) {
            return;
        }
        String defaultName = application.getString(R.string.workout_default_name, workoutId);
        workout.setName(defaultName);
        workout.setUpdatedAt(now);
        workoutDao.updateWorkout(workout);
    }

    private List<ExerciseInWorkout> safeExercises(List<ExerciseInWorkout> exercises) {
        return exercises != null ? exercises : new ArrayList<>();
    }
}
