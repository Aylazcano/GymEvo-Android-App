package com.example.gymevo.ui.workoutsList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.R;
import com.example.gymevo.ui.common.adapter.WorkoutExerciseAdapter;
import com.example.gymevo.databinding.FragmentWorkoutsListBinding;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.ExerciseType;
import com.example.gymevo.model.Workout;
import com.example.gymevo.ui.common.ConfirmDeleteDialog;
import com.example.gymevo.ui.common.DragDropItemTouchHelper;
import com.example.gymevo.ui.common.ExerciseEditDialog;
import com.example.gymevo.ui.common.FilterUi;
import com.example.gymevo.ui.common.SectionHeaderDecoration;
import com.example.gymevo.data.repository.UserPreferencesRepository;
import com.example.gymevo.ui.common.sort.SortBottomSheet;
import com.example.gymevo.ui.common.sort.SortField;
import com.example.gymevo.ui.common.sort.SortOrder;
import com.example.gymevo.ui.main.MainActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class WorkoutListFragment extends Fragment implements MainActivity.MainHeaderDelegate,
    MainActivity.BackPressDelegate {

    public static final String ARG_OPEN_ADD_WORKOUT = "OPEN_ADD_WORKOUT";

    private WorkoutListViewModel workoutsListViewModel;
    private FragmentWorkoutsListBinding binding;
    private RecyclerView workoutsRecyclerView;
    private TextView emptyView;
    private WorkoutListAdapter workoutsAdapter;
    private Workout pendingNewWorkout;
    private Workout expandedWorkoutRef;
    private Workout expandedWorkoutCopy;
    private List<ExerciseInWorkout> expandedExercises = new ArrayList<>();
    private boolean expandedIsNew;
    private androidx.activity.OnBackPressedCallback backPressedCallback;
    private SelectionContext selectionContext = SelectionContext.NONE;
    private final List<Workout> allWorkouts = new ArrayList<>();
    private String searchQuery = "";
    private String muscleFilter = null;
    private SortField sortField = SortField.RECENT;
    private SortOrder sortOrder = SortOrder.DESC;
    private boolean starPriorityEnabled;
    private List<Long> customOrderIds = new ArrayList<>();
    private SectionHeaderDecoration muscleHeaderDecoration;
    private List<Workout> currentFilteredList = new ArrayList<>();
    private UserPreferencesRepository userPreferencesRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private enum SelectionContext {
        NONE,
        WORKOUTS,
        NESTED_EXERCISES
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutsListBinding.inflate(inflater, container, false);
        workoutsListViewModel = new ViewModelProvider(this).get(WorkoutListViewModel.class);
        userPreferencesRepository = UserPreferencesRepository.getInstance(requireContext());

        setupUI();
        setupBackNavigation();
        observeAddWorkoutRequests();
        loadWorkoutPreferences();
        return binding.getRoot();
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

    private void setupBackNavigation() {
        if (!isAdded()) {
            return;
        }
        backPressedCallback = new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (handleBackPressed()) {
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

    private boolean handleBackPressed() {
        if (exitSelectionMode()) {
            return true;
        }
        if (expandedWorkoutRef != null) {
            saveAndCollapseExpandedWorkout();
            return true;
        }
        return false;
    }

    private boolean exitSelectionMode() {
        boolean closed = false;
        if (workoutsAdapter != null && workoutsAdapter.isSelectionModeActive()) {
            workoutsAdapter.clearSelection();
            updateSelectionBar(0, false, SelectionContext.WORKOUTS);
            maybePromptSaveCustomOrder();
            closed = true;
        }
        WorkoutExerciseAdapter adapter = workoutsAdapter != null
                ? workoutsAdapter.getExpandedExerciseAdapter()
                : null;
        if (adapter != null && adapter.isSelectionModeActive()) {
            adapter.clearSelection();
            updateSelectionBar(0, false, SelectionContext.NESTED_EXERCISES);
            closed = true;
        }
        return closed;
    }

    private void onMoveSelected() {
        if (selectionContext == SelectionContext.NESTED_EXERCISES) {
            onMoveSelectedNestedExercisesTop();
        } else {
            onMoveSelectedWorkoutsTop();
        }
    }

    private void onMoveSelectedBottom() {
        if (selectionContext == SelectionContext.NESTED_EXERCISES) {
            onMoveSelectedNestedExercisesBottom();
        } else {
            onMoveSelectedWorkoutsBottom();
        }
    }

    private void onDeleteSelected() {
        if (selectionContext == SelectionContext.NESTED_EXERCISES) {
            onDeleteSelectedNestedExercises();
        } else {
            onDeleteSelectedWorkouts();
        }
    }

    private void setupUI() {
        Context context = getContext();
        if (context == null) return;

        workoutsRecyclerView = binding.recyclerViewExercises;
        emptyView = binding.textWorkoutsEmpty;

        initWorkoutsRecyclerView();

        binding.fabAddWorkout.setOnClickListener(v -> onAddWorkout());
    }

    private void observeAddWorkoutRequests() {
        if (!isAdded()) {
            return;
        }
        NavController navController = NavHostFragment.findNavController(this);
        NavBackStackEntry entry = navController.getCurrentBackStackEntry();
        if (entry == null) {
            return;
        }
        SavedStateHandle handle = entry.getSavedStateHandle();
        handle.getLiveData(ARG_OPEN_ADD_WORKOUT, false).observe(getViewLifecycleOwner(), value -> {
            if (Boolean.TRUE.equals(value)) {
                handle.set(ARG_OPEN_ADD_WORKOUT, false);
                onAddWorkout();
            }
        });
    }

    private void onAddWorkout() {
        Workout newWorkout = workoutsListViewModel.createNewTemplateWorkout();
        pendingNewWorkout = newWorkout;
        expandWorkout(newWorkout, true);
        refreshAdapterDisplay();
        scrollToNewWorkout();
    }

    private void scrollToNewWorkout() {
        if (workoutsRecyclerView == null) {
            return;
        }
        workoutsRecyclerView.post(() -> workoutsRecyclerView.scrollToPosition(0));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }

    private void initWorkoutsRecyclerView() {
        Context context = getContext();
        if (context == null) return;

        workoutsAdapter = new WorkoutListAdapter(this::toggleWorkoutStar, new WorkoutListAdapter.WorkoutActionListener() {
            @Override
            public void onToggleExpanded(Workout workout, int position) {
                openWorkoutExercisesDialog(workout, position);
            }

            @Override
            public void onSaveWorkout(Workout workout, String name, List<ExerciseInWorkout> exercises) {
                onSaveExpandedWorkout(workout, name, exercises);
            }

            @Override
            public void onDeleteWorkout(Workout workout) {
                onDeleteExpandedWorkout(workout);
            }

            @Override
            public void onAddExercise(List<ExerciseInWorkout> exercises, WorkoutExerciseAdapter adapter) {
                showAddExerciseDialog(exercises, adapter);
                workoutsAdapter.setExpandedState(expandedWorkoutRef, expandedWorkoutCopy,
                        expandedExercises, expandedIsNew);
            }

            @Override
            public void onEditExercise(ExerciseInWorkout exercise,
                                       List<ExerciseInWorkout> exercises,
                                       WorkoutExerciseAdapter adapter) {
                showEditExerciseDialog(exercise, exercises, adapter);
                workoutsAdapter.setExpandedState(expandedWorkoutRef, expandedWorkoutCopy,
                        expandedExercises, expandedIsNew);
            }
        });
        workoutsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        workoutsRecyclerView.setAdapter(workoutsAdapter);
        workoutsAdapter.setRecyclerView(workoutsRecyclerView);

        workoutsAdapter.setSelectionListener(this::updateWorkoutSelectionUi);
        workoutsAdapter.setNestedExerciseSelectionListener(this::updateNestedExerciseSelectionUi);
        workoutsAdapter.setNestedExerciseOrderListener(this::persistExpandedExerciseOrder);
        workoutsAdapter.setDeleteListener(this::onDeleteSingleWorkout);
        workoutsAdapter.setNestedExerciseDeleteListener(this::onDeleteSingleNestedExercise);

        DragDropItemTouchHelper callback = new DragDropItemTouchHelper(workoutsAdapter::onItemMove, null);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(workoutsRecyclerView);
        workoutsAdapter.setItemTouchHelper(helper);

        workoutsListViewModel.getWorkoutsLiveData().observe(getViewLifecycleOwner(), workouts -> {
            allWorkouts.clear();
            if (workouts != null) {
                allWorkouts.addAll(workouts);
            }
            applyWorkoutFilters();
        });
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
                    applyWorkoutFilters();
                    persistWorkoutPreferences();
                    dialog.dismiss();
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
                    applyWorkoutFilters();
                    persistWorkoutPreferences();
                });
        sheet.show(getParentFragmentManager(), "SortBottomSheet");
    }

    private void toggleStarPriority() {
        starPriorityEnabled = !starPriorityEnabled;
        applyWorkoutFilters();
        persistWorkoutPreferences();
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

    private void applyWorkoutFilters() {
        List<Workout> filtered = new ArrayList<>();
        String query = FilterUi.normalize(searchQuery);
        for (Workout workout : allWorkouts) {
            if (workout == null) {
                continue;
            }
            if (!matchesWorkout(workout, query)) {
                continue;
            }
            filtered.add(workout);
        }

        Comparator<Workout> comparator = buildWorkoutComparator();
        filtered.sort(comparator);
        refreshWorkoutsList(filtered);
        updateWorkoutHeaders();
    }

    private void loadWorkoutPreferences() {
        if (userPreferencesRepository == null) {
            return;
        }
        disposables.add(userPreferencesRepository.getWorkoutListPreferences()
                .subscribeOn(Schedulers.io())
                .subscribe(preferences -> {
                    if (!isAdded()) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> applyWorkoutPreferences(preferences));
                }, throwable -> {
                    // Ignore preference load errors
                }));
    }

    private void applyWorkoutPreferences(UserPreferencesRepository.WorkoutListPreferences preferences) {
        if (preferences == null) {
            return;
        }
        muscleFilter = preferences.muscleFilter != null && !preferences.muscleFilter.trim().isEmpty()
                ? preferences.muscleFilter
                : null;
        sortField = normalizeSortField(SortField.from(preferences.sortField, SortField.RECENT));
        sortOrder = SortOrder.from(preferences.sortOrder, SortOrder.DESC);
        starPriorityEnabled = preferences.starPriorityEnabled;
        customOrderIds = parseIdList(preferences.customOrder);
        applyWorkoutFilters();
        requireActivity().invalidateOptionsMenu();
    }

    private void persistWorkoutPreferences() {
        if (userPreferencesRepository == null) {
            return;
        }
        String filterValue = muscleFilter != null ? muscleFilter : "";
        UserPreferencesRepository.WorkoutListPreferences preferences =
                new UserPreferencesRepository.WorkoutListPreferences(
                        filterValue,
                        sortField.name(),
                        sortOrder.name(),
                starPriorityEnabled,
                encodeIdList(customOrderIds)
                );
        disposables.add(userPreferencesRepository.setWorkoutListPreferences(preferences)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, throwable -> {
                }));
    }

    private boolean matchesWorkout(Workout workout, String query) {
        if (workout == null) {
            return false;
        }
        if (muscleFilter != null && !workoutContainsMuscle(workout, muscleFilter)) {
            return false;
        }
        if (query.isEmpty()) {
            return true;
        }
        String name = FilterUi.normalize(workout.getName());
        if (name.contains(query)) {
            return true;
        }
        return workoutContainsQuery(workout, query);
    }

    private boolean workoutContainsMuscle(Workout workout, String muscleLabel) {
        String target = FilterUi.normalize(muscleLabel);
        if (target.isEmpty()) {
            return true;
        }
        List<ExerciseInWorkout> exercises = workout.getExercises();
        if (exercises == null) {
            return false;
        }
        for (ExerciseInWorkout exercise : exercises) {
            if (exercise == null) {
                continue;
            }
            String label = FilterUi.normalize(exercise.getTargetedMusclesLabel());
            if (label.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean workoutContainsQuery(Workout workout, String query) {
        List<ExerciseInWorkout> exercises = workout.getExercises();
        if (exercises == null) {
            return false;
        }
        for (ExerciseInWorkout exercise : exercises) {
            if (exercise == null) {
                continue;
            }
            String name = FilterUi.normalize(exercise.getName());
            String muscle = FilterUi.normalize(exercise.getTargetedMusclesLabel());
            if (name.contains(query) || muscle.contains(query)) {
                return true;
            }
        }
        return false;
    }

    private Comparator<Workout> buildWorkoutComparator() {
        Comparator<Workout> base;
        switch (sortField) {
            case RECENT:
                base = Comparator.comparingLong(Workout::getUpdatedAt)
                        .thenComparingLong(Workout::getCreatedAt)
                        .reversed();
                break;
            case POPULAR:
                base = Comparator.comparingInt(this::workoutExerciseCount).reversed()
                        .thenComparingLong(Workout::getUpdatedAt).reversed()
                        .thenComparing(workout -> FilterUi.normalize(workout.getName()));
                break;
            case CUSTOM:
                base = buildCustomWorkoutComparator();
                break;
            case MUSCLE:
            case MUSCLE_GROUP:
                base = Comparator.comparing(
                    (Workout workout) -> FilterUi.normalize(firstWorkoutMuscle(workout))
                ).thenComparing(workout -> FilterUi.normalize(workout.getName()));
                break;
            case CREATED:
                base = Comparator.comparingLong(Workout::getCreatedAt);
                break;
            case UPDATED:
                base = Comparator.comparingLong(Workout::getUpdatedAt);
                break;
            case NAME:
            default:
                base = Comparator.comparing(workout -> FilterUi.normalize(workout.getName()));
                break;
        }

        if (sortField != SortField.RECENT
                && sortField != SortField.POPULAR
                && sortField != SortField.CUSTOM
                && sortOrder == SortOrder.DESC) {
            base = base.reversed();
        }
        if (starPriorityEnabled && sortField != SortField.CUSTOM) {
            base = Comparator.comparing(Workout::isStar).reversed().thenComparing(base);
        }
        return base;
    }

    private Comparator<Workout> buildCustomWorkoutComparator() {
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

    private void updateWorkoutHeaders() {
        boolean shouldShow = sortField == SortField.MUSCLE_GROUP || sortField == SortField.MUSCLE;
        if (!shouldShow) {
            if (muscleHeaderDecoration != null && workoutsRecyclerView != null) {
                workoutsRecyclerView.removeItemDecoration(muscleHeaderDecoration);
                muscleHeaderDecoration = null;
            }
            return;
        }
        if (workoutsRecyclerView == null || workoutsAdapter == null) {
            return;
        }
        if (muscleHeaderDecoration == null) {
            muscleHeaderDecoration = new SectionHeaderDecoration(requireContext(), position -> {
                Workout item = workoutsAdapter.getItemAt(position);
                if (item == null) {
                    return null;
                }
                String label = firstWorkoutMuscle(item);
                if (label == null || label.trim().isEmpty()) {
                    return getString(R.string.stats_unknown_muscle);
                }
                return label;
            });
            workoutsRecyclerView.addItemDecoration(muscleHeaderDecoration);
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

    private int workoutExerciseCount(Workout workout) {
        if (workout == null || workout.getExercises() == null) {
            return 0;
        }
        return workout.getExercises().size();
    }

    private String firstWorkoutMuscle(Workout workout) {
        List<ExerciseInWorkout> exercises = workout.getExercises();
        if (exercises == null || exercises.isEmpty()) {
            return "";
        }
        for (ExerciseInWorkout exercise : exercises) {
            if (exercise != null && exercise.getTargetedMusclesLabel() != null) {
                return exercise.getTargetedMusclesLabel();
            }
        }
        return "";
    }

    private void updateWorkoutSelectionUi(int selectedCount) {
        if (!isAdded()) {
            return;
        }
        boolean selectionMode = workoutsAdapter != null && workoutsAdapter.isSelectionModeActive();
        updateSelectionBar(selectedCount, selectionMode, SelectionContext.WORKOUTS);
    }

    private void updateNestedExerciseSelectionUi(int selectedCount) {
        if (!isAdded()) {
            return;
        }
        WorkoutExerciseAdapter adapter = workoutsAdapter != null
                ? workoutsAdapter.getExpandedExerciseAdapter()
                : null;
        boolean selectionMode = adapter != null && adapter.isSelectionModeActive();
        updateSelectionBar(selectedCount, selectionMode, SelectionContext.NESTED_EXERCISES);
    }

    private void updateSelectionBar(int selectedCount, boolean selectionModeActive, SelectionContext context) {
        MainActivity activity = (MainActivity) requireActivity();
        boolean show = selectedCount > 0 || selectionModeActive;
        if (show) {
            selectionContext = context;
            activity.showSelectionBar(
                    selectedCount,
                    v -> onMoveSelected(),
                    v -> onMoveSelectedBottom(),
                    v -> onDeleteSelected()
            );
        } else if (selectionContext == context || context == SelectionContext.NONE) {
            selectionContext = SelectionContext.NONE;
            activity.hideSelectionBar();
        }
    }

    private void onDeleteSelectedWorkouts() {
        if (workoutsAdapter == null) {
            return;
        }
        ConfirmDeleteDialog.show(
                requireContext(),
                R.string.workout_delete_title,
                R.string.workout_delete_confirm,
                () -> {
                    for (Workout workout : workoutsAdapter.getSelectedItems()) {
                        if (removePendingWorkoutIfNeeded(workout)) {
                            continue;
                        }
                        workoutsListViewModel.deleteWorkout(workout);
                    }
                }
        );
    }

    private void maybePromptSaveCustomOrder() {
        if (!isAdded() || workoutsAdapter == null) {
            return;
        }
        if (!workoutsAdapter.consumeOrderChangedInSelection()) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sort_custom_save_title)
                .setMessage(R.string.sort_custom_save_message)
                .setPositiveButton(R.string.action_save, (dialog, which) -> saveCustomOrder())
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> applyWorkoutFilters())
                .show();
    }

    private void saveCustomOrder() {
        if (workoutsAdapter == null) {
            return;
        }
        customOrderIds = extractWorkoutIds(workoutsAdapter.getItems());
        sortField = SortField.CUSTOM;
        sortOrder = SortOrder.ASC;
        persistWorkoutPreferences();
        applyWorkoutFilters();
    }

    private List<Long> extractWorkoutIds(List<Workout> items) {
        List<Long> ids = new ArrayList<>();
        if (items == null) {
            return ids;
        }
        for (Workout workout : items) {
            if (workout != null && workout.getId() != null) {
                ids.add(workout.getId());
            }
        }
        return ids;
    }

    private void onMoveSelectedWorkoutsTop() {
        if (workoutsAdapter == null) {
            return;
        }
        workoutsAdapter.moveSelectedToTop();
    }

    private void onMoveSelectedWorkoutsBottom() {
        if (workoutsAdapter == null) {
            return;
        }
        workoutsAdapter.moveSelectedToBottom();
    }

    private void onDeleteSelectedNestedExercises() {
        WorkoutExerciseAdapter adapter = workoutsAdapter != null ? workoutsAdapter.getExpandedExerciseAdapter() : null;
        if (adapter == null) {
            return;
        }
        ConfirmDeleteDialog.show(
                requireContext(),
                R.string.exercise_delete_title,
                R.string.exercise_delete_confirm,
                () -> {
                    List<ExerciseInWorkout> selected = adapter.getSelectedItems();
                    expandedExercises.removeAll(selected);
                    adapter.setExercises(new ArrayList<>(expandedExercises));
                }
        );
    }

    private void onMoveSelectedNestedExercisesTop() {
        WorkoutExerciseAdapter adapter = workoutsAdapter != null ? workoutsAdapter.getExpandedExerciseAdapter() : null;
        if (adapter == null) {
            return;
        }
        adapter.moveSelectedToTop();
        expandedExercises = adapter.getItems();
    }

    private void onMoveSelectedNestedExercisesBottom() {
        WorkoutExerciseAdapter adapter = workoutsAdapter != null ? workoutsAdapter.getExpandedExerciseAdapter() : null;
        if (adapter == null) {
            return;
        }
        adapter.moveSelectedToBottom();
        expandedExercises = adapter.getItems();
    }

    private void onDeleteSingleWorkout(Workout workout) {
        if (workout == null) {
            return;
        }
        if (removePendingWorkoutIfNeeded(workout)) {
            return;
        }
        ConfirmDeleteDialog.show(
                requireContext(),
                R.string.workout_delete_title,
                R.string.workout_delete_confirm,
                () -> {
                    workoutsListViewModel.deleteWorkout(workout);
                }
        );
    }

    private boolean removePendingWorkoutIfNeeded(Workout workout) {
        if (workout == null || workout.getId() != null) {
            return false;
        }
        if (pendingNewWorkout == workout) {
            if (isExpandedWorkout(workout)) {
                collapseExpandedWorkout();
            } else {
                pendingNewWorkout = null;
                refreshAdapterDisplay();
            }
            return true;
        }
        return false;
    }

    private void onDeleteSingleNestedExercise(ExerciseInWorkout exercise) {
        if (exercise == null) {
            return;
        }
        ConfirmDeleteDialog.show(
                requireContext(),
                R.string.exercise_delete_title,
                R.string.exercise_delete_confirm,
                () -> {
                    expandedExercises.remove(exercise);
                    WorkoutExerciseAdapter adapter = workoutsAdapter != null
                            ? workoutsAdapter.getExpandedExerciseAdapter()
                            : null;
                    if (adapter != null) {
                        adapter.setExercises(new ArrayList<>(expandedExercises));
                    }
                }
        );
    }

    private void persistExpandedExerciseOrder(List<ExerciseInWorkout> newOrder) {
        expandedExercises = newOrder != null ? new ArrayList<>(newOrder) : new ArrayList<>();
    }

    private void refreshWorkoutsList(List<Workout> workouts) {
        if (workoutsAdapter == null) {
            return;
        }
        currentFilteredList = workouts != null ? new ArrayList<>(workouts) : new ArrayList<>();
        refreshAdapterDisplay();
    }

    private void refreshAdapterDisplay() {
        if (workoutsAdapter == null) {
            return;
        }
        // Set expanded state BEFORE setWorkouts so DiffUtil rebind sees correct state
        if (expandedWorkoutRef != null) {
            workoutsAdapter.setExpandedState(expandedWorkoutRef, expandedWorkoutCopy,
                    expandedExercises, expandedIsNew);
        } else {
            workoutsAdapter.clearExpandedState();
        }
        List<Workout> display = new ArrayList<>(currentFilteredList);
        if (pendingNewWorkout != null && containsWorkout(display, pendingNewWorkout)) {
            pendingNewWorkout = null;
        }
        if (pendingNewWorkout != null) {
            display.add(0, pendingNewWorkout);
        }
        workoutsAdapter.setWorkouts(display);
        if (emptyView != null) {
            boolean isEmpty = currentFilteredList.isEmpty() && pendingNewWorkout == null;
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private boolean containsWorkout(List<Workout> workouts, Workout target) {
        if (workouts == null || workouts.isEmpty() || target == null) {
            return false;
        }
        for (Workout workout : workouts) {
            if (isSameWorkout(workout, target)) {
                return true;
            }
        }
        return false;
    }

    private void openWorkoutExercisesDialog(Workout workout) {
        openWorkoutExercisesDialog(workout, RecyclerView.NO_POSITION);
    }

    private void openWorkoutExercisesDialog(Workout workout, int position) {
        if (!isAdded() || workout == null) {
            return;
        }
        if (isExpandedWorkout(workout)) {
            saveAndCollapseExpandedWorkout();
            return;
        }
        if (expandedWorkoutRef != null) {
            saveAndCollapseExpandedWorkout();
        }
        if (pendingNewWorkout != null && !isSameWorkout(pendingNewWorkout, workout)) {
            pendingNewWorkout = null;
        }
        boolean isNew = pendingNewWorkout != null && isSameWorkout(pendingNewWorkout, workout);
        expandWorkout(workout, isNew);
        requestScrollToPositionIfNeeded(position);
    }

    private void saveAndCollapseExpandedWorkout() {
        if (expandedWorkoutRef == null || expandedWorkoutCopy == null) {
            collapseExpandedWorkout(false);
            return;
        }
        String name = expandedWorkoutCopy.getName() != null
                ? expandedWorkoutCopy.getName()
                : expandedWorkoutRef.getName();
        boolean isNew = expandedWorkoutRef.getId() == null;
        boolean hasExercises = expandedExercises != null && !expandedExercises.isEmpty();
        boolean hasName = name != null && !name.trim().isEmpty();
        if (isNew && !hasName && !hasExercises) {
            collapseExpandedWorkout(true);
            return;
        }
        onSaveExpandedWorkout(expandedWorkoutRef, name, expandedExercises);
    }

    private void expandWorkout(Workout workout, boolean isNew) {
        if (!isAdded() || workout == null) {
            return;
        }
        expandedWorkoutRef = workout;
        expandedWorkoutCopy = copyWorkout(workout);
        expandedExercises = cloneExercises(workout.getExercises());
        expandedIsNew = isNew;
        workoutsAdapter.setExpandedState(expandedWorkoutRef, expandedWorkoutCopy,
                expandedExercises, expandedIsNew);
        workoutsAdapter.notifyDataSetChanged();
        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(true);
        }
    }

    private void requestScrollToPositionIfNeeded(int position) {
        if (workoutsRecyclerView == null || position == RecyclerView.NO_POSITION) {
            return;
        }
        workoutsRecyclerView.post(() -> {
            RecyclerView.ViewHolder holder = workoutsRecyclerView.findViewHolderForAdapterPosition(position);
            if (holder == null) {
                workoutsRecyclerView.scrollToPosition(position);
                workoutsRecyclerView.post(() -> scrollExpandedIfTall(position));
                return;
            }
            scrollExpandedIfTall(position);
        });
    }

    private void scrollExpandedIfTall(int position) {
        if (workoutsRecyclerView == null) {
            return;
        }
        RecyclerView.ViewHolder holder = workoutsRecyclerView.findViewHolderForAdapterPosition(position);
        if (holder == null) {
            return;
        }
        View expandedLayout = holder.itemView.findViewById(R.id.layout_workout_expand);
        if (expandedLayout == null) {
            return;
        }
        int expandedHeight = expandedLayout.getHeight();
        int listHeight = workoutsRecyclerView.getHeight();
        if (expandedHeight >= listHeight) {
            RecyclerView.LayoutManager layoutManager = workoutsRecyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(position, 0);
            } else {
                workoutsRecyclerView.scrollToPosition(position);
            }
        }
    }

    private void collapseExpandedWorkout() {
        collapseExpandedWorkout(false);
    }

    private void collapseExpandedWorkout(boolean keepPending) {
        expandedWorkoutRef = null;
        expandedWorkoutCopy = null;
        expandedExercises = new ArrayList<>();
        expandedIsNew = false;
        if (!keepPending) {
            pendingNewWorkout = null;
        }
        workoutsAdapter.clearExpandedState();
        workoutsAdapter.notifyDataSetChanged();
        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(false);
        }
    }

    private boolean isExpandedWorkout(Workout workout) {
        return isSameWorkout(workout, expandedWorkoutRef);
    }

    private boolean isSameWorkout(Workout left, Workout right) {
        if (left == null || right == null) {
            return false;
        }
        Long leftId = left.getId();
        Long rightId = right.getId();
        if (leftId != null && rightId != null) {
            return leftId.equals(rightId);
        }
        return left == right;
    }

    void onSaveExpandedWorkout(Workout workout, String name,
                               List<ExerciseInWorkout> exercises) {
        if (workout == null || expandedWorkoutCopy == null) {
            return;
        }
        String safeName = name != null ? name.trim() : (workout.getName() != null ? workout.getName() : "");
        boolean isNew = workout.getId() == null;
        if (!isNew && !hasWorkoutChanges(workout, safeName, exercises)) {
            collapseExpandedWorkout(false);
            return;
        }
        if (isNew && safeName.isEmpty() && (exercises == null || exercises.isEmpty())) {
            collapseExpandedWorkout(true);
            return;
        }
        expandedWorkoutCopy.setName(safeName);
        expandedWorkoutCopy.setExercises(new ArrayList<>(exercises));
        expandedWorkoutCopy.setStar(workout.isStar());
        if (expandedWorkoutCopy.getCreatedAt() <= 0L) {
            expandedWorkoutCopy.setCreatedAt(System.currentTimeMillis());
        }
        workoutsListViewModel.setCurrentWorkoutWithExercises(expandedWorkoutCopy);
        workoutsListViewModel.updateCurrentWorkoutMeta(safeName);
        workoutsListViewModel.saveWorkout();
        collapseExpandedWorkout(false);
    }

    void onDeleteExpandedWorkout(Workout workout) {
        if (workout == null) {
            return;
        }
        if (workout.getId() == null) {
            collapseExpandedWorkout(false);
            return;
        }
        ConfirmDeleteDialog.show(
                requireContext(),
                R.string.workout_delete_title,
                R.string.workout_delete_confirm,
                () -> {
                    workoutsListViewModel.deleteWorkout(workout);
                    collapseExpandedWorkout();
                }
        );
    }

    private void showEditExerciseDialog(ExerciseInWorkout exercise,
                                        List<ExerciseInWorkout> workingExercises,
                                        WorkoutExerciseAdapter adapter) {
        if (exercise == null || workingExercises == null || adapter == null || !isAdded()) {
            return;
        }
        hideKeyboard();
        ExerciseEditDialog.show(
                requireContext(),
                exercise,
                updated -> {
                    if (updated != null) {
                        replaceExercise(workingExercises, exercise, updated);
                    }
                    adapter.setExercises(new ArrayList<>(workingExercises));
                },
                deleted -> {
                    workingExercises.remove(exercise);
                    adapter.setExercises(new ArrayList<>(workingExercises));
                }
        );
    }

    private void replaceExercise(List<ExerciseInWorkout> exercises,
                                 ExerciseInWorkout original,
                                 ExerciseInWorkout updated) {
        if (exercises == null || original == null || updated == null) {
            return;
        }
        int index = indexOfExercise(exercises, original);
        if (index >= 0) {
            exercises.set(index, updated);
        } else {
            exercises.add(updated);
        }
    }

    private int indexOfExercise(List<ExerciseInWorkout> exercises,
                                ExerciseInWorkout target) {
        if (exercises == null || target == null) {
            return -1;
        }
        Long targetId = target.getId();
        for (int i = 0; i < exercises.size(); i++) {
            ExerciseInWorkout item = exercises.get(i);
            if (item == null) {
                continue;
            }
            Long itemId = item.getId();
            if (targetId != null && itemId != null && targetId.equals(itemId)) {
                return i;
            }
            if (item == target) {
                return i;
            }
        }
        return -1;
    }

    private void showAddExerciseDialog(List<ExerciseInWorkout> workingExercises,
                                       WorkoutExerciseAdapter adapter) {
        if (workingExercises == null || adapter == null || !isAdded()) {
            return;
        }
        hideKeyboard();
        ExerciseInWorkout newExercise = createNewExercise();
        ExerciseEditDialog.show(
                requireContext(),
                newExercise,
                updated -> {
                    workingExercises.add(updated);
                    adapter.setExercises(new ArrayList<>(workingExercises));
                },
                null
        );
    }

    private void hideKeyboard() {
        if (!isAdded()) {
            return;
        }
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private List<ExerciseInWorkout> cloneExercises(List<ExerciseInWorkout> source) {
        List<ExerciseInWorkout> copy = new ArrayList<>();
        if (source == null) {
            return copy;
        }
        for (ExerciseInWorkout exercise : source) {
            copy.add(copyExercise(exercise));
        }
        return copy;
    }

    private ExerciseInWorkout copyExercise(ExerciseInWorkout exercise) {
        if (exercise == null) {
            return createFallbackExercise();
        }
        ExerciseInWorkout copy = new ExerciseInWorkout(
            exercise.getId(),
            exercise.getName(),
            exercise.getTargetedMusclesLabel(),
            exercise.getImageA(),
            exercise.getImageB(),
            exercise.getSeries(),
            exercise.getRepetitions(),
            exercise.getWeight(),
            exercise.getTime(),
            exercise.getHeartRates(),
            exercise.getDistance(),
            exercise.getCalories(),
            exercise.getExerciseId(),
            exercise.getWorkoutId()
        );
        copy.setType(exercise.getType());
        copy.setShowSeries(exercise.isShowSeries());
        copy.setShowRepetitions(exercise.isShowRepetitions());
        copy.setShowWeight(exercise.isShowWeight());
        copy.setShowTime(exercise.isShowTime());
        copy.setShowHeartRate(exercise.isShowHeartRate());
        copy.setWeightInKg(exercise.isWeightInKg());
        copy.setOrderIndex(exercise.getOrderIndex());
        return copy;
    }

    private ExerciseInWorkout createNewExercise() {
        ExerciseInWorkout exercise = new ExerciseInWorkout(
            null,
            "",
            "",
            null,
            null,
            3,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
        exercise.setType(ExerciseType.ANAEROBIC);
        exercise.applyDefaultMetricsForType(ExerciseType.ANAEROBIC);
        exercise.setWeightInKg(false);
        return exercise;
    }

    private ExerciseInWorkout createFallbackExercise() {
        ExerciseInWorkout exercise = new ExerciseInWorkout(
                    null,
                    "",
                    "",
                    null,
                    null,
                    1,
                    1,
                    0,
                    0,
                    0,
                    null,
                    null,
                    null,
                    null
        );
        exercise.setType(ExerciseType.ANAEROBIC);
        exercise.applyDefaultMetricsForType(ExerciseType.ANAEROBIC);
        exercise.setWeightInKg(false);
        return exercise;
    }

    private Workout copyWorkout(Workout workout) {
        if (workout == null) {
            return null;
        }
        Workout copy = new Workout(
                workout.getId(),
                workout.getName(),
                workout.getDate(),
                workout.isStar(),
                workout.getType()
        );
        copy.setCreatedAt(workout.getCreatedAt());
        copy.setUpdatedAt(workout.getUpdatedAt());
        copy.setExercises(cloneExercises(workout.getExercises()));
        return copy;
    }

    private void toggleWorkoutStar(Workout workout) {
        if (workout == null) return;
        workoutsListViewModel.toggleWorkoutStar(workout);
        if (expandedWorkoutCopy != null && isExpandedWorkout(workout)) {
            expandedWorkoutCopy.setStar(workout.isStar());
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }

    private boolean hasWorkoutChanges(Workout original, String newName, List<ExerciseInWorkout> newExercises) {
        if (original == null) {
            return false;
        }
        String originalName = original.getName() != null ? original.getName().trim() : "";
        String safeNewName = newName != null ? newName.trim() : "";
        if (!Objects.equals(originalName, safeNewName)) {
            return true;
        }
        if (original.isStar() != expandedWorkoutCopy.isStar()) {
            return true;
        }
        List<ExerciseInWorkout> originalExercises = original.getExercises();
        return !areExercisesSame(originalExercises, newExercises);
    }

    private boolean areExercisesSame(List<ExerciseInWorkout> left, List<ExerciseInWorkout> right) {
        List<ExerciseInWorkout> leftSafe = left != null ? left : new ArrayList<>();
        List<ExerciseInWorkout> rightSafe = right != null ? right : new ArrayList<>();
        if (leftSafe.size() != rightSafe.size()) {
            return false;
        }
        for (int i = 0; i < leftSafe.size(); i++) {
            ExerciseInWorkout a = leftSafe.get(i);
            ExerciseInWorkout b = rightSafe.get(i);
            if (!areExercisesSame(a, b)) {
                return false;
            }
        }
        return true;
    }

    private boolean areExercisesSame(ExerciseInWorkout a, ExerciseInWorkout b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return Objects.equals(a.getId(), b.getId())
                && Objects.equals(a.getName(), b.getName())
                && Objects.equals(a.getTargetedMusclesLabel(), b.getTargetedMusclesLabel())
                && Objects.equals(a.getImageA(), b.getImageA())
                && Objects.equals(a.getImageB(), b.getImageB())
                && a.getSeries() == b.getSeries()
                && a.getRepetitions() == b.getRepetitions()
                && Objects.equals(a.getWeight(), b.getWeight())
                && Objects.equals(a.getTime(), b.getTime())
                && Objects.equals(a.getHeartRates(), b.getHeartRates())
                && Objects.equals(a.getExerciseId(), b.getExerciseId())
                && Objects.equals(a.getWorkoutId(), b.getWorkoutId());
    }


    @Override
    public void onSearchQueryChanged(String query) {
        searchQuery = query != null ? query : "";
        applyWorkoutFilters();
    }

    @Override
    public void onSearchClosed() {
        searchQuery = "";
        applyWorkoutFilters();
    }

    @Override
    public void onFilterRequested() {
        showMuscleFilterDialog();
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
        return R.string.workout_search_hint;
    }

    @Override
    public boolean onBackPressed() {
        return handleBackPressed();
    }
}
