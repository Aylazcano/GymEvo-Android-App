package com.example.gymevo.ui.workoutTracker;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.ui.common.calendar.CalendarAdapter;
import com.example.gymevo.R;
import com.example.gymevo.ui.common.adapter.WorkoutExerciseAdapter;
import com.example.gymevo.databinding.FragmentWorkoutTrackerBinding;
import com.example.gymevo.databinding.CalendarHeaderBinding;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.Workout;
import com.example.gymevo.ui.common.ConfirmDeleteDialog;
import com.example.gymevo.ui.common.ExerciseEditDialog;
import com.example.gymevo.ui.common.DragDropItemTouchHelper;
import com.example.gymevo.ui.common.calendar.CalendarUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WorkoutTrackerFragment extends Fragment implements CalendarAdapter.OnItemListener {

    public static final String ARG_SELECTED_DATE = "SELECTED_DATE";
    public static final String ARG_OPEN_ADD_EXERCISE = "OPEN_ADD_EXERCISE_TRACKER";
    private static final int HEADER_STEP_DP = 36;

    private FragmentWorkoutTrackerBinding binding;
    private WorkoutTrackerViewModel workoutTrackerViewModel;
    private RecyclerView calendarRecyclerView;
    private RecyclerView workoutRecyclerView;
    private WorkoutExerciseAdapter workoutAdapter;
    private LocalDate selectedDate;
    private LocalDate displayDate;
    private List<ExerciseInWorkout> currentExercises = new ArrayList<>();
    private boolean isMonthView = true;
    private GestureDetector gestureDetector;
    private CalendarHeaderBinding calendarHeaderBinding;
    private final List<Workout> availableWorkouts = new ArrayList<>();
    private final Set<LocalDate> workoutDatesWithExercises = new HashSet<>();
    private float headerStartX;
    private float headerStartY;
    private LocalDate headerBaseDate;
    private LocalDate headerPreviewDate;
    private int headerTouchSlop;
    private int headerStepPx;
    private boolean headerDragging;
    private int workoutSwipeMinDistancePx;
    private int workoutSwipeMaxOffPathPx;
    private float workoutSwipeStartX;
    private float workoutSwipeStartY;
    private boolean workoutSwipeTracking;
    private boolean workoutSwipeConsumed;
    private ActionMode selectionActionMode;
    private androidx.activity.OnBackPressedCallback backPressedCallback;
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
            if (workoutAdapter != null) {
                workoutAdapter.clearSelection();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutTrackerBinding.inflate(inflater, container, false);
        selectedDate = getPersistedSelectedDate();
        displayDate = selectedDate;

        calendarHeaderBinding = CalendarHeaderBinding.bind(binding.getRoot().findViewById(R.id.calendarLayout));

        initializeViews(binding.getRoot());
        initializeViewModel();
        initializeWorkoutRecyclerView();
        initializeGestureDetector();
        initializeHeaderSwipe();
        setupCalendar();
        observeAddExerciseRequests();
        observeAvailableWorkouts();
        updateSelectedDate(selectedDate, false);
        setupBackNavigation();

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_workout_tracker, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_export_workout) {
            showSaveWorkoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private LocalDate getPersistedSelectedDate() {
        LocalDate fallback = LocalDate.now();
        androidx.lifecycle.SavedStateHandle savedStateHandle = getSavedStateHandle();
        if (savedStateHandle == null) {
            return fallback;
        }
        String dateStr = savedStateHandle.get(ARG_SELECTED_DATE);
        if (dateStr == null || dateStr.isEmpty()) {
            return fallback;
        }
        return LocalDate.parse(dateStr);
    }

    private void initializeViews(View root) {
        calendarRecyclerView = root.findViewById(R.id.calendarRecyclerView);
        workoutRecyclerView = binding.WorkoutRecyclerView;
        calendarRecyclerView.setItemAnimator(null);
        headerTouchSlop = ViewConfiguration.get(requireContext()).getScaledTouchSlop();
        headerStepPx = Math.round(HEADER_STEP_DP * getResources().getDisplayMetrics().density);
        workoutSwipeMinDistancePx = dpToPx(96);
        workoutSwipeMaxOffPathPx = dpToPx(48);
    }

    private void initializeViewModel() {
        workoutTrackerViewModel = new ViewModelProvider(this).get(WorkoutTrackerViewModel.class);
        workoutTrackerViewModel.getExercisesOnDateLiveData()
            .observe(getViewLifecycleOwner(), this::updateExercisesList);
        workoutTrackerViewModel.getWorkoutDatesWithExercisesLiveData()
            .observe(getViewLifecycleOwner(), this::updateWorkoutDatesWithExercises);
    }

    private void observeAvailableWorkouts() {
        workoutTrackerViewModel.getTemplatesLiveData()
            .observe(getViewLifecycleOwner(), workouts -> {
                availableWorkouts.clear();
                if (workouts != null) {
                    availableWorkouts.addAll(workouts);
                }
            });
    }

    private void initializeGestureDetector() {
        gestureDetector = CalendarUtils.createGestureDetector(
                getContext(),
                this,
                displayDate,
                calendarRecyclerView,
                isMonthView,
                calendarHeaderBinding.oneMonthText,
                calendarHeaderBinding.oneYearText,
                viewMode -> isMonthView = viewMode,
                workoutDatesWithExercises,
                date -> displayDate = date,
                () -> selectedDate,
                () -> displayDate
        );
        calendarRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    rv.getParent().requestDisallowInterceptTouchEvent(true);
                }
                gestureDetector.onTouchEvent(e);
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetector.onTouchEvent(e);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                // No-op
            }
        });
    }

    private void setupCalendar() {
        Context context = getContext();
        if (context == null) return;

        CalendarUtils.setCalendarView(context, calendarRecyclerView, displayDate, isMonthView,
            this, workoutDatesWithExercises, selectedDate);
        CalendarUtils.updateCalendarHeader(displayDate, calendarHeaderBinding.oneMonthText, calendarHeaderBinding.oneYearText);
    }

    private void initializeHeaderSwipe() {
        View swipeArea = calendarHeaderBinding.calendarHeaderSwipeArea;
        swipeArea.setOnTouchListener(this::handleHeaderSwipe);
        calendarHeaderBinding.buttonToday.setOnClickListener(this::jumpToToday);
    }

    private boolean handleHeaderSwipe(@NonNull View target, @NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                headerStartX = event.getX();
                headerStartY = event.getY();
                headerBaseDate = displayDate;
                headerPreviewDate = displayDate;
                headerDragging = false;
                updateHeaderTitle(displayDate);
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - headerStartX;
                float dy = event.getY() - headerStartY;
                if (Math.hypot(dx, dy) < headerTouchSlop) {
                    return true;
                }
                headerDragging = true;

                boolean horizontal = Math.abs(dx) >= Math.abs(dy);
                int delta = horizontal
                    ? (int) (-dx / headerStepPx)
                        : (int) (-dy / headerStepPx);
                LocalDate preview = headerBaseDate;
                if (delta != 0) {
                    preview = horizontal
                            ? headerBaseDate.plusMonths(delta)
                            : headerBaseDate.plusYears(delta);
                }
                if (!preview.equals(headerPreviewDate)) {
                    headerPreviewDate = preview;
                    updateHeaderTitle(preview);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (headerPreviewDate != null && !headerPreviewDate.equals(displayDate)) {
                    setDisplayDate(headerPreviewDate);
                }
                if (!headerDragging) {
                    target.performClick();
                }
                return true;
            default:
                return false;
        }
    }

    private void updateHeaderTitle(@NonNull LocalDate date) {
        calendarHeaderBinding.oneMonthText.setText(CalendarUtils.monthFromDateString(date));
        calendarHeaderBinding.oneYearText.setText(String.valueOf(date.getYear()));
    }

    private void setDisplayDate(@NonNull LocalDate date) {
        displayDate = date;
        setupCalendar();
    }

    private void jumpToToday(View view) {
        updateSelectedDate(LocalDate.now(), false);
    }

    private void initializeWorkoutRecyclerView() {
        Context context = getContext();
        if (context == null) return;

        workoutAdapter = new WorkoutExerciseAdapter(new ArrayList<>(), this::showEditExerciseDialog);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        workoutRecyclerView.setAdapter(workoutAdapter);
        workoutRecyclerView.setItemAnimator(new DefaultItemAnimator());

        workoutAdapter.setSelectionListener(this::updateSelectionUi);
        workoutAdapter.setOrderChangedListener(this::persistExerciseOrder);
        workoutAdapter.setDeleteListener(this::onDeleteSingleExercise);
        DragDropItemTouchHelper callback = new DragDropItemTouchHelper(workoutAdapter::onItemMove, null);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(workoutRecyclerView);
        workoutAdapter.setItemTouchHelper(helper);

        setupWorkoutSwipeNavigation();
    }

    private void setupWorkoutSwipeNavigation() {
        if (workoutRecyclerView == null) {
            return;
        }
        workoutRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (selectionActionMode != null
                        || (workoutAdapter != null && workoutAdapter.isSelectionModeActive())) {
                    workoutSwipeTracking = false;
                    workoutSwipeConsumed = false;
                    return false;
                }
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        workoutSwipeStartX = e.getX();
                        workoutSwipeStartY = e.getY();
                        workoutSwipeTracking = true;
                        workoutSwipeConsumed = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (workoutSwipeTracking && !workoutSwipeConsumed) {
                            float dx = e.getX() - workoutSwipeStartX;
                            float dy = e.getY() - workoutSwipeStartY;
                            if (Math.abs(dx) > workoutSwipeMinDistancePx
                                    && Math.abs(dy) < workoutSwipeMaxOffPathPx) {
                                workoutSwipeConsumed = true;
                                rv.getParent().requestDisallowInterceptTouchEvent(true);
                                return true;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (workoutSwipeTracking) {
                            handleWorkoutSwipe(e.getX(), e.getY());
                        }
                        workoutSwipeTracking = false;
                        workoutSwipeConsumed = false;
                        break;
                    default:
                        break;
                }
                return workoutSwipeConsumed;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (!workoutSwipeTracking) {
                    return;
                }
                if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                    handleWorkoutSwipe(e.getX(), e.getY());
                    workoutSwipeTracking = false;
                    workoutSwipeConsumed = false;
                }
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                // No-op
            }
        });
    }

    private void handleWorkoutSwipe(float endX, float endY) {
        float dx = endX - workoutSwipeStartX;
        float dy = endY - workoutSwipeStartY;
        if (Math.abs(dx) <= Math.abs(dy)) {
            return;
        }
        if (Math.abs(dx) < workoutSwipeMinDistancePx || Math.abs(dy) > workoutSwipeMaxOffPathPx) {
            return;
        }
        LocalDate target = dx < 0 ? selectedDate.plusDays(1) : selectedDate.minusDays(1);
        updateSelectedDate(target, false);
    }

    private void observeAddExerciseRequests() {
        NavController navController = NavHostFragment.findNavController(this);
        NavBackStackEntry entry = navController.getCurrentBackStackEntry();
        if (entry == null) {
            return;
        }
        entry.getSavedStateHandle()
                .getLiveData(ARG_OPEN_ADD_EXERCISE, false)
                .observe(getViewLifecycleOwner(), shouldOpen -> {
                    if (Boolean.TRUE.equals(shouldOpen)) {
                        entry.getSavedStateHandle().set(ARG_OPEN_ADD_EXERCISE, false);
                        showAddExerciseOptions();
                    }
                });
    }

    @Override
    public void onItemClick(int position, @NonNull String dayText, @NonNull LocalDate date) {
        if (dayText.isEmpty()) return;

        updateSelectedDate(date, true);
    }

    private void updateSelectedDate(@NonNull LocalDate date, boolean showToast) {
        selectedDate = date;
        displayDate = date;
        setupCalendar();

        if (showToast) {
            showCustomToast(String.format(Locale.getDefault(), "Selected date %d %s %d",
                date.getDayOfMonth(),
                date.getMonth(),
                date.getYear()));
        }

        workoutTrackerViewModel.getExercisesForWorkoutOnDate(date);
        persistSelectedDate(date);
    }

    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(
                R.layout.custom_toast,
            requireView().findViewById(R.id.custom_toast_container)
        );

        TextView text = layout.findViewById(R.id.text);
        text.setText(message);

        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void persistSelectedDate(@NonNull LocalDate date) {
        SavedStateHandle savedStateHandle = getSavedStateHandle();
        if (savedStateHandle == null) {
            return;
        }
        savedStateHandle.set(ARG_SELECTED_DATE, date.toString());
    }

    private SavedStateHandle getSavedStateHandle() {
        if (!isAdded()) {
            return null;
        }
        NavController navController = NavHostFragment.findNavController(this);
        NavBackStackEntry entry = navController.getCurrentBackStackEntry();
        if (entry == null) {
            return null;
        }
        return entry.getSavedStateHandle();
    }

    private void showEditExerciseDialog(ExerciseInWorkout exercise) {
        if (exercise == null || !isAdded()) {
            return;
        }
        int index = findExerciseIndexFromEnd(currentExercises, exercise);
        ExerciseEditDialog.show(
                requireContext(),
                exercise,
                updated -> workoutTrackerViewModel.updateExerciseInWorkoutAt(selectedDate, index, updated),
                deleted -> workoutTrackerViewModel.removeExerciseFromWorkout(selectedDate, deleted)
        );
    }

    private void showAddExerciseDialog() {
        if (!isAdded()) {
            return;
        }
        ExerciseInWorkout newExercise = createNewExercise();
        ExerciseEditDialog.show(
                requireContext(),
                newExercise,
            created -> workoutTrackerViewModel.addExerciseToWorkout(selectedDate, created),
            null
        );
    }

    private void showImportWorkoutDialog() {
        if (!isAdded()) {
            return;
        }
        if (availableWorkouts.isEmpty()) {
            showToast(getString(R.string.workout_import_empty));
            return;
        }

        CharSequence[] names = new CharSequence[availableWorkouts.size()];
        for (int i = 0; i < availableWorkouts.size(); i++) {
            Workout workout = availableWorkouts.get(i);
            String name = workout != null ? workout.getName() : "";
            names[i] = TextUtils.isEmpty(name)
                    ? getString(R.string.workout_import_unnamed, i + 1)
                    : name;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.workout_import_from_list)
                .setItems(names, (dialog, which) -> handleWorkoutImportSelection(which))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void handleWorkoutImportSelection(int index) {
        if (index < 0 || index >= availableWorkouts.size()) {
            return;
        }
        Workout selected = availableWorkouts.get(index);
        boolean imported = workoutTrackerViewModel.importExercisesFromWorkout(selectedDate, selected);
        if (imported) {
            showToast(getString(R.string.workout_import_success));
        } else {
            showToast(getString(R.string.workout_import_failed));
        }
    }

    private void showAddExerciseOptions() {
        if (!isAdded()) {
            return;
        }
        CharSequence[] options = new CharSequence[]{
                getString(R.string.exercise_add),
                getString(R.string.workout_import_from_list)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.workout_tracker_add_choice_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddExerciseDialog();
                    } else if (which == 1) {
                        showImportWorkoutDialog();
                    }
                })
                .show();
    }

    private void showSaveWorkoutDialog() {
        if (!isAdded()) {
            return;
        }
        if (currentExercises == null || currentExercises.isEmpty()) {
            showToast(getString(R.string.workout_export_empty));
            return;
        }

        String defaultName = getString(
                R.string.workout_export_default_name,
                selectedDate != null ? selectedDate.toString() : ""
        );

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_save_workout, null, false);
        TextInputEditText nameInput = dialogView.findViewById(R.id.input_workout_export_name);
        if (nameInput != null) {
            nameInput.setText(defaultName);
            if (nameInput.getText() != null) {
                nameInput.setSelection(nameInput.getText().length());
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.workout_export_title)
                .setView(dialogView)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String name = nameInput != null && nameInput.getText() != null
                            ? nameInput.getText().toString().trim()
                            : "";
                    if (name.isEmpty()) {
                        name = defaultName;
                    }
                    boolean saved = workoutTrackerViewModel
                            .exportWorkoutAsTemplate(name, new ArrayList<>(currentExercises));
                    if (saved) {
                        showToast(getString(R.string.workout_export_success));
                    } else {
                        showToast(getString(R.string.workout_export_failed));
                    }
                })
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void updateExercisesList(List<ExerciseInWorkout> exercises) {
        List<ExerciseInWorkout> safeExercises = exercises != null ? exercises : new ArrayList<>();
        currentExercises = new ArrayList<>(safeExercises);
        workoutAdapter.setExercises(safeExercises);
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
            if (workoutAdapter != null && workoutAdapter.isSelectionModeActive()) {
                selectionActionMode.setTitle(getString(R.string.selection_count, 0));
            } else {
                selectionActionMode.finish();
            }
        }
    }

    private void onDeleteSelectedExercises() {
        if (workoutAdapter == null) {
            return;
        }
        if (selectedDate == null) {
            return;
        }
        ConfirmDeleteDialog.show(
                requireContext(),
                R.string.exercise_delete_title,
                R.string.exercise_delete_confirm,
                () -> {
                    List<ExerciseInWorkout> selected = new ArrayList<>(workoutAdapter.getSelectedItems());
                    if (!selected.isEmpty()) {
                        List<ExerciseInWorkout> updated = new ArrayList<>(currentExercises);
                        updated.removeAll(selected);
                        currentExercises = new ArrayList<>(updated);
                        workoutTrackerViewModel.replaceExercisesForDate(selectedDate, updated);
                        workoutAdapter.setExercises(updated);
                    }
                    workoutAdapter.clearSelection();
                }
        );
    }

    private void onMoveSelectedExercisesTop() {
        if (workoutAdapter == null) {
            return;
        }
        if (selectedDate == null) {
            return;
        }
        workoutAdapter.moveSelectedToTop();
        persistExerciseOrder(workoutAdapter.getItems());
        workoutAdapter.clearSelection();
    }

    private void onMoveSelectedExercisesBottom() {
        if (workoutAdapter == null) {
            return;
        }
        if (selectedDate == null) {
            return;
        }
        workoutAdapter.moveSelectedToBottom();
        persistExerciseOrder(workoutAdapter.getItems());
        workoutAdapter.clearSelection();
    }

    private void onDeleteSingleExercise(ExerciseInWorkout exercise) {
        if (exercise == null) {
            return;
        }
        if (selectedDate == null) {
            return;
        }
        ConfirmDeleteDialog.show(
                requireContext(),
                R.string.exercise_delete_title,
                R.string.exercise_delete_confirm,
                () -> {
                    List<ExerciseInWorkout> updated = new ArrayList<>(currentExercises);
                    updated.remove(exercise);
                    currentExercises = new ArrayList<>(updated);
                    workoutTrackerViewModel.replaceExercisesForDate(selectedDate, updated);
                    workoutAdapter.setExercises(updated);
                }
        );
    }

    private void persistExerciseOrder(List<ExerciseInWorkout> newOrder) {
        if (newOrder == null) {
            return;
        }
        if (selectedDate == null) {
            return;
        }
        currentExercises = new ArrayList<>(newOrder);
        workoutTrackerViewModel.replaceExercisesForDate(selectedDate, newOrder);
    }

    private void updateWorkoutDatesWithExercises(List<LocalDate> dates) {
        workoutDatesWithExercises.clear();
        if (dates != null) {
            workoutDatesWithExercises.addAll(dates);
        }
        setupCalendar();
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

    private int findExerciseIndexFromEnd(List<ExerciseInWorkout> exercises, ExerciseInWorkout target) {
        if (exercises == null || target == null) {
            return -1;
        }
        Long targetId = target.getId();
        Long targetExerciseId = target.getExerciseId();
        for (int i = exercises.size() - 1; i >= 0; i--) {
            ExerciseInWorkout item = exercises.get(i);
            if (item == target) {
                return i;
            }
            if (item == null) {
                continue;
            }
            if (targetId != null && targetId.equals(item.getId())) {
                return i;
            }
            if (targetId == null && targetExerciseId != null
                    && targetExerciseId.equals(item.getExerciseId())) {
                return i;
            }
        }
        return -1;
    }
}
