package org.mcupdater.gui;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

public class ConsolePane extends Region
{
	public final TextArea console = new TextArea();

	public ConsolePane() {
		getChildren().add(console);
		//console.setWrapText(true);
		console.setStyle("-fx-font-family: monospace;");
	}

	@Override
	protected void layoutChildren() {
		for (Node node: getChildren()) {
			layoutInArea(node, 0, 0, getWidth(), getHeight(), 0, HPos.LEFT, VPos.TOP);
		}
	}
}
