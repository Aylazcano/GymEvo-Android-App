package com.example.gymevo.model;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "calorie_entry")
public class CalorieEntry {

    @PrimaryKey(autoGenerate = true)
    @Nullable
    private Long id;

    /** Epoch-day (LocalDate.toEpochDay()) for fast date queries. */
    private long date;

    private String mealName;
    private int calories;
    private int protein;   // grams
    private int carbs;     // grams
    private int fat;       // grams
    private long createdAt;

    public CalorieEntry(String mealName, int calories, int protein, int carbs, int fat, long date) {
        this.mealName = mealName;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.date = date;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters & setters

    @Nullable public Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public int getProtein() { return protein; }
    public void setProtein(int protein) { this.protein = protein; }

    public int getCarbs() { return carbs; }
    public void setCarbs(int carbs) { this.carbs = carbs; }

    public int getFat() { return fat; }
    public void setFat(int fat) { this.fat = fat; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
