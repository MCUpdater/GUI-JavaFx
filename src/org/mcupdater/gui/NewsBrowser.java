package org.mcupdater.gui;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Created by sbarbour on 1/2/14.
 */
public class NewsBrowser extends Region {
    WebView view = new WebView();
    WebEngine engine = view.getEngine();

    public NewsBrowser() {
        engine.load("http://www.mcupdater.com");

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
