package main.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import main.Main;
import main.downloadtasks.DownloadTask;
import main.downloadtasks.FileDownloadTask;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DownloadsController extends Controller implements Initializable {

    @FXML
    private ScrollPane scrollPane;
    private static VBox vBox;
    private static DownloadTask mock;

    public void createWindow()  {
        super.createWindow("Creating downloads window!", "/downloads.fxml");
    }

    public static void fillVBox() {
        mock = new FileDownloadTask("https://www.google.ro/file.jpg", new File("file.mock"), "MOCK");
        vBox.getChildren().clear();
        for(DownloadTask downloadTask : Main.getDownloads())vBox.getChildren().
                add(new DownloadsVBoxItem(downloadTask).getPane());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        window.setTitle("Downloads");
        window.setResizable(false);
        Main.getNumthreadsDownloading().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                fillVBox();
            }
        });
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
