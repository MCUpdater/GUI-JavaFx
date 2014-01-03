package org.mcupdater.gui;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.mcupdater.model.ServerList;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by sbarbour on 1/2/14.
 */
public class MainController implements Initializable {

    public NewsBrowser newsBrowser;
    public ListView listInstances;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listInstances.setCellFactory(new Callback<ListView<ServerList>, ListCell<ServerList>>(){

            @Override
            public ListCell<ServerList> call(ListView<ServerList> serverListListView) {
                return new InstanceListCell();
            }
        });
        System.out.println("Initialized");
    }

    public void ClickButton(ActionEvent actionEvent) {
        newsBrowser.navigate("http://www.google.com");
    }
}
