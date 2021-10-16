package org.mcupdater.gui.javafx.components;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.mcupdater.gui.javafx.Main;
import org.mcupdater.gui.javafx.MainController;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsListener;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.util.MCUpdater;

import java.io.File;
import java.util.Arrays;
import java.util.ResourceBundle;

public class SettingsPane extends Accordion implements SettingsListener {

    private final GridPane gridJava;
    private final ResourceBundle translate;
    private final GridPane gridMinecraft;
    private final GridPane gridMCUpdater;
    private final SettingsManager settingsManager;
    private final TextField fieldJvmMinMem = new TextField();
    private final TextField fieldJvmMaxMem = new TextField();
    private final TableView<String> fieldJvmOpts = new TableView<>();
    private final TextField fieldWrapper = new TextField();
    private final CheckBox fieldFullscreen = new CheckBox();
    private final TextField fieldWindowWidth = new TextField();
    private final TextField fieldWindowHeight = new TextField();
    private final CheckBox fieldAutoConnect = new CheckBox();
    private final CheckBox fieldMinimize = new CheckBox();
    private final CheckBox fieldMCConsole = new CheckBox();
    private final TextField fieldInstancePath = new TextField();
    private final TableView<Profile> fieldProfiles = new TableView<>();
    private final ListView<String> fieldUrls = new ListView<>();
    private final CheckBox fieldProfessional = new CheckBox();
    private final ObservableList<Profile> listProfiles;

    @Override
    public void stateChanged(boolean newState) {

    }

    @Override
    public void settingsChanged(Settings newSettings) {
        System.out.println("Settings changed!");
        fieldJvmMinMem.setText(newSettings.getMinMemory());
        fieldJvmMaxMem.setText(newSettings.getMaxMemory());
        //ObservableList<String> listOpts = FXCollections.observableArrayList(Arrays.asList(newSettings.getJvmOpts().split(" ")));
        //fieldJvmOpts.setItems(listOpts);
        fieldWrapper.setText(newSettings.getProgramWrapper());
        fieldFullscreen.setSelected(newSettings.isFullScreen());
        fieldWindowWidth.setText(Integer.toString(newSettings.getResWidth()));
        fieldWindowHeight.setText(Integer.toString(newSettings.getResHeight()));
        fieldAutoConnect.setSelected(newSettings.isAutoConnect());
        fieldMinimize.setSelected(newSettings.isMinimizeOnLaunch());
        fieldMCConsole.setSelected(newSettings.isMinecraftToConsole());
        fieldInstancePath.setText(newSettings.getInstanceRoot());
        listProfiles.clear();
        listProfiles.addAll(newSettings.getProfiles());
        fieldUrls.getItems().clear();
        fieldUrls.getItems().addAll(newSettings.getPackURLs());
    }

    public SettingsPane() {
        settingsManager = SettingsManager.getInstance();
        settingsManager.addListener(this);
        translate = Main.getTranslation();
        mapFields();
        ObservableList<String> listOpts = FXCollections.observableArrayList(item -> new Observable[]{new SimpleStringProperty(item)});
        { // Create JVM Opts table
            listOpts.addAll(Arrays.asList(settingsManager.getSettings().getJvmOpts().split(" ")));
            TableColumn<String, String> column1 = new TableColumn<>("JVM Option");
            column1.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()));
            column1.setCellFactory(TextFieldTableCell.forTableColumn());
            column1.setOnEditCommit(event -> {
                fieldJvmOpts.getItems().set(event.getTablePosition().getRow(), event.getNewValue());
                String newOpts = String.join(" ", fieldJvmOpts.getItems());
                settingsManager.getSettings().setJvmOpts(newOpts);
                settingsManager.saveSettings();
            });
            column1.setSortable(false);
            fieldJvmOpts.setItems(listOpts);
            fieldJvmOpts.setEditable(true);
            fieldJvmOpts.getColumns().add(column1);
        }
        listProfiles = FXCollections.observableArrayList(settingsManager.getSettings().getProfiles());
        { // Create Profiles table
            TableColumn<Profile, String> colPlayerName = new TableColumn<>(translate.getString("playerName"));
            TableColumn<Profile, String> colAuthType = new TableColumn<>(translate.getString("authType"));
            colPlayerName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
            colAuthType.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStyle()));
            fieldProfiles.setItems(listProfiles);
            fieldProfiles.setEditable(false);
            fieldProfiles.getColumns().addAll(colPlayerName, colAuthType);
        }
        gridJava = new GridPane();
        {
            gridJava.setHgap(5.0);
            gridJava.setVgap(5.0);
            int row = -1;
            addControlEntry(gridJava, ++row, "minMemory", fieldJvmMinMem);
            addControlEntry(gridJava, ++row, "maxMemory", fieldJvmMaxMem);
            VBox groupOpts = new VBox();
            {
                HBox groupOptsControls = new HBox();
                {
                    Button addOpt = new Button(translate.getString("add"));
                    addOpt.setOnAction(e -> {
                        fieldJvmOpts.getItems().add("*ChangeMe*");
                    });
                    Button deleteOpt = new Button(translate.getString("remove"));
                    deleteOpt.setOnAction(e -> {
                        fieldJvmOpts.getItems().remove(fieldJvmOpts.getSelectionModel().getSelectedIndex());
                    });
                    Button resetOpt = new Button(translate.getString("reset"));
                    resetOpt.setOnAction(e -> {
                        settingsManager.getSettings().setJvmOpts(MCUpdater.defaultJVMArgs);
                        settingsManager.saveSettings();
                        listOpts.clear();
                        listOpts.addAll(Arrays.asList(settingsManager.getSettings().getJvmOpts().split(" ")));
                        fieldJvmOpts.refresh();
                    });
                    groupOptsControls.getChildren().addAll(addOpt,deleteOpt,resetOpt);
                }
                groupOpts.getChildren().addAll(fieldJvmOpts, groupOptsControls);
            }
            addControlEntry(gridJava, ++row, "jvmOpts", groupOpts);
            BorderPane programWrapperPane = new BorderPane();
            programWrapperPane.setCenter(fieldWrapper);
            Button btnBrowseProgramWrapper = new Button();
            btnBrowseProgramWrapper.setGraphic(new ImageView(new Image(MainController.class.getResource("icons/folder_explore.png").toString())));
            btnBrowseProgramWrapper.setOnAction(event -> {
                FileChooser chooser = new FileChooser();
                if (!fieldWrapper.getText().isEmpty()) {
                    chooser.setInitialDirectory(new File(fieldWrapper.getText()).getParentFile());
                }
                File selected = chooser.showOpenDialog(this.getScene().getWindow());
                if (selected != null) {
                    fieldInstancePath.setText(selected.getAbsolutePath());
                }
            });
            programWrapperPane.setRight(btnBrowseProgramWrapper);
            addControlEntry(gridJava, ++row, "programWrapper", programWrapperPane);
        }
        gridMinecraft = new GridPane();
        {
            gridMinecraft.setHgap(5.0);
            gridMinecraft.setVgap(5.0);
            int row = -1;
            addControlEntry(gridMinecraft, ++row, "fullscreen", fieldFullscreen);
            Label lblX = new Label(" X ");
            HBox hBox = new HBox(fieldWindowWidth, lblX, fieldWindowHeight);
            hBox.setAlignment(Pos.BASELINE_LEFT);
            addControlEntry(gridMinecraft, ++row, "resolution", hBox);
            addControlEntry(gridMinecraft, ++row, "autoConnect", fieldAutoConnect);
            addControlEntry(gridMinecraft, ++row, "minimize", fieldMinimize);
            addControlEntry(gridMinecraft, ++row, "minecraftConsole", fieldMCConsole);
        }
        gridMCUpdater = new GridPane();
        {
            gridMCUpdater.setHgap(5.0);
            gridMCUpdater.setVgap(5.0);
            int row = -1;
            BorderPane instancePathPane = new BorderPane();
            instancePathPane.setCenter(fieldInstancePath);
            Button btnBrowseInstancePath = new Button();
            btnBrowseInstancePath.setGraphic(new ImageView(new Image(MainController.class.getResource("icons/folder_explore.png").toString())));
            btnBrowseInstancePath.setOnAction(event -> {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setInitialDirectory(new File(fieldInstancePath.getText()));
                File selected = chooser.showDialog(this.getScene().getWindow());
                if (selected != null) {
                    fieldInstancePath.setText(selected.getAbsolutePath());
                }
            });
            instancePathPane.setRight(btnBrowseInstancePath);
            addControlEntry(gridMCUpdater, ++row, "instancePath", instancePathPane);
            VBox groupProfiles = new VBox();
            {
                HBox groupProfilesControls = new HBox();
                {
                    Button addProfile = new Button(translate.getString("add"));
                    addProfile.setOnAction(e -> {
                        Profile newProfile = MainController.getInstance().requestLogin("");
                        if (newProfile != null) {
                            settingsManager.getSettings().addOrReplaceProfile(newProfile);
                            settingsManager.saveSettings();
                        }
                    });
                    Button deleteProfile = new Button(translate.getString("remove"));
                    deleteProfile.setOnAction(e -> {
                        settingsManager.getSettings().getProfiles().remove(fieldProfiles.getSelectionModel().getSelectedItem());
                        settingsManager.saveSettings();
                    });
                    groupProfilesControls.getChildren().addAll(addProfile,deleteProfile);
                }
                groupProfiles.getChildren().addAll(fieldProfiles, groupProfilesControls);
            }
            addControlEntry(gridMCUpdater, ++row, "profiles", groupProfiles);
            VBox groupUrls = new VBox();
            {
                HBox groupUrlsControls = new HBox();
                {
                    Button addUrl = new Button(translate.getString("add"));
                    addUrl.setOnAction((event) -> MainController.getInstance().addInstance(event));
                    Button deleteUrl = new Button(translate.getString("remove"));
                    deleteUrl.setOnAction(e -> {
                        settingsManager.getSettings().getPackURLs().remove(fieldUrls.getSelectionModel().getSelectedItem());
                        settingsManager.saveSettings();
                    });
                    groupUrlsControls.getChildren().addAll(addUrl,deleteUrl);
                }
                groupUrls.getChildren().addAll(fieldUrls, groupUrlsControls);
            }
            addControlEntry(gridMCUpdater, ++row, "definedPacks", groupUrls);
            addControlEntry(gridMCUpdater, ++row, "professionalMode", fieldProfessional);
        }
        TitledPane sectionJava = new TitledPane("Java",gridJava);
        TitledPane sectionMinecraft = new TitledPane("Minecraft",gridMinecraft);
        TitledPane sectionMCUpdater = new TitledPane("MCUpdater",gridMCUpdater);
        this.settingsChanged(settingsManager.getSettings());
        this.getPanes().addAll(sectionJava,sectionMinecraft,sectionMCUpdater);
    }

    private void addControlEntry(GridPane parent, int row, String key, Region child) {
        GridPane.setHgrow(child, Priority.ALWAYS);
        parent.addRow(row, createTranslatedLabel(key), child);
    }

    private void mapFields() {
        fieldJvmMinMem.textProperty().addListener((observable, oldValue, newValue) -> {
            settingsManager.getSettings().setMinMemory(newValue);
            settingsManager.saveSettings();
        });
        fieldJvmMaxMem.textProperty().addListener((observable, oldValue, newValue) -> {
            settingsManager.getSettings().setMaxMemory(newValue);
            settingsManager.saveSettings();
        });
//        fieldJvmOpts.editingCellProperty().addListener((observable, oldValue, newValue) -> {
//            String newOpts = String.join(" ", fieldJvmOpts.getItems());
//            settingsManager.getSettings().setJvmOpts(newOpts);
//            settingsManager.saveSettings();
//        });
        fieldWrapper.textProperty().addListener((observable, oldValue, newValue) -> {
            settingsManager.getSettings().setProgramWrapper(newValue);
            settingsManager.saveSettings();
        });
        fieldFullscreen.selectedProperty().addListener((observable, oldValue, newValue) -> {
            settingsManager.getSettings().setFullScreen(newValue);
            settingsManager.saveSettings();
        });
        fieldWindowWidth.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (Integer.parseInt(newValue) > 0) {
                    settingsManager.getSettings().setResWidth(Integer.parseInt(newValue));
                    settingsManager.saveSettings();
                }
            } catch (NumberFormatException nfe) {
                // Ignore exception
            }
        });
        fieldWindowHeight.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (Integer.parseInt(newValue) > 0) {
                    settingsManager.getSettings().setResHeight(Integer.parseInt(newValue));
                    settingsManager.saveSettings();
                }
            } catch (NumberFormatException nfe) {
                // Ignore exception
            }
        });
        fieldAutoConnect.selectedProperty().addListener((observable, oldValue, newValue) -> {
            settingsManager.getSettings().setAutoConnect(newValue);
            settingsManager.saveSettings();
        });
        fieldMinimize.selectedProperty().addListener((observable, oldValue, newValue) -> {
            settingsManager.getSettings().setMinimizeOnLaunch(newValue);
            settingsManager.saveSettings();
        });
        fieldMCConsole.selectedProperty().addListener((observable, oldValue, newValue) -> {
            settingsManager.getSettings().setMinecraftToConsole(newValue);
            settingsManager.saveSettings();
        });
        fieldInstancePath.textProperty().addListener((observable, oldValue, newValue) -> {
            settingsManager.getSettings().setInstanceRoot(newValue);
            settingsManager.saveSettings();
        });
        fieldUrls.itemsProperty().addListener((observable, oldValue, newValue) -> {
            settingsManager.getSettings().setPackURLs(newValue);
            settingsManager.saveSettings();
        });
        fieldProfessional.selectedProperty().addListener((observable, oldValue, newValue) -> {
            settingsManager.getSettings().setProfessionalMode(newValue);
            settingsManager.saveSettings();
        });
    }

    private Label createTranslatedLabel(String key) {
        return new Label(String.format("%s:",translate.getString(key)));
    }
}
