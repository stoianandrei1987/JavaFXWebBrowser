package main.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import main.Main;
import main.downloadtasks.DownloadTask;

import java.io.IOException;


public class DownloadsVBoxItem {
    private GridPane pane;
    private DownloadTask task;
    private Label filename = new Label();
    private Label downloadStatus = new Label();
    private Label progressLabel = new Label();
    private ProgressBar progressBar = new ProgressBar();
    private Hyperlink open = new Hyperlink("Open");
    private Hyperlink cancel = new Hyperlink("Cancel");
    private Hyperlink delete = new Hyperlink("Delete");
    private Hyperlink restart = new Hyperlink("Restart");
    private HBox hBox = new HBox();

    public GridPane getPane() {
        return pane;
    }

    public void setHBoxOptions () {
        hBox.getChildren().clear();
        if(task.getState() == Worker.State.RUNNING) hBox.getChildren().addAll(cancel, delete);
        if(task.getState() == Worker.State.CANCELLED)
            hBox.getChildren().addAll(open, restart, delete);
        if(task.getState() == Worker.State.SUCCEEDED)
            hBox.getChildren().addAll(open, delete);
    }

    public DownloadsVBoxItem(DownloadTask task) {

        open.setOnAction(event -> {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," +
                            task.getLocalFile().getCanonicalPath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setHeaderText(null);
                    alert.setContentText("Unable to open folder. Try opening manually!");
                    alert.showAndWait();
                }
            }
        });

        cancel.setOnAction(event -> {
            task.cancel();
        });

        restart.setOnAction(event -> {
            task.run();
        });

        delete.setOnAction(event -> {
            if(task.isRunning()) task.cancel();
            task.getLocalFile().delete();
            Main.getDownloads().removeIf(item->item.equals(task));
            DownloadsController.fillVBox();
        });

        this.task = task;
        this.pane = new GridPane();
        this.pane.setMaxHeight(70);
        RowConstraints r1 = new RowConstraints();
        RowConstraints r2 = new RowConstraints();
        RowConstraints r3 = new RowConstraints();
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        r1.setPercentHeight(33);
        r2.setPercentHeight(33);
        c1.setPercentWidth(70);
        c2.setPercentWidth(30);
        filename.setText("      filename : "+task.getLocalFile().getName());
        downloadStatus.setText("      status : "+task.getState().toString());
        task.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                downloadStatus.setText("      status : "+newValue.toString());
                setHBoxOptions();

            }
        });
        progressLabel.setText("      progress : ");
        progressLabel.visibleProperty().bind(task.runningProperty());
        GridPane.setConstraints(filename, 0, 0, 1, 1);
        GridPane.setConstraints(downloadStatus, 0, 1, 1, 1);
        GridPane.setConstraints(progressLabel, 0, 2, 1, 1);
        GridPane.setConstraints(progressBar, 0, 2, 1, 1);
        GridPane.setConstraints(hBox, 1, 0, 1,3);
        GridPane.setHalignment(hBox, HPos.CENTER);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(0));
        hBox.setSpacing(20);
        //hBox.setTranslateY();
        hBox.setTranslateX(150);
        hBox.getChildren().addAll(open, cancel, delete);
        progressBar.setTranslateX(85);
        progressBar.setTranslateY(2);
        progressBar.setMinWidth(120);
        // progressBar.setProgress(0.33);
        pane.getChildren().addAll(filename, downloadStatus, progressLabel, progressBar, hBox);
        progressBar.visibleProperty().bind(task.runningProperty());
        progressBar.progressProperty().bind(task.progressProperty());
        pane.getRowConstraints().addAll(r1, r2, r3);

    }
}
