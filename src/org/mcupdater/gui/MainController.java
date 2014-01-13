package org.mcupdater.gui;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.util.Callback;
import org.mcupdater.model.ServerList;
import org.mcupdater.translate.TranslateProxy;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    public NewsBrowser newsBrowser;
    public ListView<ServerList> listInstances;
	public Tab tabNews;
	public Tab tabConsole;
	public Tab tabSettings;
	public Tab tabModules;
	public Tab tabProgress;
	public Label lblInstances;

	@Override
    public void initialize(URL url, ResourceBundle rb) {
        listInstances.setCellFactory(new Callback<ListView<ServerList>, ListCell<ServerList>>(){

            @Override
            public ListCell<ServerList> call(ListView<ServerList> serverListListView) {
                return new InstanceListCell();
            }
        });
		setupControls();
        System.out.println("Initialized");
    }

	private void setupControls()
	{
		TranslateProxy translate = Main.getTranslation();
		lblInstances.setText(translate.instances);
		tabNews.setText(translate.news);
		tabConsole.setText(translate.console);
		tabSettings.setText(translate.settings);
		tabModules.setText(translate.modules);
		tabProgress.setText(translate.progress);
	}

	public void ClickButton(@SuppressWarnings("UnusedParameters") ActionEvent actionEvent) {
        newsBrowser.navigate("http://www.google.com");
    }
}
