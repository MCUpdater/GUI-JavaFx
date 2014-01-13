package org.mcupdater.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.mcupdater.model.ServerList;

class InstanceListCell extends ListCell<ServerList> {

    private final BorderPane cell = new BorderPane();

    @Override
    public void updateItem(ServerList item, boolean empty){
        super.updateItem(item, empty);
        ImageView serverIcon = new ImageView();
        if (empty) {
            setGraphic(null);
        } else {
            serverIcon.setImage(new Image(item.getIconUrl()));
            serverIcon.setFitHeight(32);
            serverIcon.setPreserveRatio(true);
            serverIcon.setSmooth(true);
            cell.setLeft(serverIcon);
            VBox serverData = new VBox();
            Label entryName = new Label(item.getName());
            Font entryFont = new Font("Helvetica",10);
            entryName.setFont(entryFont);
            Label entryVersion = new Label("Version: " + item.getVersion());
            entryVersion.setFont(entryFont);
            Label entryRevision = new Label("Revision: " + item.getRevision());
            entryRevision.setFont(entryFont);
            serverData.getChildren().addAll(entryName, entryVersion, entryRevision);
            serverData.setPadding(new Insets(0,0,0,5));
            cell.setCenter(serverData);
            setGraphic(cell);
        }
    }

}
