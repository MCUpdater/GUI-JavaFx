package org.mcupdater.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MCUSettings extends BorderPane {
    private static MCUSettings INSTANCE;
    public Button btnSave;
    public Button btnReload;
    public Label lblState;
    public Label lblProfiles;
    public Button btnProfileAdd;
    public Button btnProfileRemove;
    public ListView lstProfiles;
    public Label lblMinMemory;
    public TextField txtMinMemory;
    public Label lblMaxMemory;
    public TextField txtMaxMemory;
    public Label lblPermGen;
    public TextField txtPermGen;

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
        setupControls();
    }

    private void setupControls() {
        btnSave.setText("Save");
        btnReload.setText("Reload");
        lblState.setText("State: Saved");
        lblProfiles.setText("Profiles:");
        btnProfileAdd.setText("Add");
        btnProfileRemove.setText("Remove");
        lblMinMemory.setText("Minimum Memory:");
        lblMaxMemory.setText("Maximum Memory:");
        lblPermGen.setText("PermGen Space:");
    }

    public static void setState(boolean isDirty) {
        if (!(INSTANCE == null)) {
            //TODO
            System.out.println(isDirty);
        }
    }
}
