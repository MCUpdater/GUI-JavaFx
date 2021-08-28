package org.mcupdater.gui.javafx.components;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.mcupdater.gui.javafx.MainController;
import org.mcupdater.model.GenericModule;
import org.mcupdater.model.Module;
import org.mcupdater.settings.SettingsManager;

import javax.swing.*;
import javax.tools.Tool;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ModuleEntry extends VBox {
    private boolean init;
    private CheckBox chkModule;
    private Module entry;
    private ModulePanel parent;
    private boolean selected;
    private List<ModuleEntry> dependents = new ArrayList<>();

    public ModuleEntry(ModulePanel parent, Module module, Boolean overrideDefault, Boolean overrideValue) {
        this.parent = parent;
        this.entry = module;
        init = true;
        if (!entry.getRequired() || SettingsManager.getInstance().getSettings().isProfessionalMode()) {
            chkModule = new CheckBox(module.getName());
            chkModule.addEventHandler(ActionEvent.ACTION, event -> {
                if (selected != chkModule.isSelected()) {
                    selected = chkModule.isSelected();
                    if (!init) MainController.getInstance().setDirty();
                }
                for (String modid : entry.getDepends().split(" ")) {
                    for (ModuleEntry entry : this.parent.getModules()) {
                        if (entry.getModule().getId().equals(modid)) {
                            if (selected) {
                                entry.setSelected(true);
                            } else {
                                entry.checkDependents();
                            }
                        }
                    }
                }
            });
            if (this.entry.getMeta().containsKey("description")) {
                updateTooltip(chkModule, this.entry.getMeta().get("description"));
            }
            if (entry.getRequired() || (entry.getIsDefault() && !overrideDefault)) {
                chkModule.setSelected(true);
            }
            if (overrideDefault) {
                chkModule.setSelected(overrideValue);
            }
            this.getChildren().add(chkModule);
            this.setMinHeight(16);
        } else {
            Label lblRequired = new Label(this.entry.getName());
            this.getChildren().add(lblRequired);
            this.setMinHeight(16);
            if (this.entry.getMeta().containsKey("description")) {
                updateTooltip(lblRequired, this.entry.getMeta().get("description"));
            }
        }
        if (this.entry.hasSubmodules()) {
            for (GenericModule submodule : entry.getSubmodules()) {
                Label lblSubmodule = new Label("  -- " + submodule.getName());
                this.getChildren().add(lblSubmodule);
                if (submodule.getMeta().containsKey("description")) {
                    updateTooltip(lblSubmodule, submodule.getMeta().get("description"));
                }
                this.setMinHeight(this.getMinHeight()+16);
            }
            this.setMinHeight(this.getMinHeight() + Math.max(0,(entry.getSubmodules().size()*2) - 8));
        }
        init = false;
    }

    private void updateTooltip(Control control, String description) {
        Tooltip tooltip = new Tooltip(description);
        tooltip.setWrapText(true);
        control.setTooltip(tooltip);
    }

    private String splitMulti(String input) {
        if (input == null) return null;
        StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder();
        int lineLen = 0;
        output.append("<html>");
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();

            if (lineLen + word.length() > 40) {
                output.append("<br>");
                lineLen = 0;
            }
            output.append(word).append(" ");
            lineLen += word.length();
        }
        output.append("</html>");
        return output.toString();
    }

    private void checkDependents() {
        for (Node child : this.getChildren()) {
            if (child instanceof CheckBox) {
                CheckBox chkModule = (CheckBox) child;
                boolean shouldDisable = false;
                for (ModuleEntry entry : dependents) {
                    if (entry.isSelected()) {
                        shouldDisable = true;
                    }
                }
                chkModule.setDisable(shouldDisable);
            }
        }
    }

    public void addDependent(ModuleEntry entry) {
        this.dependents.add(entry);
        if (entry.isSelected()) {
            setSelected(true);
        }
    }

    public boolean isSelected() {
        if (chkModule != null) {
            return this.chkModule.isSelected();
        } else {
            return true;
        }
    }

    public void setSelected(boolean state) {
        if (chkModule != null) {
            this.chkModule.setSelected(state);
            this.chkModule.fireEvent(new ActionEvent());
        }
        checkDependents();
    }

    public Module getModule() {
        return this.entry;
    }

    public void setModule(Module data) {
        this.entry = data;
    }
}