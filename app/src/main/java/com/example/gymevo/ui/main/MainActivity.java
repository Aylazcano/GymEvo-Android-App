package com.example.gymevo.ui.main;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.lifecycle.SavedStateHandle;
import androidx.core.view.GravityCompat;

import com.example.gymevo.R;
import com.example.gymevo.databinding.ActivityMainBinding;
import com.example.gymevo.databinding.ViewSelectionBarBinding;
import com.example.gymevo.data.repository.UserPreferencesRepository;
import com.example.gymevo.ui.exercisesList.ExerciseListFragment;
import com.example.gymevo.ui.common.ThemeUtils;
import com.example.gymevo.ui.workoutTracker.WorkoutTrackerFragment;
import com.example.gymevo.ui.workoutsList.WorkoutListFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;
    private ViewSelectionBarBinding selectionBarBinding;
    private MenuItem searchMenuItem;
    private SearchView searchView;
    private boolean isSearchExpanded;
    private boolean isUpdatingSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserTheme();
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        selectionBarBinding = binding.appBarMain.selectionBar;
        hideSelectionBar();

        initBackNavigation();

        initNavigation();

        binding.appBarMain.fab.setOnClickListener(this::handleFabClick);
    }

    public void showSelectionBar(int selectedCount,
                                 View.OnClickListener moveUp,
                                 View.OnClickListener moveDown,
                                 View.OnClickListener deleteAction) {
        if (selectionBarBinding == null) {
            return;
        }
        binding.appBarMain.toolbar.setVisibility(View.GONE);
        selectionBarBinding.getRoot().setVisibility(View.VISIBLE);
        selectionBarBinding.textSelectionCount.setText(getString(R.string.selection_count, selectedCount));
        selectionBarBinding.buttonBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        selectionBarBinding.buttonMoveUp.setOnClickListener(moveUp);
        selectionBarBinding.buttonMoveDown.setOnClickListener(moveDown);
        selectionBarBinding.buttonDelete.setOnClickListener(deleteAction);
    }

    public void hideSelectionBar() {
        if (selectionBarBinding == null) {
            return;
        }
        selectionBarBinding.getRoot().setVisibility(View.GONE);
        binding.appBarMain.toolbar.setVisibility(View.VISIBLE);
    }

    private void handleFabClick(View view) {
        Integer destinationId = getCurrentDestinationId();
        if (destinationId == null) {
            return;
        }
        if (destinationId == R.id.nav_workout_tracker) {
            requestAddExerciseInTracker();
            return;
        }
        if (destinationId == R.id.nav_exercises_list) {
            requestAddExercise();
            return;
        }
        if (destinationId == R.id.nav_workout_list) {
            requestAddWorkout();
            return;
        }
        showUnsupportedAction(view);
    }

    private void initNavigation() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_workout_tracker,
                R.id.nav_statistics,
                R.id.nav_exercises_list,
            R.id.nav_workout_list,
            R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            invalidateOptionsMenu();
            if (!isHeaderActionsSupported()) {
                collapseSearchIfExpanded();
            }
        });
    }

    private void requestAddExercise() {
        setSavedStateFlag(ExerciseListFragment.ARG_OPEN_ADD_EXERCISE);
    }

    private void requestAddExerciseInTracker() {
        setSavedStateFlag(WorkoutTrackerFragment.ARG_OPEN_ADD_EXERCISE);
    }

    private void requestAddWorkout() {
        setSavedStateFlag(WorkoutListFragment.ARG_OPEN_ADD_WORKOUT);
    }

    private void setSavedStateFlag(String key) {
        SavedStateHandle handle = getSavedStateHandle();
        if (handle != null) {
            handle.set(key, true);
        }
    }

    private Integer getCurrentDestinationId() {
        return navController.getCurrentDestination() != null
                ? navController.getCurrentDestination().getId()
                : null;
    }

    private SavedStateHandle getSavedStateHandle() {
        return navController.getCurrentBackStackEntry() != null
                ? navController.getCurrentBackStackEntry().getSavedStateHandle()
                : null;
    }

    private void showUnsupportedAction(View view) {
        Snackbar.make(view, R.string.fab_action_not_supported, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        if (searchMenuItem != null) {
            searchView = (SearchView) searchMenuItem.getActionView();
            configureSearchView();
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean supported = isHeaderActionsSupported();
        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem starItem = menu.findItem(R.id.action_star);
        MenuItem filterItem = menu.findItem(R.id.action_filter);
        MenuItem sortItem = menu.findItem(R.id.action_sort);
        MenuItem saveWorkoutItem = menu.findItem(R.id.action_save_workout);

        if (searchItem != null) {
            boolean keepVisible = supported && (!isSearchExpanded || searchItem.isActionViewExpanded());
            searchItem.setVisible(keepVisible);
        }
        if (starItem != null) starItem.setVisible(supported && !isSearchExpanded);
        if (filterItem != null) filterItem.setVisible(supported);
        if (sortItem != null) sortItem.setVisible(supported);

        WorkoutTrackerMenuDelegate trackerDelegate = getWorkoutTrackerMenuDelegate();
        if (saveWorkoutItem != null) {
            boolean visible = trackerDelegate != null && trackerDelegate.isSaveWorkoutVisible();
            saveWorkoutItem.setVisible(visible);
        }

        MainHeaderDelegate delegate = getHeaderDelegate();
        if (supported && delegate != null) {
            updateSearchHint(delegate.getSearchHintResId());
            syncSearchQuery(delegate.getSearchQuery());
            updateStarIcon(starItem, delegate.isStarPriorityEnabled());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainHeaderDelegate delegate = getHeaderDelegate();
        WorkoutTrackerMenuDelegate trackerDelegate = getWorkoutTrackerMenuDelegate();
        if (item.getItemId() == R.id.action_settings && navController != null) {
            navController.navigate(R.id.nav_settings);
            return true;
        }
        if (delegate != null) {
            int id = item.getItemId();
            if (id == R.id.action_star) {
                delegate.onStarToggleRequested();
                updateStarIcon(item, delegate.isStarPriorityEnabled());
                return true;
            }
            if (id == R.id.action_filter) {
                delegate.onFilterRequested();
                return true;
            }
            if (id == R.id.action_sort) {
                delegate.onSortRequested();
                return true;
            }
        }
        if (trackerDelegate != null && item.getItemId() == R.id.action_save_workout) {
            trackerDelegate.onSaveWorkoutRequested();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        BackPressDelegate backPressDelegate = getBackPressDelegate();
        if (backPressDelegate != null && backPressDelegate.onBackPressed()) {
            return true;
        }
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void initBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (collapseSearchIfExpanded()) {
                    return;
                }
                DrawerLayout drawer = binding.drawerLayout;
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                    return;
                }
                BackPressDelegate backPressDelegate = getBackPressDelegate();
                if (backPressDelegate != null && backPressDelegate.onBackPressed()) {
                    return;
                }
                setEnabled(false);
                try {
                    getOnBackPressedDispatcher().onBackPressed();
                } finally {
                    setEnabled(true);
                }
            }
        });
    }

    private void applyUserTheme() {
        UserPreferencesRepository repository = UserPreferencesRepository.getInstance(this);
        String themeName = repository.getThemeNameImmediate();
        setTheme(ThemeUtils.getThemeResId(themeName));
    }

    private void configureSearchView() {
        if (searchView == null || searchMenuItem == null) {
            return;
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                notifySearchQueryChanged(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                notifySearchQueryChanged(newText);
                return true;
            }
        });

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                enterSearchMode();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                exitSearchMode();
                notifySearchClosed();
                return true;
            }
        });
    }

    private void enterSearchMode() {
        isSearchExpanded = true;
        binding.appBarMain.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
        binding.appBarMain.toolbar.setNavigationOnClickListener(v -> collapseSearchIfExpanded());
    }

    private void exitSearchMode() {
        isSearchExpanded = false;
        binding.appBarMain.toolbar.setNavigationOnClickListener(null);
        binding.appBarMain.toolbar.setNavigationIcon(null);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        binding.appBarMain.toolbar.setNavigationOnClickListener(v -> onSupportNavigateUp());
        invalidateOptionsMenu();
    }

    private boolean collapseSearchIfExpanded() {
        if (searchMenuItem != null && isSearchExpanded) {
            searchMenuItem.collapseActionView();
            return true;
        }
        return false;
    }

    private void notifySearchQueryChanged(String query) {
        if (isUpdatingSearch) {
            return;
        }
        MainHeaderDelegate delegate = getHeaderDelegate();
        if (delegate != null) {
            delegate.onSearchQueryChanged(query != null ? query : "");
        }
    }

    private void notifySearchClosed() {
        MainHeaderDelegate delegate = getHeaderDelegate();
        if (delegate != null) {
            delegate.onSearchClosed();
        }
    }

    private void updateSearchHint(int hintResId) {
        if (searchView != null) {
            searchView.setQueryHint(getString(hintResId));
        }
    }

    private void syncSearchQuery(String query) {
        if (searchView == null) {
            return;
        }
        String safe = query != null ? query : "";
        isUpdatingSearch = true;
        searchView.setQuery(safe, false);
        isUpdatingSearch = false;
    }

    private void updateStarIcon(@Nullable MenuItem item, boolean enabled) {
        if (item == null) {
            return;
        }
        int icon = enabled
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off;
        item.setIcon(icon);
    }

    private boolean isHeaderActionsSupported() {
        Integer destinationId = getCurrentDestinationId();
        if (destinationId == null) {
            return false;
        }
        return destinationId == R.id.nav_exercises_list
                || destinationId == R.id.nav_workout_list;
    }

    @Nullable
    private MainHeaderDelegate getHeaderDelegate() {
        Fragment fragment = getCurrentPrimaryFragment();
        if (fragment instanceof MainHeaderDelegate) {
            return (MainHeaderDelegate) fragment;
        }
        return null;
    }

    @Nullable
    private WorkoutTrackerMenuDelegate getWorkoutTrackerMenuDelegate() {
        Fragment fragment = getCurrentPrimaryFragment();
        if (fragment instanceof WorkoutTrackerMenuDelegate) {
            return (WorkoutTrackerMenuDelegate) fragment;
        }
        return null;
    }

    @Nullable
    private Fragment getCurrentPrimaryFragment() {
        Fragment host = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (host == null) {
            return null;
        }
        Fragment primary = host.getChildFragmentManager().getPrimaryNavigationFragment();
        if (primary != null) {
            return primary;
        }
        for (Fragment fragment : host.getChildFragmentManager().getFragments()) {
            if (fragment != null && fragment.isVisible()) {
                return fragment;
            }
        }
        return null;
    }

    @Nullable
    private BackPressDelegate getBackPressDelegate() {
        Fragment fragment = getCurrentPrimaryFragment();
        if (fragment instanceof BackPressDelegate) {
            return (BackPressDelegate) fragment;
        }
        return null;
    }

    public interface MainHeaderDelegate {
        void onSearchQueryChanged(String query);

        void onSearchClosed();

        void onFilterRequested();

        void onSortRequested();

        void onStarToggleRequested();

        boolean isStarPriorityEnabled();

        String getSearchQuery();

        int getSearchHintResId();
    }

    public interface WorkoutTrackerMenuDelegate {
        void onSaveWorkoutRequested();

        boolean isSaveWorkoutVisible();
    }

    public interface BackPressDelegate {
        boolean onBackPressed();
    }
}
