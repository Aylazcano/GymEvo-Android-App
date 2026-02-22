package com.example.gymevo.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseImageSyncQueueItem;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.Workout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Database(entities = {Workout.class, Exercise.class, ExerciseInWorkout.class, ExerciseImageSyncQueueItem.class}, version = 14, exportSchema = false)
@TypeConverters({LocalDateConverter.class, WorkoutTypeConverter.class, MuscleGroupConverter.class, ExerciseTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final int DB_THREAD_COUNT = 4;
    public static final Executor databaseWriteExecutor = Executors.newFixedThreadPool(DB_THREAD_COUNT);

    public abstract WorkoutDao workoutDao();
    public abstract ExerciseInWorkoutDao exerciseInWorkoutDao();
    public abstract ExerciseDao exerciseDao();
    public abstract ExerciseImageSyncQueueDao exerciseImageSyncQueueDao();

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

    private static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE exercise ADD COLUMN sourceId TEXT");
            database.execSQL("ALTER TABLE exercise ADD COLUMN force TEXT");
            database.execSQL("ALTER TABLE exercise ADD COLUMN level TEXT");
            database.execSQL("ALTER TABLE exercise ADD COLUMN mechanic TEXT");
            database.execSQL("ALTER TABLE exercise ADD COLUMN equipment TEXT");
            database.execSQL("ALTER TABLE exercise ADD COLUMN category TEXT");
            database.execSQL("ALTER TABLE exercise ADD COLUMN primaryMuscles TEXT");
            database.execSQL("ALTER TABLE exercise ADD COLUMN secondaryMuscles TEXT");
            database.execSQL("ALTER TABLE exercise ADD COLUMN instructions TEXT");

            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN sourceId TEXT");
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN force TEXT");
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN level TEXT");
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN mechanic TEXT");
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN equipment TEXT");
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN category TEXT");
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN primaryMuscles TEXT");
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN secondaryMuscles TEXT");
            database.execSQL("ALTER TABLE exercise_in_workout ADD COLUMN instructions TEXT");
        }
    };

        private static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("PRAGMA foreign_keys=OFF");

            database.execSQL("ALTER TABLE exercise RENAME TO exercise_old");
            database.execSQL("CREATE TABLE IF NOT EXISTS exercise ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "targetedMuscles TEXT, "
                + "sourceId TEXT, "
                + "force TEXT, "
                + "level TEXT, "
                + "mechanic TEXT, "
                + "equipment TEXT, "
                + "category TEXT, "
                + "primaryMuscles TEXT, "
                + "secondaryMuscles TEXT, "
                + "instructions TEXT, "
                + "imageA TEXT, "
                + "imageB TEXT, "
                + "isStar INTEGER NOT NULL, "
                + "type TEXT, "
                + "showSeries INTEGER NOT NULL, "
                + "showRepetitions INTEGER NOT NULL, "
                + "showWeight INTEGER NOT NULL, "
                + "showTime INTEGER NOT NULL, "
                + "showHeartRate INTEGER NOT NULL, "
                + "showDistance INTEGER NOT NULL, "
                + "showCalories INTEGER NOT NULL, "
                + "createdAt INTEGER NOT NULL, "
                + "updatedAt INTEGER NOT NULL"
                + ")");
            database.execSQL("INSERT INTO exercise ("
                + "id, name, targetedMuscles, sourceId, force, level, mechanic, equipment, category, "
                + "primaryMuscles, secondaryMuscles, instructions, imageA, imageB, isStar, type, "
                + "showSeries, showRepetitions, showWeight, showTime, showHeartRate, showDistance, "
                + "showCalories, createdAt, updatedAt"
                + ") SELECT "
                + "id, name, targetedMuscles, sourceId, force, level, mechanic, equipment, category, "
                + "primaryMuscles, secondaryMuscles, instructions, imageA, imageB, isStar, type, "
                + "showSeries, showRepetitions, showWeight, showTime, showHeartRate, showDistance, "
                + "showCalories, createdAt, updatedAt "
                + "FROM exercise_old");
            database.execSQL("DROP TABLE exercise_old");

            database.execSQL("ALTER TABLE exercise_in_workout RENAME TO exercise_in_workout_old");
            database.execSQL("CREATE TABLE IF NOT EXISTS exercise_in_workout ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "series INTEGER, "
                + "repetitions INTEGER, "
                + "weight INTEGER, "
                + "time INTEGER, "
                + "heartRates INTEGER, "
                + "distance INTEGER, "
                + "calories INTEGER, "
                + "weightInKg INTEGER NOT NULL, "
                + "orderIndex INTEGER NOT NULL, "
                + "exerciseId INTEGER, "
                + "workoutId INTEGER, "
                + "name TEXT, "
                + "targetedMuscles TEXT, "
                + "sourceId TEXT, "
                + "force TEXT, "
                + "level TEXT, "
                + "mechanic TEXT, "
                + "equipment TEXT, "
                + "category TEXT, "
                + "primaryMuscles TEXT, "
                + "secondaryMuscles TEXT, "
                + "instructions TEXT, "
                + "imageA TEXT, "
                + "imageB TEXT, "
                + "isStar INTEGER NOT NULL, "
                + "type TEXT, "
                + "showSeries INTEGER NOT NULL, "
                + "showRepetitions INTEGER NOT NULL, "
                + "showWeight INTEGER NOT NULL, "
                + "showTime INTEGER NOT NULL, "
                + "showHeartRate INTEGER NOT NULL, "
                + "showDistance INTEGER NOT NULL, "
                + "showCalories INTEGER NOT NULL, "
                + "createdAt INTEGER NOT NULL, "
                + "updatedAt INTEGER NOT NULL, "
                + "FOREIGN KEY(exerciseId) REFERENCES exercise(id) ON UPDATE NO ACTION ON DELETE CASCADE, "
                + "FOREIGN KEY(workoutId) REFERENCES workout(id) ON UPDATE NO ACTION ON DELETE CASCADE"
                + ")");
            database.execSQL("INSERT INTO exercise_in_workout ("
                + "id, series, repetitions, weight, time, heartRates, distance, calories, weightInKg, "
                + "orderIndex, exerciseId, workoutId, name, targetedMuscles, sourceId, force, level, "
                + "mechanic, equipment, category, primaryMuscles, secondaryMuscles, instructions, "
                + "imageA, imageB, isStar, type, showSeries, showRepetitions, showWeight, showTime, "
                + "showHeartRate, showDistance, showCalories, createdAt, updatedAt"
                + ") SELECT "
                + "id, series, repetitions, weight, time, heartRates, distance, calories, weightInKg, "
                + "orderIndex, exerciseId, workoutId, name, targetedMuscles, sourceId, force, level, "
                + "mechanic, equipment, category, primaryMuscles, secondaryMuscles, instructions, "
                + "imageA, imageB, isStar, type, showSeries, showRepetitions, showWeight, showTime, "
                + "showHeartRate, showDistance, showCalories, createdAt, updatedAt "
                + "FROM exercise_in_workout_old");
            database.execSQL("DROP TABLE exercise_in_workout_old");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_in_workout_exerciseId ON exercise_in_workout(exerciseId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_in_workout_workoutId ON exercise_in_workout(workoutId)");

            database.execSQL("PRAGMA foreign_keys=ON");
        }
        };

    private static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS exercise_image_sync_queue ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "exerciseKey TEXT, "
                    + "slot TEXT, "
                    + "sourceUri TEXT, "
                    + "localUri TEXT, "
                    + "localPath TEXT, "
                    + "localLastModified INTEGER NOT NULL, "
                    + "status TEXT, "
                    + "retryCount INTEGER NOT NULL, "
                    + "createdAt INTEGER NOT NULL, "
                    + "updatedAt INTEGER NOT NULL"
                    + ")");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_exercise_image_sync_queue_exerciseKey_slot_localPath "
                    + "ON exercise_image_sync_queue(exerciseKey, slot, localPath)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_image_sync_queue_status_updatedAt "
                    + "ON exercise_image_sync_queue(status, updatedAt)");
        }
    };

                private static final Migration MIGRATION_13_14 = new Migration(13, 14) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {
                    database.execSQL("PRAGMA foreign_keys=OFF");

                    database.execSQL("ALTER TABLE exercise_image_sync_queue RENAME TO exercise_image_sync_queue_old");
                    database.execSQL("CREATE TABLE IF NOT EXISTS exercise_image_sync_queue ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "exerciseId INTEGER, "
                        + "exerciseKey TEXT, "
                        + "slot TEXT, "
                        + "sourceUri TEXT, "
                        + "localUri TEXT, "
                        + "localPath TEXT, "
                        + "localLastModified INTEGER NOT NULL, "
                        + "status TEXT, "
                        + "retryCount INTEGER NOT NULL, "
                        + "createdAt INTEGER NOT NULL, "
                        + "updatedAt INTEGER NOT NULL, "
                        + "FOREIGN KEY(exerciseId) REFERENCES exercise(id) ON UPDATE NO ACTION ON DELETE CASCADE"
                        + ")");
                    database.execSQL("INSERT INTO exercise_image_sync_queue ("
                        + "id, exerciseId, exerciseKey, slot, sourceUri, localUri, localPath, "
                        + "localLastModified, status, retryCount, createdAt, updatedAt"
                        + ") SELECT "
                        + "id, NULL, exerciseKey, slot, sourceUri, localUri, localPath, "
                        + "localLastModified, status, retryCount, createdAt, updatedAt "
                        + "FROM exercise_image_sync_queue_old");
                    database.execSQL("DROP TABLE exercise_image_sync_queue_old");
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_exercise_image_sync_queue_exerciseId_slot_localPath "
                        + "ON exercise_image_sync_queue(exerciseId, slot, localPath)");
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_image_sync_queue_exerciseId "
                        + "ON exercise_image_sync_queue(exerciseId)");
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_image_sync_queue_status_updatedAt "
                        + "ON exercise_image_sync_queue(status, updatedAt)");

                    database.execSQL("PRAGMA foreign_keys=ON");
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
                            .addMigrations(MIGRATION_10_11)
                            .addMigrations(MIGRATION_11_12)
                            .addMigrations(MIGRATION_12_13)
                            .addMigrations(MIGRATION_13_14)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
