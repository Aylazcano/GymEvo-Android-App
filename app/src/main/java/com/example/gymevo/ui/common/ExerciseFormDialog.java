package com.example.gymevo.ui.common;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;
import androidx.core.widget.ImageViewCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.example.gymevo.R;
import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.MuscleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.provider.OpenableColumns;

import java.util.ArrayList;
import java.util.List;

public final class ExerciseFormDialog {

    private static final String DEFAULT_MUSCLES_LABEL = "-";
    private static final int DEFAULT_NAME_MAX = 100000;

    private static final int THUMB_SIZE_PX = 256;
    private static final int THUMB_JPEG_QUALITY = 80;
    private static final String THUMB_DIR = "exercise_thumbs";
    private static final String THUMB_PREFIX = "thumb_";

    public interface OnExerciseSaved {
        void onSaved(Exercise exercise, boolean isNew);
    }

    public interface OnExerciseDeleted {
        void onDeleted(Exercise exercise);
    }

    public interface ImagePicker {
        void pickImage(OnImagePicked callback);
    }

    public interface OnImagePicked {
        void onPicked(String uriString);
    }

    private ExerciseFormDialog() {
    }

    public static void show(Context context,
                            Exercise exercise,
                            OnExerciseSaved onSaved,
                            OnExerciseDeleted onDeleted,
                            ImagePicker imagePicker) {
        if (context == null) {
            return;
        }

        boolean isNew = exercise == null;
        Exercise target = isNew ? createEmptyExercise() : exercise;

        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_edit_exercise, null, false);

        TextInputEditText nameInput = dialogView.findViewById(R.id.edit_exercise_name);
        AutoCompleteTextView musclesInput = dialogView.findViewById(R.id.edit_exercise_muscles);
        TextInputEditText startImageInput = dialogView.findViewById(R.id.edit_exercise_start_image);
        TextInputEditText endImageInput = dialogView.findViewById(R.id.edit_exercise_end_image);
        TextInputLayout startImageLayout = dialogView.findViewById(R.id.layout_exercise_start_image);
        TextInputLayout endImageLayout = dialogView.findViewById(R.id.layout_exercise_end_image);
        SwitchMaterial starSwitch = dialogView.findViewById(R.id.switch_exercise_star);
        ImageView startPreview = dialogView.findViewById(R.id.image_exercise_start_preview);
        ImageView endPreview = dialogView.findViewById(R.id.image_exercise_end_preview);
        android.widget.ImageButton removeStartButton = dialogView.findViewById(R.id.button_remove_start_image);
        android.widget.ImageButton removeEndButton = dialogView.findViewById(R.id.button_remove_end_image);
        TextView startFilename = dialogView.findViewById(R.id.text_exercise_start_filename);
        TextView endFilename = dialogView.findViewById(R.id.text_exercise_end_filename);
        View startBlock = dialogView.findViewById(R.id.layout_exercise_start_block);
        View endBlock = dialogView.findViewById(R.id.layout_exercise_end_block);

        setTextIfNotNull(nameInput, target.getName());
        setupMuscleDropdown(context, musclesInput);
        setTextIfNotNull(musclesInput, target.getTargetedMusclesLabel());
        setTextIfNotNull(startImageInput, target.getImageA());
        setTextIfNotNull(endImageInput, target.getImageB());
        updatePreview(context, startPreview, startFilename, target.getImageA());
        updatePreview(context, endPreview, endFilename, target.getImageB());
        if (starSwitch != null) {
            starSwitch.setChecked(target.isStar());
        }

        boolean hasStartImage = !isNullOrEmpty(target.getImageA());
        setVisible(endImageLayout, false);
        setVisible(startImageLayout, false);
        setVisible(endBlock, hasStartImage);
        setVisible(endPreview, hasStartImage);
        setVisible(endFilename, hasStartImage && !isNullOrEmpty(target.getImageB()));
        setVisible(startBlock, true);
        setVisible(removeStartButton, hasStartImage);
        setVisible(removeEndButton, hasStartImage && !isNullOrEmpty(target.getImageB()));

        boolean hasImagePicker = imagePicker != null;
        if (!hasImagePicker) {
            setVisible(removeStartButton, false);
            setVisible(removeEndButton, false);
        }

        if (hasImagePicker) {
            if (startPreview != null) {
                startPreview.setOnClickListener(v ->
                        imagePicker.pickImage(uri -> {
                            if (isNullOrEmpty(uri)) {
                                return;
                            }
                            String thumbUri = createThumbnailUri(context, uri, THUMB_SIZE_PX);
                            setTextIfNotNull(startImageInput, thumbUri);
                            updatePreview(context, startPreview, startFilename, thumbUri);
                            setVisible(endBlock, !isNullOrEmpty(thumbUri));
                            setVisible(endPreview, !isNullOrEmpty(thumbUri));
                            setVisible(removeStartButton, !isNullOrEmpty(thumbUri));
                        })
                );
            }
            if (endPreview != null) {
                endPreview.setOnClickListener(v ->
                        imagePicker.pickImage(uri -> {
                            if (isNullOrEmpty(uri)) {
                                return;
                            }
                            String thumbUri = createThumbnailUri(context, uri, THUMB_SIZE_PX);
                            setTextIfNotNull(endImageInput, thumbUri);
                            updatePreview(context, endPreview, endFilename, thumbUri);
                            setVisible(removeEndButton, !isNullOrEmpty(thumbUri));
                        })
                );
            }
        }

        if (removeStartButton != null) {
            removeStartButton.setOnClickListener(v -> {
                clearImage(context, startImageInput, startPreview, startFilename);
                clearImage(context, endImageInput, endPreview, endFilename);
                setVisible(endBlock, false);
                setVisible(endPreview, false);
                setVisible(removeStartButton, false);
                setVisible(removeEndButton, false);
            });
        }

        if (removeEndButton != null) {
            removeEndButton.setOnClickListener(v -> {
                clearImage(context, endImageInput, endPreview, endFilename);
                setVisible(removeEndButton, false);
            });
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
            .setTitle(isNew
                ? context.getString(R.string.exercise_add)
                : context.getString(R.string.exercise_edit))
                .setView(dialogView)
            .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String name = readText(nameInput);
                    String muscles = readText(musclesInput);
                    if (name.isEmpty()) {
                        name = buildDefaultName(context);
                    }
                    if (muscles.isEmpty()) {
                        muscles = DEFAULT_MUSCLES_LABEL;
                    }

                    target.setName(name);
                    target.setTargetedMusclesLabel(muscles);
                    target.setImageA(readTextOrNull(startImageInput));
                    target.setImageB(readTextOrNull(endImageInput));
                    target.setStar(starSwitch != null && starSwitch.isChecked());

                    if (onSaved != null) {
                        onSaved.onSaved(target, isNew);
                    }
                })
                .setNegativeButton(R.string.action_cancel, null);

        if (!isNew) {
            builder.setNeutralButton(R.string.action_delete, (dialog, which) -> {
                ConfirmDeleteDialog.show(
                        context,
                        R.string.exercise_delete_title,
                        R.string.exercise_delete_confirm,
                        () -> {
                            if (onDeleted != null) {
                                onDeleted.onDeleted(target);
                            }
                        }
                );
            });
        }

        builder.show();
    }

    private static String readText(TextInputEditText input) {
        if (input == null || input.getText() == null) {
            return "";
        }
        return input.getText().toString().trim();
    }

    private static String buildDefaultName(Context context) {
        int suffix = (int) (System.currentTimeMillis() % DEFAULT_NAME_MAX);
        return context.getString(R.string.exercise_default_name, suffix);
    }

    private static String readText(AutoCompleteTextView input) {
        if (input == null || input.getText() == null) {
            return "";
        }
        return input.getText().toString().trim();
    }

    private static String readTextOrNull(TextInputEditText input) {
        String value = readText(input);
        return value.isEmpty() ? null : value;
    }

    private static void setTextIfNotNull(TextInputEditText input, String value) {
        if (input != null && value != null) {
            input.setText(value);
        }
    }

    private static void setTextIfNotNull(AutoCompleteTextView input, String value) {
        if (input != null && value != null) {
            input.setText(value, false);
        }
    }

    private static void setupMuscleDropdown(Context context, AutoCompleteTextView input) {
        if (context == null || input == null) {
            return;
        }
        List<String> labels = new ArrayList<>();
        for (MuscleGroup group : MuscleGroup.values()) {
            labels.add(group.getLabel());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                labels
        );
        input.setAdapter(adapter);
        input.setKeyListener(null);
        input.setOnClickListener(v -> input.showDropDown());
    }

    private static void setVisible(View view, boolean visible) {
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private static Exercise createEmptyExercise() {
        return new Exercise("", "", null, null, false);
    }

    private static void updatePreview(Context context, ImageView preview, TextView filename, String uri) {
        if (preview == null) {
            return;
        }
        if (isNullOrEmpty(uri)) {
            preview.setVisibility(View.VISIBLE);
            preview.setImageResource(android.R.drawable.ic_menu_camera);
            ImageViewCompat.setImageTintList(preview, android.content.res.ColorStateList.valueOf(Color.parseColor("#DDFFFFFF")));
            setVisible(filename, false);
            return;
        }
        preview.setVisibility(View.VISIBLE);
        ImageViewCompat.setImageTintList(preview, null);
        ImageUi.loadExerciseImage(preview, uri);
        if (filename != null) {
            filename.setText(resolveFileName(context, uri));
            filename.setVisibility(View.VISIBLE);
        }
    }

    private static void clearImage(Context context, TextInputEditText input, ImageView preview, TextView filename) {
        if (input != null) {
            input.setText(null);
        }
        updatePreview(context, preview, filename, null);
    }

    private static String resolveFileName(Context context, String uriString) {
        if (context == null || isNullOrEmpty(uriString)) {
            return "";
        }
        Uri uri = Uri.parse(uriString);
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver()
                    .query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        return cursor.getString(index);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        String last = uri.getLastPathSegment();
        return last != null ? last : uriString;
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String createThumbnailUri(Context context, String uriString, int maxSizePx) {
        if (context == null || isNullOrEmpty(uriString)) {
            return uriString;
        }
        Uri uri = Uri.parse(uriString);
        try (InputStream boundsStream = context.getContentResolver().openInputStream(uri)) {
            if (boundsStream == null) {
                return uriString;
            }
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(boundsStream, null, bounds);

            int sampleSize = 1;
            int width = bounds.outWidth;
            int height = bounds.outHeight;
            while (width / sampleSize > maxSizePx || height / sampleSize > maxSizePx) {
                sampleSize *= 2;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = Math.max(1, sampleSize);
            try (InputStream imageStream = context.getContentResolver().openInputStream(uri)) {
                if (imageStream == null) {
                    return uriString;
                }
                Bitmap decoded = BitmapFactory.decodeStream(imageStream, null, options);
                if (decoded == null) {
                    return uriString;
                }

                Bitmap scaled = scaleToMax(decoded, maxSizePx);
                if (scaled != decoded) {
                    decoded.recycle();
                }

                File dir = new File(context.getCacheDir(), THUMB_DIR);
                if (!dir.exists() && !dir.mkdirs()) {
                    return uriString;
                }
                File file = new File(dir, THUMB_PREFIX + System.currentTimeMillis() + ".jpg");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    scaled.compress(Bitmap.CompressFormat.JPEG, THUMB_JPEG_QUALITY, out);
                }
                scaled.recycle();
                return Uri.fromFile(file).toString();
            }
        } catch (Exception ignored) {
            return uriString;
        }
    }

    private static Bitmap scaleToMax(Bitmap source, int maxSizePx) {
        int width = source.getWidth();
        int height = source.getHeight();
        int maxSide = Math.max(width, height);
        if (maxSide <= maxSizePx) {
            return source;
        }
        float scale = maxSizePx / (float) maxSide;
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }
}
