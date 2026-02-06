package com.example.gymevo.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise")
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String name;
    private MuscleGroup targetedMuscles;
    private String imageA;
    private String imageB;
    private boolean isStar;
    private ExerciseType type;
    private boolean showSeries;
    private boolean showRepetitions;
    private boolean showWeight;
    private boolean showTime;
    private boolean showHeartRate;
    private long createdAt;
    private long updatedAt;

    @Ignore
    public Exercise(String name, String targetedMuscles, String imageA, String imageB, boolean isStar) {
        this(name, parseMuscleGroup(targetedMuscles), imageA, imageB, isStar, ExerciseType.ANAEROBIC);
    }

    @Ignore
    public Exercise(String name, MuscleGroup targetedMuscles, String imageA, String imageB, boolean isStar) {
        this(name, targetedMuscles, imageA, imageB, isStar, ExerciseType.ANAEROBIC);
    }

    public Exercise(String name, MuscleGroup targetedMuscles, String imageA, String imageB,
                    boolean isStar, ExerciseType type) {
        this.name = normalizeName(name);
        this.targetedMuscles = targetedMuscles != null ? targetedMuscles : MuscleGroup.OTHER;
        this.imageA = imageA;
        this.imageB = imageB;
        this.isStar = isStar;
        this.type = type != null ? type : ExerciseType.ANAEROBIC;
        applyDefaultMetricsForType(this.type);
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = normalizeName(name); }

    public MuscleGroup getTargetedMuscles() {
        return targetedMuscles;
    }

    public void setTargetedMuscles(MuscleGroup targetedMuscles) {
        this.targetedMuscles = targetedMuscles;
    }

    @Ignore
    public String getTargetedMusclesLabel() {
        return targetedMuscles != null ? targetedMuscles.getLabel() : null;
    }

    @Ignore
    public void setTargetedMusclesLabel(String targetedMuscles) {
        this.targetedMuscles = parseMuscleGroup(targetedMuscles);
    }

    private static MuscleGroup parseMuscleGroup(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        MuscleGroup group = MuscleGroup.fromLabel(value);
        return group != null ? group : MuscleGroup.OTHER;
    }

    public String getImageA() { return imageA; }
    public void setImageA(String imageA) { this.imageA = imageA; }

    public String getStartImage() { return imageA; }
    public void setStartImage(String startImage) { this.imageA = startImage; }

    public String getImageB() { return imageB; }
    public void setImageB(String imageB) { this.imageB = imageB; }

    public String getEndImage() { return imageB; }
    public void setEndImage(String endImage) { this.imageB = endImage; }

    public boolean isStar() { return isStar; }
    public void setStar(boolean isStar) { this.isStar = isStar; }

    public ExerciseType getType() {
        return type != null ? type : ExerciseType.ANAEROBIC;
    }

    public void setType(ExerciseType type) {
        this.type = type != null ? type : ExerciseType.ANAEROBIC;
    }

    @Ignore
    public String getTypeLabel() {
        return getType().getLabel();
    }

    public boolean isShowSeries() {
        return showSeries;
    }

    public void setShowSeries(boolean showSeries) {
        this.showSeries = showSeries;
    }

    public boolean isShowRepetitions() {
        return showRepetitions;
    }

    public void setShowRepetitions(boolean showRepetitions) {
        this.showRepetitions = showRepetitions;
    }

    public boolean isShowWeight() {
        return showWeight;
    }

    public void setShowWeight(boolean showWeight) {
        this.showWeight = showWeight;
    }

    public boolean isShowTime() {
        return showTime;
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    public boolean isShowHeartRate() {
        return showHeartRate;
    }

    public void setShowHeartRate(boolean showHeartRate) {
        this.showHeartRate = showHeartRate;
    }

    @Ignore
    public boolean hasAnyDisplayMetric() {
        return showSeries || showRepetitions || showWeight || showTime || showHeartRate;
    }

    @Ignore
    public void applyDefaultMetricsForType(ExerciseType type) {
        ExerciseType safeType = type != null ? type : ExerciseType.ANAEROBIC;
        if (safeType == ExerciseType.AEROBIC) {
            showSeries = false;
            showRepetitions = false;
            showWeight = false;
            showTime = true;
            showHeartRate = true;
        } else {
            showSeries = true;
            showRepetitions = true;
            showWeight = true;
            showTime = false;
            showHeartRate = false;
        }
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    private static String normalizeName(String value) {
        return value != null ? value : "";
    }
}
