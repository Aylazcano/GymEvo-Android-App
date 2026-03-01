package com.example.gymevo.ui.workoutsList;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.HapticFeedbackConstants;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.Workout;
import com.example.gymevo.ui.common.adapter.WorkoutExerciseAdapter;
import com.example.gymevo.ui.common.DragDropItemTouchHelper;
import com.example.gymevo.ui.common.StarUi;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class WorkoutListAdapter extends ListAdapter<Workout, WorkoutListAdapter.WorkoutViewHolder> {

    public interface OnWorkoutStarToggleListener {
        void onStarToggled(Workout workout);
    }

    public interface WorkoutActionListener {
        void onToggleExpanded(Workout workout, int position);

        void onSaveWorkout(Workout workout, String name, List<ExerciseInWorkout> exercises);

        void onDeleteWorkout(Workout workout);

        void onAddExercise(List<ExerciseInWorkout> exercises, WorkoutExerciseAdapter adapter);

        void onEditExercise(ExerciseInWorkout exercise, List<ExerciseInWorkout> exercises,
                            WorkoutExerciseAdapter adapter);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public interface OnWorkoutDeleteListener {
        void onWorkoutDelete(Workout workout);
    }

    private final OnWorkoutStarToggleListener onWorkoutStarToggleListener;
    private final WorkoutActionListener workoutActionListener;
    private ExpandedWorkoutState expandedState;
    private final Set<String> selectedKeys = new HashSet<>();
    private OnSelectionChangedListener selectionChangedListener;
    private OnWorkoutDeleteListener deleteListener;
    private WorkoutExerciseAdapter.OnSelectionChangedListener nestedSelectionListener;
    private WorkoutExerciseAdapter.OnOrderChangedListener nestedOrderListener;
    private WorkoutExerciseAdapter.OnExerciseDeleteListener nestedDeleteListener;
    private ItemTouchHelper itemTouchHelper;
    private boolean selectionModeActive;
    private WorkoutExerciseAdapter expandedExerciseAdapter;
    private boolean orderChangedInSelection;
    private RecyclerView recyclerView;

    private static final DiffUtil.ItemCallback<Workout> WORKOUT_DIFF =
            new DiffUtil.ItemCallback<Workout>() {
                @Override
                public boolean areItemsTheSame(@NonNull Workout oldItem, @NonNull Workout newItem) {
                    return areSameWorkout(oldItem, newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Workout oldItem, @NonNull Workout newItem) {
                    return areWorkoutContentsSame(oldItem, newItem);
                }
            };

    public WorkoutListAdapter(OnWorkoutStarToggleListener starListener,
                              WorkoutActionListener actionListener) {
        super(WORKOUT_DIFF);
        this.onWorkoutStarToggleListener = starListener;
        this.workoutActionListener = actionListener;
    }

    public void setWorkouts(List<Workout> items) {
        List<Workout> newItems = copyWorkouts(items);
        submitList(newItems, () -> reconcileSelection(newItems));
    }

    public void setSelectionListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public void setDeleteListener(OnWorkoutDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setNestedExerciseSelectionListener(WorkoutExerciseAdapter.OnSelectionChangedListener listener) {
        this.nestedSelectionListener = listener;
        if (expandedExerciseAdapter != null) {
            expandedExerciseAdapter.setSelectionListener(listener);
        }
    }

    public void setNestedExerciseOrderListener(WorkoutExerciseAdapter.OnOrderChangedListener listener) {
        this.nestedOrderListener = listener;
        if (expandedExerciseAdapter != null) {
            expandedExerciseAdapter.setOrderChangedListener(listener);
        }
    }

    public void setNestedExerciseDeleteListener(WorkoutExerciseAdapter.OnExerciseDeleteListener listener) {
        this.nestedDeleteListener = listener;
        if (expandedExerciseAdapter != null) {
            expandedExerciseAdapter.setDeleteListener(listener);
        }
    }

    public void setItemTouchHelper(ItemTouchHelper helper) {
        this.itemTouchHelper = helper;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        List<Workout> current = new ArrayList<>(getCurrentList());
        if (fromPosition < 0 || toPosition < 0
                || fromPosition >= current.size() || toPosition >= current.size()) {
            return false;
        }
        Workout moved = current.remove(fromPosition);
        current.add(toPosition, moved);
        submitList(current);
        markOrderChangedIfNeeded();
        return true;
    }

    public void moveSelectedToTop() {
        if (selectedKeys.isEmpty()) {
            return;
        }
        List<Workout> current = new ArrayList<>(getCurrentList());
        List<Workout> selected = getSelectedItems();
        List<Workout> remaining = new ArrayList<>();
        for (Workout workout : current) {
            if (!isSelected(workout)) {
                remaining.add(workout);
            }
        }
        List<Workout> merged = new ArrayList<>(selected);
        merged.addAll(remaining);
        submitList(merged);
        markOrderChangedIfNeeded();
        // Auto-scroll to top to focus the moved selected items
        if (recyclerView != null && !selected.isEmpty()) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    public void moveSelectedToBottom() {
        if (selectedKeys.isEmpty()) {
            return;
        }
        List<Workout> current = new ArrayList<>(getCurrentList());
        List<Workout> selected = getSelectedItems();
        List<Workout> remaining = new ArrayList<>();
        for (Workout workout : current) {
            if (!isSelected(workout)) {
                remaining.add(workout);
            }
        }
        List<Workout> merged = new ArrayList<>(remaining);
        merged.addAll(selected);
        submitList(merged);
        markOrderChangedIfNeeded();
        // Auto-scroll to bottom to focus the moved selected items
        if (recyclerView != null && !selected.isEmpty()) {
            recyclerView.smoothScrollToPosition(merged.size() - 1);
        }
    }

    public List<Workout> getSelectedItems() {
        List<Workout> selected = new ArrayList<>();
        for (Workout workout : getCurrentList()) {
            if (isSelected(workout)) {
                selected.add(workout);
            }
        }
        return selected;
    }

    public List<Workout> getItems() {
        return new ArrayList<>(getCurrentList());
    }

    public Workout getItemAt(int position) {
        if (position < 0 || position >= getCurrentList().size()) {
            return null;
        }
        return getItem(position);
    }

    public void clearSelection() {
        if (selectedKeys.isEmpty() && !selectionModeActive) {
            return;
        }
        selectedKeys.clear();
        selectionModeActive = false;
        notifyItemRangeChanged(0, getItemCount());
        notifySelectionChanged();
    }

    public void toggleSelection(Workout workout) {
        if (workout == null) {
            return;
        }
        boolean wasSelectionMode = selectionModeActive;
        String key = getSelectionKey(workout);
        if (selectedKeys.contains(key)) {
            selectedKeys.remove(key);
        } else {
            selectedKeys.add(key);
        }
        selectionModeActive = true;

        if (!wasSelectionMode) {
            // entering selection mode — collapse any expanded workout so overlays are centered and not overlapping
            expandedState = null;
            // update whole list so selection-mode UI (handles) appears
            notifyItemRangeChanged(0, getItemCount());
        } else {
            int idx = getCurrentList().indexOf(workout);
            if (idx >= 0) {
                notifyItemChanged(idx);
            }
        }

        if (recyclerView != null) {
            recyclerView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
        notifySelectionChanged();
    }

    public boolean isSelectionModeActive() {
        return selectionModeActive;
    }

    public boolean consumeOrderChangedInSelection() {
        boolean changed = orderChangedInSelection;
        orderChangedInSelection = false;
        return changed;
    }

    public WorkoutExerciseAdapter getExpandedExerciseAdapter() {
        return expandedExerciseAdapter;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view, workoutActionListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = getItem(position);
        if (workout == null) {
            return;
        }
        boolean isExpanded = isExpandedWorkout(workout);
        holder.bind(workout, expandedState, isExpanded, selectionModeActive);
        holder.applySelection(isSelected(workout));
        holder.applySelectionMode(selectionModeActive);
        holder.itemView.setOnClickListener(v -> {
            if (selectionModeActive) {
                toggleSelection(workout);
            } else if (workoutActionListener != null && workout != null) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    workoutActionListener.onToggleExpanded(workout, adapterPosition);
                }
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            toggleSelection(workout);
            return true;
        });

        holder.starIcon.setOnClickListener(v -> {
            if (selectionModeActive) {
                toggleSelection(workout);
            } else {
                onStarClicked(holder, workout);
            }
        });

        if (holder.removeIcon != null) {
            holder.removeIcon.setOnClickListener(v -> {
                if (selectionModeActive && isSelected(workout)) {
                    toggleSelection(workout);
                }
                if (deleteListener != null && workout != null) {
                    deleteListener.onWorkoutDelete(workout);
                }
            });
        }

        if (holder.dragHandle != null) {
            holder.dragHandle.setOnTouchListener((v, event) -> {
                if (itemTouchHelper != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.performClick();
                    itemTouchHelper.startDrag(holder);
                }
                return false; // allow normal downstream handling
            });
        }
    }

    public void setExpandedState(Workout workoutRef, Workout workoutCopy,
                                 List<ExerciseInWorkout> exercises, boolean isNew) {
        expandedState = new ExpandedWorkoutState(workoutRef, workoutCopy, exercises, isNew);
    }

    public void clearExpandedState() {
        expandedState = null;
    }

    private boolean isExpandedWorkout(Workout workout) {
        if (expandedState == null || workout == null || expandedState.workoutRef == null) {
            return false;
        }
        Long id = workout.getId();
        Long expandedId = expandedState.workoutRef.getId();
        if (id != null && expandedId != null) {
            return id.equals(expandedId);
        }
        return workout == expandedState.workoutRef;
    }

    public class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final TextInputEditText nameInput;
        private final TextView workoutDate;
        private final TextView exerciseCount;
        private final ImageView starIcon;
        private final ImageView dragHandle;
        private final ImageView removeIcon;
        private final View expandedLayout;
        private final RecyclerView exercisesRecycler;
        private final View addExerciseButton;
        private final WorkoutExerciseAdapter exerciseAdapter;
        private Workout workout;
        private List<ExerciseInWorkout> workingExercises = new ArrayList<>();
        private final WorkoutActionListener actionListener;
        private TextWatcher nameWatcher;
        private boolean isExpanded;

                WorkoutViewHolder(@NonNull View itemView,
                            WorkoutActionListener actionListener) {
            super(itemView);
            nameInput = itemView.findViewById(R.id.input_workout_name);
            workoutDate = itemView.findViewById(R.id.text_workout_date);
            exerciseCount = itemView.findViewById(R.id.text_workout_exercise_count);
            starIcon = itemView.findViewById(R.id.image_star);
                        dragHandle = itemView.findViewById(R.id.image_drag_handle);
            removeIcon = itemView.findViewById(R.id.image_remove);
            expandedLayout = itemView.findViewById(R.id.layout_workout_expand);
            exercisesRecycler = itemView.findViewById(R.id.recycler_workout_exercises);
            addExerciseButton = itemView.findViewById(R.id.button_add_exercise);
            this.actionListener = actionListener;

            final WorkoutExerciseAdapter[] adapterRef = new WorkoutExerciseAdapter[1];
            adapterRef[0] = new WorkoutExerciseAdapter(new ArrayList<>(), ex -> {
                if (actionListener != null && ex != null) {
                    actionListener.onEditExercise(ex, workingExercises, adapterRef[0]);
                }
            });
            exerciseAdapter = adapterRef[0];
            exerciseAdapter.setSelectionListener(nestedSelectionListener);
            exerciseAdapter.setOrderChangedListener(nestedOrderListener);
            exerciseAdapter.setDeleteListener(nestedDeleteListener);
            exercisesRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            exercisesRecycler.setAdapter(adapterRef[0]);
            exercisesRecycler.setNestedScrollingEnabled(false);

            DragDropItemTouchHelper callback = new DragDropItemTouchHelper(
                    exerciseAdapter::onItemMove,
                    () -> {
                        if (nestedOrderListener != null) {
                            nestedOrderListener.onOrderChanged(exerciseAdapter.getItems());
                        }
                    }
            );
            ItemTouchHelper helper = new ItemTouchHelper(callback);
            helper.attachToRecyclerView(exercisesRecycler);
            exerciseAdapter.setItemTouchHelper(helper);
        }

        void bind(Workout workout, ExpandedWorkoutState expandedState, boolean isExpanded, boolean selectionMode) {
            this.workout = workout;
            this.isExpanded = isExpanded;
            bindNameInput(workout, expandedState, isExpanded);
            if (workout.getDate() != null) {
                workoutDate.setText(workout.getDate().toString());
            } else {
                long updatedAt = workout.getUpdatedAt();
                if (updatedAt > 0L) {
                    LocalDate updatedDate = Instant.ofEpochMilli(updatedAt)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    String templateLabel = itemView.getResources()
                            .getString(R.string.workout_item_date_placeholder);
                    workoutDate.setText(itemView.getResources().getString(
                            R.string.workout_item_date_template_format,
                            templateLabel,
                            formatUpdatedDate(updatedDate)));
                } else {
                    workoutDate.setText(R.string.workout_item_date_placeholder);
                }
            }

            int count = workout.getExercises() != null ? workout.getExercises().size() : 0;
            exerciseCount.setText(itemView.getResources().getString(R.string.workout_item_exercise_count, count));
            applyStarState(workout);

            if (expandedLayout != null) {
                expandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            }

                if (isExpanded && expandedState != null) {
                workingExercises = expandedState.exercises != null
                        ? expandedState.exercises
                        : new ArrayList<>();
                exerciseAdapter.setExercises(new ArrayList<>(workingExercises));
                expandedExerciseAdapter = exerciseAdapter;

                if (addExerciseButton != null) {
                    addExerciseButton.setOnClickListener(v -> {
                        if (actionListener != null) {
                            actionListener.onAddExercise(workingExercises, exerciseAdapter);
                        }
                    });
                }
            } else if (expandedExerciseAdapter == exerciseAdapter) {
                expandedExerciseAdapter = null;
            }

            if (nameInput != null) {
                nameInput.setOnClickListener(v -> {
                    if (selectionMode) {
                        toggleSelection(workout);
                        return;
                    }
                    if (!isExpanded && actionListener != null && workout != null) {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            actionListener.onToggleExpanded(workout, position);
                        }
                    }
                });
            }
        }

        private void bindNameInput(Workout workout, ExpandedWorkoutState expandedState, boolean isExpanded) {
            if (nameInput == null) {
                return;
            }
            if (nameWatcher != null) {
                nameInput.removeTextChangedListener(nameWatcher);
            }
            nameInput.setText(workout.getName() != null ? workout.getName() : "");
            nameInput.setEnabled(true);
            nameInput.setFocusable(isExpanded);
            nameInput.setFocusableInTouchMode(isExpanded);
            nameInput.setCursorVisible(isExpanded);
            nameInput.setLongClickable(isExpanded);
            if (isExpanded) {
                nameInput.requestFocus();
                if (nameInput.getText() != null) {
                    nameInput.setSelection(nameInput.getText().length());
                }
            } else {
                nameInput.clearFocus();
            }
            nameWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (expandedState != null && expandedState.workoutCopy != null) {
                        expandedState.workoutCopy.setName(s != null ? s.toString() : "");
                    }
                }
            };
            nameInput.addTextChangedListener(nameWatcher);
        }

        void applyStarState(Workout workout) {
            StarUi.apply(starIcon, workout.isStar());
        }

        void applySelection(boolean selected) {
            if (!(itemView instanceof MaterialCardView)) {
                return;
            }
            MaterialCardView cardView = (MaterialCardView) itemView;
            int defaultStrokeWidth = dpToPx(cardView, 1);
                int defaultStrokeColor = resolveThemeColor(cardView, androidx.appcompat.R.attr.colorPrimary);
            int strokeWidth = selected ? dpToPx(cardView, 2) : defaultStrokeWidth;
            int strokeColor = selected
                    ? ContextCompat.getColor(cardView.getContext(), R.color.selection_highlight)
                    : defaultStrokeColor;
            cardView.setStrokeWidth(strokeWidth);
            cardView.setStrokeColor(strokeColor);
        }

        void applySelectionMode(boolean selectionMode) {
            if (dragHandle != null) {
                com.example.gymevo.ui.common.ViewUtils.animateOverlayVisibility(dragHandle, selectionMode);
            }
            if (removeIcon != null) {
                com.example.gymevo.ui.common.ViewUtils.animateOverlayVisibility(removeIcon, selectionMode);
            }
            if (starIcon != null) {
                // Show star when not in selection mode so collapsed workout items display
                // the star (keeps consistency with exercise items). During selection mode
                // the star remains hidden.
                starIcon.setVisibility(selectionMode ? View.GONE : View.VISIBLE);
            }
        }

        private int dpToPx(View view, int dp) {
            return Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    view.getResources().getDisplayMetrics()));
        }

        private int resolveThemeColor(View view, int attrResId) {
            TypedValue typedValue = new TypedValue();
            if (view.getContext().getTheme().resolveAttribute(attrResId, typedValue, true)) {
                if (typedValue.resourceId != 0) {
                    return ContextCompat.getColor(view.getContext(), typedValue.resourceId);
                }
                return typedValue.data;
            }
            return Color.TRANSPARENT;
        }

        private String formatUpdatedDate(LocalDate updatedDate) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault());
            return formatter.format(updatedDate);
        }

    }

    private static boolean areSameWorkout(Workout oldItem, Workout newItem) {
        if (oldItem == null || newItem == null) {
            return false;
        }
        Long oldId = oldItem.getId();
        Long newId = newItem.getId();
        if (oldId != null && newId != null) {
            return oldId.equals(newId);
        }
        return oldItem == newItem;
    }

    private static boolean areWorkoutContentsSame(Workout oldItem, Workout newItem) {
        if (oldItem == null || newItem == null) {
            return false;
        }
        int oldCount = oldItem.getExercises() != null ? oldItem.getExercises().size() : 0;
        int newCount = newItem.getExercises() != null ? newItem.getExercises().size() : 0;
        return Objects.equals(oldItem.getName(), newItem.getName())
            && Objects.equals(oldItem.getDate(), newItem.getDate())
                && oldItem.isStar() == newItem.isStar()
                && oldItem.getType() == newItem.getType()
                && oldCount == newCount
                && oldItem.getCreatedAt() == newItem.getCreatedAt()
                && oldItem.getUpdatedAt() == newItem.getUpdatedAt();
    }

    private static List<Workout> copyWorkouts(List<Workout> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        List<Workout> copies = new ArrayList<>();
        for (Workout item : items) {
            if (item == null) {
                continue;
            }
            if (item.getId() == null) {
                copies.add(item);
                continue;
            }
            Workout copy = new Workout(
                    item.getId(),
                    item.getName(),
                    item.getDate(),
                    item.isStar(),
                    item.getType()
            );
            copy.setCreatedAt(item.getCreatedAt());
            copy.setUpdatedAt(item.getUpdatedAt());
            copy.setExercises(item.getExercises());
            copies.add(copy);
        }
        return copies;
    }

    private boolean isSelected(Workout workout) {
        if (workout == null) {
            return false;
        }
        return selectedKeys.contains(getSelectionKey(workout));
    }

    private String getSelectionKey(Workout workout) {
        if (workout == null) {
            return "";
        }
        Long id = workout.getId();
        if (id != null) {
            return "id:" + id;
        }
        String name = workout.getName() != null ? workout.getName() : "";
        String date = workout.getDate() != null ? workout.getDate().toString() : "";
        if (name.isEmpty() && date.isEmpty()) {
            return "mem:" + System.identityHashCode(workout);
        }
        return "name:" + name + "|" + date;
    }

    private void reconcileSelection(List<Workout> newItems) {
        if (selectedKeys.isEmpty()) {
            return;
        }
        Set<String> newKeys = new HashSet<>();
        for (Workout workout : newItems) {
            newKeys.add(getSelectionKey(workout));
        }
        selectedKeys.retainAll(newKeys);
        notifySelectionChanged();
    }

    private void notifySelectionChanged() {
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selectedKeys.size());
        }
    }

    private void markOrderChangedIfNeeded() {
        if (selectionModeActive) {
            orderChangedInSelection = true;
        }
    }

    /**
     * Returns the first letter of the workout name at the given position,
     * used for the fast-scroll section popup.
     */
    public String getSectionText(int position) {
        if (position < 0 || position >= getCurrentList().size()) return "";
        Workout w = getItem(position);
        if (w == null || w.getName() == null || w.getName().isEmpty()) return "";
        return w.getName().substring(0, 1).toUpperCase();
    }

    private void onStarClicked(WorkoutViewHolder holder, Workout workout) {
        workout.setStar(!workout.isStar());
        holder.applyStarState(workout);
        if (onWorkoutStarToggleListener != null) {
            onWorkoutStarToggleListener.onStarToggled(workout);
        }
        int adapterPosition = holder.getBindingAdapterPosition();
        if (adapterPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(adapterPosition);
        }
    }


    private static class ExpandedWorkoutState {
        private final Workout workoutRef;
        private final Workout workoutCopy;
        private final List<ExerciseInWorkout> exercises;
        private final boolean isNew;

        ExpandedWorkoutState(Workout workoutRef, Workout workoutCopy,
                             List<ExerciseInWorkout> exercises, boolean isNew) {
            this.workoutRef = workoutRef;
            this.workoutCopy = workoutCopy;
            this.exercises = exercises != null ? exercises : new ArrayList<>();
            this.isNew = isNew;
        }
    }

}
