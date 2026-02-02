package com.example.gymevo.ui.common;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.gymevo.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class ConfirmDeleteDialog {

    private ConfirmDeleteDialog() {
    }

    public static void show(Context context,
                            int titleResId,
                            int messageResId,
                            Runnable onConfirm) {
        show(context, titleResId, messageResId, onConfirm, null);
    }

    public static void show(Context context,
                            int titleResId,
                            int messageResId,
                            Runnable onConfirm,
                            @Nullable AlertDialog parentDialog) {
        if (context == null) {
            return;
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(R.string.action_delete, (d, which) -> {
                    if (onConfirm != null) {
                        onConfirm.run();
                    }
                    if (parentDialog != null && parentDialog.isShowing()) {
                        parentDialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
