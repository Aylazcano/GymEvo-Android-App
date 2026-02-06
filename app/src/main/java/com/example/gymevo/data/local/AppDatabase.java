package com.example.gymevo.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.Workout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Database(entities = {Workout.class, Exercise.class, ExerciseInWorkout.class}, version = 9, exportSchema = false)
@TypeConverters({LocalDateConverter.class, WorkoutTypeConverter.class, MuscleGroupConverter.class, ExerciseTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final int DB_THREAD_COUNT = 4;
    public static final Executor databaseWriteExecutor = Executors.newFixedThreadPool(DB_THREAD_COUNT);

    public abstract WorkoutDao workoutDao();
    public abstract ExerciseInWorkoutDao exerciseInWorkoutDao();
    public abstract ExerciseDao exerciseDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "gym_evo_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
