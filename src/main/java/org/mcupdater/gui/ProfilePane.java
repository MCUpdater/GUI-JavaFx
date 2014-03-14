package org.mcupdater.gui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.translate.TranslateProxy;

public class ProfilePane extends GridPane
{
	private final ImageView imgFace;
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
		TranslateProxy translate = Main.getTranslation();
		imgFace = new ImageView();
		imgFace.setFitHeight(16);
		lblProfile = new Label(translate.profile);
		cmbProfile = new ComboBox<>();
		cmbProfile.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event) {
				if (!skipEvents) {
					changeProfile(cmbProfile.getSelectionModel().getSelectedItem());
				}
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

	private void changeProfile(Profile selectedItem) {
		try {
			MainController.getInstance().setSelectedInstance(selectedItem.getLastInstance());
			String avatarUrl = "http://cravatar.eu/helmavatar/" + selectedItem.getName() + "/16";
			imgFace.setImage(new Image(avatarUrl));
		} catch (Exception e) {
			e.printStackTrace();
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
