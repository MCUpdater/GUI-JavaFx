package org.mcupdater.gui.javafx.components;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.mcupdater.gui.javafx.MainController;
import org.mcupdater.model.Loader;
import org.mcupdater.model.ModSide;
import org.mcupdater.model.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ModulePanel extends ScrollPane
{
    final VBox content = new VBox();
    private Map<String,ModuleEntry> modules = new HashMap<>();
    private boolean init;

    public ModulePanel(){
        super();
        this.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        this.setContent(content);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        VBox.setVgrow(this, Priority.ALWAYS);
        content.setSpacing(5);
        content.setPadding(new Insets(3));
        //content.setBackground(new Background(new BackgroundImage(new Image(MainController.class.getResource("img/dark_oak_planks.png").toString()),BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
    }

    public void clear(){
        content.getChildren().clear();
    }

    public void reload(List<Loader> loaderList, List<Module> modList, Map<String, Boolean> optionalSelections) {
        this.clear();
        init = true;
        modules = new HashMap<>();
        for (Loader loader : loaderList) {
            LoaderEntry newEntry;
            newEntry = new LoaderEntry(loader);
            content.getChildren().add(newEntry);
        }
        for (Module entry : modList) {
            if (entry.getSide().equals(ModSide.SERVER)) {
                continue;
            }
            ModuleEntry newEntry;
            if (optionalSelections.containsKey(entry.getId())) {
                newEntry = new ModuleEntry(this, entry, true, optionalSelections.get(entry.getId()));
            } else {
                newEntry = new ModuleEntry(this, entry, false, false);
            }
            //= new ModuleEntry(this, entry);
            if (!entry.getRequired() && optionalSelections.containsKey(entry.getId())) {
                newEntry.setSelected(optionalSelections.get(entry.getId()));
            }
            this.content.getChildren().add(newEntry);
            modules.put(newEntry.getModule().getId(), newEntry);
        }
        for (Map.Entry<String,ModuleEntry> entry : modules.entrySet()) {
            if (!entry.getValue().getModule().getDepends().isEmpty()) {
                for (String modid : entry.getValue().getModule().getDepends().split(" ")) {
                    if (modules.get(modid) == null) {
                        MainController.getInstance().baseLogger.log(Level.WARNING, entry.getValue().getModule().getName() +  ": " + modid + " does not exist in the mod list for dependency and will be removed from the pack.");
                    } else {
                        modules.get(modid).addDependent(entry.getValue());
                    }
                }
            }
        }
        this.content.setFillWidth(true);
        init = false;
    }

    public List<ModuleEntry> getModules() {
        return new ArrayList<>(modules.values());
    }

    public boolean isInitializing() {
        return init;
    }
}