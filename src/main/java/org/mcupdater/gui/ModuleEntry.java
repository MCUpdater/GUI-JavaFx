package org.mcupdater.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Region;
import org.mcupdater.model.ModType;
import org.mcupdater.model.Module;

public class ModuleEntry extends Region
{
	private CheckBox chkModule;
	private Module data;

	public ModuleEntry(Module mod) {
		if (mod.getModType() == ModType.Option) {
			//TODO: Unimplemented
		} else {
			this.data = mod;
			chkModule = new CheckBox(mod.getName());
			if (mod.getRequired() || mod.getIsDefault()) { chkModule.setSelected(true); }
			if (mod.getRequired()) { chkModule.setDisable(true); }
			this.getChildren().add(chkModule);
			this.setMinHeight(16);
			chkModule.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event) {
					if (chkModule.isSelected()) {
						for (String modid : data.getDepends().split(" ")) {
							for (ModuleEntry entry : MainController.getInstance().pnlModule.getModules()) {
								if (entry.getModule().getId().equals(modid)) {
									entry.setSelected(true);
								}
							}
						}
					}
				}
			});
		}
	}

	public boolean isSelected() {
		return this.chkModule.isSelected();
	}

	public void setSelected(boolean state) {
		this.chkModule.setSelected(state);
		this.chkModule.fireEvent(new ActionEvent());
	}

	public Module getModule() {
		return this.data;
	}

	public void setModule(Module data) {
		this.data = data;
	}
}
