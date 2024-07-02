package com.example.gymevo.models;

import java.sql.Time;
import java.util.Date;

public class Exercice {
    private Long id;
    private String startImage;
    private String endImage;
    private String name;
    private String targetedMuscle;

    // Constructeurs, getters et setters
    public Exercice(Long id, String startImage, String endImage, String name, String targetedMuscle) {
        this.id = id;
        this.startImage = startImage;
        this.endImage = endImage;
        this.name = name;
        this.targetedMuscle = targetedMuscle;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStartImage() { return startImage; }
    public void setStartImage(String startImage) { this.startImage = startImage; }

    public String getEndImage() { return endImage; }
    public void setEndImage(String endImage) { this.endImage = endImage; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTargetedMuscle() { return targetedMuscle; }
    public void setTargetedMuscle(String targetedMuscle) { this.targetedMuscle = targetedMuscle; }
}

