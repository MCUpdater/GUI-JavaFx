package org.mcupdater.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.mcupdater.Version;
import org.mcupdater.translate.Languages;
import org.mcupdater.translate.TranslateProxy;
import org.mcupdater.util.MCUpdater;

import java.io.File;

public class Main extends Application {
	private static TranslateProxy translation;
	private static String defaultPackURL;

    public static void main(String[] args) {
	    OptionParser optParser = new OptionParser();
	    ArgumentAcceptingOptionSpec<String> packSpec = optParser.accepts("ServerPack").withRequiredArg().ofType(String.class);
	    ArgumentAcceptingOptionSpec<File> rootSpec = optParser.accepts("MCURoot").withRequiredArg().ofType(File.class);
	    OptionSet options = optParser.parse(args);
	    MCUpdater.getInstance(options.valueOf(rootSpec));
	    setDefaultPackURL(options.valueOf(packSpec));
	    try {
		    translation = Languages.valueOf(Languages.getLocale()).getProxy();
	    } catch (Exception e) {
		    System.out.println("No translation for " + Languages.getLocale() + "!");
		    translation = Languages.en_US.getProxy();
	    }
        launch(args);
    }

	@Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainDialog.fxml"));

        Scene scene = new Scene(root, 1175, 600);

        stage.setTitle("MCUpdater " + Version.VERSION);
        stage.setScene(scene);
	    stage.getIcons().add(new Image(Main.class.getResourceAsStream("mcu-icon.png")));
        stage.show();
    }

	public static TranslateProxy getTranslation() {
		return translation;
	}

	public static String getDefaultPackURL() {
		return defaultPackURL;
	}

	public static void setDefaultPackURL(String defaultPackURL) {
		Main.defaultPackURL = defaultPackURL;
	}
}
