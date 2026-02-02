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
