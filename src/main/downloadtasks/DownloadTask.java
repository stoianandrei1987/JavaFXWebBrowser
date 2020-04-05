package main.downloadtasks;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import main.Main;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public abstract class DownloadTask extends Task<File> {
    protected String taskID;
    protected File localFile;
    protected LocalDateTime downloadedAt;

    protected void setDownloadedAt() {
        downloadedAt = LocalDateTime.now();
    }

    /*
    public boolean equalsDeleted(DownloadTask check) {
        try {if (this.getLocalFile().getCanonicalPath().
                equals(check.getLocalFile().getCanonicalPath()) &&
                        this.downloadedAt.equals(check.getDownloadedAt())) return true;
        } catch(IOException e) {e.printStackTrace();}
        return false;
    } */

    public LocalDateTime getDownloadedAt() {
        return downloadedAt;
    }

    public void addStateChangeListener() {
        stateProperty().addListener(new ChangeListener<State>() {
            public void changed(ObservableValue<? extends State> source, State oldState, State newState) {
                if (newState.equals(State.SUCCEEDED)) {
                    Main.decreaseNumDownloadThreads();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success!");
                    alert.setHeaderText(null);
                    alert.setContentText("Succeeded downloading " + localFile.getName() + " !");

                    alert.showAndWait();
                } else if (newState.equals(State.FAILED)) {
                    Main.decreaseNumDownloadThreads();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed downloading " + localFile.getName() + " !");
                    alert.showAndWait();
                }
            }
        });
    }

    public File getLocalFile() {
        return localFile;
    }

    public String getTaskID() {
        return taskID;
    }

    public abstract DownloadTask copyTask();
}
