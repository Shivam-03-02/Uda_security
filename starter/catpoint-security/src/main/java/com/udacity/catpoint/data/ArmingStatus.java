package com.udacity.catpoint.data;

import java.awt.Color;

public enum ArmingStatus {
    DISARMED("Disarmed", Color.GRAY),
    ARMED_HOME("Armed - Home", Color.GREEN),
    ARMED_AWAY("Armed - Away", Color.ORANGE);

    private final String description;
    private final Color color;

    ArmingStatus(String description, Color color) {
        this.description = description;
        this.color = color;
    }

    public String getDescription() { return description; }
    public Color getColor() { return color; }
}
