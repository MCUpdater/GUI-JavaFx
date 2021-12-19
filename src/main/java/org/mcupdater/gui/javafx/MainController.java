package org.mcupdater.gui.javafx;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.mcupdater.FMLStyleFormatter;
import org.mcupdater.MCUApp;
import org.mcupdater.api.Version;
import org.mcupdater.auth.YggdrasilAuthManager;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.downloadlib.TrackerListener;
import org.mcupdater.gui.javafx.components.*;
import org.mcupdater.instance.Instance;
import org.mcupdater.model.Module;
import org.mcupdater.model.*;
import org.mcupdater.mojang.AssetIndex;
import org.mcupdater.mojang.AssetManager;
import org.mcupdater.mojang.Library;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.packbuilder.gui.MainFormController;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsListener;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.ServerPackParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController extends MCUApp implements Initializable, TrackerListener, SettingsListener {

    private static MainController INSTANCE;
    public ListView<ServerList> listInstances;
    public ProfilePane profiles;
    public Label lblStatus;
    public Label lblHard;
    public CheckBox chkHard;
    public Button btnUpdate;
    public Button btnLaunch;
    public Tab tabConsole;
    public ConsolePane mcuConsole;
    public Tab tabInstances;
    public Tab tabCreate;
    public Tab tabSearch;
    public Tab tabProgress;
    public ProgressView progress;
    public Tab tabSettings;
    public TabPane tabpaneDetail;
    public TabPane tabpaneMain;
    public Button btnReload;
    public Button btnAddURL;
    public TabPane tabpaneConsole;
    public Tab tabMainConsole;
    private ServerList selected;
    private ResourceBundle translate;
    private final Gson gson = new Gson();
    private ModulePanel currentModules;
    private int updateCounter;
    private SettingsPane paneSettings = new SettingsPane();
    private boolean playing;

    public MainController() {
        INSTANCE = this;
        MCUpdater.getInstance().setParent(this);
        this.baseLogger = Logger.getLogger("MCUpdater");
        baseLogger.setLevel(Level.ALL);
        try {
            FileHandler mcuHandler = new FileHandler(MCUpdater.getInstance().getArchiveFolder().resolve("MCUpdater.log").toString(), 0, 3);
            mcuHandler.setFormatter(new FMLStyleFormatter());
            mcuHandler.setLevel(Level.CONFIG);
            baseLogger.addHandler(mcuHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Version.setApp(this);
        this.setAuthManager(new YggdrasilAuthManager());
    }

    public static MainController getInstance() {
        return INSTANCE;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.translate = resources;
        listInstances.setCellFactory(serverListListView -> new InstanceListCell());
        if (!SettingsManager.getInstance().getSettings().getPackURLs().contains(Main.getDefaultPackURL())) {
            SettingsManager.getInstance().getSettings().addPackURL(Main.getDefaultPackURL());
            SettingsManager.getInstance().setDirty();
        }
        setupControls();
        baseLogger.info("The power is yours!");
//        System.out.println("Initialized");
        SettingsManager.getInstance().addListener(this);
        Thread daemonMonitor = new Thread() {
            private ServerList currentSelection;
            private int activeJobs = -1;
            private AtomicInteger guiUpdateValue = new AtomicInteger(-1);
            private boolean playState;

            @Override
            public void run() {
                //noinspection InfiniteLoopStatement
                while (true) {
                    try {
                        if (activeJobs != progress.getActiveCount() || currentSelection != listInstances.getSelectionModel().getSelectedItem() || playState != isPlaying()) {
                            currentSelection = listInstances.getSelectionModel().getSelectedItem();
                            playState = isPlaying();
                            if (activeJobs != progress.getActiveCount()) {
                                activeJobs = progress.getActiveCount();
                                if (guiUpdateValue.getAndSet(activeJobs) == -1) {
                                    Platform.runLater(()->{
                                        int newValue = guiUpdateValue.getAndSet(-1);
                                        tabProgress.setText(String.format("%s (%s %d)", translate.getString("progress"), translate.getString("activeJobs"), newValue));
                                    });
                                }
                            }
                            btnLaunch.setDisable(currentSelection == null || currentSelection.getState() != ServerList.State.READY || activeJobs > 0 || playState);
                            btnUpdate.setDisable(currentSelection == null || progress.getActiveById(currentSelection.getServerId()) > 0 || playState);
                        }
                        sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        daemonMonitor.setDaemon(true);
        daemonMonitor.start();
    }

    public void setPlaying(boolean newValue) {
        this.playing = newValue;
    }

    public boolean isPlaying() {
        return this.playing;
    }

    private void setupControls() {
        try {
            tabInstances.setText(translate.getString("instances"));
            FXMLLoader fxmlLoader = new FXMLLoader(MainFormController.class.getResource("MainForm.fxml"));
            Pane root = fxmlLoader.load();
            tabCreate.setContent(root);
            tabConsole.setText(translate.getString("console"));
            tabpaneConsole.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
            tabMainConsole.setClosable(false);
            tabSettings.setText(translate.getString("settings"));
            tabSettings.setContent(paneSettings);
            tabProgress.setText(translate.getString("progress"));
            btnUpdate.setText(translate.getString("update"));
            btnLaunch.setText(translate.getString("launchMinecraft"));
            lblHard.setText(translate.getString("hardUpdate"));
            btnAddURL.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/add.png"))));
            btnAddURL.setText(translate.getString("addInstance"));
            btnReload.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/arrow_refresh.png"))));
            btnReload.setText(translate.getString("reloadInstances"));
            listInstances.getSelectionModel().selectedItemProperty().addListener((observableValue, oldSL, newSL) -> {
                instanceChanged(newSL);
            });
            ConsoleHandler consoleHandler = new ConsoleHandler(mcuConsole);
            consoleHandler.setLevel(Level.INFO);
            baseLogger.addHandler(consoleHandler);
            MCUpdater.apiLogger.addHandler(consoleHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void instanceChanged(ServerList entry) {
        this.selected = entry;
        List<Module> modList = new ArrayList<>(entry.getModules().values());
        List<Loader> loaderList = new ArrayList<>(entry.getLoaders());
        Instance instData = new Instance();
        AtomicReference<Instance> ref = new AtomicReference<>(instData);
        entry.setState(getPackState(entry, ref));
        instData = ref.get();
        try {
            Collections.sort(modList, new ModuleComparator(ModuleComparator.Mode.OPTIONAL_FIRST));
        } catch (Exception e) {
            baseLogger.warning( "Unable to sort mod list!");
        }
        Set<String> modIds = new HashSet<>();
        for (Module mod : modList) {
            modIds.add(mod.getId());
        }
        for (Module mod : new ArrayList<>(modList)) {
            if (!mod.getDepends().isEmpty()) {
                for (String modid : mod.getDepends().split(" ")) {
                    if (!modIds.contains(modid)) {
                        baseLogger.log(Level.WARNING, mod.getName() + ": " + modid + " does not exist in the mod list for dependency and will be removed from the pack.");
                        modList.remove(mod);
                    }
                }
            }
        }
        tabpaneDetail.getTabs().clear();
        Tab tabModules = new Tab(translate.getString("modules"));
        ModulePanel pnlModules = new ModulePanel();
        pnlModules.reload(loaderList, modList, instData.getOptionalMods());
        tabModules.setContent(pnlModules);
        tabpaneDetail.getTabs().add(tabModules);
        if (!entry.getNewsUrl().isEmpty()) {
            Tab tabNews = new Tab(translate.getString("news"));
            WebView newsView = new WebView();
            newsView.getEngine().load(entry.getNewsUrl());
            tabNews.setContent(newsView);
            tabpaneDetail.getTabs().add(tabNews);
        }
        btnLaunch.setDisable(entry.getState() != ServerList.State.READY);
        currentModules = pnlModules;
    }

    public void refreshInstanceList() {
        Settings current = SettingsManager.getInstance().getSettings();
        List<ServerList> slList = new ArrayList<>();

        Set<String> urls = new HashSet<>();
        urls.addAll(current.getPackURLs());

        for (String serverUrl : urls) {
            ServerPack pack = ServerPackParser.loadFromURL(serverUrl, false);
            if (!(pack == null)) {
                for (Server server : pack.getServers()) {
                    if (server instanceof ServerList) {
                        Instance instData = new Instance();
                        AtomicReference<Instance> ref = new AtomicReference<>(instData);
                        ((ServerList) server).setState(getPackState((ServerList) server, ref));
                        slList.add((ServerList) server);
                    }
                }
            }
        }
        if (listInstances != null) {
            listInstances.setItems(FXCollections.observableList(slList));
        }
    }

    private ServerList.State getPackState(ServerList entry, AtomicReference<Instance> ref) {
        Set<String> digests = entry.getDigests();
        String remoteHash = MCUpdater.calculateGroupHash(digests);
        final Path instanceFile = MCUpdater.getInstance().getInstanceRoot().resolve(entry.getServerId()).resolve("instance.json");
        Instance instData = ref.get();
        if (Files.notExists(instanceFile)) {
            return ServerList.State.UNKNOWN;
        }
        try {
            BufferedReader reader = Files.newBufferedReader(instanceFile, StandardCharsets.UTF_8);
            instData = gson.fromJson(reader, Instance.class);
            ref.getAndSet(instData);
            reader.close();
        } catch (IOException e) {
            baseLogger.log(Level.WARNING, "Instance data for " + entry.getFriendlyName() + " could not be read successfully.");
        }
        boolean needUpdate = (instData.getHash().isEmpty() || !instData.getHash().equals(remoteHash) || !instData.getMCVersion().equals(entry.getVersion()));
        boolean needNewMCU = Version.isVersionOld(entry.getMCUVersion());
        if (needUpdate) { return ServerList.State.UPDATE; }
        if (needNewMCU) { return ServerList.State.ERROR; }
        return ServerList.State.READY;
    }

    public void refreshProfiles() {
        if (profiles != null) {
            profiles.refreshProfiles();
        }
    }

    public void setSelectedInstance(String instanceId) {
        for (ServerList entry : listInstances.getItems()) {
            if (entry.getServerId().equals(instanceId)) {
                listInstances.getSelectionModel().select(entry);
                return;
            }
        }
    }

    public void doUpdate(ActionEvent actionEvent) {
        btnUpdate.setDisable(true);
        try {
            Path instPath = Files.createDirectories(MCUpdater.getInstance().getInstanceRoot().resolve(selected.getServerId()));
            Instance instData;
            final Path instanceFile = instPath.resolve("instance.json");
            try {
                BufferedReader reader = Files.newBufferedReader(instanceFile, StandardCharsets.UTF_8);
                instData = gson.fromJson(reader, Instance.class);
                reader.close();
            } catch (IOException ioe) {
                instData = new Instance();
                instData.setMCVersion(selected.getVersion());
            }
            instData.setHash(MCUpdater.calculateGroupHash(selected.getDigests()));

            final List<GenericModule> selectedMods = new ArrayList<>();
            final List<ConfigFile> selectedConfigs = new ArrayList<>();
            if (currentModules == null) {
                baseLogger.severe("Mod list not present!");
                return;
            }
            for (ModuleEntry entry : currentModules.getModules()) {
                baseLogger.finer(entry.getModule().getName() + " - " + entry.getModule().getModType().toString());
                if (entry.isSelected()) {
                    selectedMods.add(entry.getModule());
                    if (entry.getModule().hasConfigs()) {
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
            baseLogger.finer("Library overrides: " + selected.getLibOverrides().size());
            MCUpdater.getInstance().installMods(selected, selectedMods, selectedConfigs, instPath, chkHard.isSelected(), instData, ModSide.CLIENT);
        } catch (IOException e1) {
            baseLogger.log(Level.SEVERE, translate.getString("errorInstanceDirectoryCreate"), e1);
        }
    }

    public void doLaunch(ActionEvent actionEvent) {
        btnLaunch.setDisable(true);
        btnUpdate.setDisable(true);
        this.setPlaying(true);
        Profile launchProfile = profiles.getSelectedProfile();
        ServerList launchPack = this.selected;
        if (launchProfile.refresh()) {
            if (!(launchProfile == null)) {
                SettingsManager.getInstance().getSettings().setLastProfile(launchProfile.getName());
                SettingsManager.getInstance().getSettings().findProfile(launchProfile.getName()).setLastInstance(launchPack.getServerId());
                SettingsManager.getInstance().setDirty();
                try {
                    if (launchPack.getLauncherType().equals("Legacy")) {
                        tryOldLaunch(launchPack, launchProfile);
                    } else {
                        tryNewLaunch(launchPack, currentModules.getModules(), launchProfile);
                    }
                } catch (Exception ex) {
                    baseLogger.log(Level.SEVERE, ex.getMessage(), ex);
                    new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
                    setPlaying(false);
                }
            }
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to refresh your token!", ButtonType.OK).showAndWait();
            setPlaying(false);
        }
    }

    private void tryNewLaunch(ServerList selected, List<ModuleEntry> modules, Profile launchProfile) throws Exception {
        File javaBin;
        //TODO: Implement pack-specific Java version requirements
        if (Version.requestedFeatureLevel(selected.getVersion(),"1.17")) {
            javaBin = Main.javaRuntimes.entrySet().stream().filter(entry -> entry.getKey() >= 16).findFirst().get().getValue();
        } else if (Version.requestedFeatureLevel(selected.getVersion(),"1.16")) {
            javaBin = Main.javaRuntimes.entrySet().stream().filter(entry -> (entry.getKey() >= 8 && entry.getKey() < 16)).max(Comparator.comparingInt(entry -> entry.getKey())).get().getValue();
        } else {
            javaBin = Main.javaRuntimes.entrySet().stream().filter(entry -> entry.getKey() <= 8).findFirst().get().getValue();
        }
        String playerName = launchProfile.getName();
        String sessionKey = launchProfile.getSessionKey(this);
        MinecraftVersion mcVersion = MinecraftVersion.loadVersion(selected.getVersion());
        selected.getLoaders().sort(new OrderComparator());
        String indexName = mcVersion.getAssets();
        if (indexName == null) {
            indexName = "legacy";
        }
        String mainClass;
        List<String> args = new ArrayList<>();
        StringBuilder clArgs;
        if (Version.requestedFeatureLevel(selected.getVersion(),"1.13") || selected.getLoaders().size() == 0) {
            clArgs = new StringBuilder(mcVersion.getEffectiveArguments());
        } else {
            clArgs = new StringBuilder();
        }
        List<String> libs = new ArrayList<>();
        MCUpdater mcu = MCUpdater.getInstance();
        Path instancePath = mcu.getInstanceRoot().resolve(selected.getServerId());
        File indexesPath = mcu.getArchiveFolder().resolve("assets").resolve("indexes").toFile();
        File indexFile = new File(indexesPath, indexName + ".json");
        String json;
        json = FileUtils.readFileToString(indexFile);
        AssetIndex index = gson.fromJson(json, AssetIndex.class);
        final Settings settings = SettingsManager.getInstance().getSettings();
        if (settings.isFullScreen()) {
            clArgs.append(" --fullscreen");
        } else {
            clArgs.append(" --width ").append(settings.getResWidth()).append(" --height ").append(settings.getResHeight());
        }
        if (settings.isAutoConnect() && selected.isAutoConnect()) {
            URI address;
            try {
                address = new URI("my://" + selected.getAddress());
                clArgs.append(" --server ").append(address.getHost());
                if (address.getPort() != -1) {
                    clArgs.append(" --port ").append(address.getPort());
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        clArgs.append(" --resourcePackDir ${resource_packs}");
        if (!settings.getProgramWrapper().isEmpty()) {
            args.add(settings.getProgramWrapper());
        }
        args.add(javaBin.getAbsolutePath());
        args.add("-Xms" + settings.getMinMemory());
        args.add("-Xmx" + settings.getMaxMemory());
        //args.add("-XX:PermSize=" + settings.getPermGen());
        args.add(mcVersion.getJVMArguments());
        if (!settings.getJvmOpts().isEmpty()) {
            args.addAll(Arrays.asList(settings.getJvmOpts().split(" ")));
        }
        if (System.getProperty("os.name").startsWith("Mac")) {
            args.add("-Xdock:icon=" + mcu.getArchiveFolder().resolve("assets").resolve("icons").resolve("minecraft.icns"));
            args.add("-Xdock:name=Minecraft(MCUpdater)");
        }
        args.add("-Djava.library.path=" + mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("libraries").resolve("natives"));
        if (!selected.getMainClass().isEmpty()) {
            mainClass = selected.getMainClass();
        } else {
            mainClass = mcVersion.getMainClass();
        }
        for (ModuleEntry entry : modules) {
            if (entry.isSelected()) {
                if (entry.getModule().getModType().equals(ModType.Library)) {
                    libs.add(entry.getModule().getFilename());
                }
                if (!entry.getModule().getLaunchArgs().isEmpty()) {
                    clArgs.append(" ").append(entry.getModule().getLaunchArgs());
                }
                if (!entry.getModule().getJreArgs().isEmpty()) {
                    args.addAll(Arrays.asList(entry.getModule().getJreArgs().split(" ")));
                }
                if (entry.getModule().hasSubmodules()) {
                    for (GenericModule sm : entry.getModule().getSubmodules()) {
                        if (sm.getModType().equals(ModType.Library)) {
                            libs.add(sm.getFilename());
                        }
                        if (!sm.getLaunchArgs().isEmpty()) {
                            clArgs.append(" ").append(sm.getLaunchArgs());
                        }
                        if (!sm.getJreArgs().isEmpty()) {
                            args.addAll(Arrays.asList(sm.getJreArgs().split(" ")));
                        }
                    }
                }
            }
        }
        for (Loader loader : selected.getLoaders()) {
            libs.addAll(loader.getILoader().getClasspathEntries(instancePath.toFile()));
            clArgs.append(loader.getILoader().getArguments(instancePath.toFile()));
        }
        for (Library lib : mcVersion.getLibraries()) {
            String key = StringUtils.join(Arrays.copyOfRange(lib.getName().split(":"),0,2),":");
            if (selected.getLibOverrides().containsKey(key)) {
                lib.setName("libraries/" + selected.getLibOverrides().get(key));
            }
            if (lib.validForOS() && !lib.hasNatives()) {
                libs.add("libraries/" + lib.getFilename());
            }
        }
        args.add("-cp");
        StringBuilder classpath = new StringBuilder();
        for (String entry : libs) {
            classpath.append(instancePath.resolve(entry)).append(MCUpdater.cpDelimiter());
        }
        classpath.append(mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("bin").resolve("minecraft.jar"));
        args.add(classpath.toString());
        args.add(mainClass);
        String tmpclArgs = clArgs.toString();
        Map<String,String> fields = new HashMap<>();
        StrSubstitutor fieldReplacer = new StrSubstitutor(fields);
        fields.put("auth_player_name", playerName);
        fields.put("auth_uuid", launchProfile.getUUID().replace("-",""));
        fields.put("auth_access_token", launchProfile.getAuthAccessToken());
        fields.put("auth_session", sessionKey);
        fields.put("version_name", selected.getVersion());
        fields.put("game_directory", mcu.getInstanceRoot().resolve(selected.getServerId()).toString());
        if (index.isVirtual()) {
            fields.put("game_assets", mcu.getArchiveFolder().resolve("assets").resolve("virtual").toString());
            fields.put("assets_root", mcu.getArchiveFolder().resolve("assets").resolve("virtual").toString());
        } else {
            fields.put("game_assets", mcu.getArchiveFolder().resolve("assets").toString());
            fields.put("assets_root", mcu.getArchiveFolder().resolve("assets").toString());
        }
        fields.put("assets_index_name", indexName);
        fields.put("resource_packs", mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("resourcepacks").toString());
        fields.put("user_properties", "{}"); //TODO: This will likely actually get used at some point.
        fields.put("user_type", (launchProfile.getStyle()));
        fields.put("version_type", mcVersion.getType());
        String[] fieldArr = tmpclArgs.split(" ");
        for (int i = 0; i < fieldArr.length; i++) {
            fieldArr[i] = fieldReplacer.replace(fieldArr[i]);
        }
        args.addAll(Arrays.asList(fieldArr));
        args.addAll(Main.passthroughArgs);

        log("Launch args:");
        log("=======================");
        for (String entry : args) {
            log(entry);
        }
        log("=======================");
        System.out.println(String.join(" ", args));
        final ProcessBuilder pb = new ProcessBuilder(args);
        pb.environment().put("openeye.tags","MCUpdater," + selected.getName() + " (" + selected.getServerId() + ")");
        pb.directory(mcu.getInstanceRoot().resolve(selected.getServerId()).toFile());
        pb.redirectErrorStream(true);
        final Thread gameThread = new Thread(() -> {
            AtomicReference<Tab> tabOutput = new AtomicReference<>();
            AtomicReference<ConsolePane> mcOutput = new AtomicReference<>();
            try{
                if (settings.isMinecraftToConsole()) {
                    Platform.runLater(() -> {
                        mcOutput.set(new ConsolePane());
                        tabOutput.set(new Tab("Instance: " + selected.getName(), mcOutput.get()));
                        tabpaneConsole.getTabs().add(tabOutput.get());
                    });
                }
                Process task = pb.start();
                BufferedReader buffRead = new BufferedReader(new InputStreamReader(task.getInputStream()));
                String line;
                while ((line = buffRead.readLine()) != null) {
                    if (line.length() > 0) {
                        if (settings.isMinecraftToConsole()) {
                            if (mcOutput.get() != null) {
                                if (line.contains("WARNING")) {
                                    mcOutput.get().appendEntry(line, Level.WARNING);
                                } else if (line.contains("SEVERE")) {
                                    mcOutput.get().appendEntry(line, Level.SEVERE);
                                } else {
                                    mcOutput.get().appendEntry(line, Level.INFO);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                baseLogger.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                if (tabOutput.get() != null) {
                    tabOutput.get().setClosable(true);
                }
                baseLogger.info("Minecraft process terminated");
                setPlaying(false);
            }
        });
        gameThread.start();
    }

    private void tryOldLaunch(ServerList selected, Profile launchProfile) throws Exception {
        File javaBin;
        //TODO: Implement pack-specific Java version requirements
        javaBin = Main.javaRuntimes.entrySet().stream().filter(entry -> entry.getKey() <= 8).findFirst().get().getValue();
        Path mcuPath = MCUpdater.getInstance().getArchiveFolder();
        final Settings settings = SettingsManager.getInstance().getSettings();
        Path instancePath = MCUpdater.getInstance().getInstanceRoot().resolve(selected.getServerId());
        String playerName = launchProfile.getName();
        String sessionKey = launchProfile.getSessionKey(this);
        List<String> args = new ArrayList<>();
        if (!settings.getProgramWrapper().isEmpty()) {
            args.add(settings.getProgramWrapper());
        }
        args.add(javaBin.getAbsolutePath());
        if (System.getProperty("os.name").startsWith("Mac")) {
            args.add("-Xdock:icon=" + mcuPath.resolve("assets").resolve("icons").resolve("minecraft.icns"));
            args.add("-Xdock:name=Minecraft(MCUpdater)");
        }
        if (!settings.getJvmOpts().isEmpty()) {
            args.addAll(Arrays.asList(settings.getJvmOpts().split(" ")));
        }
        args.add("-Xms" + settings.getMinMemory());
        args.add("-Xmx" + settings.getMaxMemory());
        //args.add("-XX:PermSize=" + settings.getPermGen());
        args.add("-classpath");
        args.add(mcuPath.resolve("lib").resolve("MCU-Launcher.jar") + System.getProperty("path.separator") + instancePath.resolve("lib") + File.separator + "*");
        args.add("org.mcupdater.MinecraftFrame");
        args.add(playerName);
        args.add(sessionKey);
        args.add(selected.getName());
        args.add(instancePath.toString());
        args.add(instancePath.resolve("lib").toString());
        args.add(selected.getIconUrl().isEmpty() ? "https://minecraft.net/favicon.png" : selected.getIconUrl());
        args.add(String.valueOf(settings.getResWidth()));
        args.add(String.valueOf(settings.getResHeight()));
        args.add(selected.getAddress().isEmpty() ? "localhost" : selected.getAddress());
        args.add(Boolean.toString(selected.isAutoConnect() && settings.isAutoConnect()));

        log("Launch args:");
        log("=======================");
        for (String entry : args) {
            log(entry);
        }
        log("=======================");
        final ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(instancePath.toFile());
        pb.redirectErrorStream(true);
        final Thread gameThread = new Thread(() -> {
            AtomicReference<Tab> tabOutput = new AtomicReference<>();
            AtomicReference<ConsolePane> mcOutput = new AtomicReference<>();
            try{
                if (settings.isMinecraftToConsole()) {
                    Platform.runLater(() -> {
                        mcOutput.set(new ConsolePane());
                        tabOutput.set(new Tab("Instance: " + selected.getName(), mcOutput.get()));
                        tabpaneConsole.getTabs().add(tabOutput.get());
                    });
                }
                Process task = pb.start();
                BufferedReader buffRead = new BufferedReader(new InputStreamReader(task.getInputStream()));
                String line;
                while ((line = buffRead.readLine()) != null) {
                    if (line.length() > 0) {
                        if (settings.isMinecraftToConsole()) {
                            if (mcOutput != null) {
                                if (line.contains("WARNING")) {
                                    mcOutput.get().appendEntry(line, Level.WARNING);
                                } else if (line.contains("SEVERE")) {
                                    mcOutput.get().appendEntry(line, Level.SEVERE);
                                } else {
                                    mcOutput.get().appendEntry(line, Level.INFO);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                baseLogger.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                if (tabOutput != null) {
                    tabOutput.get().setClosable(true);
                }
                baseLogger.info("Minecraft process terminated");
                setPlaying(false);
            }
        });
        gameThread.start();

    }

    public void setDirty() {
        listInstances.getSelectionModel().getSelectedItem().setState(ServerList.State.UPDATE);
        listInstances.refresh();
        btnLaunch.setDisable(true);
    }

    public void addInstance(ActionEvent actionEvent) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("MCUpdater");
        inputDialog.setHeaderText(translate.getString("enterUrl"));
        inputDialog.showAndWait().ifPresent(s -> {
            SettingsManager.getInstance().getSettings().addPackURL(s);
            SettingsManager.getInstance().setDirty();
        });
    }

    public void doRefresh(ActionEvent actionEvent) {
        refreshInstanceList();
        setSelectedInstance(profiles.getSelectedProfile().getLastInstance());
    }

    // Begin MCUApp methods
    // -----
    @Override
    public void setStatus(String string) {

    }

    @Override
    public void log(String msg) {
        baseLogger.info(msg);
    }

    @Override
    public Profile requestLogin(String username) {
        return LoginDialog.doLogin(tabpaneMain.getScene().getWindow(),username);
    }

    @Override
    public DownloadQueue submitNewQueue(String queueName, String parent, Collection<Downloadable> files, File basePath, File cachePath) {
        progress.addProgressBar(queueName, parent);
        if (profiles.getSelectedProfile() != null) {
            return new DownloadQueue(queueName, parent, this, files, basePath, cachePath, profiles.getSelectedProfile().getName(), this.baseLogger);
        } else {
            return new DownloadQueue(queueName, parent, this, files, basePath,cachePath, this.baseLogger);
        }
    }

    @Override
    public DownloadQueue submitAssetsQueue(String queueName, String parent, MinecraftVersion version) {
        progress.addProgressBar(queueName, parent);
        return AssetManager.downloadAssets(queueName, parent, MCUpdater.getInstance().getArchiveFolder().resolve("assets").toFile(), this, version);
    }

    @Override
    public void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.showAndWait();
    }
    // -----
    // End MCUApp methods

    // Begin SettingsListener methods
    // -----

    @Override
    public void settingsChanged(Settings newSettings) {
        Platform.runLater(() -> {
            refreshInstanceList();
            refreshProfiles();
        });
        MCUpdater.getInstance().setInstanceRoot(new File(newSettings.getInstanceRoot()).toPath());
    }
    // -----
    // End SettingsListener methods

    // Begin TrackerListener methods
    // -----
    @Override
    public void onQueueFinished(DownloadQueue queue) {
        synchronized (progress) {
            baseLogger.info(String.format("%s - %s: %s",queue.getParent(), queue.getName(), translate.getString("queueFinished")));
            progress.updateProgress(queue);
            for (Downloadable entry : queue.getFailures()) {
                baseLogger.severe(String.format("%s: %s", translate.getString("fileFailed"), entry.getFilename()));
            }
        }
    }

    @Override
    public void onQueueProgress(DownloadQueue queue) {
        synchronized (progress) {
            progress.updateProgress(queue);
        }
    }

    @Override
    public void printMessage(String msg) {
        baseLogger.fine(msg);
    }

    // ----
    // End TrackerListener methods

}