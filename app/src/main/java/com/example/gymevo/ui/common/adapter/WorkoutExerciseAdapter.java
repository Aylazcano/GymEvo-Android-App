package com.example.gymevo.ui.common.adapter;

import android.annotation.SuppressLint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.HapticFeedbackConstants;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.ui.common.ViewUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WorkoutExerciseAdapter extends RecyclerView.Adapter<WorkoutExerciseViewHolder> {

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseInWorkout exercise);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public interface OnOrderChangedListener {
        void onOrderChanged(List<ExerciseInWorkout> newOrder);
    }

    public interface OnExerciseDeleteListener {
        void onExerciseDelete(ExerciseInWorkout exercise);
    }

    private final List<ExerciseInWorkout> exercises = new ArrayList<>();
    private final OnExerciseClickListener onExerciseClickListener;
    private final Set<String> selectedKeys = new HashSet<>();
    private OnSelectionChangedListener selectionChangedListener;
    private OnOrderChangedListener orderChangedListener;
    private OnExerciseDeleteListener deleteListener;
    private ItemTouchHelper itemTouchHelper;
    private boolean selectionModeActive;
    private RecyclerView recyclerView;

    public WorkoutExerciseAdapter(List<ExerciseInWorkout> exercises) {
        this(exercises, null);
    }

    public WorkoutExerciseAdapter(List<ExerciseInWorkout> exercises, OnExerciseClickListener onExerciseClickListener) {
        this.exercises.addAll(copyExercises(exercises));
        this.onExerciseClickListener = onExerciseClickListener;
    }

    @Override
    public WorkoutExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_in_workout, parent, false);
        return new WorkoutExerciseViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(WorkoutExerciseViewHolder holder, int position) {
        ExerciseInWorkout exercise = exercises.get(position);
        holder.bind(exercise);
        boolean selected = isSelected(exercise);
        applySelection(holder, selected);
        applySelectionMode(holder, selectionModeActive);

        holder.itemView.setOnClickListener(v -> {
            if (selectionModeActive) {
                toggleSelection(exercise);
            } else {
                onExerciseClick(exercise);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            toggleSelection(exercise);
            return true;
        });
        if (holder.getDragHandle() != null) {
            holder.getDragHandle().setOnTouchListener((v, event) -> {
                if (itemTouchHelper != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.performClick();
                    itemTouchHelper.startDrag(holder);
                }
                return false;
            });
        }

        if (holder.getRemoveIcon() != null) {
            holder.getRemoveIcon().setOnClickListener(v -> {
                if (selectionModeActive && isSelected(exercise)) {
                    toggleSelection(exercise);
                }
                if (deleteListener != null && exercise != null) {
                    deleteListener.onExerciseDelete(exercise);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    @Override
    public void onViewRecycled(WorkoutExerciseViewHolder holder) {
        holder.clearImageLoop();
        super.onViewRecycled(holder);
    }

    @Override
    public void onViewDetachedFromWindow(WorkoutExerciseViewHolder holder) {
        holder.clearImageLoop();
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewAttachedToWindow(WorkoutExerciseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION || position >= exercises.size()) {
            return;
        }
        holder.bindImageLoop(exercises.get(position));
    }

    public void setExercises(List<ExerciseInWorkout> exercises) {
        List<ExerciseInWorkout> newItems = copyExercises(exercises);
        List<ExerciseInWorkout> oldItems = copyExercises(this.exercises);

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldItems.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                ExerciseInWorkout oldItem = oldItems.get(oldItemPosition);
                ExerciseInWorkout newItem = newItems.get(newItemPosition);
                return areSameExercise(oldItem, newItem);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                ExerciseInWorkout oldItem = oldItems.get(oldItemPosition);
                ExerciseInWorkout newItem = newItems.get(newItemPosition);
                return areExerciseContentsSame(oldItem, newItem);
            }
        });

        this.exercises.clear();
        this.exercises.addAll(newItems);
        reconcileSelection(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    public void setSelectionListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public void setOrderChangedListener(OnOrderChangedListener listener) {
        this.orderChangedListener = listener;
    }

    public void setDeleteListener(OnExerciseDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setItemTouchHelper(ItemTouchHelper helper) {
        this.itemTouchHelper = helper;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0
                || fromPosition >= exercises.size() || toPosition >= exercises.size()) {
            return false;
        }
        ExerciseInWorkout moved = exercises.remove(fromPosition);
        exercises.add(toPosition, moved);
        notifyItemMoved(fromPosition, toPosition);
        if (orderChangedListener != null) {
            orderChangedListener.onOrderChanged(new ArrayList<>(exercises));
        }
        return true;
    }

    public void moveSelectedToTop() {
        if (selectedKeys.isEmpty()) {
            return;
        }
        List<ExerciseInWorkout> selected = getSelectedItems();
        List<ExerciseInWorkout> remaining = new ArrayList<>();
        for (ExerciseInWorkout exercise : exercises) {
            if (!isSelected(exercise)) {
                remaining.add(exercise);
            }
        }
        exercises.clear();
        exercises.addAll(selected);
        exercises.addAll(remaining);
        notifyItemRangeChanged(0, getItemCount());
        if (orderChangedListener != null) {
            orderChangedListener.onOrderChanged(new ArrayList<>(exercises));
        }
        // Auto-scroll to top to focus the moved selected items
        if (recyclerView != null && !selected.isEmpty()) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    public void moveSelectedToBottom() {
        if (selectedKeys.isEmpty()) {
            return;
        }
        List<ExerciseInWorkout> selected = getSelectedItems();
        List<ExerciseInWorkout> remaining = new ArrayList<>();
        for (ExerciseInWorkout exercise : exercises) {
            if (!isSelected(exercise)) {
                remaining.add(exercise);
            }
        }
        exercises.clear();
        exercises.addAll(remaining);
        exercises.addAll(selected);
        notifyItemRangeChanged(0, getItemCount());
        if (orderChangedListener != null) {
            orderChangedListener.onOrderChanged(new ArrayList<>(exercises));
        }
        // Auto-scroll to bottom to focus the moved selected items
        if (recyclerView != null && !selected.isEmpty()) {
            recyclerView.smoothScrollToPosition(exercises.size() - 1);
        }
    }

    public List<ExerciseInWorkout> getSelectedItems() {
        List<ExerciseInWorkout> selected = new ArrayList<>();
        for (ExerciseInWorkout exercise : exercises) {
            if (isSelected(exercise)) {
                selected.add(exercise);
            }
        }
        return selected;
    }

    public List<ExerciseInWorkout> getItems() {
        return new ArrayList<>(exercises);
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

    public void toggleSelection(ExerciseInWorkout exercise) {
        if (exercise == null) {
            return;
        }
        boolean wasSelectionMode = selectionModeActive;
        String key = getSelectionKey(exercise);
        if (selectedKeys.contains(key)) {
            selectedKeys.remove(key);
        } else {
            selectedKeys.add(key);
        }
        selectionModeActive = true;

        if (!wasSelectionMode) {
            // entering selection mode — update whole list so selection-mode UI (handles) appears
            notifyItemRangeChanged(0, getItemCount());
        } else {
            int idx = exercises.indexOf(exercise);
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

    private void onExerciseClick(ExerciseInWorkout exercise) {
        if (onExerciseClickListener != null && exercise != null) {
            onExerciseClickListener.onExerciseClick(exercise);
        }
    }

    private void applySelection(WorkoutExerciseViewHolder holder, boolean selected) {
        if (!(holder.itemView instanceof MaterialCardView)) {
            return;
        }
        MaterialCardView card = (MaterialCardView) holder.itemView;
        int selectedColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.selection_highlight);
        TypedValue typedValue = new TypedValue();
        holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int defaultColor = typedValue.data;
        card.setStrokeColor(selected ? selectedColor : defaultColor);
    }

    private void applySelectionMode(WorkoutExerciseViewHolder holder, boolean selectionMode) {
        if (holder.getDragHandle() != null) {
            ViewUtils.animateOverlayVisibility(holder.getDragHandle(), selectionMode);
        }
        if (holder.getRemoveIcon() != null) {
            ViewUtils.animateOverlayVisibility(holder.getRemoveIcon(), selectionMode);
        }
    }

    private boolean isSelected(ExerciseInWorkout exercise) {
        if (exercise == null) {
            return false;
        }
        return selectedKeys.contains(getSelectionKey(exercise));
    }

    private String getSelectionKey(ExerciseInWorkout exercise) {
        if (exercise == null) {
            return "";
        }
        Long id = exercise.getId();
        if (id != null) {
            return "id:" + id;
        }
        return "mem:" + System.identityHashCode(exercise);
    }

    private void reconcileSelection(List<ExerciseInWorkout> newItems) {
        if (selectedKeys.isEmpty()) {
            return;
        }
        Set<String> newKeys = new HashSet<>();
        for (ExerciseInWorkout exercise : newItems) {
            newKeys.add(getSelectionKey(exercise));
        }
        selectedKeys.retainAll(newKeys);
        notifySelectionChanged();
    }

    private void notifySelectionChanged() {
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selectedKeys.size());
        }
    }

    private List<ExerciseInWorkout> copyExercises(List<ExerciseInWorkout> items) {
        return items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    private static boolean areSameExercise(ExerciseInWorkout oldItem, ExerciseInWorkout newItem) {
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

    private static boolean areExerciseContentsSame(ExerciseInWorkout oldItem, ExerciseInWorkout newItem) {
        if (oldItem == null || newItem == null) {
            return false;
        }
        return Objects.equals(oldItem.getName(), newItem.getName())
            && Objects.equals(oldItem.getTargetedMusclesLabel(), newItem.getTargetedMusclesLabel())
            && Objects.equals(oldItem.getSeries(), newItem.getSeries())
            && Objects.equals(oldItem.getRepetitions(), newItem.getRepetitions())
            && Objects.equals(oldItem.getWeight(), newItem.getWeight())
            && Objects.equals(oldItem.getTime(), newItem.getTime())
            && Objects.equals(oldItem.getHeartRates(), newItem.getHeartRates())
            && oldItem.isWeightInKg() == newItem.isWeightInKg()
            && oldItem.isShowSeries() == newItem.isShowSeries()
            && oldItem.isShowRepetitions() == newItem.isShowRepetitions()
            && oldItem.isShowWeight() == newItem.isShowWeight()
            && oldItem.isShowTime() == newItem.isShowTime()
            && oldItem.isShowHeartRate() == newItem.isShowHeartRate()
            && Objects.equals(oldItem.getExerciseId(), newItem.getExerciseId())
            && Objects.equals(oldItem.getWorkoutId(), newItem.getWorkoutId());
    }

}
