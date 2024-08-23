package com.example.gymevo.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise")
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String name;
    private String targetedMuscle;
    private String startImage;
    private String endImage;

    public Exercise(Long id, String name, String targetedMuscle, String startImage, String endImage) {
        this.id = id;
        this.name = name;
        this.targetedMuscle = targetedMuscle;
        this.startImage = startImage;
        this.endImage = endImage;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTargetedMuscle() { return targetedMuscle; }
    public void setTargetedMuscle(String targetedMuscle) { this.targetedMuscle = targetedMuscle; }

    public String getStartImage() { return startImage; }
    public void setStartImage(String startImage) { this.startImage = startImage; }

    public String getEndImage() { return endImage; }
    public void setEndImage(String endImage) { this.endImage = endImage; }
}
