package org.mcupdater.gui.javafx.components;

import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.mcupdater.auth.*;
import org.mcupdater.gui.javafx.Main;
import org.mcupdater.gui.javafx.MainController;
import org.mcupdater.settings.BasicProfile;
import org.mcupdater.settings.MSAProfile;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.YggdrasilProfile;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class LoginDialog extends Dialog<Profile> {
    private static Profile newProfile;
    public Label lblUser;
    public Label lblPassword;
    public TextField txtUser;
    public PasswordField txtPassword;
    public Button btnOK;
    public Button btnCancel;
    public Label lblError;
    public Button btnMicrosoft;
    public HBox hbox;

    public static Profile doLogin(Window parent, String initialUsername) {
        newProfile = new BasicProfile("Invalid");
        Stage dialog = new Stage(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        try {
            FXMLLoader loader = new FXMLLoader(LoginDialog.class.getResource("LoginDialog.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            dialog.setScene(scene);
            LoginDialog controller = loader.getController();
            controller.lblError.setText("");
            ResourceBundle translate = Main.getTranslation();
            dialog.setTitle(translate.getString("loginTitle"));
            controller.lblUser.setText(String.format("%s:",translate.getString("loginUser")));
            controller.lblPassword.setText(String.format("%s:",translate.getString("loginPassword")));
            controller.txtUser.setText(initialUsername);
            controller.btnMicrosoft.setText(translate.getString("loginMicrosoft"));
            controller.btnOK.setText(translate.getString("login"));
            controller.btnCancel.setText(translate.getString("cancel"));
            GridPane.setHgrow(controller.hbox, Priority.ALWAYS);
            controller.hbox.setPrefWidth(200.0);
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
            YggdrasilProfile yggProfile = new YggdrasilProfile();
            YggdrasilUserAuthentication authResponse = (YggdrasilUserAuthentication) response;
            yggProfile.setUsername(txtUser.getText());
            yggProfile.setAccessToken(authResponse.getAuthenticatedToken());
            yggProfile.setName(authResponse.getSelectedProfile().getName());
            yggProfile.setUUID(authResponse.getSelectedProfile().getId().toString());
            yggProfile.setUserId(authResponse.getUserID());
            yggProfile.setLegacy((UserType.LEGACY == authResponse.getUserType()));
            btnOK.getScene().getWindow().hide();
            newProfile = yggProfile;
        } else if (response instanceof AuthenticationException) {
            lblError.setText(((AuthenticationException) response).getMessage());
        }
    }

    public void cancelLogin() {
        btnCancel.getScene().getWindow().hide();
    }

    public void doMicrosoftLogin() {
        Dialog<String> msftLoginDialog = new Dialog<>();
        AtomicReference<String> debug = new AtomicReference<>("");
        ButtonType buttonLogin = new ButtonType("Finish login", ButtonBar.ButtonData.FINISH);
        ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        msftLoginDialog.getDialogPane().getButtonTypes().addAll(buttonLogin,buttonCancel);
        msftLoginDialog.setResultConverter(buttonType -> {
            if (buttonType.equals(buttonLogin)) {
                // Create Profile entry
                return debug.get();
            } else {
                // Return null Profile
                return null;
            }
        });
        msftLoginDialog.getDialogPane().lookupButton(buttonLogin).setDisable(true);
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        engine.setOnError((event) -> MainController.getInstance().baseLogger.severe(event.getMessage()));
        engine.setOnAlert((event) -> MainController.getInstance().baseLogger.warning(event.getData()));
        engine.load(MicrosoftAuth.getAuthUrl());
        webView.setPrefHeight(406);
        webView.setPrefWidth(406);
        engine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.startsWith(MicrosoftAuth.redirectUri)) {
                String authCode = newValue.substring(newValue.indexOf("code=")+5,newValue.indexOf("&"));
                MainController.getInstance().baseLogger.info("authCode: " + authCode);
                debug.set(authCode);
                TokenResponse token = MicrosoftAuth.getAuthToken(authCode);
                XBLToken xblToken = MicrosoftAuth.getXBLAuth(token.getAccessToken());
                XBLToken xstsToken = MicrosoftAuth.getXSTSAuth(xblToken.getToken());
                MCToken mcToken = MicrosoftAuth.getMCToken(xstsToken.getDisplayClaims().getXui()[0].getUhs(), xstsToken.getToken());
                MCProfile mcProfile = MicrosoftAuth.getMinecraftProfile(mcToken.getAccessToken());
                MainController.getInstance().baseLogger.info("Owns Minecraft: " + MicrosoftAuth.isMinecraftOwned(mcToken.getAccessToken()));
                MainController.getInstance().baseLogger.info("Minecraft player name: " + MicrosoftAuth.getMinecraftProfile(mcToken.getAccessToken()).getName());
                MainController.getInstance().baseLogger.info("access_token: " + mcToken.getAccessToken());
                MSAProfile msaProfile = new MSAProfile();
                msaProfile.setRefreshToken(token.getRefreshToken());
                msaProfile.setAuthAccessToken(mcToken.getAccessToken());
                msaProfile.setName(mcProfile.getName());
                msaProfile.setUUID(mcProfile.getId());
                newProfile = msaProfile;
                try {
                    Platform.runLater(() -> {
                        String content = String.format("<html><body style=\"background: radial-gradient(circle, #99990099, #00000000),url('http://files.mcupdater.com/images/bg_main.png');\"><table style=\"margin-left: auto; margin-right: auto;\"><tbody><tr><td style=\"text-align: center;\"><img src=\"https://cravatar.eu/helmhead/%s/240.png\"></td></tr><tr><td style=\"text-align: center;\"><h4><span style=\"font-family: helvetica, arial, sans-serif; color: #ffffff; text-shadow: 2px 2px black;\">You have successfully logged in as</span></h4><br><h2><span style=\"font-family: helvetica, arial, sans-serif; color: #ffffff; text-shadow: 2px 2px black;\">%s</span></h2></td></tr></tbody></table></body></html>",mcProfile.getId(),mcProfile.getName());
                        engine.loadContent(content);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                msftLoginDialog.getDialogPane().lookupButton(buttonLogin).setDisable(false);
            }
        });
        msftLoginDialog.getDialogPane().setContent(webView);
        Optional<String> result = msftLoginDialog.showAndWait();
        result.ifPresent(x -> {
            System.out.println(x);
            btnCancel.fire();
        });
    }

}