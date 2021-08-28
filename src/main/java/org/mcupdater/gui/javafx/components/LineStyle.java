package org.mcupdater.gui.javafx.components;

import javafx.scene.paint.Color;

public enum LineStyle {
    NORMAL(Color.WHITE,Color.TRANSPARENT),
    WARNING(Color.BLACK,new Color(1.0,1.0,0.0,0.75)),
    ERROR(Color.WHITE,new Color(1.0,0.0,0.0,0.75));

    private final Color foreground;
    private final Color background;

    LineStyle(Color foreground, Color background) {
        this.foreground = foreground;
        this.background = background;
    }

    public Color getFgColor() {
        return foreground;
    }

    public Color getBgColor() {
        return background;
    }
}
