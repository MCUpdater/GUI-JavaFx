package org.mcupdater.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.translate.TranslateProxy;

import java.io.IOException;

public class MCUSettings extends BorderPane {
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
	    btnSave.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>()
	    {
		    @Override
		    public void handle(ActionEvent actionEvent)
		    {
			    settingsManager.saveSettings();
		    }
	    });
        btnReload.setText(translate.reload);
	    btnReload.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>()
	    {
		    @Override
		    public void handle(ActionEvent actionEvent)
		    {
			    settingsManager.reload();
			    loadFields();
		    }
	    });
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
		lstProfiles.getItems().clear();
		for (Profile entry : imported.getProfiles())
		{
			lstProfiles.getItems().add(entry);
		}
		lstPackURLs.getItems().clear();
		for (String entry : imported.getPackURLs())
		{
			lstPackURLs.getItems().add(entry);
		}
		MainController.getInstance().refreshInstanceList();
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
			    }
		    }
	    });
    }

    public static void setState(boolean isDirty) {
        if (!(INSTANCE == null)) {
	        if (isDirty) {
		        INSTANCE.lblState.setText("State: Not Saved");
	        } else {
		        INSTANCE.lblState.setText("State: Saved");
	        }
        }
    }

	private void watchField(final TextField toWatch, final Settings.TextField toChange)
	{
		toWatch.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldVal, String newVal)
			{
				if (allowEvents) {
					settingsManager.getSettings().updateField(toChange, newVal);
					settingsManager.setDirty();
				}
			}
		});
	}
}
