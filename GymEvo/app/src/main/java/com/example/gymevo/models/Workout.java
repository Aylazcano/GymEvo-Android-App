package com.example.gymevo.models;

import java.sql.Time;
import java.util.Date;

public class Workout {
    public Long id;
    public int Series;
    public int Repetitions;
    public int Weight;
    public Time Time;
    public int HeartRates;
    public Date Date;
    public Exercice Exercice;



    public Workout(Long pId, int pSeriesQty, int pRepetitionsQty, int pWeight, Time pTime, int pHeartRates, Date pDate, Exercice pExercice){
        id = pId;
        Series = pSeriesQty;
        Repetitions = pRepetitionsQty;
        Weight = pWeight;
        Time = pTime;
        HeartRates = pHeartRates;
        Date = pDate;
        Exercice = pExercice;
    }

}
