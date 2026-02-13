package com.example.gymevo.ui.exercisesList;

import android.annotation.SuppressLint;
import android.graphics.Color;
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
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.model.Exercise;
import com.example.gymevo.ui.common.ImageUi;
import com.example.gymevo.ui.common.StarUi;
import com.google.android.material.R.attr;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ExerciseListAdapter extends ListAdapter<Exercise, ExerciseListAdapter.ViewHolder> {

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

    private final OnExerciseClickListener onExerciseClickListener;
    private final OnExerciseStarToggleListener onExerciseStarToggleListener;
    private final Set<String> selectedKeys = new HashSet<>();
    private OnSelectionChangedListener selectionChangedListener;
    private ItemTouchHelper itemTouchHelper;
    private OnExerciseDeleteListener deleteListener;
    private boolean selectionModeActive;
    private boolean orderChangedInSelection;
    private RecyclerView recyclerView;

    private static final DiffUtil.ItemCallback<Exercise> EXERCISE_DIFF =
            new DiffUtil.ItemCallback<Exercise>() {
                @Override
                public boolean areItemsTheSame(@NonNull Exercise oldItem, @NonNull Exercise newItem) {
                    return areSameExercise(oldItem, newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Exercise oldItem, @NonNull Exercise newItem) {
                    return areExerciseContentsSame(oldItem, newItem);
                }
            };

    public ExerciseListAdapter(OnExerciseClickListener onExerciseClickListener,
                               OnExerciseStarToggleListener onExerciseStarToggleListener) {
        super(EXERCISE_DIFF);
        this.onExerciseClickListener = onExerciseClickListener;
        this.onExerciseStarToggleListener = onExerciseStarToggleListener;
    }

    public void setExercises(List<Exercise> items) {
        List<Exercise> newItems = copyExercises(items);
        submitList(newItems, () -> reconcileSelection(newItems));
    }

    public void setSelectionListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public void setItemTouchHelper(ItemTouchHelper helper) {
        this.itemTouchHelper = helper;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void setDeleteListener(OnExerciseDeleteListener listener) {
        this.deleteListener = listener;
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        List<Exercise> current = new ArrayList<>(getCurrentList());
        if (fromPosition < 0 || toPosition < 0
                || fromPosition >= current.size() || toPosition >= current.size()) {
            return false;
        }
        Exercise moved = current.remove(fromPosition);
        current.add(toPosition, moved);
        submitList(current);
        markOrderChangedIfNeeded();
        return true;
    }

    public void moveSelectedToTop() {
        if (selectedKeys.isEmpty()) {
            return;
        }
        List<Exercise> current = new ArrayList<>(getCurrentList());
        List<Exercise> selected = getSelectedItems();
        List<Exercise> remaining = new ArrayList<>();
        for (Exercise exercise : current) {
            if (!isSelected(exercise)) {
                remaining.add(exercise);
            }
        }
        List<Exercise> merged = new ArrayList<>(selected);
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
        List<Exercise> current = new ArrayList<>(getCurrentList());
        List<Exercise> selected = getSelectedItems();
        List<Exercise> remaining = new ArrayList<>();
        for (Exercise exercise : current) {
            if (!isSelected(exercise)) {
                remaining.add(exercise);
            }
        }
        List<Exercise> merged = new ArrayList<>(remaining);
        merged.addAll(selected);
        submitList(merged);
        markOrderChangedIfNeeded();
        // Auto-scroll to bottom to focus the moved selected items
        if (recyclerView != null && !selected.isEmpty()) {
            recyclerView.smoothScrollToPosition(merged.size() - 1);
        }
    }

    public List<Exercise> getSelectedItems() {
        List<Exercise> selected = new ArrayList<>();
        for (Exercise exercise : getCurrentList()) {
            if (isSelected(exercise)) {
                selected.add(exercise);
            }
        }
        return selected;
    }

    public List<Exercise> getItems() {
        return new ArrayList<>(getCurrentList());
    }

    public Exercise getItemAt(int position) {
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

    public void toggleSelection(Exercise exercise) {
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
            // only the toggled item needs to update — prevents UI flicker
            int idx = getCurrentList().indexOf(exercise);
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_catalog, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = getItem(position);
        if (exercise == null) {
            return;
        }
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
                if (itemTouchHelper != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.performClick();
                    itemTouchHelper.startDrag(holder);
                }
                return false;
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.clearImageLoop();
        super.onViewRecycled(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.clearImageLoop();
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION || position >= getCurrentList().size()) {
            return;
        }
        holder.bindImageLoop(getCurrentList().get(position));
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
            bindImageLoop(exercise);
        }

        void bindImageLoop(Exercise exercise) {
            if (exercise == null) {
                exerciseImage.setImageResource(R.drawable.ic_menu_gallery);
                return;
            }
            ImageUi.startExerciseLoop(exerciseImage, exercise.getImageA(), exercise.getImageB());
        }

        void clearImageLoop() {
            ImageUi.stopExerciseLoop(exerciseImage);
        }

        void applySelection(boolean selected) {
            if (cardView == null) {
                return;
            }
            int defaultStrokeWidth = dpToPx(cardView, 1);
            int defaultStrokeColor = resolveThemeColor(cardView, androidx.appcompat.R.attr.colorPrimary);
            int strokeWidth = selected ? dpToPx(cardView, 2) : defaultStrokeWidth;
            int strokeColor = selected
                    ? MaterialColors.getColor(cardView, com.google.android.material.R.attr.colorSecondary)
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
                && Objects.equals(oldItem.getType(), newItem.getType())
                && oldItem.isShowSeries() == newItem.isShowSeries()
                && oldItem.isShowRepetitions() == newItem.isShowRepetitions()
                && oldItem.isShowWeight() == newItem.isShowWeight()
                && oldItem.isShowTime() == newItem.isShowTime()
                && oldItem.isShowHeartRate() == newItem.isShowHeartRate()
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
            copy.setType(item.getType());
            copy.setShowSeries(item.isShowSeries());
            copy.setShowRepetitions(item.isShowRepetitions());
            copy.setShowWeight(item.isShowWeight());
            copy.setShowTime(item.isShowTime());
            copy.setShowHeartRate(item.isShowHeartRate());
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

    private void markOrderChangedIfNeeded() {
        if (selectionModeActive) {
            orderChangedInSelection = true;
        }
    }
}
