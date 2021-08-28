package org.mcupdater.gui.javafx.components;

import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;

import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
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
import javafx.stage.Window;
import org.mcupdater.auth.AuthManager;
import org.mcupdater.gui.javafx.MainController;
import org.mcupdater.settings.Profile;

import java.io.IOException;
import java.util.UUID;

public class LoginDialog {
    private static Profile newProfile;
    public Label lblUser;
    public Label lblPassword;
    public TextField txtUser;
    public PasswordField txtPassword;
    public Button btnOK;
    public Button btnCancel;
    public Label lblError;

    public static Profile doLogin(Window parent, String initialUsername) {
        newProfile = new Profile();
        newProfile.setStyle("Invalid");
        Stage dialog = new Stage(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        try {
            FXMLLoader loader = new FXMLLoader(LoginDialog.class.getResource("LoginDialog.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            dialog.setTitle("Login");
            dialog.setScene(scene);
            LoginDialog controller = loader.getController();
            controller.txtUser.setText(initialUsername);
            controller.lblError.setText("");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        dialog.showAndWait();
        return newProfile;
    }

    public void validateCredentials() {
        AuthManager auth = MainController.getInstance().getAuthManager();
        Object response = auth.authenticate(txtUser.getText(), txtPassword.getText(), UUID.randomUUID().toString());
        if (response instanceof YggdrasilUserAuthentication) {
            YggdrasilUserAuthentication authResponse = (YggdrasilUserAuthentication) response;
            newProfile.setStyle("Yggdrasil");
            newProfile.setUsername(txtUser.getText());
            newProfile.setAccessToken(authResponse.getAuthenticatedToken());
            newProfile.setName(authResponse.getSelectedProfile().getName());
            newProfile.setUUID(authResponse.getSelectedProfile().getId().toString());
            newProfile.setUserId(authResponse.getUserID());
            newProfile.setLegacy((UserType.LEGACY == authResponse.getUserType()));
            btnOK.getScene().getWindow().hide();
        } else if (response instanceof AuthenticationException) {
            lblError.setText(((AuthenticationException) response).getMessage());
        }
    }

    public void cancelLogin() {
        btnCancel.getScene().getWindow().hide();
    }
}