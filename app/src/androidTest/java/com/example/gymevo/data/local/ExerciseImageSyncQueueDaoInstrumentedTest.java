package com.example.gymevo.data.local;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseImageSyncQueueItem;
import com.example.gymevo.model.MuscleGroup;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ExerciseImageSyncQueueDaoInstrumentedTest {

    private AppDatabase db;
    private ExerciseDao exerciseDao;
    private ExerciseImageSyncQueueDao queueDao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        exerciseDao = db.exerciseDao();
        queueDao = db.exerciseImageSyncQueueDao();
    }

    @After
    public void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    @Test
    public void queueItem_isReturnedAndDeletedByCascade() {
        Exercise exercise = new Exercise("phase3_queue_test", MuscleGroup.CHEST, null, null, false);
        long exerciseId = exerciseDao.insert(exercise);
        exercise.setId(exerciseId);

        long now = System.currentTimeMillis();
        ExerciseImageSyncQueueItem item = new ExerciseImageSyncQueueItem();
        item.setExerciseId(exerciseId);
        item.setExerciseKey(exercise.getName());
        item.setSlot("imageA");
        item.setSourceUri("file:///tmp/source.jpg");
        item.setLocalUri("file:///tmp/local.jpg");
        item.setLocalPath("/tmp/local.jpg");
        item.setLocalLastModified(now);
        item.setStatus(ExerciseImageSyncQueueItem.STATUS_PENDING);
        item.setRetryCount(0);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        queueDao.upsert(item);

        List<ExerciseImageSyncQueueItem> pending = queueDao.getNextBatchForSync(
                ExerciseImageSyncQueueItem.STATUS_PENDING,
                ExerciseImageSyncQueueItem.STATUS_FAILED,
                10,
                10
        );
        assertEquals(1, pending.size());

        exerciseDao.delete(exercise);

        List<ExerciseImageSyncQueueItem> afterDelete = queueDao.getNextBatchForSync(
                ExerciseImageSyncQueueItem.STATUS_PENDING,
                ExerciseImageSyncQueueItem.STATUS_FAILED,
                10,
                10
        );
        assertEquals(0, afterDelete.size());
    }
}
