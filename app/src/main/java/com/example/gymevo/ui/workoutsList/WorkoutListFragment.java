package com.example.gymevo.ui.workoutsList;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
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
import com.example.gymevo.model.Workout;
import com.example.gymevo.model.Workout.WorkoutType;
import com.example.gymevo.ui.common.ConfirmDeleteDialog;
import com.example.gymevo.ui.common.DragDropItemTouchHelper;
import com.example.gymevo.ui.common.ExerciseEditDialog;
import com.example.gymevo.ui.common.FilterUi;
import com.example.gymevo.ui.common.ItemSpacingDecoration;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WorkoutListFragment extends Fragment {

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
    private ActionMode selectionActionMode;
    private ActionMode selectionActionModeExercises;
    private final List<Workout> allWorkouts = new ArrayList<>();
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
                onDeleteSelectedWorkouts();
                return true;
            } else if (id == R.id.action_move_up) {
                onMoveSelectedWorkoutsTop();
                return true;
            } else if (id == R.id.action_move_down) {
                onMoveSelectedWorkoutsBottom();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selectionActionMode = null;
            if (workoutsAdapter != null) {
                workoutsAdapter.clearSelection();
            }
        }
    };

    private final ActionMode.Callback selectionExercisesCallback = new ActionMode.Callback() {
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
                onDeleteSelectedNestedExercises();
                return true;
            } else if (id == R.id.action_move_up) {
                onMoveSelectedNestedExercisesTop();
                return true;
            } else if (id == R.id.action_move_down) {
                onMoveSelectedNestedExercisesBottom();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selectionActionModeExercises = null;
            if (workoutsAdapter != null && workoutsAdapter.getExpandedExerciseAdapter() != null) {
                workoutsAdapter.getExpandedExerciseAdapter().clearSelection();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutsListBinding.inflate(inflater, container, false);
        workoutsListViewModel = new ViewModelProvider(this).get(WorkoutListViewModel.class);

        setupUI();
        setupBackNavigation();
        setupFilters();
        observeAddWorkoutRequests();
        return binding.getRoot();
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
                if (selectionActionModeExercises != null) {
                    selectionActionModeExercises.finish();
                    return;
                }
                if (expandedWorkoutRef != null) {
                    String name = expandedWorkoutCopy != null
                            ? expandedWorkoutCopy.getName()
                            : expandedWorkoutRef.getName();
                    onSaveExpandedWorkout(expandedWorkoutRef, name, expandedExercises);
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressedCallback);
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
        Workout newWorkout = new Workout();
        newWorkout.setType(WorkoutType.TEMPLATE);
        newWorkout.setDate(null);
        pendingNewWorkout = newWorkout;
        expandWorkout(newWorkout, true);
        updateWorkoutsList(workoutsListViewModel.getWorkoutsLiveData().getValue());
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
        workoutsRecyclerView.addItemDecoration(new ItemSpacingDecoration(dpToPx(4), true));

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
                applyWorkoutFilters();
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
            applyWorkoutFilters();
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
                    applyWorkoutFilters();
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
                    applyWorkoutFilters();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void toggleStarPriority() {
        starPriorityEnabled = !starPriorityEnabled;
        updateStarButton();
        applyWorkoutFilters();
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
        updateWorkoutsList(filtered);
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
            case MUSCLE:
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

        if (sortOrder == SortOrder.DESC) {
            base = base.reversed();
        }
        if (starPriorityEnabled) {
            base = Comparator.comparing(Workout::isStar).reversed().thenComparing(base);
        }
        return base;
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
        if (selectedCount > 0) {
            if (selectionActionMode == null) {
                AppCompatActivity activity = (AppCompatActivity) requireActivity();
                selectionActionMode = activity.startSupportActionMode(selectionCallback);
            }
            if (selectionActionMode != null) {
                selectionActionMode.setTitle(getString(R.string.selection_count, selectedCount));
            }
        } else if (selectionActionMode != null) {
            if (workoutsAdapter != null && workoutsAdapter.isSelectionModeActive()) {
                selectionActionMode.setTitle(getString(R.string.selection_count, 0));
            } else {
                selectionActionMode.finish();
            }
        }
    }

    private void updateNestedExerciseSelectionUi(int selectedCount) {
        if (!isAdded()) {
            return;
        }
        if (selectedCount > 0) {
            if (selectionActionModeExercises == null) {
                AppCompatActivity activity = (AppCompatActivity) requireActivity();
                selectionActionModeExercises = activity.startSupportActionMode(selectionExercisesCallback);
            }
            if (selectionActionModeExercises != null) {
                selectionActionModeExercises.setTitle(getString(R.string.selection_count, selectedCount));
            }
        } else if (selectionActionModeExercises != null) {
            WorkoutExerciseAdapter adapter = workoutsAdapter != null
                    ? workoutsAdapter.getExpandedExerciseAdapter()
                    : null;
            if (adapter != null && adapter.isSelectionModeActive()) {
                selectionActionModeExercises.setTitle(getString(R.string.selection_count, 0));
            } else {
                selectionActionModeExercises.finish();
            }
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
                        workoutsListViewModel.deleteWorkout(workout);
                    }
                    workoutsAdapter.clearSelection();
                }
        );
    }

    private void onMoveSelectedWorkoutsTop() {
        if (workoutsAdapter == null) {
            return;
        }
        workoutsAdapter.moveSelectedToTop();
        workoutsAdapter.clearSelection();
    }

    private void onMoveSelectedWorkoutsBottom() {
        if (workoutsAdapter == null) {
            return;
        }
        workoutsAdapter.moveSelectedToBottom();
        workoutsAdapter.clearSelection();
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
                    adapter.clearSelection();
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
        adapter.clearSelection();
    }

    private void onMoveSelectedNestedExercisesBottom() {
        WorkoutExerciseAdapter adapter = workoutsAdapter != null ? workoutsAdapter.getExpandedExerciseAdapter() : null;
        if (adapter == null) {
            return;
        }
        adapter.moveSelectedToBottom();
        expandedExercises = adapter.getItems();
        adapter.clearSelection();
    }

    private void onDeleteSingleWorkout(Workout workout) {
        if (workout == null) {
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

    private void updateWorkoutsList(List<Workout> workouts) {
        if (workoutsAdapter == null) {
            return;
        }
        List<Workout> safe = workouts != null ? workouts : new ArrayList<>();
        List<Workout> display = new ArrayList<>(safe);
        if (pendingNewWorkout != null) {
            display.add(0, pendingNewWorkout);
        }
        workoutsAdapter.setWorkouts(display);
        if (expandedWorkoutRef != null) {
            workoutsAdapter.setExpandedState(expandedWorkoutRef, expandedWorkoutCopy,
                    expandedExercises, expandedIsNew);
        } else {
            workoutsAdapter.clearExpandedState();
        }
        if (emptyView != null) {
            boolean isEmpty = safe.isEmpty() && pendingNewWorkout == null;
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void openWorkoutExercisesDialog(Workout workout) {
        openWorkoutExercisesDialog(workout, RecyclerView.NO_POSITION);
    }

    private void openWorkoutExercisesDialog(Workout workout, int position) {
        if (!isAdded() || workout == null) {
            return;
        }
        boolean isCurrentlyExpanded = isExpandedWorkout(workout);
        if (isCurrentlyExpanded) {
            saveAndCollapseExpandedWorkout();
        } else {
            if (expandedWorkoutRef != null) {
                saveAndCollapseExpandedWorkout();
            }
            if (pendingNewWorkout != null && pendingNewWorkout != workout) {
                pendingNewWorkout = null;
            }
            boolean isNew = pendingNewWorkout != null && pendingNewWorkout == workout;
            expandWorkout(workout, isNew);
            requestScrollToPositionIfNeeded(position);
        }
    }

    private void saveAndCollapseExpandedWorkout() {
        if (expandedWorkoutRef == null || expandedWorkoutCopy == null) {
            collapseExpandedWorkout();
            return;
        }
        String name = expandedWorkoutCopy.getName() != null
                ? expandedWorkoutCopy.getName()
                : expandedWorkoutRef.getName();
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
        expandedWorkoutRef = null;
        expandedWorkoutCopy = null;
        expandedExercises = new ArrayList<>();
        expandedIsNew = false;
        pendingNewWorkout = null;
        workoutsAdapter.clearExpandedState();
        workoutsAdapter.notifyDataSetChanged();
        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(false);
        }
    }

    private boolean isExpandedWorkout(Workout workout) {
        if (workout == null || expandedWorkoutRef == null) {
            return false;
        }
        Long id = workout.getId();
        Long expandedId = expandedWorkoutRef.getId();
        if (id != null && expandedId != null) {
            return id.equals(expandedId);
        }
        return workout == expandedWorkoutRef;
    }

    void onSaveExpandedWorkout(Workout workout, String name,
                               List<ExerciseInWorkout> exercises) {
        if (workout == null || expandedWorkoutCopy == null) {
            return;
        }
        String safeName = name != null ? name.trim() : (workout.getName() != null ? workout.getName() : "");
        expandedWorkoutCopy.setName(safeName);
        expandedWorkoutCopy.setExercises(new ArrayList<>(exercises));
        expandedWorkoutCopy.setStar(workout.isStar());
        if (expandedWorkoutCopy.getCreatedAt() <= 0L) {
            expandedWorkoutCopy.setCreatedAt(System.currentTimeMillis());
        }
        workoutsListViewModel.setCurrentWorkoutWithExercises(expandedWorkoutCopy);
        workoutsListViewModel.updateCurrentWorkoutMeta(safeName);
        workoutsListViewModel.saveWorkout();
        collapseExpandedWorkout();
    }

    void onDeleteExpandedWorkout(Workout workout) {
        if (workout == null) {
            return;
        }
        if (workout.getId() == null) {
            collapseExpandedWorkout();
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
        return new ExerciseInWorkout(
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
            exercise.getExerciseId(),
            exercise.getWorkoutId()
        );
    }

    private ExerciseInWorkout createNewExercise() {
        return new ExerciseInWorkout(
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
            null
        );
    }

    private ExerciseInWorkout createFallbackExercise() {
        return new ExerciseInWorkout(
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
                    null
        );
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
