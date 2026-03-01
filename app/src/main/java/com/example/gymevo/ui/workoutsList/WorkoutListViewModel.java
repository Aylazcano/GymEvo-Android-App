package com.example.gymevo.ui.workoutsList;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gymevo.data.repository.WorkoutRepository;
import com.example.gymevo.model.Workout;
import com.example.gymevo.model.Workout.WorkoutType;
import com.example.gymevo.model.WorkoutWithExercises;
import com.example.gymevo.ui.common.WorkoutMapper;

import java.util.List;

public class WorkoutListViewModel extends AndroidViewModel {
    private final MutableLiveData<Workout> currentWorkoutLiveData = new MutableLiveData<>();
    private final MediatorLiveData<List<Workout>> workoutsLiveData = new MediatorLiveData<>();
    private final WorkoutRepository workoutRepository;

    public WorkoutListViewModel(@NonNull Application application) {
        super(application);
        workoutRepository = new WorkoutRepository(application);
        workoutsLiveData.addSource(workoutRepository.getTemplateWorkouts(),
                items -> workoutsLiveData.setValue(mapToWorkouts(items)));
    }

    public LiveData<List<Workout>> getWorkoutsLiveData() {
        return workoutsLiveData;
    }

    public Workout createNewTemplateWorkout() {
        Workout workout = new Workout();
        applyTemplateDefaults(workout);
        workout.setDate(null);
        long now = System.currentTimeMillis();
        workout.setCreatedAt(now);
        workout.setUpdatedAt(now);
        workoutRepository.saveTemplate(workout);
        currentWorkoutLiveData.postValue(workout);
        return workout;
    }

    public void setCurrentWorkoutWithExercises(Workout workout) {
        applyTemplateDefaults(workout);
        currentWorkoutLiveData.setValue(workout);
    }

    public void updateCurrentWorkoutMeta(String name) {
        Workout currentWorkout = ensureCurrentWorkout();
        if (name != null) {
            currentWorkout.setName(name);
        }
        currentWorkout.setDate(null);
        currentWorkoutLiveData.setValue(currentWorkout);
    }

    public void saveWorkout() {
        Workout currentWorkout = currentWorkoutLiveData.getValue();
        if (currentWorkout != null) {
            applyTemplateDefaults(currentWorkout);
            workoutRepository.saveTemplate(currentWorkout);
        }
    }

    public void deleteWorkout(Workout workout) {
        if (workout == null) return;
        workoutRepository.deleteTemplate(workout);
        currentWorkoutLiveData.setValue(new Workout());
    }

    public void duplicateWorkout(Workout workout) {
        if (workout == null) return;
        applyTemplateDefaults(workout);
        workoutRepository.saveTemplate(workout);
    }

    public void toggleWorkoutStar(Workout workout) {
        if (workout == null) return;
        workoutRepository.updateWorkoutStar(workout);
    }

    private List<Workout> mapToWorkouts(List<WorkoutWithExercises> items) {
        List<Workout> workouts = WorkoutMapper.mapToWorkouts(items);
        if (!workouts.isEmpty()) {
            currentWorkoutLiveData.setValue(workouts.get(0));
        } else {
            currentWorkoutLiveData.setValue(new Workout());
        }
        return workouts;
    }

    private Workout ensureCurrentWorkout() {
        Workout currentWorkout = currentWorkoutLiveData.getValue();
        if (currentWorkout == null) {
            currentWorkout = new Workout();
        }
        applyTemplateDefaults(currentWorkout);
        return currentWorkout;
    }

    private void applyTemplateDefaults(Workout workout) {
        if (workout == null) {
            return;
        }
        workout.setType(WorkoutType.TEMPLATE);
    }

}
