package org.mcupdater.gui.javafx.components;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.logging.Level;

public class ConsolePane extends Region
{
    private final WebView console = new WebView();
    private final WebEngine engine = console.getEngine();
    private final Deque<String> logEntries = new ArrayDeque<>();

    public ConsolePane() {
        getChildren().add(console);
        //console.setWrapText(true);
        //console.setStyle("-fx-font-family: monospace;");
    }

    @Override
    protected void layoutChildren() {
        for (Node node: getChildren()) {
            layoutInArea(node, 0, 0, getWidth(), getHeight(), 0, HPos.LEFT, VPos.TOP);
        }
    }

    public void appendEntry(String msg, Level level) {
        String entry = "<div class=" + level.getName().toLowerCase(Locale.ROOT) + ">" + msg + "</div>";
        logEntries.addLast(entry);
        while (logEntries.size() > 500) {
            logEntries.removeFirst();
        }
        String allEntries = String.join("", logEntries);
        Platform.runLater(() -> {
            engine.loadContent("<html><head><style>body { background-image: linear-gradient(#555555, black); background-attachment: fixed; font-family: monospace; } .info { background-image: linear-gradient(to right, #00660066, #00220022, #00000000); color: #ffffffff; } .warning { background-image: linear-gradient(to right, #ffff0066, #aaaa0044, #aaaa0022, #00000000); color: #ffffffff; } .severe { background-image: linear-gradient(to right, #ff000088, #ff000022, #00000000); color: #ffffffff; }</style></head><body>" + allEntries + "</body></html>");
            engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED)
                    engine.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            });
        });
    }
}