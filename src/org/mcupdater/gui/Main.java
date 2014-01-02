package org.mcupdater.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by sbarbour on 1/2/14.
 */
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainDialog.fxml"));

        Scene scene = new Scene(root, 1175, 600);

        stage.setTitle("MCUpdater 3.2.0");
        stage.setScene(scene);
        stage.show();
    }
}
