package com.example.gymevo.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise")
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String name;
    private String targetedMuscles;
    private String startImage;  // Peut être null
    private String endImage;    // Peut être null
    private boolean isStar;

    // Constructeur sans validation pour les images, permettant qu'elles soient null
    public Exercise(String name, String targetedMuscles, String startImage, String endImage, boolean isStar) {
        if (name == null || targetedMuscles == null) {
            throw new IllegalArgumentException("Les champs 'name' et 'targetedMuscle' ne peuvent pas être nuls.");
        }
        this.name = name;
        this.targetedMuscles = targetedMuscles;
        this.startImage = startImage;  // Peut être null
        this.endImage = endImage;      // Peut être null
        this.isStar = isStar;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTargetedMuscles() { return targetedMuscles; }
    public void setTargetedMuscles(String targetedMuscles) { this.targetedMuscles = targetedMuscles; }

    public String getStartImage() { return startImage; }
    public void setStartImage(String startImage) { this.startImage = startImage; }

    public String getEndImage() { return endImage; }
    public void setEndImage(String endImage) { this.endImage = endImage; }

    public boolean isStar() { return isStar; }
    public void setStar(boolean isStar) { this.isStar = isStar; }
}