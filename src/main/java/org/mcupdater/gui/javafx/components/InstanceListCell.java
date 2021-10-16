package org.mcupdater.gui.javafx.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.mcupdater.gui.javafx.MainController;
import org.mcupdater.model.ServerList;
import org.mcupdater.util.MCUpdater;

import java.net.URI;
import java.net.URL;

public class InstanceListCell extends ListCell<ServerList> {

    private final StackPane stack = new StackPane();
    private final VBox cell = new VBox();
    private final ImageView statusIcon = new ImageView();
    private Label entryName = new Label();
    private ImageView serverIcon = new ImageView();

    public final Image STATUS_UNKNOWN = new Image(MainController.class.getResource("icons/disconnect.png").toString());
    public final Image STATUS_ERROR = new Image(MainController.class.getResource("icons/cross.png").toString());
    public final Image STATUS_UPDATE = new Image(MainController.class.getResource("icons/asterisk_yellow.png").toString());
    public final Image STATUS_READY = new Image(MainController.class.getResource("icons/tick.png").toString());

    public InstanceListCell() {
        stack.setMaxSize(100,100);
        stack.setMinSize(100,100);
        stack.setAlignment(Pos.TOP_LEFT);
        serverIcon.setEffect(new Blend(BlendMode.OVERLAY, new Reflection(), new InnerShadow(50,Color.rgb(0,0,0,0.9))));
        entryName.setTextFill(Color.LIGHTGREY);
        entryName.setEffect(null);
    }

    @Override
    public void updateSelected(boolean selected){
        super.updateSelected(selected);
        Effect effect;
        if (selected) {
            effect = new DropShadow(50, Color.VIOLET);
            serverIcon.setEffect(new Reflection());
            entryName.setTextFill(Color.YELLOW);
            entryName.setEffect(new Bloom());
        } else {
            effect = null;
            serverIcon.setEffect(new Blend(BlendMode.OVERLAY, new Reflection(), new InnerShadow(50,Color.rgb(0,0,0,0.9))));
            entryName.setTextFill(Color.LIGHTGREY);
            entryName.setEffect(null);
        }
        cell.setEffect(effect);
    }

    @Override
    public boolean isItemChanged(ServerList oldItem, ServerList newItem) {
        boolean changed = super.isItemChanged(oldItem, newItem);
        if (!changed && oldItem.getState() != newItem.getState()) {
            changed = true;
        }
        return changed;
    }

    @Override
    public void updateItem(ServerList item, boolean empty){
        super.updateItem(item, empty);
        cell.getChildren().clear();
        cell.setMaxSize(100.0,100.0);
        cell.setMinSize(100.0, 100.0);
        cell.setAlignment(Pos.CENTER);
        this.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,null,null)));
        if (!empty) {
            URL icon;
            try {
                icon = (new URI(item.getIconUrl())).toURL();
            } catch (Exception e) {
                icon = MCUpdater.class.getResource("/minecraft.png");
            }
            serverIcon.setImage(new Image(icon.toString()));
            serverIcon.setFitHeight(64);
            serverIcon.setPreserveRatio(true);
            serverIcon.setSmooth(true);
            switch (item.getState()) {
                case READY -> statusIcon.setImage(STATUS_READY);
                case UPDATE -> statusIcon.setImage(STATUS_UPDATE);
                case ERROR -> statusIcon.setImage(STATUS_ERROR);
                case UNKNOWN -> statusIcon.setImage(STATUS_UNKNOWN);
                default -> statusIcon.setImage(null);
            }
            //cell.setLeft(serverIcon);
            //VBox serverData = new VBox();
            entryName.setText(item.getName());
            //Font primaryFont = new Font("Helvetica",14);
            Font primaryFont = Font.font("Helvetica", FontWeight.BOLD, 12);
            Font secondaryFont = new Font("Helvetica", 12);
            DropShadow shadow = new DropShadow(3, 0.25, 0.25, Color.BLACK);
            shadow.setBlurType(BlurType.ONE_PASS_BOX);
            entryName.setFont(primaryFont);
            //entryName.setTextFill(Color.WHITE);
            entryName.setWrapText(true);
            entryName.setTextAlignment(TextAlignment.CENTER);
            entryName.setPrefHeight(36);
            //entryName.setEffect(shadow);
            //entryName.getStyleClass().add("outline");
            //entryName.setMaxWidth(120.0);
            Label entryVersion = new Label("MC: " + item.getVersion());
            entryVersion.setFont(secondaryFont);
            entryVersion.setTextFill(Color.WHITE);
            entryVersion.setEffect(shadow);
            Label entryRevision = new Label("Rev: " + item.getRevision());
            entryRevision.setFont(secondaryFont);
            entryRevision.setTextFill(Color.WHITE);
            entryRevision.setEffect(shadow);
            cell.getChildren().addAll(serverIcon, entryName);
            //cell.setPadding(new Insets(0,0,0,5));
            //this.setBackground(new Background(new BackgroundImage(serverIcon.getImage(), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(75.0, 75.0, true, true, true, false))));
            //cell.setCenter(serverData);
        }
        stack.getChildren().clear();
        stack.getChildren().add(cell);
        if (!empty) stack.getChildren().add(statusIcon);
        setGraphic(stack);
    }

}