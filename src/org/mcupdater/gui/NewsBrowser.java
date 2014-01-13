package org.mcupdater.gui;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class NewsBrowser extends Region {
    final WebView view = new WebView();
    final WebEngine engine = view.getEngine();

    public NewsBrowser() {
        getChildren().add(view);
    }

    public void navigate(String newURL) {
       engine.load(newURL);
    }

    @Override
    protected void layoutChildren() {
        for (Node node: getChildren()) {
            layoutInArea(node, 0, 0, getWidth(), getHeight(), 0, HPos.LEFT, VPos.TOP);
        }
    }
}
