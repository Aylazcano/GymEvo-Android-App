package com.example.gymevo.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gymevo.R;
import com.example.gymevo.databinding.FragmentStatisticsBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private static final int RANGE_4 = 4;
    private static final int RANGE_8 = 8;
    private static final int RANGE_12 = 12;

    private FragmentStatisticsBinding binding;
    private List<WeeklyFrequencyItem> weeklyItems = new ArrayList<>();
    private int weeklyRange = RANGE_12;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StatisticsViewModel viewModel =
            new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupWeeklyRangeSelector();
        configureWeeklyTrendChart();

        viewModel.getStatistics().observe(getViewLifecycleOwner(), this::bindStatistics);
        return root;
    }

    private void bindStatistics(StatisticsUiModel model) {
        if (binding == null || model == null) {
            return;
        }

        binding.statsDateRange.setText(model.getDateRange());

        binding.statSessionsValue.setText(model.getTotalSessions());
        binding.statExercisesValue.setText(model.getTotalExercises());
        binding.statSetsValue.setText(model.getTotalSets());
        binding.statRepsValue.setText(model.getTotalReps());
        binding.statVolumeValue.setText(model.getTotalVolume());
        binding.statTimeValue.setText(model.getTotalTime());
        binding.statAvgHrValue.setText(model.getAverageHeartRate());
        binding.statBest1rmValue.setText(model.getBest1RM());
        binding.statUniqueExercisesValue.setText(model.getUniqueExercises());
        binding.statAvgVolumeValue.setText(model.getAvgVolumePerSession());
        binding.statCurrentStreakValue.setText(model.getCurrentStreak());
        binding.statBestStreakValue.setText(model.getBestStreak());

        updateStatRows(binding.topExercisesContainer, model.getTopExercises());
        updateStatRows(binding.topMuscleGroupsContainer, model.getTopMuscleGroups());
        updateStatRows(binding.topPrsContainer, model.getPersonalRecords());
        weeklyItems = model.getWeeklyFrequency();
        renderWeeklyFrequency();
    }

    private void setupWeeklyRangeSelector() {
        if (binding == null) {
            return;
        }
        binding.weeklyRangeToggle.check(R.id.weekly_range_12);
        binding.weeklyRangeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.weekly_range_4) {
                weeklyRange = RANGE_4;
            } else if (checkedId == R.id.weekly_range_8) {
                weeklyRange = RANGE_8;
            } else {
                weeklyRange = RANGE_12;
            }
            renderWeeklyFrequency();
        });
    }

    private void configureWeeklyTrendChart() {
        if (binding == null) {
            return;
        }
        binding.weeklyTrendChart.getDescription().setEnabled(false);
        binding.weeklyTrendChart.getLegend().setEnabled(false);
        binding.weeklyTrendChart.setTouchEnabled(false);
        binding.weeklyTrendChart.setPinchZoom(false);
        binding.weeklyTrendChart.setScaleEnabled(false);

        XAxis xAxis = binding.weeklyTrendChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        YAxis left = binding.weeklyTrendChart.getAxisLeft();
        left.setAxisMinimum(0f);
        left.setGranularity(1f);
        left.setDrawGridLines(true);
        binding.weeklyTrendChart.getAxisRight().setEnabled(false);
    }

    private void renderWeeklyFrequency() {
        List<WeeklyFrequencyItem> slice = getWeeklySlice();
        updateWeeklyFrequency(binding.weeklyFrequencyContainer, slice);
        updateWeeklyTrendChart(slice);
    }

    private List<WeeklyFrequencyItem> getWeeklySlice() {
        if (weeklyItems == null || weeklyItems.isEmpty()) {
            return Collections.emptyList();
        }
        int size = weeklyItems.size();
        int start = Math.max(0, size - weeklyRange);
        return weeklyItems.subList(start, size);
    }

    private void updateStatRows(LinearLayout container, java.util.List<StatisticsRow> rows) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        if (rows == null || rows.isEmpty()) {
            container.addView(createEmptyRow(container));
            return;
        }

        for (StatisticsRow row : rows) {
            View view = inflater.inflate(R.layout.item_stat_row, container, false);
            TextView title = view.findViewById(R.id.stat_row_title);
            TextView value = view.findViewById(R.id.stat_row_value);
            TextView subtitle = view.findViewById(R.id.stat_row_subtitle);
            title.setText(row.getTitle());
            value.setText(row.getValue());
            subtitle.setText(row.getSubtitle());
            container.addView(view);
        }
    }

    private void updateWeeklyFrequency(LinearLayout container,
                                       java.util.List<WeeklyFrequencyItem> items) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        if (items == null || items.isEmpty()) {
            container.addView(createEmptyRow(container));
            return;
        }

        int maxCount = 0;
        for (WeeklyFrequencyItem item : items) {
            if (item != null && item.getCount() > maxCount) {
                maxCount = item.getCount();
            }
        }

        int maxBarHeight = dpToPx(120);
        int minBarHeight = dpToPx(12);

        for (WeeklyFrequencyItem item : items) {
            if (item == null) {
                continue;
            }
            View view = inflater.inflate(R.layout.item_weekly_bar, container, false);
            TextView label = view.findViewById(R.id.weekly_bar_label);
            TextView value = view.findViewById(R.id.weekly_bar_value);
            View bar = view.findViewById(R.id.weekly_bar_fill);

            label.setText(item.getLabel());
            value.setText(String.valueOf(item.getCount()));

            int height = minBarHeight;
            if (maxCount > 0) {
                height = minBarHeight + Math.round((maxBarHeight - minBarHeight)
                    * (item.getCount() / (float) maxCount));
            }
            ViewGroup.LayoutParams params = bar.getLayoutParams();
            params.height = height;
            bar.setLayoutParams(params);

            container.addView(view);
        }
    }

    private void updateWeeklyTrendChart(List<WeeklyFrequencyItem> items) {
        if (binding == null) {
            return;
        }
        if (items == null || items.isEmpty()) {
            binding.weeklyTrendChart.clear();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            WeeklyFrequencyItem item = items.get(i);
            if (item == null) {
                continue;
            }
            entries.add(new Entry(i, item.getCount()));
            labels.add(item.getLabel());
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.stats_weekly_frequency_title));
        dataSet.setColor(getResources().getColor(R.color.purple_500, null));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(getResources().getColor(R.color.purple_700, null));
        dataSet.setCircleRadius(3.5f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        binding.weeklyTrendChart.setData(lineData);

        XAxis xAxis = binding.weeklyTrendChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-35f);
        xAxis.setLabelCount(Math.min(labels.size(), 6), false);

        binding.weeklyTrendChart.invalidate();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private TextView createEmptyRow(ViewGroup container) {
        TextView empty = new TextView(container.getContext());
        empty.setText(getString(R.string.stats_empty_list));
        empty.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2);
        return empty;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}