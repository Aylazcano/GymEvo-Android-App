package com.example.gymevo.ui.exercisesList;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.model.Exercise;
import com.example.gymevo.ui.common.ImageUi;
import com.example.gymevo.ui.common.StarUi;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ExerciseListAdapter extends RecyclerView.Adapter<ExerciseListAdapter.ViewHolder> {

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    public interface OnExerciseStarToggleListener {
        void onStarToggled(Exercise exercise);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public interface OnExerciseDeleteListener {
        void onExerciseDelete(Exercise exercise);
    }

    private final List<Exercise> exercises = new ArrayList<>();
    private final OnExerciseClickListener onExerciseClickListener;
    private final OnExerciseStarToggleListener onExerciseStarToggleListener;
    private final Set<String> selectedKeys = new HashSet<>();
    private OnSelectionChangedListener selectionChangedListener;
    private ItemTouchHelper itemTouchHelper;
    private OnExerciseDeleteListener deleteListener;
    private boolean selectionModeActive;

    public ExerciseListAdapter(OnExerciseClickListener onExerciseClickListener,
                               OnExerciseStarToggleListener onExerciseStarToggleListener) {
        this.onExerciseClickListener = onExerciseClickListener;
        this.onExerciseStarToggleListener = onExerciseStarToggleListener;
    }

    public void setExercises(List<Exercise> items) {
        List<Exercise> newItems = copyExercises(items);
        List<Exercise> oldItems = copyExercises(exercises);

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
                Exercise oldItem = oldItems.get(oldItemPosition);
                Exercise newItem = newItems.get(newItemPosition);
                return areSameExercise(oldItem, newItem);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Exercise oldItem = oldItems.get(oldItemPosition);
                Exercise newItem = newItems.get(newItemPosition);
                return areExerciseContentsSame(oldItem, newItem);
            }
        });

        exercises.clear();
        exercises.addAll(newItems);
        reconcileSelection(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    public void setSelectionListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public void setItemTouchHelper(ItemTouchHelper helper) {
        this.itemTouchHelper = helper;
    }

    public void setDeleteListener(OnExerciseDeleteListener listener) {
        this.deleteListener = listener;
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0
                || fromPosition >= exercises.size() || toPosition >= exercises.size()) {
            return false;
        }
        Exercise moved = exercises.remove(fromPosition);
        exercises.add(toPosition, moved);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public void moveSelectedToTop() {
        if (selectedKeys.isEmpty()) {
            return;
        }
        List<Exercise> selected = getSelectedItems();
        List<Exercise> remaining = new ArrayList<>();
        for (Exercise exercise : exercises) {
            if (!isSelected(exercise)) {
                remaining.add(exercise);
            }
        }
        exercises.clear();
        exercises.addAll(selected);
        exercises.addAll(remaining);
        notifyDataSetChanged();
    }

    public void moveSelectedToBottom() {
        if (selectedKeys.isEmpty()) {
            return;
        }
        List<Exercise> selected = getSelectedItems();
        List<Exercise> remaining = new ArrayList<>();
        for (Exercise exercise : exercises) {
            if (!isSelected(exercise)) {
                remaining.add(exercise);
            }
        }
        exercises.clear();
        exercises.addAll(remaining);
        exercises.addAll(selected);
        notifyDataSetChanged();
    }

    public List<Exercise> getSelectedItems() {
        List<Exercise> selected = new ArrayList<>();
        for (Exercise exercise : exercises) {
            if (isSelected(exercise)) {
                selected.add(exercise);
            }
        }
        return selected;
    }

    public void clearSelection() {
        if (selectedKeys.isEmpty() && !selectionModeActive) {
            return;
        }
        selectedKeys.clear();
        selectionModeActive = false;
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void toggleSelection(Exercise exercise) {
        if (exercise == null) {
            return;
        }
        String key = getSelectionKey(exercise);
        if (selectedKeys.contains(key)) {
            selectedKeys.remove(key);
        } else {
            selectedKeys.add(key);
        }
        selectionModeActive = true;
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public boolean isSelectionModeActive() {
        return selectionModeActive;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_catalog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.bind(exercise);
        boolean selected = isSelected(exercise);
        holder.applySelection(selected);
        holder.applySelectionMode(selectionModeActive);

        holder.itemView.setOnClickListener(v -> {
            if (selectionModeActive) {
                toggleSelection(exercise);
            } else {
                onExerciseClicked(exercise);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            toggleSelection(exercise);
            return true;
        });
        holder.starIcon.setOnClickListener(v -> {
            if (selectionModeActive) {
                toggleSelection(exercise);
            } else {
                onStarClicked(holder, exercise);
            }
        });
        if (holder.removeIcon != null) {
            holder.removeIcon.setOnClickListener(v -> {
                if (selectionModeActive && isSelected(exercise)) {
                    toggleSelection(exercise);
                }
                if (deleteListener != null && exercise != null) {
                    deleteListener.onExerciseDelete(exercise);
                }
            });
        }
        if (holder.dragHandle != null) {
            holder.dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                        && itemTouchHelper != null) {
                    itemTouchHelper.startDrag(holder);
                }
                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.clearImageLoop();
        super.onViewRecycled(holder);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView musclesText;
        private final ImageView exerciseImage;
        final ImageView starIcon;
        final ImageView dragHandle;
        final ImageView removeIcon;
        private final MaterialCardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView instanceof MaterialCardView
                    ? (MaterialCardView) itemView
                    : null;
            nameText = itemView.findViewById(R.id.text_exercise_name);
            musclesText = itemView.findViewById(R.id.text_exercise_muscles);
            exerciseImage = itemView.findViewById(R.id.image_exercise_photo);
            starIcon = itemView.findViewById(R.id.image_exercise_star);
            dragHandle = itemView.findViewById(R.id.image_drag_handle);
            removeIcon = itemView.findViewById(R.id.image_remove);
        }

        void bind(Exercise exercise) {
            nameText.setText(exercise.getName());
            musclesText.setText(exercise.getTargetedMusclesLabel());
            StarUi.apply(starIcon, exercise.isStar());
            ImageUi.startExerciseLoop(exerciseImage, exercise.getImageA(), exercise.getImageB());
        }

        void clearImageLoop() {
            ImageUi.stopExerciseLoop(exerciseImage);
        }

        void applySelection(boolean selected) {
            if (cardView == null) {
                return;
            }
            int strokeWidth = selected ? dpToPx(cardView, 2) : 0;
            int strokeColor = ContextCompat.getColor(cardView.getContext(), R.color.example_1_selection_color);
            cardView.setStrokeWidth(strokeWidth);
            cardView.setStrokeColor(selected ? strokeColor : Color.TRANSPARENT);
        }

        void applySelectionMode(boolean selectionMode) {
            if (dragHandle != null) {
                dragHandle.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
            }
            if (removeIcon != null) {
                removeIcon.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
            }
            if (starIcon != null) {
                starIcon.setVisibility(selectionMode ? View.GONE : View.VISIBLE);
            }
        }

        private int dpToPx(View view, int dp) {
            return Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    view.getResources().getDisplayMetrics()));
        }
    }

    private static boolean areSameExercise(Exercise oldItem, Exercise newItem) {
        if (oldItem == null || newItem == null) {
            return false;
        }
        Long oldId = oldItem.getId();
        Long newId = newItem.getId();
        if (oldId != null && newId != null) {
            return oldId.equals(newId);
        }
        return Objects.equals(oldItem.getName(), newItem.getName())
            && Objects.equals(oldItem.getTargetedMusclesLabel(), newItem.getTargetedMusclesLabel());
    }

    private static boolean areExerciseContentsSame(Exercise oldItem, Exercise newItem) {
        if (oldItem == null || newItem == null) {
            return false;
        }
        return Objects.equals(oldItem.getName(), newItem.getName())
            && Objects.equals(oldItem.getTargetedMusclesLabel(), newItem.getTargetedMusclesLabel())
            && Objects.equals(oldItem.getImageA(), newItem.getImageA())
            && Objects.equals(oldItem.getImageB(), newItem.getImageB())
                && oldItem.isStar() == newItem.isStar()
                && oldItem.getCreatedAt() == newItem.getCreatedAt()
                && oldItem.getUpdatedAt() == newItem.getUpdatedAt();
    }

    private static List<Exercise> copyExercises(List<Exercise> items) {
        List<Exercise> copies = new ArrayList<>();
        if (items == null) {
            return copies;
        }
        for (Exercise item : items) {
            if (item == null) {
                continue;
            }
            Exercise copy = new Exercise(
                    item.getName() != null ? item.getName() : "",
                    item.getTargetedMusclesLabel() != null ? item.getTargetedMusclesLabel() : "",
                        item.getImageA(),
                        item.getImageB(),
                    item.isStar()
            );
            copy.setId(item.getId());
            copy.setCreatedAt(item.getCreatedAt());
            copy.setUpdatedAt(item.getUpdatedAt());
            copies.add(copy);
        }
        return copies;
    }

    private void onExerciseClicked(Exercise exercise) {
        if (onExerciseClickListener != null) {
            onExerciseClickListener.onExerciseClick(exercise);
        }
    }

    private void onStarClicked(ViewHolder holder, Exercise exercise) {
        exercise.setStar(!exercise.isStar());
        if (onExerciseStarToggleListener != null) {
            onExerciseStarToggleListener.onStarToggled(exercise);
        }
        int adapterPosition = holder.getBindingAdapterPosition();
        if (adapterPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(adapterPosition);
        }
    }

    private boolean isSelected(Exercise exercise) {
        if (exercise == null) {
            return false;
        }
        return selectedKeys.contains(getSelectionKey(exercise));
    }

    private String getSelectionKey(Exercise exercise) {
        if (exercise == null) {
            return "";
        }
        Long id = exercise.getId();
        if (id != null) {
            return "id:" + id;
        }
        String name = exercise.getName() != null ? exercise.getName() : "";
        String muscles = exercise.getTargetedMusclesLabel() != null ? exercise.getTargetedMusclesLabel() : "";
        return "name:" + name + "|" + muscles;
    }

    private void reconcileSelection(List<Exercise> newItems) {
        if (selectedKeys.isEmpty()) {
            return;
        }
        Set<String> newKeys = new HashSet<>();
        for (Exercise exercise : newItems) {
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
}
