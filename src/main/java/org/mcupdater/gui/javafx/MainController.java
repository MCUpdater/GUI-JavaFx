package org.mcupdater.gui.javafx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.util.Callback;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mcupdater.FMLStyleFormatter;
import org.mcupdater.MCUApp;
import org.mcupdater.api.Version;
import org.mcupdater.auth.YggdrasilAuthManager;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.downloadlib.TrackerListener;
import org.mcupdater.gui.javafx.components.*;
import org.mcupdater.instance.Instance;
import org.mcupdater.model.*;
import org.mcupdater.model.Module;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsListener;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.ServerPackParser;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController extends MCUApp implements Initializable, TrackerListener, SettingsListener {

    private static MainController INSTANCE;
    public ListView listInstances;
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
    private ServerList selected;
    private ResourceBundle translate;

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
        listInstances.setCellFactory((Callback<ListView<ServerList>, ListCell<ServerList>>) serverListListView -> new InstanceListCell());
        if (!SettingsManager.getInstance().getSettings().getPackURLs().contains(Main.getDefaultPackURL())) {
            SettingsManager.getInstance().getSettings().addPackURL(Main.getDefaultPackURL());
            SettingsManager.getInstance().saveSettings();
        }
        setupControls();
        System.out.println("Initialized");
        SettingsManager.getInstance().addListener(this);
        Thread daemonMonitor = new Thread() {
            private ServerList currentSelection;
            private int activeJobs = 0;
            private boolean playState;

            @Override
            public void run() {
                //int x=0;
                //noinspection InfiniteLoopStatement
                while (true) {
                    try {
                        if (activeJobs != progress.getActiveCount() || currentSelection != listInstances.getSelectionModel().getSelectedItem() || playState != isPlaying()) {
                            currentSelection = (ServerList) listInstances.getSelectionModel().getSelectedItem();
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
                                if (progress.getActiveById(currentSelection.getServerId()) > 0 || playState) {
                                    btnUpdate.setDisable(true);
                                } else {
                                    btnUpdate.setDisable(false);
                                }
                            } else {
                                btnUpdate.setDisable(true);
                            }
                        }
                        //baseLogger.info("Test");
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

    private boolean isPlaying() {
        return false;
    }

    private void setupControls() {
        tabInstances.setText(translate.getString("instances"));
        //tabNews.setText(translate.getString("news"));
        tabConsole.setText(translate.getString("console"));
        tabSettings.setText(translate.getString("settings"));
        //tabModules.setText(translate.getString("modules"));
        tabProgress.setText(translate.getString("progress"));
        btnUpdate.setText(translate.getString("update"));
        btnLaunch.setText(translate.getString("launchMinecraft"));
        lblHard.setText(translate.getString("hardUpdate"));
        //btnAddURL.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("add.png"))));
        //btnRefresh.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("arrow_refresh.png"))));
        listInstances.getSelectionModel().selectedItemProperty().addListener((ChangeListener<ServerList>) (observableValue, oldSL, newSL) -> {
            instanceChanged(newSL);
        });
        ConsoleHandler consoleHandler = new ConsoleHandler(mcuConsole);
        consoleHandler.setLevel(Level.INFO);
        baseLogger.addHandler(consoleHandler);
        MCUpdater.apiLogger.addHandler(consoleHandler);
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
    }


    @Override
    public void setStatus(String string) {

    }

    @Override
    public void log(String msg) {

    }

    @Override
    public Profile requestLogin(String username) {
        return null;
    }

    @Override
    public DownloadQueue submitNewQueue(String queueName, String parent, Collection<Downloadable> files, File basePath, File cachePath) {
        return null;
    }

    @Override
    public DownloadQueue submitAssetsQueue(String queueName, String parent, MinecraftVersion version) {
        return null;
    }

    @Override
    public void alert(String msg) {

    }

    @Override
    public void stateChanged(boolean newState) {

    }

    @Override
    public void settingsChanged(Settings newSettings) {

    }

    @Override
    public void onQueueFinished(DownloadQueue queue) {

    }

    @Override
    public void onQueueProgress(DownloadQueue queue) {

    }

    @Override
    public void printMessage(String msg) {

    }

    public void refreshInstanceList()
    {
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
        /*
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
        */
    }

    private ServerList.State getPackState(ServerList server, AtomicReference<Instance> ref) {
        return null;
    }

    public void refreshProfiles() {
        if (profiles != null) {
            profiles.refreshProfiles();
        }
    }

    public void setSelectedInstance(String lastInstance) {

    }

    public void doUpdate(ActionEvent actionEvent) {

    }

    public void doLaunch(ActionEvent actionEvent) {

    }

    public void setDirty() {
        //TODO: Implement based on Swing version
    }
}
