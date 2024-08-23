package com.example.gymevo.data;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @TypeConverter
    public static LocalDate fromString(String date) {
        return date == null ? null : LocalDate.parse(date, formatter);
    }

    @TypeConverter
    public static String toString(LocalDate date) {
        return date == null ? null : date.format(formatter);
    }
}
