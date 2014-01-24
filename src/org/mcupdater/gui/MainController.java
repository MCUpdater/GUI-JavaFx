package org.mcupdater.gui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mcupdater.DownloadQueue;
import org.mcupdater.Downloadable;
import org.mcupdater.MCUApp;
import org.mcupdater.TrackerListener;
import org.mcupdater.model.ServerList;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.translate.TranslateProxy;
import org.mcupdater.util.ServerPackParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.net.URL;
import java.util.*;

public class MainController extends MCUApp implements Initializable, TrackerListener
{

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
	public Label lblStatus;
	public ProfilePane profiles;
	public Button btnUpdate;
	public Button btnLaunch;
	//public Tab tabPackXML;
	//public NewsBrowser xmlBrowser;
	private int updateCounter = 0;

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
		profiles.refreshProfiles(SettingsManager.getInstance().getSettings());
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
		btnUpdate.setText(translate.update);
		btnLaunch.setText(translate.launchMinecraft);
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
					log("Unable to get server information from " + serverUrl);
				}
			} catch (Exception e) {
				log(ExceptionUtils.getStackTrace(e));
			}
		}
		if (listInstances != null) {
			listInstances.setItems(FXCollections.observableList(slList));
		}
	}

	public void doUpdate(ActionEvent event) {

	}

	public void doLaunch(ActionEvent event) {

	}

	public void instanceClicked(MouseEvent event) {
		instanceChanged(listInstances.getSelectionModel().getSelectedItem());
	}

	public void instanceChanged(ServerList selected) {
		newsBrowser.navigate(selected.getNewsUrl());
	}

	@Override
	public void setStatus(String newStatus) {
		lblStatus.setText(newStatus);
	}

	@Override
	public void addProgressBar(String title, String parent) {
		progress.addProgressBar(title, parent);
	}

	@Override
	public void log(String msg) {
		baseLogger.info(msg);
	}

	@Override
	public boolean requestLogin() {
		return false;
	}

	@Override
	public void addServer(ServerList entry) {

	}

	@Override
	public DownloadQueue submitNewQueue(String queueName, String parent, Collection<Downloadable> files, File basePath, File cachePath) {
		progress.addProgressBar(queueName, parent);
		if (profiles.getSelectedProfile() != null) {
			return new DownloadQueue(queueName, parent, this, files, basePath, cachePath, profiles.getSelectedProfile().getName());
		} else {
			return new DownloadQueue(queueName, parent, this, files, basePath, cachePath);
		}
	}

	@Override
	public DownloadQueue submitAssetsQueue(String queueName, String parent, MinecraftVersion version) {
		return null;
	}

	@Override
	public void onQueueFinished(DownloadQueue queue) {
		synchronized (progress) {
			log(queue.getParent() + " - " + queue.getName() + ": Finished!"); //TODO: i18n
			if (progress != null) {
				progress.updateProgress(queue.getName(), queue.getParent(), 1f, queue.getTotalFileCount(), queue.getSuccessFileCount());
			}
		}
	}

	@Override
	public void onQueueProgress(DownloadQueue queue) {
		updateCounter++;
		if (updateCounter == 10) {
			synchronized (progress) {
				if (progress != null) {
					progress.updateProgress(queue.getName(),queue.getParent(),queue.getProgress(),queue.getTotalFileCount(),queue.getSuccessFileCount());
				}
			}
			updateCounter = 0;
		}
	}

	@Override
	public void printMessage(String msg) {
		log(msg);
	}

	public void setSelectedInstance(String instanceId) {}
}
