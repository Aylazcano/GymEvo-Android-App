package com.example.gymevo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.MuscleGroup;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExerciseImageStoreInstrumentedTest {

    @Test
    public void persistIfNeeded_writesManagedImagesAndNoSidecar() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        String exerciseName = "phase3_image_store_test_" + System.currentTimeMillis();
        Exercise exercise = new Exercise(exerciseName, MuscleGroup.CHEST, null, null, false);

        File sourceA = createSourceImage(context, "source_a_" + System.nanoTime() + ".jpg");
        File sourceB = createSourceImage(context, "source_b_" + System.nanoTime() + ".jpg");
        exercise.setImageA(Uri.fromFile(sourceA).toString());
        exercise.setImageB(Uri.fromFile(sourceB).toString());

        ExerciseImageStore.PersistResult result = ExerciseImageStore.persistIfNeeded(context, exercise);

        assertNotNull(result.imageA);
        assertNotNull(result.imageB);

        File managedA = new File(Uri.parse(result.imageA).getPath());
        File managedB = new File(Uri.parse(result.imageB).getPath());
        assertTrue(managedA.exists());
        assertTrue(managedB.exists());
        assertTrue(managedA.getName().equals("0.jpg"));
        assertTrue(managedB.getName().equals("1.jpg"));

        File exerciseDir = managedA.getParentFile();
        assertNotNull(exerciseDir);
        assertFalse(new File(exerciseDir, "_image_sources.json").exists());
    }

    private File createSourceImage(Context context, String fileName) throws Exception {
        File file = new File(context.getCacheDir(), fileName);
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        try (FileOutputStream output = new FileOutputStream(file, false)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output);
        } finally {
            bitmap.recycle();
        }
        return file;
    }
}
