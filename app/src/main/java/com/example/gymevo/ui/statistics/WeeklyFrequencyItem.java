package com.example.gymevo.ui.statistics;

public class WeeklyFrequencyItem {
    private final String label;
    private final int count;

    public WeeklyFrequencyItem(String label, int count) {
        this.label = label;
        this.count = count;
    }

    public String getLabel() {
        return label;
    }

    public int getCount() {
        return count;
    }
}
