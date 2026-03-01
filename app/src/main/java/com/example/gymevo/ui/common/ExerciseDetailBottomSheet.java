package com.example.gymevo.ui.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gymevo.R;
import com.example.gymevo.model.Exercise;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * Bottom sheet showing exercise details: images, metadata chips,
 * muscles, and instructions. Provides an Edit button to open
 * the edit form.
 */
public class ExerciseDetailBottomSheet extends BottomSheetDialogFragment {

    private Exercise exercise;
    private Runnable onEditClicked;

    public static ExerciseDetailBottomSheet newInstance(Exercise exercise, Runnable onEditClicked) {
        ExerciseDetailBottomSheet sheet = new ExerciseDetailBottomSheet();
        sheet.exercise = exercise;
        sheet.onEditClicked = onEditClicked;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercise_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (exercise == null) {
            dismiss();
            return;
        }
        bindExercise(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Expand the bottom sheet fully
        View sheet = getDialog() != null ? getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet) : null;
        if (sheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    private void bindExercise(View view) {
        // Name
        TextView nameText = view.findViewById(R.id.text_exercise_name);
        nameText.setText(exercise.getName());

        // Images
        ImageView imageA = view.findViewById(R.id.image_a);
        ImageView imageB = view.findViewById(R.id.image_b);
        View cardA = view.findViewById(R.id.card_image_a);
        View cardB = view.findViewById(R.id.card_image_b);

        boolean hasA = !TextUtils.isEmpty(exercise.getImageA());
        boolean hasB = !TextUtils.isEmpty(exercise.getImageB());

        if (hasA) {
            ImageUi.loadExerciseImage(imageA, exercise.getImageA());
        } else {
            cardA.setVisibility(View.GONE);
        }

        if (hasB) {
            ImageUi.loadExerciseImage(imageB, exercise.getImageB());
        } else {
            cardB.setVisibility(View.GONE);
        }

        // Star
        ImageView starIcon = view.findViewById(R.id.icon_star);
        StarUi.apply(starIcon, exercise.isStar());

        // Type chip
        Chip typeChip = view.findViewById(R.id.chip_type);
        if (exercise.getType() != null) {
            typeChip.setText(exercise.getType().name());
        } else {
            typeChip.setVisibility(View.GONE);
        }

        // Edit button
        MaterialButton editButton = view.findViewById(R.id.button_edit);
        editButton.setOnClickListener(v -> {
            dismiss();
            if (onEditClicked != null) {
                onEditClicked.run();
            }
        });

        // Metadata chips
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_metadata);
        addChipIfPresent(chipGroup, getString(R.string.exercise_detail_force, exercise.getForce()), exercise.getForce());
        addChipIfPresent(chipGroup, getString(R.string.exercise_detail_level, exercise.getLevel()), exercise.getLevel());
        addChipIfPresent(chipGroup, getString(R.string.exercise_detail_mechanic, exercise.getMechanic()), exercise.getMechanic());
        addChipIfPresent(chipGroup, getString(R.string.exercise_detail_equipment, exercise.getEquipment()), exercise.getEquipment());
        addChipIfPresent(chipGroup, getString(R.string.exercise_detail_category, exercise.getCategory()), exercise.getCategory());

        if (chipGroup.getChildCount() == 0) {
            chipGroup.setVisibility(View.GONE);
        }

        // Muscles
        TextView labelMuscles = view.findViewById(R.id.label_muscles);
        TextView primaryText = view.findViewById(R.id.text_primary_muscles);
        TextView secondaryText = view.findViewById(R.id.text_secondary_muscles);

        String targeted = exercise.getTargetedMusclesLabel();
        String primary = exercise.getPrimaryMuscles();
        String secondary = exercise.getSecondaryMuscles();

        boolean hasMuscleInfo = !TextUtils.isEmpty(targeted)
                || !TextUtils.isEmpty(primary)
                || !TextUtils.isEmpty(secondary);

        if (hasMuscleInfo) {
            labelMuscles.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(primary)) {
                primaryText.setText(getString(R.string.exercise_detail_primary, primary));
                primaryText.setVisibility(View.VISIBLE);
            } else if (!TextUtils.isEmpty(targeted)) {
                primaryText.setText(targeted);
                primaryText.setVisibility(View.VISIBLE);
            } else {
                primaryText.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(secondary)) {
                secondaryText.setText(getString(R.string.exercise_detail_secondary, secondary));
                secondaryText.setVisibility(View.VISIBLE);
            } else {
                secondaryText.setVisibility(View.GONE);
            }
        } else {
            labelMuscles.setVisibility(View.GONE);
            primaryText.setVisibility(View.GONE);
            secondaryText.setVisibility(View.GONE);
        }

        // Instructions
        TextView labelInstructions = view.findViewById(R.id.label_instructions);
        TextView instructionsText = view.findViewById(R.id.text_instructions);
        String instructions = exercise.getInstructions();

        if (!TextUtils.isEmpty(instructions)) {
            labelInstructions.setVisibility(View.VISIBLE);
            instructionsText.setVisibility(View.VISIBLE);
            instructionsText.setText(instructions);
        }
    }

    private void addChipIfPresent(ChipGroup group, String label, String value) {
        if (TextUtils.isEmpty(value) || !isAdded()) {
            return;
        }
        Chip chip = new Chip(requireContext());
        chip.setText(label);
        chip.setClickable(false);
        chip.setCheckable(false);
        group.addView(chip);
    }
}
