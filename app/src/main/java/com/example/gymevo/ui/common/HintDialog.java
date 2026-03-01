package com.example.gymevo.ui.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.gymevo.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Multi-step onboarding hint dialog shown on first launch.
 * Walks the user through the major sections of the app.
 */
public class HintDialog {

    private static final String PREFS_NAME = "gymevo_hint_prefs";
    private static final String KEY_HINT_SHOWN = "hint_shown";

    private static final int[][] PAGES = {
            {R.string.hint_welcome_title, R.string.hint_welcome_message},
            {R.string.hint_tracker_title, R.string.hint_tracker_message},
            {R.string.hint_exercises_title, R.string.hint_exercises_message},
            {R.string.hint_workouts_title, R.string.hint_workouts_message},
            {R.string.hint_statistics_title, R.string.hint_statistics_message},
            {R.string.hint_tips_title, R.string.hint_tips_message},
    };

    /**
     * Shows the hint dialog only if it hasn't been shown before.
     */
    public static void showIfFirstTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_HINT_SHOWN, false)) {
            return;
        }
        show(context);
        prefs.edit().putBoolean(KEY_HINT_SHOWN, true).apply();
    }

    /**
     * Shows the hint dialog unconditionally (e.g. from Settings).
     */
    public static void show(Context context) {
        showPage(context, 0);
    }

    /**
     * Resets the hint so it shows again on next launch.
     */
    public static void reset(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_HINT_SHOWN, false).apply();
    }

    private static void showPage(Context context, int pageIndex) {
        if (pageIndex < 0 || pageIndex >= PAGES.length) {
            return;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_hint, null);
        TextView title = view.findViewById(R.id.hint_title);
        TextView message = view.findViewById(R.id.hint_message);
        TextView indicator = view.findViewById(R.id.hint_page_indicator);
        MaterialButton btnBack = view.findViewById(R.id.hint_button_back);
        MaterialButton btnSkip = view.findViewById(R.id.hint_button_skip);
        MaterialButton btnNext = view.findViewById(R.id.hint_button_next);

        title.setText(PAGES[pageIndex][0]);
        message.setText(PAGES[pageIndex][1]);
        indicator.setText(context.getString(R.string.hint_page_indicator, pageIndex + 1, PAGES.length));

        boolean isFirst = pageIndex == 0;
        boolean isLast = pageIndex == PAGES.length - 1;

        btnBack.setVisibility(isFirst ? View.GONE : View.VISIBLE);
        btnSkip.setVisibility(isLast ? View.GONE : View.VISIBLE);
        btnNext.setText(isLast ? R.string.hint_done : R.string.hint_next);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        btnBack.setOnClickListener(v -> {
            dialog.dismiss();
            showPage(context, pageIndex - 1);
        });

        btnSkip.setOnClickListener(v -> dialog.dismiss());

        btnNext.setOnClickListener(v -> {
            dialog.dismiss();
            if (!isLast) {
                showPage(context, pageIndex + 1);
            }
        });

        dialog.show();
    }
}
