package org.mcupdater.gui;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.mcupdater.model.Module;

import java.util.List;
import java.util.Map;

public class ModulePanel extends ScrollPane
{
	VBox content = new VBox();

	public ModulePanel(){
		super();
		this.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		this.setContent(content);
		this.setFitToWidth(true);
		this.setFitToHeight(true);
		VBox.setVgrow(this, Priority.ALWAYS);
		content.setSpacing(5);
		content.setPadding(new Insets(3));
	}

	public void clear(){
		content.getChildren().clear();
	}

	public void reload(List<Module> modList, Map<String, Boolean> optionalSelections) {
		this.clear();
		for (Module m : modList) {
			ModuleEntry newEntry = new ModuleEntry(m);
			if (!m.getRequired() && optionalSelections.containsKey(m.getId())) {
				newEntry.setSelected(optionalSelections.get(m.getId()));
			}
			this.content.getChildren().add(newEntry);
		}
		this.content.setFillWidth(true);
	}
}
