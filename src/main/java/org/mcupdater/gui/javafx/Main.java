package org.mcupdater.gui.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.mcupdater.api.Version;
import org.mcupdater.gui.javafx.components.LoginDialog;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.util.MCUpdater;

import java.io.File;
import java.util.*;

public class Main extends Application {
	private static ResourceBundle translation;
	private static String defaultPackURL;
	public static List<String> passthroughArgs;
	public static Map<Integer,File> javaRuntimes = new HashMap<>();

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
		loader.setResources(translation);
		Parent root = loader.load();

		Scene scene = new Scene(root, 1175, 600);
		scene.getStylesheets().addAll(getClass().getResource("css/MCUpdater.css").toExternalForm());
		
		stage.setTitle("MCUpdater "+Version.VERSION);
		stage.setScene(scene);
		stage.getIcons().add(getImage("icons/mcu-icon.png"));
		stage.show();
		MainController controller = loader.getController();
		Settings settings = SettingsManager.getInstance().getSettings();
		MCUpdater.getInstance().setInstanceRoot(new File(settings.getInstanceRoot()).toPath());
		Profile newProfile;
		if (settings.getProfiles().size() == 0) {
			newProfile = LoginDialog.doLogin(stage, "");
			if (Arrays.asList("Yggdrasil","MSA").contains(newProfile.getStyle())) {
				settings.addOrReplaceProfile(newProfile);
				settings.setLastProfile(newProfile.getName());
				if (!SettingsManager.getInstance().isDirty()) {
					SettingsManager.getInstance().saveSettings();
				}
			}
		} else {
			newProfile = settings.findProfile(settings.getLastProfile());
		}
		controller.refreshInstanceList();
		controller.refreshProfiles();
		controller.profiles.setSelectedProfile(newProfile.getName());
	}
	
	public Image getImage(String filename) {
		return new Image(getClass().getResourceAsStream(filename));
	}

	public static void main(String[] args) {
		OptionParser optParser = new OptionParser();
		optParser.allowsUnrecognizedOptions();
		ArgumentAcceptingOptionSpec<String> packSpec = optParser.accepts("ServerPack").withRequiredArg().ofType(String.class);
		ArgumentAcceptingOptionSpec<File> rootSpec = optParser.accepts("MCURoot").withRequiredArg().ofType(File.class);
		ArgumentAcceptingOptionSpec<String> runtimes = optParser.accepts("runtimes").withRequiredArg().withValuesSeparatedBy(";").ofType(String.class).required();
		NonOptionArgumentSpec<String> nonOpts = optParser.nonOptions();
		OptionSet options = optParser.parse(args);
		for (String entry : options.valuesOf(runtimes)) {
			String[] nvPair = entry.split("#");
			javaRuntimes.put(Integer.valueOf(nvPair[0]), new File(nvPair[1]));
		}
		passthroughArgs = options.valuesOf(nonOpts);
		MCUpdater.getInstance(options.valueOf(rootSpec));
		setDefaultPackURL(options.valueOf(packSpec));
		Locale locale = new Locale(System.getProperty("user.language"),System.getProperty("user.country"));
		translation = ResourceBundle.getBundle("org.mcupdater.gui.javafx.translate.language",locale);
		launch(args);
	}

	public static ResourceBundle getTranslation() {
		return translation;
	}

	public static String getDefaultPackURL() {
		return defaultPackURL;
	}

	public static void setDefaultPackURL(String defaultPackURL) {
		Main.defaultPackURL = defaultPackURL;
	}
}
