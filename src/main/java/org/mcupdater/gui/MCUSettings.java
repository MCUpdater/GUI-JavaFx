package org.mcupdater.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsListener;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.translate.TranslateProxy;

import java.io.File;
import java.io.IOException;

public class MCUSettings extends BorderPane implements SettingsListener {
    public static MCUSettings INSTANCE;
    public Button btnSave;
    public Button btnReload;
    public Label lblState;
    public Label lblProfiles;
    public Button btnProfileAdd;
    public Button btnProfileRemove;
    public ListView<Profile> lstProfiles;
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
	public ListView<String> lstPackURLs;
	public TextField txtNewURL;
	public Button btnPackURLAdd;
	public Button btnPackURLRemove;
	public Label lblNewURL;
	private SettingsManager settingsManager = SettingsManager.getInstance();
	boolean allowEvents = false;

	public MCUSettings() {
        INSTANCE = this;
		settingsManager.addListener(this);
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
	    setWatchers();
	    loadFields();
    }

	private void loadFields()
	{
		allowEvents=false;
		Settings imported = settingsManager.getSettings();
		txtMinMemory.setText(imported.getMinMemory());
		txtMaxMemory.setText(imported.getMaxMemory());
		txtPermGen.setText(imported.getPermGen());
		chkFullscreen.setSelected(imported.isFullScreen());
		txtResWidth.setText(String.valueOf(imported.getResWidth()));
		txtResHeight.setText(String.valueOf(imported.getResHeight()));
		txtJRE.setText(imported.getJrePath());
		txtJVMOpts.setText(imported.getJvmOpts());
		txtInstancePath.setText(imported.getInstanceRoot());
		txtProgramWrapper.setText(imported.getProgramWrapper());
		chkMinimize.setSelected(imported.isMinimizeOnLaunch());
		chkAutoConnect.setSelected(imported.isAutoConnect());
		reloadProfiles();
		reloadURLs();
		MainController.getInstance().refreshInstanceList();
		MainController.getInstance().refreshProfiles();
		allowEvents=true;
	}


	private void setWatchers() {
	    watchField(txtMinMemory, Settings.TextField.minMemory);
	    watchField(txtMaxMemory, Settings.TextField.maxMemory);
	    watchField(txtPermGen, Settings.TextField.permGen);
	    watchField(txtResWidth, Settings.TextField.resWidth);
	    watchField(txtResHeight, Settings.TextField.resHeight);
	    watchField(txtJRE, Settings.TextField.jrePath);
	    watchField(txtJVMOpts, Settings.TextField.jvmOpts);
	    watchField(txtInstancePath, Settings.TextField.instanceRoot);
	    watchField(txtProgramWrapper, Settings.TextField.programWrapper);
	    chkFullscreen.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>()
	    {
		    @Override
		    public void handle(ActionEvent actionEvent)
		    {
			    if (allowEvents) {
				    settingsManager.getSettings().setFullScreen(chkFullscreen.isSelected());
				    settingsManager.setDirty();
				    settingsManager.fireSettingsUpdate();
			    }
		    }
	    });
	    chkMinimize.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>()
	    {
		    @Override
		    public void handle(ActionEvent actionEvent)
		    {
			    if (allowEvents) {
				    settingsManager.getSettings().setMinimizeOnLaunch(chkMinimize.isSelected());
				    settingsManager.setDirty();
				    settingsManager.fireSettingsUpdate();
			    }
		    }
	    });
	    chkAutoConnect.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>()
	    {
		    @Override
		    public void handle(ActionEvent actionEvent)
		    {
			    if (allowEvents) {
				    settingsManager.getSettings().setAutoConnect(chkAutoConnect.isSelected());
				    settingsManager.setDirty();
				    settingsManager.fireSettingsUpdate();
			    }
		    }
	    });
    }

	private void watchField(final TextField toWatch, final Settings.TextField toChange)
	{
		toWatch.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldVal, String newVal) {
				if (allowEvents) {
					settingsManager.getSettings().updateField(toChange, newVal);
					settingsManager.setDirty();
					settingsManager.fireSettingsUpdate();
				}
			}
		});
	}

	public void saveSettings() {
		settingsManager.saveSettings();
	}

	private void reloadProfiles() {
		lstProfiles.getItems().clear();
		lstProfiles.getItems().addAll(settingsManager.getSettings().getProfiles());
	}

	private void reloadURLs() {
		lstPackURLs.getItems().clear();
		lstPackURLs.getItems().addAll(settingsManager.getSettings().getPackURLs());
	}

	public void reloadSettings() {
		settingsManager.reload();
		loadFields();
	}

	public void addProfile() {
		Profile newProfile = LoginDialog.doLogin(this.getScene().getWindow(), "");
		if (newProfile.getStyle().equals("Yggdrasil")) {
			settingsManager.getSettings().addOrReplaceProfile(newProfile);
			settingsManager.setDirty();
			settingsManager.fireSettingsUpdate();
			reloadProfiles();
			String selectedProfile = MainController.getInstance().profiles.getSelectedProfile().getName();
			MainController.getInstance().refreshProfiles();
			MainController.getInstance().profiles.setSelectedProfile(selectedProfile);
		}
	}

	public void removeProfile() {
		if (lstProfiles.getSelectionModel().getSelectedItem() != null) {
			settingsManager.getSettings().removeProfile(lstProfiles.getSelectionModel().getSelectedItem().getName());
			settingsManager.setDirty();
			settingsManager.fireSettingsUpdate();
			reloadProfiles();
			MainController.getInstance().refreshProfiles();
		}
	}

	public void jreBrowse() {
		DirectoryChooser chooser = new DirectoryChooser();
		File selected = chooser.showDialog(this.getScene().getWindow());
		if (selected != null) {
			txtJRE.setText(selected.getAbsolutePath());
		}
	}

	public void instanceBrowse() {
		DirectoryChooser chooser = new DirectoryChooser();
		File selected = chooser.showDialog(this.getScene().getWindow());
		if (selected != null) {
			txtInstancePath.setText(selected.getAbsolutePath());
		}
	}

	public void addPack() {
		if (!txtNewURL.getText().isEmpty()) {
			settingsManager.getSettings().addPackURL(txtNewURL.getText());
			settingsManager.setDirty();
			settingsManager.fireSettingsUpdate();
			txtNewURL.setText("");
			reloadURLs();
			MainController.getInstance().refreshInstanceList();
		}
	}

	public void removePack() {
		if (lstPackURLs.getSelectionModel().getSelectedIndex() != -1) {
			settingsManager.getSettings().removePackUrl(lstPackURLs.getSelectionModel().getSelectedItem());
			settingsManager.setDirty();
			settingsManager.fireSettingsUpdate();
			reloadURLs();
			MainController.getInstance().refreshInstanceList();
		}
	}

	@Override
	public void stateChanged(boolean newState) {
		if (newState) {
			lblState.setText("State: Not Saved");
		} else {
			lblState.setText("State: Saved");
		}
	}

	@Override
	public void settingsChanged(Settings newSettings) {
	}
}
