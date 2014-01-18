package org.mcupdater.gui;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;

public class ProgressView extends Region
{
	private ScrollPane scroll = new ScrollPane();
	private Accordion content = new Accordion();
	private Map<MultiKey, ProgressItem> items = new HashMap<MultiKey, ProgressItem>();

	public ProgressView() {
		getChildren().add(scroll);
		scroll.setContent(content);
		TitledPane special = new TitledPane("",null);
		BorderPane testHeader = new BorderPane();
		testHeader.setLeft(new Label("Foobar!"));
		ProgressBar pbTest = new ProgressBar();
		pbTest.setProgress(0.65);
		testHeader.setCenter(pbTest);
		testHeader.setRight(new Button("I'm a button!"));
		special.setGraphic(HBoxBuilder.create().children(testHeader).build());
		content.getPanes().addAll(new TitledPane("T1", new Button("A")), new TitledPane("T2", new Button("B")), special);
		scroll.setFitToHeight(true);
		scroll.setFitToWidth(true);
	}

	@Override
	protected void layoutChildren() {
		for (Node node: getChildren()) {
			layoutInArea(node, 0, 0, getWidth(), getHeight(), 0, HPos.LEFT, VPos.TOP);
		}
	}

	private class MultiKey
	{
		private final String parent;
		private final String job;

		public MultiKey(String parent, String job){
			this.parent = parent;
			this.job = job;
		}

		public String getParent(){
			return parent;
		}

		public String getJob(){
			return job;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof MultiKey)) return false;
			MultiKey key = (MultiKey) o;
			return parent.equals(key.parent) && job.equals(key.job);
		}

		@Override
		public int hashCode() {
			int result = parent.hashCode();
			result = 31 * result + job.hashCode();
			return result;
		}
	}

	private class ProgressItem
	{
	}
}
