package org.mcupdater.gui.javafx.components;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.VPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import org.mcupdater.gui.javafx.Main;
import org.mcupdater.gui.javafx.MainController;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsManager;

import java.util.ResourceBundle;
import java.util.logging.Level;

public class ProfilePane extends GridPane
{
    private final ImageView imgFace = new ImageView();
    private Image avatar;
    private final Label lblProfile;
    private final ComboBox<Profile> cmbProfile;
    private boolean skipEvents = false;

    public Profile getSelectedProfile() {
        return cmbProfile.getSelectionModel().getSelectedItem();
    }

    public void refreshProfiles() {
        skipEvents = true;
        Profile selected = cmbProfile.getSelectionModel().getSelectedItem();
        Settings settings = SettingsManager.getInstance().getSettings();
        cmbProfile.setItems(FXCollections.observableList(settings.getProfiles()));
        cmbProfile.getSelectionModel().select(selected);
        skipEvents = false;
    }

    public ProfilePane() {
        ResourceBundle translate = Main.getTranslation();
        //setAvatar(new Image(MainController.class.getResource("icons/cross.png").toString(),16,16, true, true));
        imgFace.setImage(avatar);
        lblProfile = new Label(translate.getString("profile"));
        lblProfile.setTextFill(Color.WHITE);
        cmbProfile = new ComboBox<>();
        cmbProfile.addEventHandler(ActionEvent.ACTION, event -> {
            if (!skipEvents) {
                changeProfile(cmbProfile.getSelectionModel().getSelectedItem());
            }
        });
        cmbProfile.setMinWidth(100);
        GridPane.setColumnIndex(imgFace, 0);
        GridPane.setColumnIndex(lblProfile,1);
        GridPane.setColumnIndex(cmbProfile,2);
        setHgap(3);
        setValignment(imgFace, VPos.CENTER);
        setValignment(lblProfile, VPos.CENTER);
        setValignment(cmbProfile, VPos.CENTER);
        getRowConstraints().add(new RowConstraints(30,30,30, Priority.ALWAYS,VPos.CENTER, true));
        getChildren().addAll(imgFace, lblProfile, cmbProfile);
    }

    public void setAvatar(Image avatar) {
        Platform.runLater(() -> {
            this.avatar = avatar;
            imgFace.setImage(avatar);
        });
    }

    private void changeProfile(Profile selectedItem) {
        try {
            MainController.getInstance().setSelectedInstance(selectedItem.getLastInstance());
            String avatarUrl = "https://cravatar.eu/helmhead/" + selectedItem.getName();
            setAvatar(new Image(avatarUrl,32,32,true,true));
        } catch (Exception e) {
            MainController.getInstance().baseLogger.log(Level.SEVERE, "Failed to fully load profile", e);
        }
    }

    public void setSelectedProfile(String selectedProfile) {
        for (Profile entry : cmbProfile.getItems()) {
            if (entry.getName().equals(selectedProfile)) {
                cmbProfile.getSelectionModel().select(entry);
                return;
            }
        }
    }
}