package org.mcupdater.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;

public class MessageDialog
{
	public Label lblMessage;
	public Button btnOK;

	public static void showMessage(Window parent, String message, String title) {
        Stage dialog = new Stage(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        try {
            FXMLLoader loader = new FXMLLoader(MessageDialog.class.getResource("MessageDialog.fxml"));
            Parent root = (Parent) loader.load();
            Scene scene = new Scene(root);
            dialog.setTitle(title);
            dialog.setScene(scene);
            MessageDialog controller = loader.getController();
            controller.lblMessage.setText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dialog.showAndWait();
    }

	public void dismiss() {
		btnOK.getScene().getWindow().hide();
	}
}
