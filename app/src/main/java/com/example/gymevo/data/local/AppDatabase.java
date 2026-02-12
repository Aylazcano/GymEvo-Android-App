package com.example.gymevo.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.Workout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Database(entities = {Workout.class, Exercise.class, ExerciseInWorkout.class}, version = 10, exportSchema = false)
@TypeConverters({LocalDateConverter.class, WorkoutTypeConverter.class, MuscleGroupConverter.class, ExerciseTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final int DB_THREAD_COUNT = 4;
    public static final Executor databaseWriteExecutor = Executors.newFixedThreadPool(DB_THREAD_COUNT);

    public abstract WorkoutDao workoutDao();
    public abstract ExerciseInWorkoutDao exerciseInWorkoutDao();
    public abstract ExerciseDao exerciseDao();

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add new display flags to Exercise (default false = 0)
            database.execSQL("ALTER TABLE exercise ADD COLUMN showDistance INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE exercise ADD COLUMN showCalories INTEGER NOT NULL DEFAULT 0");
            // Add per-workout metrics to ExerciseInWorkout (nullable integers)
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN distance INTEGER");
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN calories INTEGER");
            // Add display flags to ExerciseInWorkout (inherits from Exercise) - set defaults
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN showDistance INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN showCalories INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "gym_evo_database")
                            .addMigrations(MIGRATION_9_10)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
