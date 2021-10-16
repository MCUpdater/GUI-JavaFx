package org.mcupdater.gui.javafx.components;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.QueueStatus;
import org.mcupdater.gui.javafx.Main;
import org.mcupdater.gui.javafx.MainController;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ProgressView extends Region
{
    private final ScrollPane scroll = new ScrollPane();
    private final VBox content = new VBox();
    private final Map<MultiKey, ProgressItem> items = new HashMap<>();

    public ProgressView() {
        getChildren().add(scroll);
        scroll.setContent(content);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        content.setPadding(new Insets(5));
        content.setSpacing(3);
        //Testing code
        //addProgressBar("I haz long name!", "Test");
        //addProgressBar("2", "Test");
        //addProgressBar("3", "Test");
        //updateProgress("2", "Test", 0.25f, 4, 1);
        //updateProgress("3", "Test", 1f, 1000,300);
    }

    public synchronized void addProgressBar(String jobName, String parentId) {
        MultiKey key = new MultiKey(parentId, jobName);
        content.getChildren().remove(items.get(key)); // Remove old row if one exists
        ProgressItem newItem = new ProgressItem(jobName, parentId);
        content.getChildren().add(newItem);
        items.put(key, newItem);
    }

    public synchronized void updateProgress(DownloadQueue queue) {
        ProgressItem bar = items.get(new MultiKey(queue.getParent(), queue.getName()));
        synchronized(bar){
            if (bar == null) { return; }
            bar.setProgress(queue.getProgress(), queue.getTotalFileCount(), queue.getSuccessFileCount(), queue.getStatus());
        }
    }

    @Override
    protected void layoutChildren() {
        for (Node node: getChildren()) {
            layoutInArea(node, 0, 0, getWidth(), getHeight(), 0, HPos.LEFT, VPos.TOP);
        }
    }

    public int getActiveCount() {
        int activeCount = 0;
        synchronized (this) {
            for (Entry<MultiKey, ProgressItem> item : items.entrySet()) {
                if (item.getValue().isActive()) {
                    activeCount++;
                }
            }
        }
        return activeCount;
    }

    public int getActiveById(String serverId) {
        int activeCount = 0;
        synchronized (this) {
            for (Entry<MultiKey, ProgressItem> item : items.entrySet()) {
                if (item.getKey().getParent().equals(serverId)) {
                    if (item.getValue().isActive()) {
                        activeCount++;
                    }
                }
            }
        }
        return activeCount;
    }

    public String getActiveJobs() {
        String jobs = "";
        synchronized (this) {
            for (Entry<MultiKey, ProgressItem> item : items.entrySet()) {
                if (item.getValue().isActive()) {
                    jobs += item.getKey().toString() + "|";
                }
            }
        }
        return jobs;
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
        public String toString() { return parent + "/" + job; }

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

    private class ProgressItem extends GridPane
    {
        private final Label lblName;
        private final ProgressBar pbProgress;
        private final Label lblStatus;
        private final Button btnDismiss;
        private boolean active;

        public ProgressItem(final String jobName, final String parentId) {
            active = true;
            lblName = new Label(parentId + " - " + jobName);
            pbProgress = new ProgressBar();
            pbProgress.setMaxWidth(Double.MAX_VALUE);
            lblStatus = new Label("Inactive");
            btnDismiss = new Button("", new ImageView(new Image(MainController.class.getResourceAsStream("icons/remove.png"))));
            btnDismiss.setDisable(true);
            btnDismiss.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    MultiKey key = new MultiKey(parentId, jobName);
                    ProgressItem self = items.get(key);
                    items.remove(key);
                    content.getChildren().remove(self);
                }
            });
            ColumnConstraints ccJob = new ColumnConstraints();
            ColumnConstraints ccProgress = new ColumnConstraints(100,100,Double.MAX_VALUE, Priority.SOMETIMES, HPos.CENTER, true);
            ColumnConstraints ccStatus = new ColumnConstraints();
            ColumnConstraints ccDismiss = new ColumnConstraints();
            this.getColumnConstraints().addAll(ccJob, ccProgress, ccStatus, ccDismiss);
            setColumnIndex(lblName, 0);
            setColumnIndex(pbProgress, 1);
            setColumnIndex(lblStatus, 2);
            setColumnIndex(btnDismiss, 3);
            this.setHgap(5);
            this.getChildren().addAll(lblName, pbProgress, lblStatus, btnDismiss);
        }

        public void setProgress(final float progress, final int totalFiles, final int successfulFiles, QueueStatus status) {
            Platform.runLater(() -> {
                pbProgress.setProgress(progress);
                switch (status) {
                    case DOWNLOADING -> {
                        lblStatus.setText(String.format("%d/%d %s", successfulFiles, totalFiles, Main.getTranslation().getString("downloaded")));
                        break;
                    }
                    case POSTPROCESSING -> {
                        lblStatus.setText("Executing");
                        break;
                    }
                    case FINISHED -> {
                        if (successfulFiles == totalFiles) {
                            lblStatus.setText(Main.getTranslation().getString("finished"));
                        } else {
                            lblStatus.setText(String.format("%d %s!",(totalFiles - successfulFiles), Main.getTranslation().getString("failed")));
                        }
                        btnDismiss.setDisable(false);
                        active = false;
                        break;
                    }
                }
            });
        }

        public boolean isActive() {
            return active;
        }
    }
}