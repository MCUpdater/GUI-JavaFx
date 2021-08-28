package org.mcupdater.gui.javafx.components;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import org.mcupdater.model.Loader;

public class LoaderEntry extends Region {

    private final Loader loader;

    public LoaderEntry(Loader loader) {
        this.loader = loader;
        Label lblEntry = new Label(loader.getFriendlyName());
        this.getChildren().add(lblEntry);
        this.setMinHeight(16);
    }

    public Loader getLoader() {
        return this.loader;
    }
}
