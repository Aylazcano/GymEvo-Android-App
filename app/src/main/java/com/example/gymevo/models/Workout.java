package com.example.gymevo.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.Relation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "workout")
public class Workout {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String name;
    private LocalDate date;

    // Indicateur pour savoir si c'est un workout favori (isStar)
    private boolean isStar; // Renommé de 'optimalBoolName' à 'isStar'

    @Ignore // Ceci n'est pas stocké dans la base de données, c'est pour l'utilisation au runtime
    @Relation(parentColumn = "id", entityColumn = "workoutId")
    private List<ExerciseInWorkout> exercises;

    // Constructeurs
    public Workout(Long id, String name, LocalDate date, boolean isStar) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.isStar = isStar; // Initialisation du champ isStar
        this.exercises = new ArrayList<>();
    }

    public Workout() {
        this.id = null; // ou une valeur par défaut
        this.name = ""; // une chaîne vide par défaut
        this.date = LocalDate.now(); // Date actuelle par défaut
        this.isStar = false; // valeur par défaut pour isStar
        this.exercises = new ArrayList<>(); // Liste vide pour les exercices
    }

    @Ignore
    public Workout(String name, LocalDate date, boolean isStar) {
        this(null, name, date, isStar); // Appel au constructeur principal avec id=null
    }

    // Méthodes pour gérer les exercices (non stockées dans la base de données, utilisées au runtime)
    public void addExercise(ExerciseInWorkout exercise) {
        if (exercise != null && !exercises.contains(exercise)) {
            exercises.add(exercise);
            exercise.setWorkoutId(this.id); // Définir l'ID du workout pour l'ExerciseInWorkout
        }
    }

    public void removeExercise(ExerciseInWorkout exercise) {
        exercises.remove(exercise);
        exercise.setWorkoutId(null); // Retirer l'ID du workout pour l'ExerciseInWorkout
    }

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isStar() {  // Renommé la méthode pour 'isStar'
        return isStar;
    }

    public void setStar(boolean isStar) {  // Renommé la méthode pour 'setStar'
        this.isStar = isStar;
    }

    public List<ExerciseInWorkout> getExercises() {
        return exercises;
    }

    public void setExercises(List<ExerciseInWorkout> exercisesList) {
        this.exercises = exercisesList;
    }
}
