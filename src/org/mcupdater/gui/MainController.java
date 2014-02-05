package org.mcupdater.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mcupdater.*;
import org.mcupdater.instance.Instance;
import org.mcupdater.model.*;
import org.mcupdater.mojang.AssetManager;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsListener;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.translate.TranslateProxy;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.ServerPackParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController extends MCUApp implements Initializable, TrackerListener, SettingsListener
{

	private static MainController INSTANCE;
	public ModulePanel pnlModule;
	private FileHandler mcuHandler;
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
	public BorderPane pnlContent;
	//public Tab tabPackXML;
	//public NewsBrowser xmlBrowser;
	private Thread daemonMonitor;
	private int updateCounter = 0;
	private boolean playing;
	private ServerList selected;
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public MainController() {
		INSTANCE = this;
		MCUpdater.getInstance().setParent(this);
		this.baseLogger = Logger.getLogger("MCUpdater");
		baseLogger.setLevel(Level.ALL);
		try {
			mcuHandler = new FileHandler(MCUpdater.getInstance().getArchiveFolder().resolve("MCUpdater.log").toString(),0,3);
			mcuHandler.setFormatter(new FMLStyleFormatter());
			baseLogger.addHandler(mcuHandler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Version.setApp(this);
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
		SettingsManager.getInstance().addListener(this);
		Settings settings = SettingsManager.getInstance().getSettings();
		MCUpdater.getInstance().setInstanceRoot(new File(settings.getInstanceRoot()).toPath());
		Profile newProfile;
		if (settings.getProfiles().size() == 0) {
			newProfile = LoginDialog.doLogin(pnlContent.getScene().getWindow(), "");
			if (newProfile.getStyle().equals("Yggdrasil")) {
				settings.addOrReplaceProfile(newProfile);
				settings.setLastProfile(newProfile.getName());
				if (!SettingsManager.getInstance().isDirty()) {
					SettingsManager.getInstance().saveSettings();
				}
			}
		} else {
			newProfile = settings.findProfile(settings.getLastProfile());
		}
		refreshInstanceList();
		refreshProfiles();
		profiles.setSelectedProfile(newProfile.getName());
		daemonMonitor = new Thread(){
			private ServerList currentSelection;
			private int activeJobs = 0;
			private boolean playState;

			@Override
			public void run() {
				int x=0;
				//noinspection InfiniteLoopStatement
				while(true){
					if (x >= 100000) {
						x=0;
						activeJobs=99;
						currentSelection = null;
						playState = false;
					} else {
						x++;
					}
					try {
						if (activeJobs != progress.getActiveCount() || currentSelection != listInstances.getSelectionModel().getSelectedItem() || playState != isPlaying()) {
							currentSelection = listInstances.getSelectionModel().getSelectedItem();
							activeJobs = progress.getActiveCount();
							playState = isPlaying();
							setStatus("Active jobs: " + activeJobs);
							if (activeJobs > 0) {
								btnLaunch.setDisable(true);
							} else {
								if (!(currentSelection == null) && !playState) {
									btnLaunch.setDisable(false);
								} else {
									btnLaunch.setDisable(true);
								}
							}
							if (!(currentSelection == null)) {
								if (progress.getActiveById(currentSelection.getServerId()) > 0) {
									btnUpdate.setDisable(true);
								} else {
									btnUpdate.setDisable(false);
								}
							} else {
								btnUpdate.setDisable(true);
							}
						}
					} finally {
					}
				}
			}
		};
		daemonMonitor.setDaemon(true);
		daemonMonitor.start();
    }

	private boolean isPlaying() {
		return this.playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
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

	public void doUpdate() {
		btnUpdate.setDisable(true);
		MCUpdater.getInstance().getInstanceRoot().resolve(selected.getServerId()).toFile().mkdirs();

		Instance instData;
		final Path instanceFile = MCUpdater.getInstance().getInstanceRoot().resolve(selected.getServerId()).resolve("instance.json");
		try {
			BufferedReader reader = Files.newBufferedReader(instanceFile, StandardCharsets.UTF_8);
			instData = gson.fromJson(reader, Instance.class);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			instData = new Instance();
		}

		final List<GenericModule> selectedMods = new ArrayList<>();
		final List<ConfigFile> selectedConfigs = new ArrayList<>();
		for (ModuleEntry entry : pnlModule.getModules()) {
			System.out.println(entry.getModule().getName() + " - " + entry.getModule().getModType().toString());
			if (entry.isSelected()) {
				selectedMods.add(entry.getModule());
				if (entry.getModule().hasConfigs()){
					selectedConfigs.addAll(entry.getModule().getConfigs());
				}
				if (entry.getModule().hasSubmodules()) {
					selectedMods.addAll(entry.getModule().getSubmodules());
				}
			}
			if (!entry.getModule().getRequired()) {
				instData.setModStatus(entry.getModule().getId(), entry.isSelected());
			}
		}
		try {
			MCUpdater.getInstance().installMods(selected, selectedMods, selectedConfigs, false, instData, ModSide.CLIENT);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void doLaunch() {
		//TODO: Code here
	}

	public void instanceClicked() {
		instanceChanged(listInstances.getSelectionModel().getSelectedItem());
	}

	public void instanceChanged(ServerList entry) {
		this.selected = entry;
		newsBrowser.navigate(selected.getNewsUrl());
		List<Module> modList = ServerPackParser.loadFromURL(selected.getPackUrl(), selected.getServerId());
		Instance instData = new Instance();
		final Path instanceFile = MCUpdater.getInstance().getInstanceRoot().resolve(entry.getServerId()).resolve("instance.json");
		try {
			BufferedReader reader = Files.newBufferedReader(instanceFile, StandardCharsets.UTF_8);
			instData = gson.fromJson(reader, Instance.class);
			reader.close();
		} catch (IOException e) {
			baseLogger.log(Level.WARNING, "instance.json file not found.  This is not an error if the instance has not been installed.");
		}
		refreshModList(modList, instData.getOptionalMods());
	}

	private void refreshModList(List<Module> modList, Map<String, Boolean> optionalMods) {
		pnlModule.reload(modList, optionalMods);
	}

	@Override
	public void setStatus(final String newStatus) {
		Platform.runLater(new Runnable()
		{
			@Override
			public void run() {
				lblStatus.setText(newStatus);
			}
		});
	}

	@Override
	public void log(String msg) {
		baseLogger.info(msg);
	}

	@Override
	public Profile requestLogin(String username) {
		return LoginDialog.doLogin(pnlContent.getScene().getWindow(), username);
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
		progress.addProgressBar(queueName, parent);
		return AssetManager.downloadAssets(queueName, parent, MCUpdater.getInstance().getArchiveFolder().resolve("assets").toFile(), this, version);
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

	public void setSelectedInstance(String instanceId) {
		for (ServerList entry : listInstances.getItems()) {
			if (entry.getServerId().equals(instanceId)) {
				listInstances.getSelectionModel().select(entry);
				return;
			}
		}
	}

	public void refreshProfiles() {
		if (profiles != null) {
			profiles.refreshProfiles();
		}
	}

	@Override
	public void stateChanged(boolean newState) {

	}

	@Override
	public void settingsChanged(Settings newSettings) {
		MCUpdater.getInstance().setInstanceRoot(new File(newSettings.getInstanceRoot()).toPath());
	}

}
