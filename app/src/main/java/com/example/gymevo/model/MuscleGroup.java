package com.example.gymevo.model;

import java.util.Locale;

public enum MuscleGroup {
    CHEST("Chest"),
    UPPER_CHEST("Upper chest"),
    LOWER_CHEST("Lower chest"),
    BACK("Back"),
    LATS("Lats"),
    UPPER_BACK("Upper back"),
    MIDDLE_BACK("Middle back"),
    LOWER_BACK("Lower back"),
    TRAPS("Traps"),
    RHOMBOIDS("Rhomboids"),
    LEGS("Legs"),
    QUADRICEPS("Quadriceps"),
    HAMSTRINGS("Hamstrings"),
    ADDUCTORS("Adductors"),
    ABDUCTORS("Abductors"),
    HIP_FLEXORS("Hip flexors"),
    SHOULDERS("Shoulders"),
    FRONT_DELTS("Front delts"),
    LATERAL_DELTS("Lateral delts"),
    REAR_DELTS("Rear delts"),
    ARMS("Arms"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    ABS("Abs"),
    OBLIQUES("Obliques"),
    GLUTES("Glutes"),
    CALVES("Calves"),
    FOREARMS("Forearms"),
    NECK("Neck"),
    FULL_BODY("Full body"),
    OTHER("Other");

    private final String label;

    MuscleGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns an integer representing the anatomical order from head to toe.
     * Used for top-to-bottom body sort.
     */
    public int anatomicalOrder() {
        switch (this) {
            case NECK:          return 0;
            case TRAPS:         return 1;
            case SHOULDERS:     return 2;
            case FRONT_DELTS:   return 3;
            case LATERAL_DELTS: return 4;
            case REAR_DELTS:    return 5;
            case CHEST:         return 6;
            case UPPER_CHEST:   return 7;
            case LOWER_CHEST:   return 8;
            case BACK:          return 9;
            case UPPER_BACK:    return 10;
            case MIDDLE_BACK:   return 11;
            case LATS:          return 12;
            case RHOMBOIDS:     return 13;
            case ARMS:          return 14;
            case BICEPS:        return 15;
            case TRICEPS:       return 16;
            case FOREARMS:      return 17;
            case ABS:           return 18;
            case OBLIQUES:      return 19;
            case LOWER_BACK:    return 20;
            case HIP_FLEXORS:   return 21;
            case GLUTES:        return 22;
            case LEGS:          return 23;
            case QUADRICEPS:    return 24;
            case HAMSTRINGS:    return 25;
            case ADDUCTORS:     return 26;
            case ABDUCTORS:     return 27;
            case CALVES:        return 28;
            case FULL_BODY:     return 29;
            case OTHER:
            default:            return 30;
        }
    }

    public static MuscleGroup fromLabel(String value) {
        if (value == null) {
            return null;
        }
        String normalized = normalize(value);
        MuscleGroup alias = fromAlias(normalized);
        if (alias != null) {
            return alias;
        }
        for (MuscleGroup group : values()) {
            if (normalize(group.label).equals(normalized) || group.name().equalsIgnoreCase(normalized)) {
                return group;
            }
        }
        return null;
    }

    private static MuscleGroup fromAlias(String normalized) {
        switch (normalized) {
            case "pec":
            case "pecs":
                return CHEST;
            case "upper chest":
                return UPPER_CHEST;
            case "lower chest":
                return LOWER_CHEST;
            case "lat":
            case "lats":
                return LATS;
            case "trap":
            case "traps":
                return TRAPS;
            case "rhomboid":
            case "rhomboids":
                return RHOMBOIDS;
            case "quad":
            case "quads":
                return QUADRICEPS;
            case "hamstring":
            case "hamstrings":
                return HAMSTRINGS;
            case "adductor":
            case "adductors":
                return ADDUCTORS;
            case "abductor":
            case "abductors":
                return ABDUCTORS;
            case "hip flexor":
            case "hip flexors":
                return HIP_FLEXORS;
            case "delt":
            case "delts":
                return SHOULDERS;
            case "front deltoid":
            case "front deltoids":
                return FRONT_DELTS;
            case "lateral deltoid":
            case "lateral deltoids":
                return LATERAL_DELTS;
            case "rear deltoid":
            case "rear deltoids":
                return REAR_DELTS;
            case "bicep":
            case "biceps":
                return BICEPS;
            case "tricep":
            case "triceps":
                return TRICEPS;
            case "oblique":
            case "obliques":
                return OBLIQUES;
            case "glute":
            case "glutes":
            case "butt":
            case "buttocks":
                return GLUTES;
            case "calf":
            case "calves":
                return CALVES;
            case "forearm":
            case "forearms":
                return FOREARMS;
            case "neck":
                return NECK;
            case "core":
                return ABS;
            case "erectors":
            case "erector spinae":
                return LOWER_BACK;
            default:
                return null;
        }
    }

    private static String normalize(String value) {
        return value.trim().replace('-', ' ').replace('_', ' ').toLowerCase(Locale.ROOT);
    }
}
