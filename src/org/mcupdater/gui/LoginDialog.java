package org.mcupdater.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.mcupdater.settings.Profile;

import java.io.IOException;

public class LoginDialog {
    private static Profile newProfile;
    public Label lblUser;
    public Label lblPassword;
    public TextField txtUser;
    public PasswordField txtPassword;
    public Button btnOK;
    public Button btnCancel;

    public static Profile doLogin(Stage parent, String initialUsername) {
        newProfile = new Profile();
        newProfile.setStyle("Invalid");
        Stage dialog = new Stage(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        try {
            FXMLLoader loader = new FXMLLoader(LoginDialog.class.getResource("LoginDialog.fxml"));
            Parent root = (Parent) loader.load();
            Scene scene = new Scene(root);
            dialog.setTitle("Login");
            dialog.setScene(scene);
            LoginDialog controller = loader.getController();
            controller.txtUser.setText(initialUsername);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        dialog.showAndWait();
        return newProfile;
    }
}
