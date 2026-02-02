package com.example.gymevo.data.local;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private LocalDateConverter() {
    }

    @TypeConverter
    public static LocalDate fromString(String date) {
        return date == null ? null : LocalDate.parse(date, FORMATTER);
    }

    @TypeConverter
    public static String toString(LocalDate date) {
        return date == null ? null : date.format(FORMATTER);
    }
}
