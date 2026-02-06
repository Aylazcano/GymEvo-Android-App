package com.example.gymevo.ui.common;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.example.gymevo.R;
import com.example.gymevo.model.MuscleGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FilterUi {

    public interface OnFilterSelected {
        void onSelected(String value, boolean isAll);
    }

    private FilterUi() {
    }

    public static void setupMuscleFilter(MaterialAutoCompleteTextView input,
                                         Context context,
                                         String allLabel,
                                         OnFilterSelected listener) {
        if (input == null || context == null) {
            return;
        }
        List<String> labels = getMuscleLabels(allLabel);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_list_item_1,
                labels
        );
        input.setAdapter(adapter);
        input.setText(allLabel, false);
        input.setOnItemClickListener((parent, view, position, id) -> {
            String selected = position >= 0 && position < labels.size() ? labels.get(position) : null;
            boolean isAll = isAllFilter(selected, allLabel);
            if (listener != null) {
                listener.onSelected(selected, isAll);
            }
        });
    }

    public static boolean isAllFilter(String value, String allLabel) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return normalize(value).equals(normalize(allLabel));
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.US);
    }

    public static List<String> getMuscleLabels(String allLabel) {
        return buildMuscleLabels(allLabel);
    }

    public static void setupExerciseTypeFilter(MaterialAutoCompleteTextView input,
                                               Context context,
                                               String allLabel,
                                               OnFilterSelected listener) {
        if (input == null || context == null) {
            return;
        }
        List<String> labels = getExerciseTypeLabels(context, allLabel);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_list_item_1,
                labels
        );
        input.setAdapter(adapter);
        input.setText(allLabel, false);
        input.setOnItemClickListener((parent, view, position, id) -> {
            String selected = position >= 0 && position < labels.size() ? labels.get(position) : null;
            boolean isAll = isAllFilter(selected, allLabel);
            if (listener != null) {
                listener.onSelected(selected, isAll);
            }
        });
    }

    public static List<String> getExerciseTypeLabels(Context context, String allLabel) {
        List<String> labels = new ArrayList<>();
        labels.add(allLabel != null ? allLabel : "");
        if (context != null) {
            labels.add(context.getString(R.string.exercise_type_anaerobic));
            labels.add(context.getString(R.string.exercise_type_aerobic));
        }
        return labels;
    }

    private static List<String> buildMuscleLabels(String allLabel) {
        List<String> labels = new ArrayList<>();
        labels.add(allLabel != null ? allLabel : "");
        for (MuscleGroup group : MuscleGroup.values()) {
            labels.add(group.getLabel());
        }
        return labels;
    }
}
