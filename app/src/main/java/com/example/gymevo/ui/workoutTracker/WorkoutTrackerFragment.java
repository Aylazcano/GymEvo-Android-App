package com.example.gymevo.ui.workoutTracker;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.example.gymevo.R;
import com.example.gymevo.databinding.FragmentWorkoutTrackerBinding;
import com.example.gymevo.databinding.CalendarHeaderBinding;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.ExerciseType;
import com.example.gymevo.model.Workout;
import com.example.gymevo.ui.common.ConfirmDeleteDialog;
import com.example.gymevo.ui.common.ExerciseEditDialog;
import com.example.gymevo.ui.common.DragDropItemTouchHelper;
import com.example.gymevo.data.repository.UserPreferencesRepository;
import com.example.gymevo.ui.common.adapter.WorkoutExerciseAdapter;
import com.example.gymevo.ui.common.calendar.CalendarAdapter;
import com.example.gymevo.ui.common.calendar.CalendarUtils;
import com.example.gymevo.ui.main.MainActivity;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WorkoutTrackerFragment extends Fragment implements CalendarAdapter.OnItemListener, MainActivity.WorkoutTrackerMenuDelegate {

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
    private GestureDetector workoutSwipeDetector;
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
    private float workoutSwipeStartX;
    private float workoutSwipeStartY;
    private int workoutTouchSlop;
    private boolean workoutSwipeInProgress;
    private androidx.activity.OnBackPressedCallback backPressedCallback;
    private UserPreferencesRepository userPreferencesRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutTrackerBinding.inflate(inflater, container, false);
        selectedDate = getPersistedSelectedDate();
        displayDate = selectedDate;

        calendarHeaderBinding = CalendarHeaderBinding.bind(binding.getRoot().findViewById(R.id.calendarLayout));
        userPreferencesRepository = UserPreferencesRepository.getInstance(requireContext());

        initializeViews(binding.getRoot());
        initializeViewModel();
        initializeWorkoutRecyclerView();
        initializeHeaderSwipe();
        observeAddExerciseRequests();
        observeAvailableWorkouts();
        loadCalendarPreferences(() -> {
            initializeGestureDetector();
            setupCalendar();
            updateSelectedDate(selectedDate, false);
        });
        setupBackNavigation();
        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (workoutAdapter != null && workoutAdapter.isSelectionModeActive()) {
            workoutAdapter.clearSelection();
            updateSelectionUi(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            requireActivity().invalidateOptionsMenu();
        }
        if (selectedDate != null) {
            workoutTrackerViewModel.getExercisesForWorkoutOnDate(selectedDate);
        }
    }

    private void setupBackNavigation() {
        if (!isAdded()) {
            return;
        }
        backPressedCallback = new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (workoutAdapter != null && workoutAdapter.isSelectionModeActive()) {
                    workoutAdapter.clearSelection();
                    updateSelectionUi(0);
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
        workoutTouchSlop = ViewConfiguration.get(requireContext()).getScaledTouchSlop();
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
                viewMode -> {
                    isMonthView = viewMode;
                    persistCalendarViewMode(viewMode);
                },
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

    private void loadCalendarPreferences(@NonNull Runnable onComplete) {
        if (userPreferencesRepository == null) {
            onComplete.run();
            return;
        }
        disposables.add(userPreferencesRepository.getCalendarPreferences()
                .subscribeOn(Schedulers.io())
                .subscribe(preferences -> {
                    isMonthView = preferences != null && preferences.isMonthView;
                    if (!isAdded()) {
                        return;
                    }
                    requireActivity().runOnUiThread(onComplete);
                }, throwable -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(onComplete);
                    }
                }));
    }

    private void persistCalendarViewMode(boolean isMonthView) {
        if (userPreferencesRepository == null) {
            return;
        }
        disposables.add(userPreferencesRepository.setCalendarViewMode(isMonthView)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, throwable -> {
                }));
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
        workoutAdapter.setRecyclerView(workoutRecyclerView);
        workoutRecyclerView.setItemAnimator(new DefaultItemAnimator());

        workoutAdapter.setSelectionListener(this::updateSelectionUi);
        workoutAdapter.setOrderChangedListener(this::persistExerciseOrder);
        workoutAdapter.setDeleteListener(this::onDeleteSingleExercise);
        DragDropItemTouchHelper callback = new DragDropItemTouchHelper(workoutAdapter::onItemMove, null);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(workoutRecyclerView);
        workoutAdapter.setItemTouchHelper(helper);

        workoutSwipeDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD_PX = 80;
            private static final int SWIPE_VELOCITY_PX = 200;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false;
                }
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();
                if (Math.abs(dx) <= Math.abs(dy)) {
                    return false;
                }
                if (Math.abs(dx) < SWIPE_THRESHOLD_PX || Math.abs(velocityX) < SWIPE_VELOCITY_PX) {
                    return false;
                }
                int delta = dx < 0 ? 1 : -1;
                changeSelectedDateByDays(delta);
                return true;
            }
        });

        workoutRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                forwardWorkoutSwipeEvent(e);
                return shouldInterceptWorkoutTouch(e);
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                forwardWorkoutSwipeEvent(e);
                resetWorkoutSwipeOnFinish(e);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                // No-op
            }
        });
    }

    private void forwardWorkoutSwipeEvent(@NonNull MotionEvent event) {
        if (workoutSwipeDetector != null) {
            workoutSwipeDetector.onTouchEvent(event);
        }
    }

    private boolean shouldInterceptWorkoutTouch(@NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                workoutSwipeStartX = event.getX();
                workoutSwipeStartY = event.getY();
                workoutSwipeInProgress = false;
                return false;
            case MotionEvent.ACTION_MOVE:
                if (!workoutSwipeInProgress && isHorizontalSwipe(event)) {
                    workoutSwipeInProgress = true;
                    return true;
                }
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return workoutSwipeInProgress;
            default:
                return false;
        }
    }

    private boolean isHorizontalSwipe(@NonNull MotionEvent event) {
        float dx = event.getX() - workoutSwipeStartX;
        float dy = event.getY() - workoutSwipeStartY;
        return Math.abs(dx) > workoutTouchSlop && Math.abs(dx) > Math.abs(dy);
    }

    private void resetWorkoutSwipeOnFinish(@NonNull MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            workoutSwipeInProgress = false;
        }
    }

    private void changeSelectedDateByDays(int delta) {
        if (selectedDate == null) {
            return;
        }
        updateSelectedDate(selectedDate.plusDays(delta), false);
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
        if (!isAdded()) {
            return;
        }
        View root = getView();
        if (root == null) {
            return;
        }
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
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

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showSaveWorkoutDialog() {
        if (!isAdded()) {
            return;
        }
        if (currentExercises == null || currentExercises.isEmpty()) {
            showToast(getString(R.string.workout_export_empty));
            return;
        }

        LocalDate baseDate = selectedDate != null ? selectedDate : LocalDate.now();
        String defaultName = getString(R.string.workout_export_default_name, baseDate);
        EditText input = new EditText(requireContext());
        input.setText(defaultName);
        input.setSelection(defaultName.length());

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.workout_export_title)
                .setView(input)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String name = input.getText() != null ? input.getText().toString() : "";
                    boolean saved = workoutTrackerViewModel.exportWorkoutAsTemplate(name, currentExercises);
                    if (saved) {
                        showToast(getString(R.string.workout_export_success));
                    } else {
                        showToast(getString(R.string.workout_export_failed));
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
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
        boolean selectionMode = workoutAdapter != null && workoutAdapter.isSelectionModeActive();
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
            null
        );
        exercise.setType(ExerciseType.ANAEROBIC);
        exercise.applyDefaultMetricsForType(ExerciseType.ANAEROBIC);
        exercise.setWeightInKg(false);
        return exercise;
    }

    private int findExerciseIndexFromEnd(List<ExerciseInWorkout> exercises, ExerciseInWorkout target) {
        if (exercises == null || target == null) {
            return -1;
        }
        Long targetId = target.getId();
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
        }
        return -1;
    }

    @Override
    public void onSaveWorkoutRequested() {
        showSaveWorkoutDialog();
    }

    @Override
    public boolean isSaveWorkoutVisible() {
        return true;
    }
}
