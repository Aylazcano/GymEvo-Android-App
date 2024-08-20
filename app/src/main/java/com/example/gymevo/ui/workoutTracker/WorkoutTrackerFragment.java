package com.example.gymevo.ui.workoutTracker;

import static java.time.LocalDate.now;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymevo.CalendarAdapter;
import com.example.gymevo.R;
import com.example.gymevo.WorkoutAdapter;
import com.example.gymevo.databinding.FragmentWorkoutTrackerBinding;
import com.example.gymevo.models.ExerciseInWorkout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class WorkoutTrackerFragment extends Fragment implements CalendarAdapter.OnItemListener {

    // Déclaration des variables pour les widgets et les objets
    private FragmentWorkoutTrackerBinding binding;
    private WorkoutTrackerViewModel workoutTrackerViewModel;
    private TextView oneMonthText;
    private TextView oneYearText;
    private RecyclerView calendarRecyclerView;
    private RecyclerView workoutRecyclerView;
    private LocalDate selectedDate;
    private GestureDetector gestureDetector;
    private boolean isMonthView = true; // Indique si la vue actuelle est une vue mensuelle

    // Méthode statique pour créer une nouvelle instance de ce fragment
    public static WorkoutTrackerFragment newInstance() {
        return new WorkoutTrackerFragment();
    }

    // Méthode appelée pour créer la vue du fragment
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Initialiser selectedDate avec la date actuelle avant de l'utiliser ailleurs
        selectedDate = now();

        // Liaison de la vue avec le FragmentWorkoutTrackerBinding
        binding = FragmentWorkoutTrackerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialisation des widgets (TextViews, RecyclerViews, etc.)
        initWidgets(root);

        // Initialisation du ViewModel pour gérer les données de la vue
        workoutTrackerViewModel = new ViewModelProvider(this).get(WorkoutTrackerViewModel.class);

        // Initialisation du RecyclerView pour afficher la liste des exercices
        initWorkoutRecyclerView(selectedDate);

        // Initialisation du GestureDetector pour détecter les gestes de l'utilisateur
        InitGestureDetector();

        // Configuration initiale de la vue en mode mois
        setMonthView();

        return root;
    }

    /**
     * Initialiser le RecyclerView pour afficher la liste des exercices dans l'entraînement.
     */
    private void initWorkoutRecyclerView(LocalDate selectedDate) {
        Context context = getContext();
        if (context == null) return;

        // Créer l'adaptateur avec une liste vide
        WorkoutAdapter workoutAdapter = new WorkoutAdapter(context, new ArrayList<>());

        // Configurer le RecyclerView
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        workoutRecyclerView.setAdapter(workoutAdapter);
        workoutRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Observer les données LiveData pour mettre à jour l'adaptateur
        workoutTrackerViewModel.getExercisesForWorkoutOnDate(selectedDate).observe(getViewLifecycleOwner(), exercisesInWorkout -> {
            if (exercisesInWorkout != null && !exercisesInWorkout.isEmpty()) {
                // Mettre à jour les données de l'adaptateur
                workoutAdapter.setExercises(exercisesInWorkout);
            } else {
                // Gérer le cas où il n'y a pas d'exercices pour la date sélectionnée
                workoutAdapter.setExercises(new ArrayList<>());
            }
        });
    }



    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    @SuppressLint("ClickableViewAccessibility")
    private void InitGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new GestureListener());

        binding.calendarLayout.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true; // Indique que l'événement tactile a été consommé
        });

        calendarRecyclerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true; // Indique que l'événement tactile a été consommé
        });
    }

    // Initialiser les widgets de la vue
    private void initWidgets(View root) {
        calendarRecyclerView = root.findViewById(R.id.calendarRecyclerView);

        //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir
        oneMonthText = root.findViewById(R.id.oneMonthText);
        oneYearText = root.findViewById(R.id.oneYearText);

        // Initialiser le RecyclerView pour afficher les exercices
        workoutRecyclerView = binding.WorkoutRecyclerView;
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Convert a date to a string representing the day of the month
    private String dayFromDateString(LocalDate date) {
        return DateTimeFormatter.ofPattern("dd").format(date);
    }

    // Convertir une date en nom de mois abrégé (ex: "Jan.")
    private String monthFromDateString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM.");
        return date.format(formatter);
    }

    // Convertir une date en année (ex: "2024")
    private String yearFromDateString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return date.format(formatter);
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Générer un tableau contenant les jours d'un mois donné
    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        // Remplir le tableau avec les jours du mois ou des cases vides pour aligner le début du mois
        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Configurer la vue pour afficher le mois sélectionné
    private void setMonthView() {
        if (oneMonthText != null && oneYearText != null) {
            oneMonthText.setText(monthFromDateString(selectedDate));
            oneYearText.setText(yearFromDateString(selectedDate));
            ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

            CalendarAdapter calendarAdapter = new CalendarAdapter(getContext(), daysInMonth, this);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext().getApplicationContext(), 7);
            calendarRecyclerView.setLayoutManager(layoutManager);
            calendarRecyclerView.setAdapter(calendarAdapter);

            // Configurer l'animateur d'éléments pour des animations fluides
            calendarRecyclerView.setItemAnimator(new DefaultItemAnimator());

            // Définir la hauteur du cadre du calendrier à 240dp
            ViewGroup.LayoutParams params = calendarRecyclerView.getLayoutParams();
            params.height = (int) (240 * getResources().getDisplayMetrics().density);
            calendarRecyclerView.setLayoutParams(params);

            isMonthView = true; // Indiquer que la vue actuelle est une vue mensuelle
        } else {
            // Gérer le cas où les TextViews ne sont pas initialisés
            Toast.makeText(getContext(), "Error: TextViews not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Générer un tableau contenant les jours de la semaine sélectionnée
    private ArrayList<String> daysInWeekArray(LocalDate date) {
        ArrayList<String> daysInWeekArray = new ArrayList<>();
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);

        for (int i = 0; i < 7; i++) {
            daysInWeekArray.add(String.valueOf(startOfWeek.plusDays(i).getDayOfMonth()));
        }
        return daysInWeekArray;
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Configurer la vue pour afficher la semaine sélectionnée
    private void setWeekView() {
        if (oneMonthText != null && oneYearText != null) {
            oneMonthText.setText(monthFromDateString(selectedDate));
            oneYearText.setText(yearFromDateString(selectedDate));
            ArrayList<String> daysInWeek = daysInWeekArray(selectedDate);

            CalendarAdapter calendarAdapter = new CalendarAdapter(getContext(), daysInWeek, this);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext().getApplicationContext(), 7);
            calendarRecyclerView.setLayoutManager(layoutManager);
            calendarRecyclerView.setAdapter(calendarAdapter);

            // Configurer l'animateur d'éléments pour des animations fluides
            calendarRecyclerView.setItemAnimator(new DefaultItemAnimator());

            // Ajuster la hauteur pour la vue de la semaine
            ViewGroup.LayoutParams params = calendarRecyclerView.getLayoutParams();
            params.height = (int) (40 * getResources().getDisplayMetrics().density);
            calendarRecyclerView.setLayoutParams(params);

            isMonthView = false; // Indiquer que la vue actuelle est une vue hebdomadaire
        } else {
            // Gérer le cas où les TextViews ne sont pas initialisés
            Toast.makeText(getContext(), "Error: TextViews not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Gérer la sélection d'un jour dans le calendrier
    @Override
    public void onItemClick(int position, @NonNull String dayText) {
        if (dayText.isEmpty()) return;

        int day = Integer.parseInt(dayText);
        int month = selectedDate.getMonthValue();
        int year = selectedDate.getYear();
        selectedDate = LocalDate.of(year, month, day);

        String message = String.format("Selected date %s %s %d", dayFromDateString(selectedDate),
                monthFromDateString(selectedDate), year);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

        initWorkoutRecyclerView(selectedDate);
    }


    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Gérer l'action de swipe gauche pour afficher la semaine suivante
    private void nextWeekAction() {
        selectedDate = selectedDate.plusWeeks(1);
        setWeekView();
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Gérer l'action de swipe droite pour afficher la semaine précédente
    private void previousWeekAction() {
        selectedDate = selectedDate.minusWeeks(1);
        setWeekView();
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Gérer l'action de swipe gauche pour afficher le mois suivant
    private void nextMonthAction() {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Gérer l'action de swipe droite pour afficher le mois précédent
    private void previousMonthAction() {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Classe interne pour gérer les gestes de l'utilisateur
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    return true;
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeDown();
                    } else {
                        onSwipeUp();
                    }
                    return true;
                }
            }
            return false;
        }
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Méthode appelée lors d'un swipe vers le haut
    private void onSwipeUp() {
        if (isMonthView) {
            Toast.makeText(getContext(), "Swipe Up - Switch to Week View", Toast.LENGTH_SHORT).show();
            setWeekView();
        }
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Méthode appelée lors d'un swipe vers le bas
    private void onSwipeDown() {
        if (!isMonthView) {
            Toast.makeText(getContext(), "Swipe Down - Switch to Month View", Toast.LENGTH_SHORT).show();
            setMonthView();
        }
    }
    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Méthode appelée lors d'un swipe à gauche
    private void onSwipeLeft() {
        if (isMonthView) {
            nextMonthAction(); // Passer au mois suivant si on est en vue mensuelle
        } else {
            nextWeekAction(); // Passer à la semaine suivante si on est en vue hebdomadaire
        }
    }

    //TODO (OPTIMAZATION): DEPLACER DANS CalendarViewHolder ou CalendarAdapter a voir

    // Méthode appelée lors d'un swipe à droite
    private void onSwipeRight() {
        if (isMonthView) {
            previousMonthAction(); // Revenir au mois précédent si on est en vue mensuelle
        } else {
            previousWeekAction(); // Revenir à la semaine précédente si on est en vue hebdomadaire
        }
    }

}
