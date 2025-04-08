package de.polo.core.utils;

import lombok.Getter;
import lombok.Setter;

public class DynMapZone {

    @Getter
    private final String name;
    @Getter
    private final double[] xCoords;
    @Getter
    private final double[] zCoords;
    @Getter
    private final String worldName;
    @Getter
    @Setter
    private int lineColor;
    @Getter
    @Setter
    private int fillColor;

    public DynMapZone(String name, String worldName, double[] xCoords, double[] zCoords, int lineColor, int fillColor) {
        this.name = name;
        this.xCoords = xCoords;
        this.zCoords = zCoords;
        this.worldName = worldName;
        this.lineColor = lineColor;
        this.fillColor = fillColor;
    }
}
