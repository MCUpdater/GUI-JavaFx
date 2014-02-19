package org.mcupdater.gui;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.Region;
import org.mcupdater.model.ModType;
import org.mcupdater.model.Module;

public class ModuleEntry extends Region
{
	private CheckBox chkModule;
	private Module data;

	public ModuleEntry(Module data) {
		if (data.getModType() == ModType.Option) {
			//TODO: Unimplemented
		} else {
			this.data = data;
			chkModule = new CheckBox(data.getName());
			if (data.getRequired() || data.getIsDefault()) { chkModule.setSelected(true); }
			if (data.getRequired()) { chkModule.setDisable(true); }
			this.getChildren().add(chkModule);
			this.setMinHeight(16);
		}
	}

	public boolean isSelected() {
		return this.chkModule.isSelected();
	}

	public void setSelected(boolean state) {
		this.chkModule.setSelected(state);
	}

	public Module getModule() {
		return this.data;
	}

	public void setModule(Module data) {
		this.data = data;
	}
}
