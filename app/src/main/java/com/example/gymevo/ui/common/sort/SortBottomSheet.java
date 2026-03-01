package com.example.gymevo.ui.common.sort;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gymevo.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class SortBottomSheet extends BottomSheetDialogFragment {

    public interface OnSortSelectedListener {
        void onSortSelected(SortField field, SortOrder order);
    }

    public static class SortOption {
        public final int labelRes;
        public final SortField field;
        public final SortOrder order;

        public SortOption(int labelRes, SortField field, SortOrder order) {
            this.labelRes = labelRes;
            this.field = field;
            this.order = order;
        }
    }

    public static class SortGroup {
        public final int titleRes;
        public final List<SortOption> options;

        public SortGroup(int titleRes, List<SortOption> options) {
            this.titleRes = titleRes;
            this.options = options != null ? options : new ArrayList<>();
        }
    }

    private List<SortGroup> groups = new ArrayList<>();
    private SortField selectedField;
    private SortOrder selectedOrder;
    private OnSortSelectedListener listener;

    public void setOptions(List<SortGroup> groups,
                           SortField selectedField,
                           SortOrder selectedOrder,
                           OnSortSelectedListener listener) {
        this.groups = groups != null ? groups : new ArrayList<>();
        this.selectedField = selectedField;
        this.selectedOrder = selectedOrder;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.bottom_sheet_sort, container, false);
        LinearLayout groupContainer = root.findViewById(R.id.sort_sheet_groups_container);
        if (groupContainer != null) {
            buildGroups(groupContainer);
        }
        return root;
    }

    private void buildGroups(@NonNull LinearLayout container) {
        container.removeAllViews();
        List<Chip> allChips = new ArrayList<>();

        for (SortGroup group : groups) {
            if (group == null || group.options == null || group.options.isEmpty()) {
                continue;
            }
            TextView titleView = new TextView(requireContext());
            titleView.setText(group.titleRes);
            titleView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleParams.topMargin = (container.getChildCount() == 0)
                    ? 0
                    : getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin) / 2;
            titleView.setLayoutParams(titleParams);
            container.addView(titleView);

            ChipGroup chipGroup = new ChipGroup(requireContext());
            chipGroup.setSingleLine(false);
            chipGroup.setSelectionRequired(false);
            chipGroup.setChipSpacing(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin) / 2);
            LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            groupParams.topMargin = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin) / 2;
            chipGroup.setLayoutParams(groupParams);
            container.addView(chipGroup);

            for (SortOption option : group.options) {
                Chip chip = new Chip(requireContext());
                chip.setText(option.labelRes);
                chip.setCheckable(true);
                chip.setChecked(isSelected(option));
                chip.setTag(option);
                chip.setOnClickListener(v -> {
                    selectOption(option, allChips);
                    if (listener != null) {
                        listener.onSortSelected(option.field, option.order);
                    }
                    dismiss();
                });
                chipGroup.addView(chip);
                allChips.add(chip);
            }
        }
    }

    private void selectOption(SortOption option, List<Chip> chips) {
        selectedField = option.field;
        selectedOrder = option.order;
        for (Chip chip : chips) {
            SortOption chipOption = chip.getTag() instanceof SortOption
                    ? (SortOption) chip.getTag()
                    : null;
            boolean isCurrent = chipOption != null
                    && chipOption.field == option.field
                    && chipOption.order == option.order;
            if (!isCurrent && chip.isChecked()) {
                chip.setChecked(false);
            }
        }
    }

    private boolean isSelected(SortOption option) {
        if (option == null) {
            return false;
        }
        return option.field == selectedField && option.order == selectedOrder;
    }
}
