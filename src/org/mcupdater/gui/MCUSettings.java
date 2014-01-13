package org.mcupdater.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.mcupdater.translate.TranslateProxy;

import java.io.IOException;
import java.net.URL;

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
	public CheckBox chkFullscreen;
	public TextField txtResWidth;
	public TextField txtResHeight;
	public TextField txtJRE;
	public Button btnJREBrowse;
	public TextField txtJVMOpts;
	public TextField txtInstancePath;
	public Button btnInstancePathBrowse;
	public TextField txtProgramWrapper;
	public CheckBox chkMinimize;
	public CheckBox chkAutoConnect;
	public ListView<URL> lstPackURLs;
	public TextField txtNewURL;
	public Button btnPackURLAdd;
	public Button btnPackURLRemove;
	public Label lblNewURL;

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
	    TranslateProxy translate = Main.getTranslation();
        btnSave.setText(translate.save);
        btnReload.setText(translate.reload);
        lblState.setText("State: Saved");
        lblProfiles.setText(translate.profiles);
        btnProfileAdd.setText(translate.add);
        btnProfileRemove.setText(translate.remove);
        lblMinMemory.setText(translate.minMemory);
        lblMaxMemory.setText(translate.maxMemory);
        lblPermGen.setText(translate.permGen);
        lblMemMessage.setText(translate.memDisclaimer);
        lblFullscreen.setText(translate.fullscreen);
        lblResolution.setText(translate.resolution);
        lblJRE.setText(translate.javaHome);
        lblJVMOpts.setText(translate.jvmOpts);
        lblInstancePath.setText(translate.instancePath);
        lblProgramWrapper.setText(translate.programWrapper);
        lblMinimizeAtLaunch.setText(translate.minimize);
        lblAutoConnect.setText(translate.autoConnect);
        lblPackURLs.setText(translate.definedPacks);
	    btnJREBrowse.setText(translate.browse);
	    btnInstancePathBrowse.setText(translate.browse);
	    lblNewURL.setText("URL:");
	    btnPackURLAdd.setText(translate.add);
	    btnPackURLRemove.setText(translate.remove);
    }

    public static void setState(boolean isDirty) {
        if (!(INSTANCE == null)) {
            //TODO
            System.out.println(isDirty);
        }
    }
}
