package com.example.gymevo.ui.exercisesList;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.text.TextUtils;
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
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.model.Exercise;
import com.example.gymevo.model.ExerciseType;
import com.example.gymevo.model.MuscleGroup;
import com.example.gymevo.ui.common.ImageUi;
import com.example.gymevo.ui.common.StarUi;
import com.example.gymevo.ui.common.TextFormatUtils;
import com.google.android.material.R.attr;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ExerciseListAdapter extends ListAdapter<Exercise, ExerciseListAdapter.ViewHolder> {

    public enum ImageSlot {
        A,
        B
    }

    private static final List<String> FORCE_OPTIONS = Arrays.asList(
        "Push", "Pull", "Static"
    );

    private static final List<String> LEVEL_OPTIONS = Arrays.asList(
        "Beginner", "Intermediate", "Expert"
    );

    private static final List<String> MECHANIC_OPTIONS = Arrays.asList(
        "Compound", "Isolation"
    );

    private static final List<String> EQUIPMENT_OPTIONS = Arrays.asList(
        "Body only", "Machine", "Barbell", "Dumbbell", "Cable", "Kettlebells",
        "Bands", "Medicine ball", "Exercise ball", "E-Z curl bar", "Foam roll", "Other"
    );

    private static final List<String> CATEGORY_OPTIONS = Arrays.asList(
        "Strength", "Cardio", "Stretching", "Plyometrics", "Strongman", "Olympic Weightlifting", "Powerlifting"
    );

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

    public interface OnExerciseSaveListener {
        void onSaveExercise(Exercise exercise);
    }

    public interface OnExerciseImageRequestListener {
        void onRequestImage(Exercise exercise, ImageSlot slot);
    }

    private final OnExerciseClickListener onExerciseClickListener;
    private final OnExerciseStarToggleListener onExerciseStarToggleListener;
    private final Set<String> selectedKeys = new HashSet<>();
    private OnSelectionChangedListener selectionChangedListener;
    private ItemTouchHelper itemTouchHelper;
    private OnExerciseDeleteListener deleteListener;
    private OnExerciseSaveListener saveListener;
    private OnExerciseImageRequestListener imageRequestListener;
    private boolean selectionModeActive;
    private boolean orderChangedInSelection;
    private RecyclerView recyclerView;
    private String expandedKey = null;  // key of currently expanded exercise
    private String editingKey = null;
    private final Map<String, String[]> editingImageOverrides = new HashMap<>();

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
        setExercises(items, null);
    }

    public void setExercises(List<Exercise> items, Runnable onCommitted) {
        List<Exercise> newItems = copyExercises(items);
        submitList(newItems, () -> {
            reconcileSelection(newItems);
            if (onCommitted != null) {
                onCommitted.run();
            }
        });
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

    public void setSaveListener(OnExerciseSaveListener listener) {
        this.saveListener = listener;
    }

    public void setImageRequestListener(OnExerciseImageRequestListener listener) {
        this.imageRequestListener = listener;
    }

    /** Toggle expand/collapse for the given exercise. */
    public void toggleExpand(Exercise exercise) {
        String key = getSelectionKey(exercise);
        int oldPos = findPositionByKey(expandedKey);
        int targetPos = RecyclerView.NO_POSITION;
        if (key.equals(expandedKey)) {
            expandedKey = null;
            editingKey = null;
            editingImageOverrides.remove(key);
            if (oldPos >= 0) {
                notifyItemChanged(oldPos);
                targetPos = oldPos;
            }
        } else {
            expandedKey = key;
            editingKey = null;
            if (oldPos >= 0) notifyItemChanged(oldPos);
            int newPos = findPositionByKey(expandedKey);
            if (newPos >= 0) {
                notifyItemChanged(newPos);
                targetPos = newPos;
            }
        }
        requestScrollToPositionIfNeeded(targetPos);
    }

    public void collapseExpanded() {
        if (expandedKey != null) {
            int pos = findPositionByKey(expandedKey);
            editingImageOverrides.remove(expandedKey);
            expandedKey = null;
            editingKey = null;
            if (pos >= 0) notifyItemChanged(pos);
            requestScrollToPositionIfNeeded(pos);
        }
    }

    public boolean isExpanded() {
        return expandedKey != null;
    }

    private boolean isExpanded(Exercise exercise) {
        return expandedKey != null && expandedKey.equals(getSelectionKey(exercise));
    }

    private boolean isEditing(Exercise exercise) {
        return editingKey != null && editingKey.equals(getSelectionKey(exercise));
    }

    private void setEditing(Exercise exercise, boolean editing) {
        String key = exercise != null ? getSelectionKey(exercise) : null;
        int oldPos = findPositionByKey(editingKey);
        int newPos = findPositionByKey(key);
        if (!editing && key != null) {
            editingImageOverrides.remove(key);
        }
        editingKey = editing ? key : null;
        if (oldPos >= 0) notifyItemChanged(oldPos);
        if (newPos >= 0 && newPos != oldPos) notifyItemChanged(newPos);
    }

    public void startEditing(Exercise exercise) {
        if (exercise == null) {
            return;
        }
        String key = getSelectionKey(exercise);
        if (!key.equals(expandedKey)) {
            toggleExpand(exercise);
        }
        setEditing(exercise, true);
        int position = findPositionByKey(key);
        requestScrollToPositionIfNeeded(position);
    }

    public void setPendingImageForEditing(Exercise exercise, ImageSlot slot, String imagePath) {
        if (exercise == null || slot == null) {
            return;
        }
        String key = getSelectionKey(exercise);
        String[] images = editingImageOverrides.computeIfAbsent(key, unused -> new String[] {null, null});
        images[slot == ImageSlot.A ? 0 : 1] = imagePath;
        int pos = findPositionByKey(key);
        if (pos >= 0) {
            notifyItemChanged(pos);
        }
    }

    public void setPendingImagesForEditing(Exercise exercise, String imageA, String imageB) {
        if (exercise == null) {
            return;
        }
        String key = getSelectionKey(exercise);
        String[] images = editingImageOverrides.computeIfAbsent(key, unused -> new String[] {null, null});
        images[0] = imageA;
        images[1] = imageB;
        int pos = findPositionByKey(key);
        if (pos >= 0) {
            notifyItemChanged(pos);
        }
    }

    private String resolveImage(Exercise exercise, ImageSlot slot) {
        if (exercise == null) {
            return null;
        }
        String key = getSelectionKey(exercise);
        String[] overrides = editingImageOverrides.get(key);
        int index = slot == ImageSlot.A ? 0 : 1;
        if (overrides != null && overrides[index] != null) {
            return overrides[index];
        }
        return slot == ImageSlot.A ? exercise.getImageA() : exercise.getImageB();
    }

    private int findPositionByKey(String key) {
        if (key == null) return -1;
        for (int i = 0; i < getCurrentList().size(); i++) {
            if (key.equals(getSelectionKey(getItem(i)))) return i;
        }
        return -1;
    }

    private void requestScrollToPositionIfNeeded(int position) {
        if (recyclerView == null || position == RecyclerView.NO_POSITION) {
            return;
        }
        recyclerView.post(() -> {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
            if (holder == null) {
                recyclerView.scrollToPosition(position);
                recyclerView.post(() -> scrollExpandedToTop(position));
                return;
            }
            scrollExpandedToTop(position);
        });
    }

    private void scrollExpandedToTop(int position) {
        if (recyclerView == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
                @Override
                protected int getVerticalSnapPreference() {
                    return SNAP_TO_START;
                }
            };
            smoothScroller.setTargetPosition(position);
            linearLayoutManager.startSmoothScroll(smoothScroller);
        } else {
            recyclerView.scrollToPosition(position);
        }
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
            // entering selection mode — collapse any expanded item and update whole list
            expandedKey = null;
            editingKey = null;
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

        View clickTarget = holder.headerLayout != null ? holder.headerLayout : holder.itemView;
        clickTarget.setOnClickListener(v -> {
            if (selectionModeActive) {
                toggleSelection(exercise);
            } else {
                toggleExpand(exercise);
            }
        });
        clickTarget.setOnLongClickListener(v -> {
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

        // Expansion
        boolean expanded = isExpanded(exercise);
        boolean editing = expanded && isEditing(exercise);
        String imageA = resolveImage(exercise, ImageSlot.A);
        String imageB = resolveImage(exercise, ImageSlot.B);
        holder.bindExpanded(exercise, expanded, editing, imageA, imageB);
        if (holder.viewModeLayout != null) {
            holder.viewModeLayout.setOnClickListener(expanded && !editing ? v -> toggleExpand(exercise) : null);
        }
        if (holder.expandCardA != null) {
            holder.expandCardA.setOnClickListener(expanded
                    ? (editing
                        ? v -> {
                            if (imageRequestListener != null) {
                                imageRequestListener.onRequestImage(exercise, ImageSlot.A);
                            }
                        }
                        : v -> toggleExpand(exercise))
                    : null);
        }
        if (holder.expandCardB != null) {
            holder.expandCardB.setOnClickListener(expanded
                    ? (editing
                        ? v -> {
                            if (imageRequestListener != null) {
                                imageRequestListener.onRequestImage(exercise, ImageSlot.B);
                            }
                        }
                        : v -> toggleExpand(exercise))
                    : null);
        }
        if (holder.editButton != null) {
            holder.editButton.setOnClickListener(v -> setEditing(exercise, true));
        }
        if (holder.saveButton != null) {
            holder.saveButton.setOnClickListener(v -> {
                Exercise updated = holder.buildUpdatedExercise(exercise, imageA, imageB);
                if (saveListener != null && updated != null) {
                    saveListener.onSaveExercise(updated);
                }
                editingImageOverrides.remove(getSelectionKey(exercise));
                setEditing(exercise, false);
                collapseExpanded();
            });
        }
        if (holder.cancelButton != null) {
            holder.cancelButton.setOnClickListener(v -> setEditing(exercise, false));
        }
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
        private final View headerLayout;
        private final TextView nameText;
        private final TextView musclesText;
        private final TextView secondaryMusclesText;
        private final ImageView exerciseImage;
        final ImageView starIcon;
        final ImageView dragHandle;
        final ImageView removeIcon;
        private final MaterialCardView cardView;

        // Expanded detail views
        final View expandedLayout;
        final ImageView expandImageA;
        final ImageView expandImageB;
        final View expandCardA;
        final View expandCardB;
        final View viewModeLayout;
        final View editModeLayout;
        final View editActionsLayout;
        final TextView viewType;
        final TextView viewForce;
        final TextView viewLevel;
        final TextView viewMechanic;
        final TextView viewEquipment;
        final TextView viewCategory;
        final TextView viewInstructions;
        final TextInputEditText inputName;
        final MaterialAutoCompleteTextView inputTargetedMuscle;
        final MaterialAutoCompleteTextView inputType;
        final MaterialAutoCompleteTextView inputForce;
        final MaterialAutoCompleteTextView inputLevel;
        final MaterialAutoCompleteTextView inputMechanic;
        final MaterialAutoCompleteTextView inputEquipment;
        final MaterialAutoCompleteTextView inputCategory;
        final TextInputEditText inputSecondaryMuscles;
        final TextInputEditText inputInstructions;
        final com.google.android.material.button.MaterialButton editButton;
        final com.google.android.material.button.MaterialButton saveButton;
        final com.google.android.material.button.MaterialButton cancelButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView instanceof MaterialCardView
                    ? (MaterialCardView) itemView
                    : null;
                headerLayout = itemView.findViewById(R.id.layout_exercise_header);
            nameText = itemView.findViewById(R.id.text_exercise_name);
            musclesText = itemView.findViewById(R.id.text_exercise_muscles);
            secondaryMusclesText = itemView.findViewById(R.id.text_exercise_secondary_muscles);
            exerciseImage = itemView.findViewById(R.id.image_exercise_photo);
            starIcon = itemView.findViewById(R.id.image_exercise_star);
            dragHandle = itemView.findViewById(R.id.image_drag_handle);
            removeIcon = itemView.findViewById(R.id.image_remove);

            // Expanded views
            expandedLayout = itemView.findViewById(R.id.layout_exercise_expand);
            expandImageA = itemView.findViewById(R.id.expand_image_a);
            expandImageB = itemView.findViewById(R.id.expand_image_b);
            expandCardA = itemView.findViewById(R.id.expand_card_image_a);
            expandCardB = itemView.findViewById(R.id.expand_card_image_b);
            viewModeLayout = itemView.findViewById(R.id.layout_exercise_view_mode);
            editModeLayout = itemView.findViewById(R.id.layout_exercise_edit_mode);
            editActionsLayout = itemView.findViewById(R.id.layout_exercise_edit_actions);
            viewType = itemView.findViewById(R.id.expand_view_type);
            viewForce = itemView.findViewById(R.id.expand_view_force);
            viewLevel = itemView.findViewById(R.id.expand_view_level);
            viewMechanic = itemView.findViewById(R.id.expand_view_mechanic);
            viewEquipment = itemView.findViewById(R.id.expand_view_equipment);
            viewCategory = itemView.findViewById(R.id.expand_view_category);
            viewInstructions = itemView.findViewById(R.id.expand_view_instructions);
            inputName = itemView.findViewById(R.id.expand_input_name);
            inputTargetedMuscle = itemView.findViewById(R.id.expand_input_targeted_muscle);
            inputType = itemView.findViewById(R.id.expand_input_type);
            inputForce = itemView.findViewById(R.id.expand_input_force);
            inputLevel = itemView.findViewById(R.id.expand_input_level);
            inputMechanic = itemView.findViewById(R.id.expand_input_mechanic);
            inputEquipment = itemView.findViewById(R.id.expand_input_equipment);
            inputCategory = itemView.findViewById(R.id.expand_input_category);
            inputSecondaryMuscles = itemView.findViewById(R.id.expand_input_secondary_muscles);
            inputInstructions = itemView.findViewById(R.id.expand_input_instructions);
            editButton = itemView.findViewById(R.id.expand_button_edit);
            saveButton = itemView.findViewById(R.id.expand_button_save);
            cancelButton = itemView.findViewById(R.id.expand_button_cancel);
        }

        void bind(Exercise exercise) {
            nameText.setText(exercise.getName());
            musclesText.setText(exercise.getTargetedMusclesLabel());
            bindSecondaryMuscles(exercise.getSecondaryMuscles());
            StarUi.apply(starIcon, exercise.isStar());
            bindImageLoop(exercise);
        }

        private void bindSecondaryMuscles(String raw) {
            if (secondaryMusclesText == null) {
                return;
            }
            String display = formatSecondaryMuscles(raw);
            if (TextUtils.isEmpty(display)) {
                secondaryMusclesText.setVisibility(View.GONE);
            } else {
                secondaryMusclesText.setText(display);
                secondaryMusclesText.setVisibility(View.VISIBLE);
            }
        }

        private String formatSecondaryMuscles(String raw) {
            String result = TextFormatUtils.toEditableList(raw);
            return result.isEmpty() ? null : result;
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

        void bindExpanded(Exercise exercise, boolean expanded, boolean editing, String imageA, String imageB) {
            if (expandedLayout == null) return;
            expandedLayout.setVisibility(expanded ? View.VISIBLE : View.GONE);
            if (!expanded) return;

            if (viewModeLayout != null) viewModeLayout.setVisibility(editing ? View.GONE : View.VISIBLE);
            if (editModeLayout != null) editModeLayout.setVisibility(editing ? View.VISIBLE : View.GONE);
            if (editActionsLayout != null) editActionsLayout.setVisibility(editing ? View.VISIBLE : View.GONE);
            if (editButton != null) editButton.setVisibility(editing ? View.GONE : View.VISIBLE);

            bindViewText(viewType, itemView.getContext().getString(R.string.exercise_type), typeDisplay(exercise.getType()));
            bindViewText(viewForce, itemView.getContext().getString(R.string.exercise_detail_force, TextFormatUtils.safeText(exercise.getForce())), exercise.getForce());
            bindViewText(viewLevel, itemView.getContext().getString(R.string.exercise_detail_level, TextFormatUtils.safeText(exercise.getLevel())), exercise.getLevel());
            bindViewText(viewMechanic, itemView.getContext().getString(R.string.exercise_detail_mechanic, TextFormatUtils.safeText(exercise.getMechanic())), exercise.getMechanic());
            bindViewText(viewEquipment, itemView.getContext().getString(R.string.exercise_detail_equipment, TextFormatUtils.safeText(exercise.getEquipment())), exercise.getEquipment());
            bindViewText(viewCategory, itemView.getContext().getString(R.string.exercise_detail_category, TextFormatUtils.safeText(exercise.getCategory())), exercise.getCategory());
            bindViewText(viewInstructions, TextFormatUtils.toDisplayLines(exercise.getInstructions()), TextFormatUtils.toDisplayLines(exercise.getInstructions()));

            // Images
            boolean hasA = !TextUtils.isEmpty(imageA);
            boolean hasB = !TextUtils.isEmpty(imageB);
            if (hasA && expandImageA != null) {
                ImageUi.loadExerciseImage(expandImageA, imageA);
                if (expandCardA != null) {
                    expandCardA.setVisibility(View.VISIBLE);
                    expandCardA.setAlpha(1f);
                }
            } else if (expandImageA != null) {
                expandImageA.setImageResource(R.drawable.ic_menu_gallery);
                if (expandCardA != null) {
                    expandCardA.setVisibility(editing ? View.VISIBLE : View.GONE);
                    expandCardA.setAlpha(editing ? 0.55f : 1f);
                }
            }
            if (hasB && expandImageB != null) {
                ImageUi.loadExerciseImage(expandImageB, imageB);
                if (expandCardB != null) {
                    expandCardB.setVisibility(View.VISIBLE);
                    expandCardB.setAlpha(1f);
                }
            } else if (expandImageB != null) {
                expandImageB.setImageResource(R.drawable.ic_menu_gallery);
                if (expandCardB != null) {
                    expandCardB.setVisibility(editing ? View.VISIBLE : View.GONE);
                    expandCardB.setAlpha(editing ? 0.55f : 1f);
                }
            }

            if (!editing) {
                return;
            }

            if (inputName != null) inputName.setText(TextFormatUtils.safeText(exercise.getName()));
            setupTargetedMuscleDropdown();
            setupTypeDropdown();
            setupMetadataDropdowns();
            if (inputTargetedMuscle != null) inputTargetedMuscle.setText(TextFormatUtils.safeText(exercise.getTargetedMusclesLabel()), false);
            if (inputType != null) inputType.setText(typeDisplay(exercise.getType()), false);
            if (inputForce != null) inputForce.setText(TextFormatUtils.safeText(exercise.getForce()), false);
            if (inputLevel != null) inputLevel.setText(TextFormatUtils.safeText(exercise.getLevel()), false);
            if (inputMechanic != null) inputMechanic.setText(TextFormatUtils.safeText(exercise.getMechanic()), false);
            if (inputEquipment != null) inputEquipment.setText(TextFormatUtils.safeText(exercise.getEquipment()), false);
            if (inputCategory != null) inputCategory.setText(TextFormatUtils.safeText(exercise.getCategory()), false);
            if (inputSecondaryMuscles != null) inputSecondaryMuscles.setText(TextFormatUtils.toEditableList(exercise.getSecondaryMuscles()));
            if (inputInstructions != null) inputInstructions.setText(TextFormatUtils.toDisplayLines(exercise.getInstructions()));

        }

        Exercise buildUpdatedExercise(Exercise source, String imageA, String imageB) {
            if (source == null) {
                return null;
            }
            Exercise updated = new Exercise(
                    TextFormatUtils.safeText(valueOf(inputName, source.getName())),
                    TextFormatUtils.safeText(valueOf(inputTargetedMuscle, source.getTargetedMusclesLabel())),
                    imageA,
                    imageB,
                    source.isStar()
            );
                updated.setType(parseType(valueOf(inputType,
                    source.getType() != null ? source.getType().name() : ExerciseType.ANAEROBIC.name())));
            updated.setId(source.getId());
            updated.setCreatedAt(source.getCreatedAt());
            updated.setUpdatedAt(System.currentTimeMillis());
            updated.setStar(source.isStar());
                updated.setSourceId(source.getSourceId());
            updated.setForce(TextFormatUtils.emptyToNull(valueOf(inputForce, source.getForce())));
            updated.setLevel(TextFormatUtils.emptyToNull(valueOf(inputLevel, source.getLevel())));
            updated.setMechanic(TextFormatUtils.emptyToNull(valueOf(inputMechanic, source.getMechanic())));
            updated.setEquipment(TextFormatUtils.emptyToNull(valueOf(inputEquipment, source.getEquipment())));
            updated.setCategory(TextFormatUtils.emptyToNull(valueOf(inputCategory, source.getCategory())));
            updated.setPrimaryMuscles(TextFormatUtils.toSingleItemJsonArray(updated.getTargetedMusclesLabel()));
            updated.setSecondaryMuscles(TextFormatUtils.toJsonArrayString(valueOf(inputSecondaryMuscles, source.getSecondaryMuscles())));
            updated.setInstructions(TextFormatUtils.toJsonArrayString(valueOf(inputInstructions, source.getInstructions())));
            updated.setShowSeries(source.isShowSeries());
            updated.setShowRepetitions(source.isShowRepetitions());
            updated.setShowWeight(source.isShowWeight());
            updated.setShowTime(source.isShowTime());
            updated.setShowHeartRate(source.isShowHeartRate());
            updated.setShowDistance(source.isShowDistance());
            updated.setShowCalories(source.isShowCalories());
            return updated;
        }

        private void setupTypeDropdown() {
            if (inputType == null || inputType.getAdapter() != null) {
                return;
            }
            List<String> options = new ArrayList<>();
            options.add(typeDisplay(ExerciseType.ANAEROBIC));
            options.add(typeDisplay(ExerciseType.AEROBIC));
            inputType.setAdapter(new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_list_item_1, options));
        }

        private void setupTargetedMuscleDropdown() {
            if (inputTargetedMuscle == null || inputTargetedMuscle.getAdapter() != null) {
                return;
            }
            List<String> options = new ArrayList<>();
            for (MuscleGroup group : MuscleGroup.values()) {
                options.add(group.getLabel());
            }
            inputTargetedMuscle.setAdapter(new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_list_item_1, options));
        }

        private void setupMetadataDropdowns() {
            setupEditableDropdown(inputForce, FORCE_OPTIONS);
            setupEditableDropdown(inputLevel, LEVEL_OPTIONS);
            setupEditableDropdown(inputMechanic, MECHANIC_OPTIONS);
            setupEditableDropdown(inputEquipment, EQUIPMENT_OPTIONS);
            setupEditableDropdown(inputCategory, CATEGORY_OPTIONS);
        }

        private void setupEditableDropdown(MaterialAutoCompleteTextView input, List<String> options) {
            if (input == null || input.getAdapter() != null) {
                return;
            }
            input.setAdapter(new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_list_item_1, options));
            input.setThreshold(0);
            input.setOnClickListener(v -> input.showDropDown());
            input.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    input.showDropDown();
                }
            });
        }

        private String typeDisplay(ExerciseType type) {
            return type != null ? type.getLabel() : ExerciseType.ANAEROBIC.getLabel();
        }

        private ExerciseType parseType(String rawType) {
            ExerciseType byLabel = ExerciseType.fromLabel(rawType);
            if (byLabel != null) {
                return byLabel;
            }
            String normalized = TextFormatUtils.safeText(rawType).toUpperCase(Locale.ROOT);
            return "AEROBIC".equals(normalized) ? ExerciseType.AEROBIC : ExerciseType.ANAEROBIC;
        }

        private String valueOf(TextView input, String fallback) {
            if (input == null || input.getText() == null) {
                return fallback;
            }
            return input.getText().toString();
        }

        private void bindViewText(TextView view, String formattedValue, String rawValue) {
            if (view == null) {
                return;
            }
            boolean visible = !TextUtils.isEmpty(TextFormatUtils.safeText(rawValue));
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible) {
                view.setText(formattedValue);
            }
        }

        void applySelection(boolean selected) {
            if (cardView == null) {
                return;
            }
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
                && Objects.equals(oldItem.getSourceId(), newItem.getSourceId())
                && Objects.equals(oldItem.getForce(), newItem.getForce())
                && Objects.equals(oldItem.getLevel(), newItem.getLevel())
                && Objects.equals(oldItem.getMechanic(), newItem.getMechanic())
                && Objects.equals(oldItem.getEquipment(), newItem.getEquipment())
                && Objects.equals(oldItem.getCategory(), newItem.getCategory())
                && Objects.equals(oldItem.getPrimaryMuscles(), newItem.getPrimaryMuscles())
                && Objects.equals(oldItem.getSecondaryMuscles(), newItem.getSecondaryMuscles())
                && Objects.equals(oldItem.getInstructions(), newItem.getInstructions())
                && oldItem.isStar() == newItem.isStar()
                && Objects.equals(oldItem.getType(), newItem.getType())
                && oldItem.isShowSeries() == newItem.isShowSeries()
                && oldItem.isShowRepetitions() == newItem.isShowRepetitions()
                && oldItem.isShowWeight() == newItem.isShowWeight()
                && oldItem.isShowTime() == newItem.isShowTime()
                && oldItem.isShowHeartRate() == newItem.isShowHeartRate()
                && oldItem.isShowDistance() == newItem.isShowDistance()
                && oldItem.isShowCalories() == newItem.isShowCalories()
                && oldItem.getCreatedAt() == newItem.getCreatedAt()
                && oldItem.getUpdatedAt() == newItem.getUpdatedAt();
    }

    private static List<Exercise> copyExercises(List<Exercise> items) {
        List<Exercise> copies = new ArrayList<>();
        if (items == null) {
            return copies;
        }
        for (Exercise item : items) {
            if (item != null) {
                copies.add(new Exercise(item));
            }
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
        StarUi.apply(holder.starIcon, exercise.isStar());
        if (onExerciseStarToggleListener != null) {
            onExerciseStarToggleListener.onStarToggled(exercise);
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

    /**
     * Returns the first letter of the exercise name at the given position,
     * used for the fast-scroll section popup.
     */
    public String getSectionText(int position) {
        if (position < 0 || position >= getCurrentList().size()) return "";
        Exercise ex = getItem(position);
        if (ex == null || ex.getName() == null || ex.getName().isEmpty()) return "";
        return ex.getName().substring(0, 1).toUpperCase();
    }
}
