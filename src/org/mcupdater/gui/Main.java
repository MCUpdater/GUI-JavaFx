package org.mcupdater.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.mcupdater.translate.Languages;
import org.mcupdater.translate.TranslateProxy;

public class Main extends Application {
	private static TranslateProxy translation;

    public static void main(String[] args) {
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

        stage.setTitle("MCUpdater 3.2.0");
        stage.setScene(scene);
	    stage.getIcons().add(new Image(Main.class.getResourceAsStream("mcu-icon.png")));
        stage.show();
    }

	public static TranslateProxy getTranslation() {
		return translation;
	}
}
