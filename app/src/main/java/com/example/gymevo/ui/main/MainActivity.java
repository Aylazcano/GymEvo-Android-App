package com.example.gymevo.ui.main;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.lifecycle.SavedStateHandle;

import com.example.gymevo.R;
import com.example.gymevo.databinding.ActivityMainBinding;
import com.example.gymevo.ui.exercisesList.ExerciseListFragment;
import com.example.gymevo.ui.workoutTracker.WorkoutTrackerFragment;
import com.example.gymevo.ui.workoutsList.WorkoutListFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        initNavigation();

        binding.appBarMain.fab.setOnClickListener(this::handleFabClick);
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
                R.id.nav_workout_list)
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
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
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
