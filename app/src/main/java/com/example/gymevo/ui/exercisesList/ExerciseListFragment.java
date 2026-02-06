package com.example.gymevo.ui.exercisesList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.gymevo.data.repository.UserPreferencesRepository;
import com.example.gymevo.ui.common.ConfirmDeleteDialog;
import com.example.gymevo.ui.common.DragDropItemTouchHelper;
import com.example.gymevo.ui.common.ExerciseFormDialog;
import com.example.gymevo.ui.common.FilterUi;
import com.example.gymevo.ui.common.SectionHeaderDecoration;
import com.example.gymevo.ui.common.sort.SortBottomSheet;
import com.example.gymevo.ui.common.sort.SortField;
import com.example.gymevo.ui.common.sort.SortOrder;
import com.example.gymevo.ui.main.MainActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExerciseListFragment extends Fragment implements MainActivity.MainHeaderDelegate {

    public static final String ARG_OPEN_ADD_EXERCISE = "OPEN_ADD_EXERCISE";

    private FragmentExercisesListBinding binding;
    private ExerciseListAdapter adapter;
    private ExerciseListViewModel viewModel;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ExerciseFormDialog.OnImagePicked pendingImagePicked;
    private androidx.activity.OnBackPressedCallback backPressedCallback;
    private final List<Exercise> allExercises = new ArrayList<>();
    private String searchQuery = "";
    private String muscleFilter = null;
    private String typeFilter = null;
    private SortField sortField = SortField.RECENT;
    private SortOrder sortOrder = SortOrder.DESC;
    private boolean starPriorityEnabled;
    private List<Long> customOrderIds = new ArrayList<>();
    private SectionHeaderDecoration muscleHeaderDecoration;
    private UserPreferencesRepository userPreferencesRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImagePicker();
    }

    @Override
    public void onPause() {
        super.onPause();
        exitSelectionMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            requireActivity().invalidateOptionsMenu();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ExerciseListViewModel.class);
        userPreferencesRepository = UserPreferencesRepository.getInstance(requireContext());

        binding = FragmentExercisesListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        adapter = new ExerciseListAdapter(this::showEditDialog, viewModel::toggleExerciseStar);
        binding.recyclerExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerExercises.setAdapter(adapter);
        adapter.setRecyclerView(binding.recyclerExercises);

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

        loadExercisePreferences();

        observeFabAddRequests();
        setupBackNavigation();
        return root;
    }

    private boolean exitSelectionMode() {
        if (adapter != null && adapter.isSelectionModeActive()) {
            adapter.clearSelection();
            updateSelectionUi(0);
            maybePromptSaveCustomOrder();
            return true;
        }
        return false;
    }

    private void showExerciseFilterDialog() {
        if (!isAdded()) {
            return;
        }
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_filter_exercise, null, false);

        MaterialAutoCompleteTextView muscleInput = dialogView.findViewById(R.id.filter_exercise_muscle);
        MaterialAutoCompleteTextView typeInput = dialogView.findViewById(R.id.filter_exercise_type);
        String allLabel = getString(R.string.filter_all);

        final String[] selectedMuscle = {muscleFilter != null ? muscleFilter : allLabel};
        final String[] selectedType = {typeFilter != null ? typeFilter : allLabel};

        FilterUi.setupMuscleFilter(muscleInput, requireContext(), allLabel, (value, isAll) -> {
            selectedMuscle[0] = isAll ? allLabel : value;
        });
        FilterUi.setupExerciseTypeFilter(typeInput, requireContext(), allLabel, (value, isAll) -> {
            selectedType[0] = isAll ? allLabel : value;
        });

        if (muscleInput != null) {
            muscleInput.setText(muscleFilter != null ? muscleFilter : allLabel, false);
        }
        if (typeInput != null) {
            typeInput.setText(typeFilter != null ? typeFilter : allLabel, false);
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.filter_title)
                .setView(dialogView)
                .setPositiveButton(R.string.action_apply, (dialog, which) -> {
                    muscleFilter = FilterUi.isAllFilter(selectedMuscle[0], allLabel)
                            ? null : selectedMuscle[0];
                    typeFilter = FilterUi.isAllFilter(selectedType[0], allLabel)
                            ? null : selectedType[0];
                    applyExerciseFilters();
                    persistExercisePreferences();
                })
                .setNeutralButton(R.string.action_reset, (dialog, which) -> {
                    muscleFilter = null;
                    typeFilter = null;
                    applyExerciseFilters();
                    persistExercisePreferences();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showSortDialog() {
        if (!isAdded()) {
            return;
        }
        SortBottomSheet sheet = new SortBottomSheet();
        sheet.setOptions(buildSortGroups(),
                normalizeSortField(sortField),
                sortOrder,
                (field, order) -> {
                    sortField = normalizeSortField(field);
                    sortOrder = order;
                    applyExerciseFilters();
                    persistExercisePreferences();
                });
        sheet.show(getParentFragmentManager(), "SortBottomSheet");
    }

    private void toggleStarPriority() {
        starPriorityEnabled = !starPriorityEnabled;
        applyExerciseFilters();
        persistExercisePreferences();
    }

    private List<SortBottomSheet.SortGroup> buildSortGroups() {
        List<SortBottomSheet.SortGroup> groups = new ArrayList<>();
        List<SortBottomSheet.SortOption> smart = new ArrayList<>();
        smart.add(new SortBottomSheet.SortOption(R.string.sort_recent, SortField.RECENT, SortOrder.DESC));
        smart.add(new SortBottomSheet.SortOption(R.string.sort_popular, SortField.POPULAR, SortOrder.DESC));
        groups.add(new SortBottomSheet.SortGroup(R.string.sort_group_smart, smart));

        List<SortBottomSheet.SortOption> anatomical = new ArrayList<>();
        anatomical.add(new SortBottomSheet.SortOption(R.string.sort_muscle_asc, SortField.MUSCLE_GROUP, SortOrder.ASC));
        groups.add(new SortBottomSheet.SortGroup(R.string.sort_group_anatomical, anatomical));

        List<SortBottomSheet.SortOption> basic = new ArrayList<>();
        basic.add(new SortBottomSheet.SortOption(R.string.sort_name_asc, SortField.NAME, SortOrder.ASC));
        basic.add(new SortBottomSheet.SortOption(R.string.sort_name_desc, SortField.NAME, SortOrder.DESC));
        basic.add(new SortBottomSheet.SortOption(R.string.sort_custom, SortField.CUSTOM, SortOrder.ASC));
        groups.add(new SortBottomSheet.SortGroup(R.string.sort_group_basic, basic));

        return groups;
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
        updateExerciseHeaders();

        boolean isEmpty = filtered.isEmpty();
        binding.textExercisesEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerExercises.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void loadExercisePreferences() {
        if (userPreferencesRepository == null) {
            return;
        }
        disposables.add(userPreferencesRepository.getExerciseListPreferences()
                .subscribeOn(Schedulers.io())
                .subscribe(preferences -> {
                    if (!isAdded()) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> applyExercisePreferences(preferences));
                }, throwable -> {
                    // Ignore preference load errors
                }));
    }

    private void applyExercisePreferences(UserPreferencesRepository.ExerciseListPreferences preferences) {
        if (preferences == null) {
            return;
        }
        muscleFilter = preferences.muscleFilter != null && !preferences.muscleFilter.trim().isEmpty()
                ? preferences.muscleFilter
                : null;
        typeFilter = preferences.typeFilter != null && !preferences.typeFilter.trim().isEmpty()
            ? preferences.typeFilter
            : null;
        sortField = normalizeSortField(SortField.from(preferences.sortField, SortField.RECENT));
        sortOrder = SortOrder.from(preferences.sortOrder, SortOrder.DESC);
        starPriorityEnabled = preferences.starPriorityEnabled;
        customOrderIds = parseIdList(preferences.customOrder);
        applyExerciseFilters();
        requireActivity().invalidateOptionsMenu();
    }

    private void persistExercisePreferences() {
        if (userPreferencesRepository == null) {
            return;
        }
        String filterValue = muscleFilter != null ? muscleFilter : "";
        String typeValue = typeFilter != null ? typeFilter : "";
        UserPreferencesRepository.ExerciseListPreferences preferences =
                new UserPreferencesRepository.ExerciseListPreferences(
                filterValue,
                typeValue,
                        sortField.name(),
                        sortOrder.name(),
                starPriorityEnabled,
                encodeIdList(customOrderIds)
                );
        disposables.add(userPreferencesRepository.setExerciseListPreferences(preferences)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, throwable -> {
                }));
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
        if (typeFilter != null) {
            String typeLabel = FilterUi.normalize(exercise.getTypeLabel());
            if (!typeLabel.equals(FilterUi.normalize(typeFilter))) {
                return false;
            }
        }
        if (query.isEmpty()) {
            return true;
        }
        String name = FilterUi.normalize(exercise.getName());
        String muscleLabel = FilterUi.normalize(exercise.getTargetedMusclesLabel());
        String typeLabel = FilterUi.normalize(exercise.getTypeLabel());
        return name.contains(query) || muscleLabel.contains(query) || typeLabel.contains(query);
    }

    private Comparator<Exercise> buildExerciseComparator() {
        Comparator<Exercise> base;
        switch (sortField) {
            case RECENT:
                base = Comparator.comparingLong(Exercise::getUpdatedAt)
                        .thenComparingLong(Exercise::getCreatedAt)
                        .reversed();
                break;
            case POPULAR:
                base = Comparator.comparing(Exercise::isStar).reversed()
                        .thenComparingLong(Exercise::getUpdatedAt).reversed()
                        .thenComparing(exercise -> FilterUi.normalize(exercise.getName()));
                break;
            case CUSTOM:
                base = buildCustomExerciseComparator();
                break;
            case MUSCLE:
            case MUSCLE_GROUP:
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

        if (sortField != SortField.RECENT
                && sortField != SortField.POPULAR
                && sortField != SortField.CUSTOM
                && sortOrder == SortOrder.DESC) {
            base = base.reversed();
        }
        if (starPriorityEnabled && sortField != SortField.CUSTOM) {
            base = Comparator.comparing(Exercise::isStar).reversed().thenComparing(base);
        }
        return base;
    }

    private Comparator<Exercise> buildCustomExerciseComparator() {
        java.util.Map<Long, Integer> orderMap = buildOrderMap(customOrderIds);
        return (a, b) -> {
            // First priority: star status (starred items first)
            if (starPriorityEnabled) {
                boolean starA = a != null && a.isStar();
                boolean starB = b != null && b.isStar();
                if (starA != starB) {
                    return starA ? -1 : 1; // starred first
                }
            }
            // Second priority: custom order
            int indexA = orderMap.getOrDefault(a != null ? a.getId() : null, Integer.MAX_VALUE);
            int indexB = orderMap.getOrDefault(b != null ? b.getId() : null, Integer.MAX_VALUE);
            if (indexA != indexB) {
                return Integer.compare(indexA, indexB);
            }
            // Third priority: name as tiebreaker
            String nameA = a != null ? FilterUi.normalize(a.getName()) : "";
            String nameB = b != null ? FilterUi.normalize(b.getName()) : "";
            return nameA.compareTo(nameB);
        };
    }

    private void updateExerciseHeaders() {
        boolean shouldShow = sortField == SortField.MUSCLE_GROUP || sortField == SortField.MUSCLE;
        if (!shouldShow) {
            if (muscleHeaderDecoration != null && binding != null) {
                binding.recyclerExercises.removeItemDecoration(muscleHeaderDecoration);
                muscleHeaderDecoration = null;
            }
            return;
        }
        if (binding == null || adapter == null) {
            return;
        }
        if (muscleHeaderDecoration == null) {
            muscleHeaderDecoration = new SectionHeaderDecoration(requireContext(), position -> {
                Exercise item = adapter.getItemAt(position);
                if (item == null) {
                    return null;
                }
                String label = item.getTargetedMusclesLabel();
                if (label == null || label.trim().isEmpty()) {
                    return getString(R.string.stats_unknown_muscle);
                }
                return label;
            });
            binding.recyclerExercises.addItemDecoration(muscleHeaderDecoration);
        }
    }

    private SortField normalizeSortField(SortField field) {
        if (field == SortField.MUSCLE) {
            return SortField.MUSCLE_GROUP;
        }
        return field != null ? field : SortField.RECENT;
    }

    private java.util.List<Long> parseIdList(String value) {
        java.util.List<Long> ids = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return ids;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            try {
                ids.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    private String encodeIdList(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            if (id == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(id);
        }
        return builder.toString();
    }

    private java.util.Map<Long, Integer> buildOrderMap(java.util.List<Long> ids) {
        java.util.Map<Long, Integer> map = new java.util.HashMap<>();
        if (ids == null) {
            return map;
        }
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            if (id != null && !map.containsKey(id)) {
                map.put(id, i);
            }
        }
        return map;
    }

    private void setupBackNavigation() {
        if (!isAdded()) {
            return;
        }
        backPressedCallback = new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (exitSelectionMode()) {
                    return;
                }
                setEnabled(false);
                try {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                } finally {
                    setEnabled(true);
                }
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
        boolean selectionMode = adapter != null && adapter.isSelectionModeActive();
        boolean show = selectedCount > 0 || selectionMode;
        MainActivity activity = (MainActivity) requireActivity();
        if (show) {
            activity.showSelectionBar(
                    selectedCount,
                    v -> onMoveSelectedExercisesTop(),
                    v -> onMoveSelectedExercisesBottom(),
                    v -> onDeleteSelectedExercises()
            );
        } else {
            activity.hideSelectionBar();
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
                }
        );
    }

    private void onMoveSelectedExercisesTop() {
        if (adapter == null) {
            return;
        }
        adapter.moveSelectedToTop();
    }

    private void onMoveSelectedExercisesBottom() {
        if (adapter == null) {
            return;
        }
        adapter.moveSelectedToBottom();
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

    private void maybePromptSaveCustomOrder() {
        if (!isAdded() || adapter == null) {
            return;
        }
        if (!adapter.consumeOrderChangedInSelection()) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sort_custom_save_title)
                .setMessage(R.string.sort_custom_save_message)
                .setPositiveButton(R.string.action_save, (dialog, which) -> saveCustomOrder())
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> applyExerciseFilters())
                .show();
    }

    private void saveCustomOrder() {
        if (adapter == null) {
            return;
        }
        customOrderIds = extractExerciseIds(adapter.getItems());
        sortField = SortField.CUSTOM;
        sortOrder = SortOrder.ASC;
        persistExercisePreferences();
        applyExerciseFilters();
    }

    private List<Long> extractExerciseIds(List<Exercise> items) {
        List<Long> ids = new ArrayList<>();
        if (items == null) {
            return ids;
        }
        for (Exercise exercise : items) {
            if (exercise != null && exercise.getId() != null) {
                ids.add(exercise.getId());
            }
        }
        return ids;
    }

    @Override
    public void onSearchQueryChanged(String query) {
        searchQuery = query != null ? query : "";
        applyExerciseFilters();
    }

    @Override
    public void onSearchClosed() {
        searchQuery = "";
        applyExerciseFilters();
    }

    @Override
    public void onFilterRequested() {
        showExerciseFilterDialog();
    }

    @Override
    public void onSortRequested() {
        showSortDialog();
    }

    @Override
    public void onStarToggleRequested() {
        toggleStarPriority();
    }

    @Override
    public boolean isStarPriorityEnabled() {
        return starPriorityEnabled;
    }

    @Override
    public String getSearchQuery() {
        return searchQuery;
    }

    @Override
    public int getSearchHintResId() {
        return R.string.exercise_search_hint;
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
        copy.setType(exercise.getType());
        copy.setShowSeries(exercise.isShowSeries());
        copy.setShowRepetitions(exercise.isShowRepetitions());
        copy.setShowWeight(exercise.isShowWeight());
        copy.setShowTime(exercise.isShowTime());
        copy.setShowHeartRate(exercise.isShowHeartRate());
        copy.setCreatedAt(exercise.getCreatedAt());
        copy.setUpdatedAt(exercise.getUpdatedAt());
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
        disposables.clear();
        binding = null;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }

}
