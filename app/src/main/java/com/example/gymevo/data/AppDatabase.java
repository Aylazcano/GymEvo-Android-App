package com.example.gymevo.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.example.gymevo.models.Exercise;
import com.example.gymevo.models.ExerciseInWorkout;
import com.example.gymevo.models.Workout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Database(entities = {Workout.class, Exercise.class, ExerciseInWorkout.class}, version = 1, exportSchema = false)
@TypeConverters(LocalDateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public static Executor databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public abstract WorkoutDao workoutDao();
    public abstract ExerciseInWorkoutDao exerciseInWorkoutDao();
    public abstract ExerciseDao exerciseDao();

    private static volatile AppDatabase INSTANCE;
    private static final Object LOCK = new Object();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "gym_evo_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
