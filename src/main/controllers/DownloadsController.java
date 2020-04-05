package main.controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import main.downloadtasks.DownloadTask;
import main.downloadtasks.FileDownloadTask;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DownloadsController extends Controller implements Initializable {

    @FXML
    private ScrollPane scrollPane;
    private VBox vBox;
    private DownloadTask mock;

    public void createWindow()  {
        super.createWindow("Creating downloads window!", "/downloads.fxml");
    }

    public void fillVBox() {
        mock = new FileDownloadTask("https://www.google.ro/file.jpg", new File("file.mock"), "MOCK");

        for(int i = 0; i<20; i++)vBox.getChildren().add(new DownloadsVBoxItem(mock).getPane());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        window.setResizable(false);
        vBox = new VBox();
        vBox.setSpacing(3);
        scrollPane.setContent(vBox);
        fillVBox();
    }

    public void closePressed(ActionEvent event) {
        windowIsCreated = false;
        window.close();
    }
}
