package org.mcupdater.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.mcupdater.*;
import org.mcupdater.instance.Instance;
import org.mcupdater.model.*;
import org.mcupdater.mojang.AssetIndex;
import org.mcupdater.mojang.AssetManager;
import org.mcupdater.mojang.Library;
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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
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
	public ConsolePane mcuConsole;
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
			mcuHandler.setLevel(Level.CONFIG);
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
		daemonMonitor = new Thread(){
			private ServerList currentSelection;
			private int activeJobs = 0;
			private boolean playState;

			@Override
			public void run() {
				int x=0;
				//noinspection InfiniteLoopStatement
				while(true){
					if (x >= 1000000) {
						x=0;
						activeJobs=99;
						currentSelection = null;
						playState = false;
						if (progress.getActiveCount() > 0) {
							baseLogger.finest("Active jobs: " + progress.getActiveJobs());
						}
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
					} catch (Exception e) {
						e.printStackTrace();
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
		listInstances.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ServerList>()
		{
			@Override
			public void changed(ObservableValue<? extends ServerList> observableValue, ServerList oldSL, ServerList newSL) {
				instanceChanged(newSL);
			}
		});
		ConsoleHandler consoleHandler = new ConsoleHandler(mcuConsole);
		consoleHandler.setLevel(Level.INFO);
		baseLogger.addHandler(consoleHandler);
		MCUpdater.apiLogger.addHandler(consoleHandler);
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
			//e.printStackTrace();
			instData = new Instance();
		}
		Set<String> digests = new HashSet<>();
		List<Module> fullModList = ServerPackParser.loadFromURL(selected.getPackUrl(), selected.getServerId());
		for (Module mod : fullModList) {
			if ( !mod.getMD5().isEmpty() ) { digests.add(mod.getMD5()); }
			for (ConfigFile cf : mod.getConfigs()) {
				if ( !cf.getMD5().isEmpty() ) { digests.add(cf.getMD5()); }
			}
			for (GenericModule sm : mod.getSubmodules()) {
				if ( !sm.getMD5().isEmpty() ) { digests.add(sm.getMD5()); }
			}
		}
		instData.setHash(MCUpdater.calculateGroupHash(digests));

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
		btnLaunch.setDisable(true);
		Profile launchProfile = profiles.getSelectedProfile();
		if (!(launchProfile == null)) {
			SettingsManager.getInstance().getSettings().setLastProfile(launchProfile.getName());
			SettingsManager.getInstance().getSettings().findProfile(launchProfile.getName()).setLastInstance(selected.getServerId());
			if (!SettingsManager.getInstance().isDirty()) {
				SettingsManager.getInstance().saveSettings();
			}
			try {
				tryLaunch(selected, pnlModule.getModules(), launchProfile);
			} catch (Exception e) {
				log(e.getMessage());
				MessageDialog.showMessage(pnlContent.getScene().getWindow(), (e.getMessage() + "\n\nNote: An authentication error can occur if your profile is out of sync with Mojang's servers.\nRe-add your profile in the Settings tab to resync with Mojang."), "MCUpdater");
			}
		}
	}

	private void tryLaunch(ServerList selected, List<ModuleEntry> modules, Profile user) throws Exception {
		String playerName = user.getName();
		String sessionKey = user.getSessionKey(this);
		MinecraftVersion mcVersion = MinecraftVersion.loadVersion(selected.getVersion());
		String indexName = mcVersion.getAssets();
		if (indexName == null) {
			indexName = "legacy";
		}
		String mainClass;
		List<String> args = new ArrayList<>();
		StringBuilder clArgs = new StringBuilder(mcVersion.getMinecraftArguments());
		List<String> libs = new ArrayList<>();
		MCUpdater mcu = MCUpdater.getInstance();
		File indexesPath = mcu.getArchiveFolder().resolve("assets").resolve("indexes").toFile();
		File indexFile = new File(indexesPath, indexName + ".json");
		String json;
		json = FileUtils.readFileToString(indexFile);
		AssetIndex index = gson.fromJson(json, AssetIndex.class);
		Settings settings = SettingsManager.getInstance().getSettings();
		if (settings.isFullScreen()) {
			clArgs.append(" --fullscreen");
		} else {
			clArgs.append(" --width " + settings.getResWidth() + " --height " + settings.getResHeight());
		}
		if (settings.isAutoConnect() && selected.isAutoConnect()) {
			URI address;
			try {
				address = new URI("my://" + selected.getAddress());
				clArgs.append(" --server " + address.getHost());
				if (address.getPort() != -1) {
					clArgs.append(" --port " + address.getPort());
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		clArgs.append(" --resourcePackDir ${resource_packs}");
		args.add((new File(settings.getJrePath()).toPath().resolve("bin").resolve("java").toString()));
		args.add("-Xms" + settings.getMinMemory());
		args.add("-Xmx" + settings.getMaxMemory());
		args.add("-XX:PermSize=" + settings.getPermGen());
		args.addAll(Arrays.asList(settings.getJvmOpts().split(" ")));
		if (System.getProperty("os.name").startsWith("Mac")) {
			args.add("-Xdock:icon=" + mcu.getArchiveFolder().resolve("assets").resolve("icons").resolve("minecraft.icns").toString());
			args.add("-Xdock:name=Minecraft(MCUpdater)");
		}
		args.add("-Djava.library.path=" + mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("lib").resolve("natives").toString());
		if (!Version.requestedFeatureLevel(selected.getVersion(), "1.6")){
			args.add("-Dminecraft.applet.TargetDirectory=" + mcu.getInstanceRoot().resolve(selected.getServerId()).toString());
		}
		if (!selected.getMainClass().isEmpty()) {
			mainClass = selected.getMainClass();
		} else {
			mainClass = mcVersion.getMainClass();
		}
		for (ModuleEntry entry : modules) {
			if (entry.isSelected()) {
				if (entry.getModule().getModType().equals(ModType.Library)) {
					libs.add(entry.getModule().getId() + ".jar");
				}
				if (!entry.getModule().getLaunchArgs().isEmpty()) {
					clArgs.append(" " + entry.getModule().getLaunchArgs());
				}
				if (!entry.getModule().getJreArgs().isEmpty()) {
					args.addAll(Arrays.asList(entry.getModule().getJreArgs().split(" ")));
				}
				if (entry.getModule().hasSubmodules()) {
					for (GenericModule sm : entry.getModule().getSubmodules()) {
						if (sm.getModType().equals(ModType.Library)) {
							libs.add(sm.getId() + ".jar");
						}
						if (!sm.getLaunchArgs().isEmpty()) {
							clArgs.append(" " + sm.getLaunchArgs());
						}
						if (!sm.getJreArgs().isEmpty()) {
							args.addAll(Arrays.asList(sm.getJreArgs().split(" ")));
						}
					}
				}
			}
		}
		for (Library lib : mcVersion.getLibraries()) {
			if (lib.validForOS() && !lib.hasNatives()) {
				libs.add(lib.getFilename());
			}
		}
		args.add("-cp");
		StringBuilder classpath = new StringBuilder();
		for (String entry : libs) {
			classpath.append(mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("lib").resolve(entry).toString()).append(MCUpdater.cpDelimiter());
		}
		classpath.append(mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("bin").resolve("minecraft.jar").toString());
		args.add(classpath.toString());
		args.add(mainClass);
		String tmpclArgs = clArgs.toString();
		Map<String,String> fields = new HashMap<>();
		StrSubstitutor fieldReplacer = new StrSubstitutor(fields);
		fields.put("auth_player_name", playerName);
		fields.put("auth_uuid", user.getUUID());
		fields.put("auth_access_token", user.getAccessToken());
		fields.put("auth_session", sessionKey);
		fields.put("version_name", selected.getVersion());
		fields.put("game_directory", mcu.getInstanceRoot().resolve(selected.getServerId()).toString());
		if (index.isVirtual()) {
			fields.put("game_assets", mcu.getArchiveFolder().resolve("assets").resolve("virtual").toString());
		} else {
			fields.put("game_assets", mcu.getArchiveFolder().resolve("assets").toString());
		}
		fields.put("resource_packs", mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("resourcepacks").toString());
		String[] fieldArr = tmpclArgs.split(" ");
		for (int i = 0; i < fieldArr.length; i++) {
			fieldArr[i] = fieldReplacer.replace(fieldArr[i]);
		}
		args.addAll(Arrays.asList(fieldArr));

		log("Launch args:");
		log("=======================");
		for (String entry : args) {
			log(entry);
		}
		log("=======================");
		final ProcessBuilder pb = new ProcessBuilder(args);
		pb.directory(mcu.getInstanceRoot().resolve(selected.getServerId()).toFile());
		pb.redirectErrorStream(true);
		Thread gameThread = new Thread(new Runnable(){
			@Override
			public void run() {
				try{
					Process task = pb.start();
					BufferedReader buffRead = new BufferedReader(new InputStreamReader(task.getInputStream()));
					String line;
					while ((line = buffRead.readLine()) != null) {
						if (line.length() > 0) {
							consoleWrite(line);
						}
					}
				} catch (Exception e) {
					consoleWrite(e.getMessage());
				} finally {
					setPlaying(false);
				}
			}
		});
		gameThread.start();
		setPlaying(true);
	}

	private void consoleWrite(final String message) {
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				mcuConsole.console.appendText(message + "\n");
			}
		});
	}

	public void instanceChanged(ServerList entry) {
		this.selected = entry;
		if (entry != null) {
			newsBrowser.navigate(selected.getNewsUrl());
			List<Module> modList = ServerPackParser.loadFromURL(selected.getPackUrl(), selected.getServerId());
			Set<String> digests = new HashSet<>();
			for (Module mod : modList) {
				if ( !mod.getMD5().isEmpty() ) { digests.add(mod.getMD5()); }
				for (ConfigFile cf : mod.getConfigs()) {
					if ( !cf.getMD5().isEmpty() ) { digests.add(cf.getMD5()); }
				}
				for (GenericModule sm : mod.getSubmodules()) {
					if ( !sm.getMD5().isEmpty() ) { digests.add(sm.getMD5()); }
				}
			}
			String remoteHash = MCUpdater.calculateGroupHash(digests);
			//System.out.println("Hash: " + MCUpdater.calculateGroupHash(digests));
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
			boolean needUpdate = (instData.getHash().isEmpty() || !instData.getHash().equals(remoteHash));
			boolean needNewMCU = Version.isVersionOld(entry.getMCUVersion());

			if (needUpdate) {
				MessageDialog.showMessage(pnlContent.getScene().getWindow(), Main.getTranslation().updateRequired, "MCUpdater");
			}
			if (needNewMCU) {
				MessageDialog.showMessage(pnlContent.getScene().getWindow(), Main.getTranslation().oldMCUpdater, "MCUpdater");
			}
		}
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
			for (Downloadable entry : queue.getFailures()) {
				System.out.println("Failed: " + entry.getFilename());
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
