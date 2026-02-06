package com.example.gymevo.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class UserPreferencesRepository {

    private static final String DATASTORE_NAME = "user_preferences";

    private static final String DEFAULT_SORT_FIELD = "RECENT";
    private static final String DEFAULT_SORT_ORDER = "DESC";
    private static final boolean DEFAULT_STAR_PRIORITY = false;
    private static final String DEFAULT_FILTER = "";
    private static final boolean DEFAULT_IS_MONTH_VIEW = true;

    private static final String DEFAULT_THEME_MODE = "system";
    private static final String DEFAULT_THEME_PRIMARY = "";
    private static final String DEFAULT_THEME_ACCENT = "";
    private static final String DEFAULT_THEME_NAME = "default";

    private static final String THEME_CACHE_PREFS = "theme_cache";
    private static final String KEY_THEME_NAME_CACHE = "theme_name_cache";

    private static final Preferences.Key<String> KEY_EXERCISE_SORT_FIELD = PreferencesKeys.stringKey("exercise_sort_field");
    private static final Preferences.Key<String> KEY_EXERCISE_SORT_ORDER = PreferencesKeys.stringKey("exercise_sort_order");
    private static final Preferences.Key<Boolean> KEY_EXERCISE_STAR_FIRST = PreferencesKeys.booleanKey("exercise_star_first");
    private static final Preferences.Key<String> KEY_EXERCISE_FILTER = PreferencesKeys.stringKey("exercise_filter");
    private static final Preferences.Key<String> KEY_EXERCISE_TYPE_FILTER = PreferencesKeys.stringKey("exercise_type_filter");
    private static final Preferences.Key<String> KEY_EXERCISE_CUSTOM_ORDER = PreferencesKeys.stringKey("exercise_custom_order");

    private static final Preferences.Key<String> KEY_WORKOUT_SORT_FIELD = PreferencesKeys.stringKey("workout_sort_field");
    private static final Preferences.Key<String> KEY_WORKOUT_SORT_ORDER = PreferencesKeys.stringKey("workout_sort_order");
    private static final Preferences.Key<Boolean> KEY_WORKOUT_STAR_FIRST = PreferencesKeys.booleanKey("workout_star_first");
    private static final Preferences.Key<String> KEY_WORKOUT_FILTER = PreferencesKeys.stringKey("workout_filter");
    private static final Preferences.Key<String> KEY_WORKOUT_CUSTOM_ORDER = PreferencesKeys.stringKey("workout_custom_order");

    private static final Preferences.Key<Boolean> KEY_CALENDAR_MONTH_VIEW = PreferencesKeys.booleanKey("calendar_month_view");

    private static final Preferences.Key<String> KEY_THEME_MODE = PreferencesKeys.stringKey("theme_mode");
    private static final Preferences.Key<String> KEY_THEME_PRIMARY = PreferencesKeys.stringKey("theme_primary_color");
    private static final Preferences.Key<String> KEY_THEME_ACCENT = PreferencesKeys.stringKey("theme_accent_color");
    private static final Preferences.Key<String> KEY_THEME_NAME = PreferencesKeys.stringKey("theme_name");

    private static volatile UserPreferencesRepository instance;

    private final RxDataStore<Preferences> dataStore;
    private final SharedPreferences themeCache;
    private volatile String cachedThemeName = DEFAULT_THEME_NAME;

    private UserPreferencesRepository(Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context.getApplicationContext(), DATASTORE_NAME).build();
        themeCache = context.getApplicationContext()
                .getSharedPreferences(THEME_CACHE_PREFS, Context.MODE_PRIVATE);
        cachedThemeName = themeCache.getString(KEY_THEME_NAME_CACHE, DEFAULT_THEME_NAME);
    }

    public static UserPreferencesRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (UserPreferencesRepository.class) {
                if (instance == null) {
                    instance = new UserPreferencesRepository(context);
                }
            }
        }
        return instance;
    }

    public Single<ExerciseListPreferences> getExerciseListPreferences() {
        return dataStore.data()
                .firstOrError()
                .map(this::mapExerciseListPreferences);
    }

    public Completable setExerciseListPreferences(ExerciseListPreferences preferences) {
        if (preferences == null) {
            return Completable.complete();
        }
        return dataStore.updateDataAsync(current -> {
            MutablePreferences mutable = current.toMutablePreferences();
            mutable.set(KEY_EXERCISE_SORT_FIELD, safeString(preferences.sortField, DEFAULT_SORT_FIELD));
            mutable.set(KEY_EXERCISE_SORT_ORDER, safeString(preferences.sortOrder, DEFAULT_SORT_ORDER));
            mutable.set(KEY_EXERCISE_STAR_FIRST, preferences.starPriorityEnabled);
            mutable.set(KEY_EXERCISE_FILTER, safeString(preferences.muscleFilter, DEFAULT_FILTER));
            mutable.set(KEY_EXERCISE_TYPE_FILTER, safeString(preferences.typeFilter, DEFAULT_FILTER));
            mutable.set(KEY_EXERCISE_CUSTOM_ORDER, safeString(preferences.customOrder, DEFAULT_FILTER));
            return Single.just(mutable);
        }).ignoreElement();
    }

    public Single<WorkoutListPreferences> getWorkoutListPreferences() {
        return dataStore.data()
                .firstOrError()
                .map(this::mapWorkoutListPreferences);
    }

    public Completable setWorkoutListPreferences(WorkoutListPreferences preferences) {
        if (preferences == null) {
            return Completable.complete();
        }
        return dataStore.updateDataAsync(current -> {
            MutablePreferences mutable = current.toMutablePreferences();
            mutable.set(KEY_WORKOUT_SORT_FIELD, safeString(preferences.sortField, DEFAULT_SORT_FIELD));
            mutable.set(KEY_WORKOUT_SORT_ORDER, safeString(preferences.sortOrder, DEFAULT_SORT_ORDER));
            mutable.set(KEY_WORKOUT_STAR_FIRST, preferences.starPriorityEnabled);
            mutable.set(KEY_WORKOUT_FILTER, safeString(preferences.muscleFilter, DEFAULT_FILTER));
            mutable.set(KEY_WORKOUT_CUSTOM_ORDER, safeString(preferences.customOrder, DEFAULT_FILTER));
            return Single.just(mutable);
        }).ignoreElement();
    }

    public Single<CalendarPreferences> getCalendarPreferences() {
        return dataStore.data()
                .firstOrError()
                .map(this::mapCalendarPreferences);
    }

    public Completable setCalendarViewMode(boolean isMonthView) {
        return dataStore.updateDataAsync(current -> {
            MutablePreferences mutable = current.toMutablePreferences();
            mutable.set(KEY_CALENDAR_MONTH_VIEW, isMonthView);
            return Single.just(mutable);
        }).ignoreElement();
    }

    public Single<ThemePreferences> getThemePreferences() {
        return dataStore.data()
                .firstOrError()
                .map(this::mapThemePreferences);
    }

    public Completable setThemePreferences(ThemePreferences preferences) {
        if (preferences == null) {
            return Completable.complete();
        }
        cacheThemeName(preferences.themeName);
        return dataStore.updateDataAsync(current -> {
            MutablePreferences mutable = current.toMutablePreferences();
            mutable.set(KEY_THEME_MODE, safeString(preferences.themeMode, DEFAULT_THEME_MODE));
            mutable.set(KEY_THEME_PRIMARY, safeString(preferences.primaryColor, DEFAULT_THEME_PRIMARY));
            mutable.set(KEY_THEME_ACCENT, safeString(preferences.accentColor, DEFAULT_THEME_ACCENT));
            mutable.set(KEY_THEME_NAME, safeString(preferences.themeName, DEFAULT_THEME_NAME));
            return Single.just(mutable);
        }).ignoreElement();
    }

    public Single<String> getThemeName() {
        return dataStore.data()
                .firstOrError()
                .map(prefs -> valueOrDefault(prefs.get(KEY_THEME_NAME), DEFAULT_THEME_NAME));
    }

    public String getThemeNameImmediate() {
        return cachedThemeName != null ? cachedThemeName : DEFAULT_THEME_NAME;
    }

    public Completable setThemeName(String themeName) {
        cacheThemeName(themeName);
        return dataStore.updateDataAsync(current -> {
            MutablePreferences mutable = current.toMutablePreferences();
            mutable.set(KEY_THEME_NAME, safeString(themeName, DEFAULT_THEME_NAME));
            return Single.just(mutable);
        }).ignoreElement();
    }

    private void cacheThemeName(String themeName) {
        String safeTheme = safeString(themeName, DEFAULT_THEME_NAME);
        cachedThemeName = safeTheme;
        themeCache.edit().putString(KEY_THEME_NAME_CACHE, safeTheme).apply();
    }

    private ExerciseListPreferences mapExerciseListPreferences(Preferences prefs) {
        String sortField = valueOrDefault(prefs.get(KEY_EXERCISE_SORT_FIELD), DEFAULT_SORT_FIELD);
        String sortOrder = valueOrDefault(prefs.get(KEY_EXERCISE_SORT_ORDER), DEFAULT_SORT_ORDER);
        boolean starFirst = boolOrDefault(prefs.get(KEY_EXERCISE_STAR_FIRST), DEFAULT_STAR_PRIORITY);
        String filter = valueOrDefault(prefs.get(KEY_EXERCISE_FILTER), DEFAULT_FILTER);
        String typeFilter = valueOrDefault(prefs.get(KEY_EXERCISE_TYPE_FILTER), DEFAULT_FILTER);
        String customOrder = valueOrDefault(prefs.get(KEY_EXERCISE_CUSTOM_ORDER), DEFAULT_FILTER);
        return new ExerciseListPreferences(filter, typeFilter, sortField, sortOrder, starFirst, customOrder);
    }

    private WorkoutListPreferences mapWorkoutListPreferences(Preferences prefs) {
        String sortField = valueOrDefault(prefs.get(KEY_WORKOUT_SORT_FIELD), DEFAULT_SORT_FIELD);
        String sortOrder = valueOrDefault(prefs.get(KEY_WORKOUT_SORT_ORDER), DEFAULT_SORT_ORDER);
        boolean starFirst = boolOrDefault(prefs.get(KEY_WORKOUT_STAR_FIRST), DEFAULT_STAR_PRIORITY);
        String filter = valueOrDefault(prefs.get(KEY_WORKOUT_FILTER), DEFAULT_FILTER);
        String customOrder = valueOrDefault(prefs.get(KEY_WORKOUT_CUSTOM_ORDER), DEFAULT_FILTER);
        return new WorkoutListPreferences(filter, sortField, sortOrder, starFirst, customOrder);
    }

    private CalendarPreferences mapCalendarPreferences(Preferences prefs) {
        boolean isMonthView = boolOrDefault(prefs.get(KEY_CALENDAR_MONTH_VIEW), DEFAULT_IS_MONTH_VIEW);
        return new CalendarPreferences(isMonthView);
    }

    private ThemePreferences mapThemePreferences(Preferences prefs) {
        String mode = valueOrDefault(prefs.get(KEY_THEME_MODE), DEFAULT_THEME_MODE);
        String primary = valueOrDefault(prefs.get(KEY_THEME_PRIMARY), DEFAULT_THEME_PRIMARY);
        String accent = valueOrDefault(prefs.get(KEY_THEME_ACCENT), DEFAULT_THEME_ACCENT);
        String themeName = valueOrDefault(prefs.get(KEY_THEME_NAME), DEFAULT_THEME_NAME);
        cacheThemeName(themeName);
        return new ThemePreferences(mode, primary, accent, themeName);
    }

    private static String valueOrDefault(String value, String fallback) {
        return value != null ? value : fallback;
    }

    private static boolean boolOrDefault(Boolean value, boolean fallback) {
        return value != null ? value : fallback;
    }

    private static String safeString(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    public static class ExerciseListPreferences {
        public final String muscleFilter;
        public final String typeFilter;
        public final String sortField;
        public final String sortOrder;
        public final boolean starPriorityEnabled;
        public final String customOrder;

        public ExerciseListPreferences(String muscleFilter,
                                      String typeFilter,
                                      String sortField,
                                      String sortOrder,
                                      boolean starPriorityEnabled,
                                      String customOrder) {
            this.muscleFilter = muscleFilter;
            this.typeFilter = typeFilter;
            this.sortField = sortField;
            this.sortOrder = sortOrder;
            this.starPriorityEnabled = starPriorityEnabled;
            this.customOrder = customOrder;
        }
    }

    public static class WorkoutListPreferences {
        public final String muscleFilter;
        public final String sortField;
        public final String sortOrder;
        public final boolean starPriorityEnabled;
        public final String customOrder;

        public WorkoutListPreferences(String muscleFilter,
                                     String sortField,
                                     String sortOrder,
                                     boolean starPriorityEnabled,
                                     String customOrder) {
            this.muscleFilter = muscleFilter;
            this.sortField = sortField;
            this.sortOrder = sortOrder;
            this.starPriorityEnabled = starPriorityEnabled;
            this.customOrder = customOrder;
        }
    }

    public static class CalendarPreferences {
        public final boolean isMonthView;

        public CalendarPreferences(boolean isMonthView) {
            this.isMonthView = isMonthView;
        }
    }

    public static class ThemePreferences {
        public final String themeMode;
        public final String primaryColor;
        public final String accentColor;
        public final String themeName;

        public ThemePreferences(String themeMode, String primaryColor, String accentColor) {
            this(themeMode, primaryColor, accentColor, DEFAULT_THEME_NAME);
        }

        public ThemePreferences(String themeMode, String primaryColor, String accentColor, String themeName) {
            this.themeMode = themeMode;
            this.primaryColor = primaryColor;
            this.accentColor = accentColor;
            this.themeName = themeName;
        }
    }
}
