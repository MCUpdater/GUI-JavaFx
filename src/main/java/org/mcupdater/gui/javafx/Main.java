package org.mcupdater.gui.javafx;

import org.mcupdater.api.Version;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		// TODO: switch to FXML
		StackPane root = new StackPane();
		
		Scene scene = new Scene(root, 1175, 600);
		
		stage.setTitle("MCUpdater "+Version.VERSION);
		stage.setScene(scene);
		stage.getIcons().add(new Image(Main.class.getResourceAsStream("mcu-icon.png")));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
