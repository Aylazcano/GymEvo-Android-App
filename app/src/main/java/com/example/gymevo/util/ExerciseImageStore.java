package com.example.gymevo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.example.gymevo.model.Exercise;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;

public final class ExerciseImageStore {
    private static final int MAX_SIDE_PX = 750;
    private static final int JPEG_QUALITY = 82;
    private static final String BASE_RELATIVE = "free_exercise_db/images";
    private static final String IMAGE_A_NAME = "0.jpg";
    private static final String IMAGE_B_NAME = "1.jpg";

    private ExerciseImageStore() {
    }

    /**
     * Persists exercise images to managed local storage.
     *
     * @return a {@link PersistResult} with the (possibly unchanged) local URIs for imageA and imageB;
     *         never mutates the Exercise argument.
     */
    public static PersistResult persistIfNeeded(Context context, Exercise exercise) {
        if (context == null || exercise == null) {
            return new PersistResult(
                    exercise != null ? exercise.getImageA() : null,
                    exercise != null ? exercise.getImageB() : null
            );
        }
        String exerciseName = trimToNull(exercise.getName());
        if (exerciseName == null) {
            return new PersistResult(exercise.getImageA(), exercise.getImageB());
        }

        File baseDir = resolveExerciseDir(context, exerciseName);
        if (!ensureDir(baseDir)) {
            return new PersistResult(exercise.getImageA(), exercise.getImageB());
        }

        String sourceImageA = trimToNull(exercise.getImageA());
        String sourceImageB = trimToNull(exercise.getImageB());
        String localImageA = persistSingle(context, sourceImageA, new File(baseDir, IMAGE_A_NAME));
        String localImageB = persistSingle(context, sourceImageB, new File(baseDir, IMAGE_B_NAME));

        return new PersistResult(localImageA, localImageB);
    }

    public static final class PersistResult {
        public final String imageA;
        public final String imageB;

        public PersistResult(String imageA, String imageB) {
            this.imageA = imageA;
            this.imageB = imageB;
        }
    }

    private static String persistSingle(Context context, String uriString, File destFile) {
        if (TextUtils.isEmpty(uriString)) {
            return null;
        }

        Uri uri = Uri.parse(uriString);
        if (isAlreadyManagedFile(uri, destFile)) {
            return uriString;
        }

        Size bounds = decodeBounds(context, uri);
        if (bounds == null) {
            return uriString;
        }

        Bitmap decoded = decodeBitmap(context, uri, bounds, MAX_SIDE_PX);
        if (decoded == null) {
            return uriString;
        }

        Bitmap scaled = scaleToMax(decoded, MAX_SIDE_PX);
        if (scaled != decoded) {
            decoded.recycle();
        }

        if (!writeJpeg(destFile, scaled)) {
            scaled.recycle();
            return uriString;
        }
        scaled.recycle();
        return Uri.fromFile(destFile).toString();
    }

    private static Size decodeBounds(Context context, Uri uri) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return null;
            }
            BitmapFactory.decodeStream(in, null, bounds);
        } catch (Exception ignored) {
            return null;
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }
        return new Size(bounds.outWidth, bounds.outHeight);
    }

    private static Bitmap decodeBitmap(Context context, Uri uri, Size sourceSize, int maxSidePx) {
        int sampleSize = 1;
        while (sourceSize.width / sampleSize > maxSidePx || sourceSize.height / sampleSize > maxSidePx) {
            sampleSize *= 2;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = Math.max(1, sampleSize);
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return null;
            }
            return BitmapFactory.decodeStream(in, null, options);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Bitmap scaleToMax(Bitmap source, int maxSide) {
        int width = source.getWidth();
        int height = source.getHeight();
        int longest = Math.max(width, height);
        if (longest <= maxSide) {
            return source;
        }
        float ratio = (float) maxSide / longest;
        int w = Math.round(width * ratio);
        int h = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(source, w, h, true);
    }

    private static boolean writeJpeg(File destFile, Bitmap bitmap) {
        try (FileOutputStream out = new FileOutputStream(destFile, false)) {
            return bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static File resolveExerciseDir(Context context, String exerciseName) {
        String safeName = sanitiseName(exerciseName);
        return new File(context.getFilesDir(), BASE_RELATIVE + "/" + safeName);
    }

    private static boolean ensureDir(File dir) {
        return dir != null && (dir.exists() || dir.mkdirs());
    }

    private static boolean isAlreadyManagedFile(Uri uri, File destFile) {
        if (uri == null || destFile == null) {
            return false;
        }
        if (!"file".equalsIgnoreCase(uri.getScheme())) {
            return false;
        }
        String path = uri.getPath();
        return path != null && path.equals(destFile.getAbsolutePath());
    }

    private static String sanitiseName(String raw) {
        if (raw == null) {
            return "exercise";
        }
        String clean = raw.trim().toLowerCase(Locale.US);
        String normalized = clean.replaceAll("[^a-z0-9]+", "_");
        return normalized.isEmpty() ? "exercise" : normalized;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private static final class Size {
        private final int width;
        private final int height;

        private Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
