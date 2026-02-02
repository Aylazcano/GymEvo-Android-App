package com.example.gymevo.ui.statistics;

public class StatisticsRow {
    private final String title;
    private final String value;
    private final String subtitle;

    public StatisticsRow(String title, String value, String subtitle) {
        this.title = title;
        this.value = value;
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
