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
    public Label lblMemMessage;
    public Label lblFullscreen;
    public Label lblResolution;
    public Label lblJRE;
    public Label lblJVMOpts;
    public Label lblInstancePath;
    public Label lblProgramWrapper;
    public Label lblMinimizeAtLaunch;
    public Label lblAutoConnect;
    public Label lblPackURLs;

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
        lblMemMessage.setText("Memory can be specified in MB or GB (i.e. 512M or 1G).\nIncreasing memory may help performance, but often has no measurable impact.");
        lblFullscreen.setText("Fullscreen:");
        lblResolution.setText("Resolution:");
        lblJRE.setText("Java Home Path:");
        lblJVMOpts.setText("JVM Options:");
        lblInstancePath.setText("Instance Root Path:");
        lblProgramWrapper.setText("Program Wrapper:");
        lblMinimizeAtLaunch.setText("Minimize On Launch:");
        lblAutoConnect.setText("Automatically Connect:");
        lblPackURLs.setText("Defined Pack URLs:");
    }

    public static void setState(boolean isDirty) {
        if (!(INSTANCE == null)) {
            //TODO
            System.out.println(isDirty);
        }
    }
}
