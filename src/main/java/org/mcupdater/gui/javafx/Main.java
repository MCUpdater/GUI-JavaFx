package org.mcupdater.gui.javafx;

import org.mcupdater.api.Version;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
		
		Scene scene = new Scene(root, 1175, 600);
		
		stage.setTitle("MCUpdater "+Version.VERSION);
		stage.setScene(scene);
		stage.getIcons().add(getImage("icons/mcu-icon.png"));
		stage.show();
	}
	
	public Image getImage(String filename) {
		return new Image(getClass().getResourceAsStream(filename));
	}

	public static void main(String[] args) {
		launch(args);
	}

}
