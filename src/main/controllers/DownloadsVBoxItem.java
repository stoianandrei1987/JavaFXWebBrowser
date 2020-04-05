package main.controllers;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import main.downloadtasks.DownloadTask;

public class DownloadsVBoxItem {
    private GridPane pane;
    private DownloadTask task;
    private Label filename = new Label();
    private Label downloadStatus = new Label();
    private Label progressLabel = new Label();
    private ProgressBar progressBar = new ProgressBar();

    public GridPane getPane() {
        return pane;
    }

    public DownloadsVBoxItem(DownloadTask task) {
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
        progressLabel.setText("      progress : ");
        GridPane.setConstraints(filename, 0, 0, 1, 1);
        GridPane.setConstraints(downloadStatus, 0, 1, 1, 1);
        GridPane.setConstraints(progressLabel, 0, 2, 1, 1);
        GridPane.setConstraints(progressBar, 0, 2, 1, 1);
        progressBar.setTranslateX(70);
        progressBar.setTranslateY(2);
        progressBar.setMinWidth(120);
        progressBar.setProgress(0.33);
        pane.getChildren().addAll(filename, downloadStatus, progressLabel, progressBar);

        // progressBar.progressProperty().bind(task.progressProperty());
        pane.getRowConstraints().addAll(r1, r2, r3);

    }
}
