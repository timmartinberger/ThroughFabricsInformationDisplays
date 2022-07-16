package de.uni_hannover.hci.informationalDisplaysControl.baseData;

import android.graphics.Color;

public enum DrawingColor {
    WHITE,
    LTBLUE,
    RED,
    ORANGE,
    YELLOW,
    DKGRAY,
    GREEN,
    BLUE,
    MAGENTA,
    BROWN;

    public int getColorValue() {
        switch (this) {
            case WHITE:
                return Color.WHITE;
            case LTBLUE:
                return Color.rgb(63, 164, 252);
            case RED:
                return Color.RED;
            case ORANGE:
                return Color.rgb(240, 133, 26);
            case YELLOW:
                return Color.YELLOW;
            case DKGRAY:
                return Color.DKGRAY;
            case GREEN:
                return Color.GREEN;
            case BLUE:
                return Color.BLUE;
            case MAGENTA:
                return Color.MAGENTA;
            case BROWN:
                return Color.rgb(105, 57, 9);
        }
        return Color.WHITE;
    }

    public int getColorCode() {
        switch (this) {
            case WHITE:
                return 8;
            case LTBLUE:
                return 9;
            case RED:
                return 0;
            case ORANGE:
                return 3;
            case YELLOW:
                return 2;
            case DKGRAY:
                return 5;
            case GREEN:
                return 6;
            case BLUE:
                return 1;
            case MAGENTA:
                return 4;
            case BROWN:
                return 7;
            default:
                return 8;
        }
    }
}
