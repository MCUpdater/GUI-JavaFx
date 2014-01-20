package org.mcupdater.gui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.util.Callback;
import org.mcupdater.model.ServerList;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.translate.TranslateProxy;
import org.mcupdater.util.ServerPackParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

	private static MainController INSTANCE;
	public NewsBrowser newsBrowser;
    public ListView<ServerList> listInstances;
	public Tab tabNews;
	public Tab tabConsole;
	public Tab tabSettings;
	public Tab tabModules;
	public Tab tabProgress;
	public Label lblInstances;
	public ProgressView progress;

	public MainController() {
		INSTANCE = this;
	}

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
		refreshInstanceList();
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

	public static MainController getInstance()
	{
		return INSTANCE;
	}

	public void refreshInstanceList()
	{
		Settings current = SettingsManager.getInstance().getSettings();
		List<ServerList> slList = new ArrayList<>();

		Set<String> urls = new HashSet<>();
		urls.addAll(current.getPackURLs());

		for (String serverUrl : urls)
		{
			try {
				Element docEle;
				Document serverHeader = ServerPackParser.readXmlFromUrl(serverUrl);
				if (!(serverHeader == null))
				{
					Element parent = serverHeader.getDocumentElement();
					if (parent.getNodeName().equals("ServerPack")) {
						String mcuVersion = parent.getAttribute("version");
						NodeList servers = parent.getElementsByTagName("Server");
						for (int i = 0; i < servers.getLength(); i++)
						{
							docEle = (Element)servers.item(i);
							ServerList sl = ServerList.fromElement(mcuVersion, serverUrl, docEle);
							if (!sl.isFakeServer()) { slList.add(sl); }
						}
					} else {
						ServerList sl = ServerList.fromElement("1.0", serverUrl, parent);
						slList.add(sl);
					}
				} else {
					//TODO: Log
					System.out.println("Unable to get server information from " + serverUrl);
				}
			} catch (Exception e) {
				//TODO: Log
				//apiLogger.log(Level.SEVERE, "General Error", e);
				e.printStackTrace();
			}
		}
		if (listInstances != null) {
			listInstances.setItems(FXCollections.observableList(slList));
		}
	}
}
