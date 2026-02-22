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
    private String sourceId;
    private String force;
    private String level;
    private String mechanic;
    private String equipment;
    private String category;
    private String primaryMuscles;
    private String secondaryMuscles;
    private String instructions;
    private String imageA;
    private String imageB;
    private boolean isStar;
    private ExerciseType type;
    private boolean showSeries;
    private boolean showRepetitions;
    private boolean showWeight;
    private boolean showTime;
    private boolean showHeartRate;
    private boolean showDistance;
    private boolean showCalories;
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

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getForce() {
        return force;
    }

    public void setForce(String force) {
        this.force = force;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMechanic() {
        return mechanic;
    }

    public void setMechanic(String mechanic) {
        this.mechanic = mechanic;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrimaryMuscles() {
        return primaryMuscles;
    }

    public void setPrimaryMuscles(String primaryMuscles) {
        this.primaryMuscles = primaryMuscles;
    }

    public String getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public void setSecondaryMuscles(String secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
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

    public boolean isShowDistance() {
        return showDistance;
    }

    public void setShowDistance(boolean showDistance) {
        this.showDistance = showDistance;
    }

    public boolean isShowCalories() {
        return showCalories;
    }

    public void setShowCalories(boolean showCalories) {
        this.showCalories = showCalories;
    }

    @Ignore
    public boolean hasAnyDisplayMetric() {
        return showSeries || showRepetitions || showWeight || showTime || showHeartRate || showDistance || showCalories;
    }

    @Ignore
    public void applyDefaultMetricsForType(ExerciseType type) {
        ExerciseType safeType = type != null ? type : ExerciseType.ANAEROBIC;
        if (safeType == ExerciseType.AEROBIC) {
            showSeries = false;
            showRepetitions = false;
            showWeight = false;
            showTime = true;
            showHeartRate = false;
            showDistance = true;
            showCalories = true;
        } else {
            showSeries = true;
            showRepetitions = true;
            showWeight = true;
            showTime = false;
            showHeartRate = false;
            showDistance = false;
            showCalories = true;
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

    public void copyCatalogMetadataFrom(Exercise source) {
        if (source == null) {
            return;
        }
        sourceId = source.getSourceId();
        force = source.getForce();
        level = source.getLevel();
        mechanic = source.getMechanic();
        equipment = source.getEquipment();
        category = source.getCategory();
        primaryMuscles = source.getPrimaryMuscles();
        secondaryMuscles = source.getSecondaryMuscles();
        instructions = source.getInstructions();
    }

    private static String normalizeName(String value) {
        return value != null ? value : "";
    }
}
