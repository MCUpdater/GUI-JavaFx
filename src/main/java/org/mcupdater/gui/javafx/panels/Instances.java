package org.mcupdater.gui.javafx.panels;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import org.mcupdater.gui.javafx.components.InstanceListCell;
import org.mcupdater.model.v2.ServerList;
import org.mcupdater.settings.SettingsManager;

import java.net.URL;
import java.util.ResourceBundle;

public class Instances implements Initializable {
	@FXML
	public ListView<ServerList> listInstances;
	private ResourceBundle translate;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		this.translate = resourceBundle;
		listInstances.setCellFactory(serverListListView -> new InstanceListCell());
//		if (!SettingsManager.getInstance())
	}

	private void instanceChanged(ServerList newSL) {

	}

	public void doUpdate(ActionEvent actionEvent) {

	}

	public void doLaunch(ActionEvent actionEvent) {

	}

}
