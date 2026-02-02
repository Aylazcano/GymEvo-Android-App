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
    private long createdAt;
    private long updatedAt;

    @Ignore
    public Exercise(String name, String targetedMuscles, String imageA, String imageB, boolean isStar) {
        this.name = normalizeName(name);
        this.targetedMuscles = parseMuscleGroup(targetedMuscles);
        this.imageA = imageA;
        this.imageB = imageB;
        this.isStar = isStar;
    }

    public Exercise(String name, MuscleGroup targetedMuscles, String imageA, String imageB, boolean isStar) {
        this.name = normalizeName(name);
        this.targetedMuscles = targetedMuscles != null ? targetedMuscles : MuscleGroup.OTHER;
        this.imageA = imageA;
        this.imageB = imageB;
        this.isStar = isStar;
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
