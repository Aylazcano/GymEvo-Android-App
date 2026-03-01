package com.example.gymevo.ui.common;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.model.Workout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Bottom sheet for importing a workout template with search, sort, and star priority.
 */
public class ImportWorkoutBottomSheet extends BottomSheetDialogFragment {

    public interface OnWorkoutSelectedListener {
        void onWorkoutSelected(Workout workout);
    }

    private List<Workout> allWorkouts = new ArrayList<>();
    private OnWorkoutSelectedListener listener;
    private ImportAdapter adapter;
    private boolean starFirst = false;
    private boolean sortByName = false;
    private String searchQuery = "";

    public static ImportWorkoutBottomSheet newInstance(List<Workout> workouts,
                                                       OnWorkoutSelectedListener listener) {
        ImportWorkoutBottomSheet sheet = new ImportWorkoutBottomSheet();
        sheet.allWorkouts = workouts != null ? new ArrayList<>(workouts) : new ArrayList<>();
        sheet.listener = listener;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_import_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recycler = view.findViewById(R.id.recycler_import_workouts);
        TextView emptyView = view.findViewById(R.id.text_import_empty);
        EditText searchEdit = view.findViewById(R.id.edit_import_search);
        Chip chipStarFirst = view.findViewById(R.id.chip_star_first);
        Chip chipSortName = view.findViewById(R.id.chip_sort_name);
        Chip chipSortRecent = view.findViewById(R.id.chip_sort_recent);

        adapter = new ImportAdapter(workout -> {
            dismiss();
            if (listener != null) listener.onWorkoutSelected(workout);
        });
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                applyFilters(recycler, emptyView);
            }
        });

        chipStarFirst.setOnCheckedChangeListener((btn, checked) -> {
            starFirst = checked;
            applyFilters(recycler, emptyView);
        });

        chipSortName.setOnClickListener(v -> {
            sortByName = true;
            chipSortName.setChecked(true);
            chipSortRecent.setChecked(false);
            applyFilters(recycler, emptyView);
        });

        chipSortRecent.setOnClickListener(v -> {
            sortByName = false;
            chipSortRecent.setChecked(true);
            chipSortName.setChecked(false);
            applyFilters(recycler, emptyView);
        });

        applyFilters(recycler, emptyView);
    }

    @Override
    public void onStart() {
        super.onStart();
        View sheet = getDialog() != null
                ? getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet)
                : null;
        if (sheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    private void applyFilters(RecyclerView recycler, TextView emptyView) {
        List<Workout> filtered = new ArrayList<>();
        String query = searchQuery.toLowerCase(Locale.ROOT);
        for (Workout w : allWorkouts) {
            if (w == null) continue;
            if (!TextUtils.isEmpty(query)) {
                String name = w.getName() != null ? w.getName().toLowerCase(Locale.ROOT) : "";
                if (!name.contains(query)) continue;
            }
            filtered.add(w);
        }

        // Sort
        Comparator<Workout> comparator;
        if (sortByName) {
            comparator = Comparator.comparing(
                    w -> w.getName() != null ? w.getName().toLowerCase(Locale.ROOT) : "");
        } else {
            comparator = Comparator.comparingLong(Workout::getCreatedAt).reversed();
        }

        if (starFirst) {
            comparator = Comparator.<Workout, Boolean>comparing(w -> !w.isStar())
                    .thenComparing(comparator);
        }

        Collections.sort(filtered, comparator);

        adapter.submitList(filtered);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        recycler.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // ---- Inner adapter ----

    private static class ImportAdapter extends ListAdapter<Workout, ImportAdapter.VH> {

        interface OnItemClickListener {
            void onClick(Workout workout);
        }

        private final OnItemClickListener clickListener;

        ImportAdapter(OnItemClickListener clickListener) {
            super(DIFF);
            this.clickListener = clickListener;
        }

        private static final DiffUtil.ItemCallback<Workout> DIFF = new DiffUtil.ItemCallback<Workout>() {
            @Override
            public boolean areItemsTheSame(@NonNull Workout a, @NonNull Workout b) {
                return a.getId() != null && a.getId().equals(b.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Workout a, @NonNull Workout b) {
                return TextUtils.equals(a.getName(), b.getName()) && a.isStar() == b.isStar();
            }
        };

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_import_workout, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Workout workout = getItem(position);
            holder.bind(workout);
            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onClick(workout);
            });
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView nameText;
            final TextView countText;
            final ImageView starIcon;

            VH(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.text_workout_name);
                countText = itemView.findViewById(R.id.text_exercise_count);
                starIcon = itemView.findViewById(R.id.icon_star);
            }

            void bind(Workout workout) {
                String name = workout.getName();
                nameText.setText(TextUtils.isEmpty(name) ? "Workout" : name);
                StarUi.apply(starIcon, workout.isStar());

                int exerciseCount = workout.getExercises() != null
                        ? workout.getExercises().size() : 0;
                countText.setText(itemView.getContext().getString(
                        R.string.workout_item_exercise_count, exerciseCount));
            }
        }
    }
}
