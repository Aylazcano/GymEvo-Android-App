package com.example.gymevo.ui.statistics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.gymevo.R;
import com.example.gymevo.data.repository.WorkoutRepository;
import com.example.gymevo.model.ExerciseInWorkout;
import com.example.gymevo.model.WorkoutWithExercises;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StatisticsViewModel extends AndroidViewModel {

    private static class ExerciseAggregate {
        String name;
        int volume;
        float maxWeight;
        int best1RM;
        float bestWeight;
        int bestReps;
    }

    private final WorkoutRepository repository;
    private final MediatorLiveData<StatisticsUiModel> statisticsLiveData = new MediatorLiveData<>();

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        statisticsLiveData.addSource(repository.getSessionWorkouts(), this::computeStatistics);
    }

    public LiveData<StatisticsUiModel> getStatistics() {
        return statisticsLiveData;
    }

    private void computeStatistics(List<WorkoutWithExercises> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            statisticsLiveData.setValue(StatisticsUiModel.empty(getApplication()));
            return;
        }

        String unknownExerciseLabel = getApplication().getString(R.string.stats_unknown_exercise);
        String unknownMuscleLabel = getApplication().getString(R.string.stats_unknown_muscle);
        String placeholder = getApplication().getString(R.string.stats_value_placeholder);

        int totalSessions = sessions.size();
        int totalExercises = 0;
        int totalSets = 0;
        int totalReps = 0;
        int totalVolume = 0;
        int totalTimeSeconds = 0;
        int totalHeartRate = 0;
        int heartRateCount = 0;
        float maxWeight = 0;
        int best1RM = 0;

        Set<String> uniqueExercises = new HashSet<>();
        Set<LocalDate> sessionDates = new HashSet<>();

        Map<String, ExerciseAggregate> exercises = new HashMap<>();
        Map<String, Integer> muscleVolume = new HashMap<>();

        for (WorkoutWithExercises workoutWithExercises : sessions) {
            if (workoutWithExercises == null || workoutWithExercises.workout == null) {
                continue;
            }
            LocalDate date = workoutWithExercises.workout.getDate();
            if (date != null) {
                sessionDates.add(date);
            }

            List<ExerciseInWorkout> exerciseList = workoutWithExercises.exercises;
            if (exerciseList == null) {
                continue;
            }

            totalExercises += exerciseList.size();
            for (ExerciseInWorkout exercise : exerciseList) {
                if (exercise == null) {
                    continue;
                }
                int series = safeInt(exercise.getSeries());
                int reps = safeInt(exercise.getRepetitions());
                float weight = safeFloat(exercise.getWeight());
                int time = safeInt(exercise.getTime());
                int heartRate = safeInt(exercise.getHeartRates());

                totalSets += series;
                totalReps += series * reps;
                int volume = (int) (series * reps * weight);
                totalVolume += volume;
                totalTimeSeconds += time;

                if (heartRate > 0) {
                    totalHeartRate += heartRate;
                    heartRateCount += 1;
                }

                if (weight > maxWeight) {
                    maxWeight = weight;
                }

                int estimated1RM = estimate1RM(weight, reps);
                if (estimated1RM > best1RM) {
                    best1RM = estimated1RM;
                }

                String exerciseKey = buildExerciseKey(exercise);
                uniqueExercises.add(exerciseKey);
                ExerciseAggregate aggregate = exercises.get(exerciseKey);
                if (aggregate == null) {
                    aggregate = new ExerciseAggregate();
                    aggregate.name = getExerciseName(exercise, unknownExerciseLabel);
                    exercises.put(exerciseKey, aggregate);
                }

                aggregate.volume += volume;
                aggregate.maxWeight = Math.max(aggregate.maxWeight, weight);
                if (estimated1RM > aggregate.best1RM) {
                    aggregate.best1RM = estimated1RM;
                    aggregate.bestWeight = weight;
                    aggregate.bestReps = reps;
                }

                String muscleGroup = getMuscleLabel(exercise, unknownMuscleLabel);
                muscleVolume.put(muscleGroup,
                    muscleVolume.getOrDefault(muscleGroup, 0) + volume);
            }
        }

        DateRange range = computeDateRange(sessionDates);
        Streak streak = computeStreak(sessionDates);

        NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
        String dateRangeLabel = formatDateRange(range);

        String avgHeartRateLabel = heartRateCount > 0
            ? numberFormat.format(Math.round((float) totalHeartRate / heartRateCount)) + " bpm"
            : placeholder;

        String best1RMLabel = best1RM > 0
            ? numberFormat.format(best1RM) + " lb"
            : placeholder;

        int avgVolume = totalSessions > 0 ? Math.round((float) totalVolume / totalSessions) : 0;

        StatisticsUiModel uiModel = new StatisticsUiModel(
            dateRangeLabel,
            numberFormat.format(totalSessions),
            numberFormat.format(totalExercises),
            numberFormat.format(totalSets),
            numberFormat.format(totalReps),
            numberFormat.format(totalVolume) + " lb",
            formatDuration(totalTimeSeconds),
            avgHeartRateLabel,
            best1RMLabel,
            numberFormat.format(uniqueExercises.size()),
            numberFormat.format(avgVolume) + " lb",
            formatStreak(streak.current),
            formatStreak(streak.best),
            buildTopExercises(exercises, numberFormat),
            buildTopMuscles(muscleVolume, totalVolume, numberFormat),
            buildPersonalRecords(exercises, numberFormat),
            buildWeeklyFrequency(sessionDates),
            new HashMap<>(muscleVolume)
        );

        statisticsLiveData.setValue(uiModel);
    }

    private static String buildExerciseKey(ExerciseInWorkout exercise) {
        Long id = exercise.getExerciseId();
        if (id != null) {
            return "id:" + id;
        }
        String name = exercise.getName();
        return name != null ? "name:" + name : "unknown";
    }

    private int estimate1RM(float weight, int reps) {
        if (weight <= 0 || reps <= 0) {
            return 0;
        }
        return Math.round(weight * (1f + reps / 30f));
    }

    private static int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private static float safeFloat(Float value) {
        return value != null ? value : 0f;
    }

    private static String formatWeight(float weight) {
        if (weight == (int) weight) {
            return String.valueOf((int) weight);
        }
        return String.format(Locale.US, "%.1f", weight);
    }

    private String getExerciseName(ExerciseInWorkout exercise, String fallback) {
        if (exercise == null) {
            return fallback;
        }
        String name = exercise.getName();
        return name != null && !name.trim().isEmpty() ? name : fallback;
    }

    private String getMuscleLabel(ExerciseInWorkout exercise, String fallback) {
        if (exercise == null) {
            return fallback;
        }
        String muscle = exercise.getTargetedMusclesLabel();
        return muscle != null && !muscle.trim().isEmpty() ? muscle : fallback;
    }

    private String formatDateRange(DateRange range) {
        if (range == null || range.start == null || range.end == null) {
            return getApplication().getString(R.string.stats_date_range_placeholder);
        }
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault());
        String start = range.start.format(formatter);
        String end = range.end.format(formatter);
        if (range.start.equals(range.end)) {
            return start;
        }
        return start + " – " + end;
    }

    private static class DateRange {
        LocalDate start;
        LocalDate end;
    }

    private DateRange computeDateRange(Set<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return null;
        }
        List<LocalDate> sorted = new ArrayList<>(dates);
        Collections.sort(sorted);
        DateRange range = new DateRange();
        range.start = sorted.get(0);
        range.end = sorted.get(sorted.size() - 1);
        return range;
    }

    private static class Streak {
        int current;
        int best;
    }

    private Streak computeStreak(Set<LocalDate> dates) {
        Streak streak = new Streak();
        if (dates == null || dates.isEmpty()) {
            return streak;
        }
        LocalDate cursor = LocalDate.now();
        while (dates.contains(cursor)) {
            streak.current += 1;
            cursor = cursor.minusDays(1);
        }

        List<LocalDate> sorted = new ArrayList<>(dates);
        Collections.sort(sorted);
        int best = 1;
        int current = 1;
        for (int i = 1; i < sorted.size(); i++) {
            LocalDate prev = sorted.get(i - 1);
            LocalDate next = sorted.get(i);
            if (prev.plusDays(1).equals(next)) {
                current += 1;
                best = Math.max(best, current);
            } else {
                current = 1;
            }
        }
        streak.best = best;
        return streak;
    }

    private String formatDuration(int seconds) {
        if (seconds <= 0) {
            return getApplication().getString(R.string.stats_value_placeholder);
        }
        int totalMinutes = seconds / 60;
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        }
        return String.format(Locale.getDefault(), "%dm", minutes);
    }

    private String formatStreak(int days) {
        if (days <= 0) {
            return getApplication().getString(R.string.stats_value_placeholder);
        }
        return getApplication().getResources().getQuantityString(
            R.plurals.stats_days, days, days);
    }

    private List<StatisticsRow> buildTopExercises(Map<String, ExerciseAggregate> exercises,
                                                  NumberFormat numberFormat) {
        if (exercises == null || exercises.isEmpty()) {
            return Collections.emptyList();
        }
        List<ExerciseAggregate> list = new ArrayList<>(exercises.values());
        list.sort((a, b) -> Integer.compare(b.volume, a.volume));
        List<StatisticsRow> rows = new ArrayList<>();
        int limit = Math.min(5, list.size());
        for (int i = 0; i < limit; i++) {
            ExerciseAggregate agg = list.get(i);
            String value = numberFormat.format(agg.volume) + " lb";
            String subtitle = getApplication().getString(
                R.string.stats_max_weight_format, formatWeight(agg.maxWeight));
            rows.add(new StatisticsRow(agg.name, value, subtitle));
        }
        return rows;
    }

    private List<StatisticsRow> buildTopMuscles(Map<String, Integer> muscleVolume,
                                                int totalVolume,
                                                NumberFormat numberFormat) {
        if (muscleVolume == null || muscleVolume.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(muscleVolume.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<StatisticsRow> rows = new ArrayList<>();
        int limit = Math.min(5, list.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = list.get(i);
            int volume = entry.getValue();
            int percent = totalVolume > 0 ? Math.round(100f * volume / totalVolume) : 0;
            String value = numberFormat.format(volume) + " lb";
            String subtitle = getApplication().getString(
                R.string.stats_volume_percent_format, percent);
            rows.add(new StatisticsRow(entry.getKey(), value, subtitle));
        }
        return rows;
    }

    private List<StatisticsRow> buildPersonalRecords(Map<String, ExerciseAggregate> exercises,
                                                     NumberFormat numberFormat) {
        if (exercises == null || exercises.isEmpty()) {
            return Collections.emptyList();
        }
        List<ExerciseAggregate> list = new ArrayList<>(exercises.values());
        list.removeIf(item -> item.best1RM <= 0);
        list.sort((a, b) -> Integer.compare(b.best1RM, a.best1RM));
        List<StatisticsRow> rows = new ArrayList<>();
        int limit = Math.min(5, list.size());
        for (int i = 0; i < limit; i++) {
            ExerciseAggregate agg = list.get(i);
            String value = getApplication().getString(
                R.string.stats_pr_value_format, numberFormat.format(agg.best1RM));
            String subtitle = getApplication().getString(
                R.string.stats_pr_subtitle_format,
                formatWeight(agg.bestWeight),
                numberFormat.format(agg.bestReps));
            rows.add(new StatisticsRow(agg.name, value, subtitle));
        }
        return rows;
    }

    private List<WeeklyFrequencyItem> buildWeeklyFrequency(Set<LocalDate> sessionDates) {
        if (sessionDates == null || sessionDates.isEmpty()) {
            return Collections.emptyList();
        }

        Map<LocalDate, Integer> weeklyCounts = new HashMap<>();
        for (LocalDate date : sessionDates) {
            if (date == null) {
                continue;
            }
            LocalDate weekStart = date.with(DayOfWeek.MONDAY);
            weeklyCounts.put(weekStart, weeklyCounts.getOrDefault(weekStart, 0) + 1);
        }

        LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        int weeksToShow = 12;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault());
        List<WeeklyFrequencyItem> items = new ArrayList<>();
        for (int i = weeksToShow - 1; i >= 0; i--) {
            LocalDate weekStart = currentWeekStart.minusWeeks(i);
            int count = weeklyCounts.getOrDefault(weekStart, 0);
            String label = formatter.format(weekStart);
            items.add(new WeeklyFrequencyItem(label, count));
        }
        return items;
    }
}
