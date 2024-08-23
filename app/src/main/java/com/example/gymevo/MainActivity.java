package com.example.gymevo;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.gymevo.databinding.ActivityMainBinding;
import com.example.gymevo.ui.workoutCreation.WorkoutCreationFragment;

import java.time.LocalDate;

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

        // Initialize NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Initialize AppBarConfiguration before using it
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_workout_tracker, R.id.nav_statistics, R.id.nav_exercices_list, R.id.nav_change_user,
                R.id.nav_exercice_creation, R.id.nav_workout_creation)
                .setOpenableLayout(drawer)
                .build();

        // Set up ActionBar with NavController
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentFragment = navController.getCurrentDestination().getId();

                // Check if the current fragment is WorkoutTrackerFragment
                if (currentFragment == R.id.nav_workout_tracker) {
                    // Clear the back stack
                    navController.popBackStack(R.id.nav_workout_tracker, true);

                    // Navigate to WorkoutCreationFragment
                    WorkoutCreationFragment workoutCreationFragment = WorkoutCreationFragment.newInstance(LocalDate.now());
                    navController.navigate(R.id.nav_workout_creation, null, new NavOptions.Builder().setPopUpTo(R.id.nav_workout_tracker, true).build());
                }
                // Check if the current fragment is WorkoutCreationFragment
                else if (currentFragment == R.id.nav_workout_creation) {
                    // Clear the back stack
                    navController.popBackStack(R.id.nav_workout_creation, true);

                    // Navigate to ExeciseInWorkoutCreationFragment
                    navController.navigate(R.id.nav_exercise_in_workout_creation, null, new NavOptions.Builder().setPopUpTo(R.id.nav_workout_creation, true).build());
                }
                else {
                    Snackbar.make(view, "Current fragment is not WorkoutTrackerFragment", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
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
