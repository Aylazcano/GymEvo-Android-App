package com.example.gymevo.ui.statistics;

import android.app.Application;

import androidx.annotation.NonNull;

import com.example.gymevo.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsUiModel {

    private final String dateRange;
    private final String totalSessions;
    private final String totalExercises;
    private final String totalSets;
    private final String totalReps;
    private final String totalVolume;
    private final String totalTime;
    private final String averageHeartRate;
    private final String best1RM;
    private final String uniqueExercises;
    private final String avgVolumePerSession;
    private final String currentStreak;
    private final String bestStreak;
    private final List<StatisticsRow> topExercises;
    private final List<StatisticsRow> topMuscleGroups;
    private final List<StatisticsRow> personalRecords;
    private final List<WeeklyFrequencyItem> weeklyFrequency;
    private final Map<String, Integer> muscleVolumeMap;

    public StatisticsUiModel(String dateRange,
                             String totalSessions,
                             String totalExercises,
                             String totalSets,
                             String totalReps,
                             String totalVolume,
                             String totalTime,
                             String averageHeartRate,
                             String best1RM,
                             String uniqueExercises,
                             String avgVolumePerSession,
                             String currentStreak,
                             String bestStreak,
                             List<StatisticsRow> topExercises,
                             List<StatisticsRow> topMuscleGroups,
                             List<StatisticsRow> personalRecords,
                             List<WeeklyFrequencyItem> weeklyFrequency,
                             Map<String, Integer> muscleVolumeMap) {
        this.dateRange = dateRange;
        this.totalSessions = totalSessions;
        this.totalExercises = totalExercises;
        this.totalSets = totalSets;
        this.totalReps = totalReps;
        this.totalVolume = totalVolume;
        this.totalTime = totalTime;
        this.averageHeartRate = averageHeartRate;
        this.best1RM = best1RM;
        this.uniqueExercises = uniqueExercises;
        this.avgVolumePerSession = avgVolumePerSession;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
        this.topExercises = topExercises != null ? topExercises : Collections.emptyList();
        this.topMuscleGroups = topMuscleGroups != null ? topMuscleGroups : Collections.emptyList();
        this.personalRecords = personalRecords != null ? personalRecords : Collections.emptyList();
        this.weeklyFrequency = weeklyFrequency != null ? weeklyFrequency : Collections.emptyList();
        this.muscleVolumeMap = muscleVolumeMap != null ? muscleVolumeMap : Collections.emptyMap();
    }

    public static StatisticsUiModel empty(@NonNull Application application) {
        String placeholder = application.getString(R.string.stats_value_placeholder);
        return new StatisticsUiModel(
            application.getString(R.string.stats_date_range_placeholder),
            "0",
            "0",
            "0",
            "0",
            "0 lb",
            placeholder,
            placeholder,
            placeholder,
            "0",
            "0 lb",
            placeholder,
            placeholder,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyMap()
        );
    }

    public String getDateRange() {
        return dateRange;
    }

    public String getTotalSessions() {
        return totalSessions;
    }

    public String getTotalExercises() {
        return totalExercises;
    }

    public String getTotalSets() {
        return totalSets;
    }

    public String getTotalReps() {
        return totalReps;
    }

    public String getTotalVolume() {
        return totalVolume;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public String getAverageHeartRate() {
        return averageHeartRate;
    }

    public String getBest1RM() {
        return best1RM;
    }

    public String getUniqueExercises() {
        return uniqueExercises;
    }

    public String getAvgVolumePerSession() {
        return avgVolumePerSession;
    }

    public String getCurrentStreak() {
        return currentStreak;
    }

    public String getBestStreak() {
        return bestStreak;
    }

    public List<StatisticsRow> getTopExercises() {
        return topExercises;
    }

    public List<StatisticsRow> getTopMuscleGroups() {
        return topMuscleGroups;
    }

    public List<StatisticsRow> getPersonalRecords() {
        return personalRecords;
    }

    public List<WeeklyFrequencyItem> getWeeklyFrequency() {
        return weeklyFrequency;
    }

    public Map<String, Integer> getMuscleVolumeMap() {
        return muscleVolumeMap;
    }
}
