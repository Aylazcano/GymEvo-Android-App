package com.example.gymevo.ui.workoutTracker;

import static java.time.LocalDate.now;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymevo.CalendarAdapater;
import com.example.gymevo.R;
import com.example.gymevo.databinding.FragmentWorkoutTrackerBinding;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class WorkoutTrackerFragment extends Fragment implements CalendarAdapater.OnItemListener {

    private FragmentWorkoutTrackerBinding binding;
    private WorkoutTrackerViewModel mViewModel;
    private TextView oneMonthText;
    private TextView oneYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;

    public static WorkoutTrackerFragment newInstance() {
        return new WorkoutTrackerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutTrackerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize widgets
        initWidgets(root);

        selectedDate = LocalDate.now();
        setMonthView();

        WorkoutTrackerViewModel workoutTrackerVM =
                new ViewModelProvider(this).get(WorkoutTrackerViewModel.class);

        return root;
    }

    private void initWidgets(View root) {
        calendarRecyclerView = root.findViewById(R.id.calendarMonthRecyclerView);
        oneMonthText = root.findViewById(R.id.oneMonthText);
        oneYearText = root.findViewById(R.id.oneYearText);
    }

    private void setMonthView() {
        if (oneMonthText != null && oneYearText != null) {
            oneMonthText.setText(monthFromDate(selectedDate));
            oneYearText.setText(yearFromDate(selectedDate));
            ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

            CalendarAdapater calendarAdapater = new CalendarAdapater(daysInMonth, this);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext().getApplicationContext(), 7);
            calendarRecyclerView.setLayoutManager(layoutManager);
            calendarRecyclerView.setAdapter(calendarAdapater);
        } else {
            // Handle case when TextViews are null
            Toast.makeText(getContext(), "Error: TextViews not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    private String monthFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM.");
        return date.format(formatter);
    }

    private String yearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return date.format(formatter);
    }

    public void previousMonthAction(View view) {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WorkoutTrackerViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            String message = "Selected date " + dayText + " " + monthFromDate(selectedDate) + " "
                    + yearFromDate(selectedDate);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
