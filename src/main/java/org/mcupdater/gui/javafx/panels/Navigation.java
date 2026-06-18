package org.mcupdater.gui.javafx.panels;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import org.mcupdater.FMLStyleFormatter;
import org.mcupdater.MCUApp;
import org.mcupdater.api.Version;
import org.mcupdater.auth.TokenResponse;
import org.mcupdater.auth.YggdrasilAuthManager;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.downloadlib.TrackerListener;
import org.mcupdater.gui.javafx.Main;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.packbuilder.gui.MainFormController;
import org.mcupdater.settings.MSAProfile;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsListener;
import org.mcupdater.util.MCUpdater;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class Navigation extends MCUApp implements Initializable, TrackerListener, SettingsListener {
	private static Navigation INSTANCE;
	@FXML
	private VBox navPane;
	@FXML
	private BorderPane content;
	@FXML
	private VBox vboxInstances;
	@FXML
	private VBox vboxFind;
	@FXML
	private VBox vboxCreate;
	@FXML
	private VBox vboxConsole;
	@FXML
	private VBox vboxSettings;
	@FXML
	private SVGPath instances;
	@FXML
	private SVGPath find;
	@FXML
	private SVGPath create;
	@FXML
	private SVGPath console;
	@FXML
	private SVGPath settings;

	public Navigation() {
		INSTANCE = this;
		MCUpdater.getInstance().setParent(this);
		this.baseLogger = Logger.getLogger("MCUpdater");
		this.baseLogger.setLevel(Level.ALL);
		try {
			FileHandler mcuHandler = new FileHandler(MCUpdater.getInstance().getArchiveFolder().resolve("MCupdater.log").toString(), 0, 3);
			mcuHandler.setFormatter(new FMLStyleFormatter());
			mcuHandler.setLevel(Level.CONFIG);
			baseLogger.addHandler(mcuHandler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Version.setApp(this);
		this.setAuthManager(new YggdrasilAuthManager());
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		resetGraphics();
		navPane.setBackground(new Background(new BackgroundImage(new Image("/org/mcupdater/gui/javafx/img/bg_main.png",true), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
		navPane.fillWidthProperty().set(true);
		Tooltip ttInstances = new Tooltip("Instances");
		ttInstances.setFont(Font.font("Liberation Sans", 18));
		Tooltip.install(vboxInstances,ttInstances);
		Tooltip ttFind = new Tooltip("Find Modpacks");
		ttFind.setFont(Font.font("Liberation Sans", 18));
		Tooltip.install(vboxFind, ttFind);
		Tooltip ttCreate = new Tooltip("Create Modpacks");
		ttCreate.setFont(Font.font("Liberation Sans", 18));
		Tooltip.install(vboxCreate, ttCreate);
		Tooltip ttConsole = new Tooltip("Console");
		ttConsole.setFont(Font.font("Liberation Sans", 18));
		Tooltip.install(vboxConsole, ttConsole);
		Tooltip ttSettings = new Tooltip("Settings");
		ttSettings.setFont(Font.font("Liberation Sans", 18));
		Tooltip.install(vboxSettings, ttSettings);
	}

	private void resetGraphics() {
		instances.setEffect(new DropShadow(20, Color.BLACK));
		instances.setFill(Color.DIMGRAY);
		find.setEffect(new DropShadow(20, Color.BLACK));
		find.setFill(Color.DIMGRAY);
		create.setEffect(new DropShadow(20, Color.BLACK));
		create.setFill(Color.DIMGRAY);
		console.setEffect(new DropShadow(20, Color.BLACK));
		console.setFill(Color.DIMGRAY);
		settings.setEffect(new DropShadow(20, Color.BLACK));
		settings.setFill(Color.DIMGRAY);
	}

	private void loadFXML(URL resource) {
		try {
			FXMLLoader loader = new FXMLLoader(resource);
			loader.setResources(Main.getTranslation());
			content.setCenter(loader.load());
		} catch (IOException e) {
			// TODO: Logging
			e.printStackTrace();
		}
	}

	@FXML
	public void openSettings(MouseEvent mouseEvent) {
		resetGraphics();
		settings.setFill(Color.WHITE);
		settings.setEffect(new DropShadow(20, Color.MAGENTA));
		loadFXML(getClass().getResource("settings.fxml"));
	}

	public void openConsole(MouseEvent mouseEvent) {
		resetGraphics();
		console.setFill(Color.WHITE);
		console.setEffect(new DropShadow(20, Color.MAGENTA));
		loadFXML(getClass().getResource("console.fxml"));
	}

	public void openCreate(MouseEvent mouseEvent) {
		resetGraphics();
		create.setFill(Color.WHITE);
		create.setEffect(new DropShadow(20, Color.MAGENTA));
		loadFXML(MainFormController.class.getResource("MainForm.fxml"));
	}

	public void openFind(MouseEvent mouseEvent) {
		resetGraphics();
		find.setFill(Color.WHITE);
		find.setEffect(new DropShadow(20, Color.MAGENTA));
		loadFXML(getClass().getResource("find.fxml"));
	}

	public void openInstances(MouseEvent mouseEvent) {
		resetGraphics();
		instances.setFill(Color.WHITE);
		instances.setEffect(new DropShadow(20, Color.MAGENTA));
		loadFXML(getClass().getResource("instances.fxml"));
	}

	public void clickGrid(MouseEvent mouseEvent) {
	}

	@Override
	public void setStatus(String string) {

	}

	@Override
	public void log(String msg) {

	}

	@Override
	public Profile requestLogin(String username) {
		return null;
	}

	@Override
	public DownloadQueue submitNewQueue(String queueName, String parent, Collection<Downloadable> files, File basePath, File cachePath) {
		return null;
	}

	@Override
	public DownloadQueue submitAssetsQueue(String queueName, String parent, MinecraftVersion version) {
		return null;
	}

	@Override
	public void alert(String msg) {

	}

	@Override
	public TokenResponse refreshAuth(MSAProfile msaProfile) {
		return null;
	}

	@Override
	public void onQueueFinished(DownloadQueue queue) {

	}

	@Override
	public void onQueueProgress(DownloadQueue queue) {

	}

	@Override
	public void printMessage(String msg) {

	}

	@Override
	public void settingsChanged(Settings newSettings) {

	}
}
