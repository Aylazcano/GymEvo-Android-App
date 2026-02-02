package com.example.gymevo.ui.exercisesList;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gymevo.R;
import com.example.gymevo.databinding.FragmentExercisesListBinding;
import com.example.gymevo.model.Exercise;
import com.example.gymevo.ui.common.ConfirmDeleteDialog;
import com.example.gymevo.ui.common.DragDropItemTouchHelper;
import com.example.gymevo.ui.common.ExerciseFormDialog;
import com.example.gymevo.ui.common.FilterUi;
import com.example.gymevo.ui.common.ItemSpacingDecoration;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExerciseListFragment extends Fragment {

    public static final String ARG_OPEN_ADD_EXERCISE = "OPEN_ADD_EXERCISE";

    private FragmentExercisesListBinding binding;
    private ExerciseListAdapter adapter;
    private ExerciseListViewModel viewModel;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ExerciseFormDialog.OnImagePicked pendingImagePicked;
    private ActionMode selectionActionMode;
    private androidx.activity.OnBackPressedCallback backPressedCallback;
    private final List<Exercise> allExercises = new ArrayList<>();
    private String searchQuery = "";
    private String muscleFilter = null;
    private SortField sortField = SortField.NAME;
    private SortOrder sortOrder = SortOrder.ASC;
    private boolean starPriorityEnabled;
    private final ActionMode.Callback selectionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multi_select, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_delete) {
                onDeleteSelectedExercises();
                return true;
            } else if (id == R.id.action_move_up) {
                onMoveSelectedExercisesTop();
                return true;
            } else if (id == R.id.action_move_down) {
                onMoveSelectedExercisesBottom();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selectionActionMode = null;
            if (adapter != null) {
                adapter.clearSelection();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImagePicker();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ExerciseListViewModel.class);

        binding = FragmentExercisesListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        adapter = new ExerciseListAdapter(this::showEditDialog, viewModel::toggleExerciseStar);
        binding.recyclerExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerExercises.setAdapter(adapter);
        binding.recyclerExercises.addItemDecoration(new ItemSpacingDecoration(dpToPx(4), true));

        adapter.setSelectionListener(this::updateSelectionUi);
        adapter.setDeleteListener(this::onDeleteSingleExercise);

        DragDropItemTouchHelper callback = new DragDropItemTouchHelper(adapter::onItemMove, null);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(binding.recyclerExercises);
        adapter.setItemTouchHelper(helper);

        binding.fabAddExercise.setOnClickListener(v -> showEditDialog(null));

        viewModel.getExercises().observe(getViewLifecycleOwner(), exercises -> {
            allExercises.clear();
            if (exercises != null) {
                allExercises.addAll(exercises);
            }
            applyExerciseFilters();
        });

        setupFilters();
        observeFabAddRequests();
        setupBackNavigation();
        return root;
    }

    private void setupFilters() {
        if (binding == null) {
            return;
        }

        binding.filterHeader.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString() : "";
                applyExerciseFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.filterHeader.buttonFilter.setOnClickListener(v -> showMuscleFilterDialog());
        binding.filterHeader.buttonSort.setOnClickListener(v -> showSortDialog());
        binding.filterHeader.buttonStar.setOnClickListener(v -> toggleStarPriority());
        binding.filterHeader.buttonClear.setOnClickListener(v -> {
            if (binding.filterHeader.inputSearch.getText() != null) {
                binding.filterHeader.inputSearch.getText().clear();
            }
            searchQuery = "";
            applyExerciseFilters();
        });
        updateStarButton();
    }

    private void showMuscleFilterDialog() {
        if (!isAdded()) {
            return;
        }
        List<String> labels = FilterUi.getMuscleLabels(getString(R.string.filter_all));
        String selected = muscleFilter != null ? muscleFilter : getString(R.string.filter_all);
        int selectedIndex = Math.max(0, labels.indexOf(selected));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.filter_title)
                .setSingleChoiceItems(labels.toArray(new String[0]), selectedIndex, (dialog, which) -> {
                    String value = labels.get(which);
                    muscleFilter = FilterUi.isAllFilter(value, getString(R.string.filter_all))
                            ? null : value;
                    applyExerciseFilters();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showSortDialog() {
        if (!isAdded()) {
            return;
        }
        List<SortOption> options = buildSortOptions();
        CharSequence[] labels = new CharSequence[options.size()];
        for (int i = 0; i < options.size(); i++) {
            labels[i] = getString(options.get(i).labelRes);
        }
        int selectedIndex = findSelectedSortIndex(options);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sort_title)
                .setSingleChoiceItems(labels, selectedIndex, (dialog, which) -> {
                    SortOption option = options.get(which);
                    sortField = option.field;
                    sortOrder = option.order;
                    applyExerciseFilters();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void toggleStarPriority() {
        starPriorityEnabled = !starPriorityEnabled;
        updateStarButton();
        applyExerciseFilters();
    }

    private void updateStarButton() {
        if (binding == null) {
            return;
        }
        int icon = starPriorityEnabled
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off;
        binding.filterHeader.buttonStar.setImageResource(icon);
    }

    private List<SortOption> buildSortOptions() {
        List<SortOption> options = new ArrayList<>();
        options.add(new SortOption(R.string.sort_name_asc, SortField.NAME, SortOrder.ASC));
        options.add(new SortOption(R.string.sort_name_desc, SortField.NAME, SortOrder.DESC));
        options.add(new SortOption(R.string.sort_muscle_asc, SortField.MUSCLE, SortOrder.ASC));
        options.add(new SortOption(R.string.sort_muscle_desc, SortField.MUSCLE, SortOrder.DESC));
        options.add(new SortOption(R.string.sort_created_new, SortField.CREATED, SortOrder.DESC));
        options.add(new SortOption(R.string.sort_created_old, SortField.CREATED, SortOrder.ASC));
        options.add(new SortOption(R.string.sort_updated_new, SortField.UPDATED, SortOrder.DESC));
        options.add(new SortOption(R.string.sort_updated_old, SortField.UPDATED, SortOrder.ASC));
        return options;
    }

    private int findSelectedSortIndex(List<SortOption> options) {
        for (int i = 0; i < options.size(); i++) {
            SortOption option = options.get(i);
            if (option.field == sortField && option.order == sortOrder) {
                return i;
            }
        }
        return 0;
    }

    private void applyExerciseFilters() {
        List<Exercise> filtered = new ArrayList<>();
        String query = FilterUi.normalize(searchQuery);
        for (Exercise exercise : allExercises) {
            if (exercise == null) {
                continue;
            }
            if (!matchesExercise(exercise, query)) {
                continue;
            }
            filtered.add(exercise);
        }

        Comparator<Exercise> comparator = buildExerciseComparator();
        filtered.sort(comparator);
        adapter.setExercises(filtered);

        boolean isEmpty = filtered.isEmpty();
        binding.textExercisesEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerExercises.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private boolean matchesExercise(Exercise exercise, String query) {
        if (exercise == null) {
            return false;
        }
        if (muscleFilter != null) {
            String muscleLabel = FilterUi.normalize(exercise.getTargetedMusclesLabel());
            if (!muscleLabel.equals(FilterUi.normalize(muscleFilter))) {
                return false;
            }
        }
        if (query.isEmpty()) {
            return true;
        }
        String name = FilterUi.normalize(exercise.getName());
        String muscleLabel = FilterUi.normalize(exercise.getTargetedMusclesLabel());
        return name.contains(query) || muscleLabel.contains(query);
    }

    private Comparator<Exercise> buildExerciseComparator() {
        Comparator<Exercise> base;
        switch (sortField) {
            case MUSCLE:
                base = Comparator.comparing(
                    (Exercise exercise) -> FilterUi.normalize(exercise.getTargetedMusclesLabel())
                ).thenComparing(exercise -> FilterUi.normalize(exercise.getName()));
                break;
            case CREATED:
                base = Comparator.comparingLong(Exercise::getCreatedAt);
                break;
            case UPDATED:
                base = Comparator.comparingLong(Exercise::getUpdatedAt);
                break;
            case NAME:
            default:
                base = Comparator.comparing(exercise -> FilterUi.normalize(exercise.getName()));
                break;
        }

        if (sortOrder == SortOrder.DESC) {
            base = base.reversed();
        }
        if (starPriorityEnabled) {
            base = Comparator.comparing(Exercise::isStar).reversed().thenComparing(base);
        }
        return base;
    }

    private void setupBackNavigation() {
        if (!isAdded()) {
            return;
        }
        backPressedCallback = new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (selectionActionMode != null) {
                    selectionActionMode.finish();
                    return;
                }
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressedCallback);
    }

    private void observeFabAddRequests() {
        NavController navController = NavHostFragment.findNavController(this);
        if (navController.getCurrentBackStackEntry() == null) {
            return;
        }
        navController.getCurrentBackStackEntry()
                .getSavedStateHandle()
                .getLiveData(ARG_OPEN_ADD_EXERCISE, false)
                .observe(getViewLifecycleOwner(), shouldOpen -> {
                    if (Boolean.TRUE.equals(shouldOpen)) {
                        navController.getCurrentBackStackEntry()
                                .getSavedStateHandle()
                                .set(ARG_OPEN_ADD_EXERCISE, false);
                        showEditDialog(null);
                    }
                });
    }

    private void showEditDialog(Exercise exercise) {
        Exercise safeExercise = copyExercise(exercise);
        ExerciseFormDialog.ImagePicker imagePicker = callback -> {
            pendingImagePicked = callback;
            imagePickerLauncher.launch("image/*");
        };

        ExerciseFormDialog.show(
                requireContext(),
                safeExercise,
                (updated, isNew) -> {
                    if (isNew) {
                        if (updated.getCreatedAt() <= 0L) {
                            updated.setCreatedAt(System.currentTimeMillis());
                        }
                        viewModel.addExercise(updated);
                    } else {
                        viewModel.updateExercise(updated);
                    }
                },
                viewModel::deleteExercise,
                imagePicker
        );
    }

    private void updateSelectionUi(int selectedCount) {
        if (!isAdded()) {
            return;
        }
        if (selectedCount > 0) {
            if (selectionActionMode == null) {
                AppCompatActivity activity = (AppCompatActivity) requireActivity();
                selectionActionMode = activity.startSupportActionMode(selectionCallback);
            }
            if (selectionActionMode != null) {
                selectionActionMode.setTitle(getString(R.string.selection_count, selectedCount));
            }
        } else if (selectionActionMode != null) {
            if (adapter != null && adapter.isSelectionModeActive()) {
                selectionActionMode.setTitle(getString(R.string.selection_count, 0));
            } else {
                selectionActionMode.finish();
            }
        }
    }

    private void onDeleteSelectedExercises() {
        if (adapter == null) {
            return;
        }
        ConfirmDeleteDialog.show(
                requireContext(),
                R.string.exercise_delete_title,
                R.string.exercise_delete_confirm,
                () -> {
                    for (Exercise exercise : adapter.getSelectedItems()) {
                        viewModel.deleteExercise(exercise);
                    }
                    adapter.clearSelection();
                }
        );
    }

    private void onMoveSelectedExercisesTop() {
        if (adapter == null) {
            return;
        }
        adapter.moveSelectedToTop();
        adapter.clearSelection();
    }

    private void onMoveSelectedExercisesBottom() {
        if (adapter == null) {
            return;
        }
        adapter.moveSelectedToBottom();
        adapter.clearSelection();
    }

    private void onDeleteSingleExercise(Exercise exercise) {
        if (exercise == null) {
            return;
        }
        ConfirmDeleteDialog.show(
                requireContext(),
                R.string.exercise_delete_title,
                R.string.exercise_delete_confirm,
                () -> {
                    viewModel.deleteExercise(exercise);
                }
        );
    }

    private Exercise copyExercise(Exercise exercise) {
        if (exercise == null) {
            return null;
        }
        Exercise copy = new Exercise(
                exercise.getName() != null ? exercise.getName() : "",
                exercise.getTargetedMusclesLabel() != null ? exercise.getTargetedMusclesLabel() : "",
                exercise.getImageA(),
                exercise.getImageB(),
                exercise.isStar()
        );
        copy.setId(exercise.getId());
        return copy;
    }

    private void initImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (pendingImagePicked != null) {
                        pendingImagePicked.onPicked(uri != null ? uri.toString() : null);
                        pendingImagePicked = null;
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }

    private enum SortField {
        NAME,
        MUSCLE,
        CREATED,
        UPDATED
    }

    private enum SortOrder {
        ASC,
        DESC
    }

    private static class SortOption {
        final int labelRes;
        final SortField field;
        final SortOrder order;

        SortOption(int labelRes, SortField field, SortOrder order) {
            this.labelRes = labelRes;
            this.field = field;
            this.order = order;
        }
    }
}
