package org.mcupdater.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;

import java.io.IOException;

/**
 * Created by sbarbour on 1/3/14.
 */
public class MCUSettings extends GridPane {
    private static MCUSettings INSTANCE;

    public MCUSettings() {
        INSTANCE = this;
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("MCUSettings.fxml"));
        fxml.setRoot(this);
        fxml.setController(this);

        try {
            fxml.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void setState(boolean isDirty) {
        if (!(INSTANCE == null)) {
            //TODO
            System.out.println(isDirty);
        }
    }
}
