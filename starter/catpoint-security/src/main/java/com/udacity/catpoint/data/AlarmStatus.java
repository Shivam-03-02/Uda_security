package com.udacity.catpoint.data;

import java.awt.Color;

public enum AlarmStatus {
    NO_ALARM("No Alarm", Color.GREEN),
    PENDING_ALARM("Pending Alarm", Color.ORANGE),
    ALARM("Alarm", Color.RED);

    private final String description;
    private final Color color;

    AlarmStatus(String description, Color color) {
        this.description = description;
        this.color = color;
    }

    public String getDescription() { return description; }
    public Color getColor() { return color; }
}
