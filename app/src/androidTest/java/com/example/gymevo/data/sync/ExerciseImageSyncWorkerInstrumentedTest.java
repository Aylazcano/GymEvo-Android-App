package com.example.gymevo.data.sync;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.work.ListenableWorker;
import androidx.work.testing.TestWorkerBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExerciseImageSyncWorkerInstrumentedTest {

    @Test
    public void doWork_returnsSuccessWhenSyncDisabled() {
        Context context = ApplicationProvider.getApplicationContext();

        ExerciseImageSyncWorker worker = TestWorkerBuilder
                .from(context, ExerciseImageSyncWorker.class)
                .build();

        ListenableWorker.Result result = worker.doWork();
        assertTrue(result instanceof ListenableWorker.Result.Success);
    }
}
