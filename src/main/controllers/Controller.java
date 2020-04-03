package main.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class Controller {

    protected static boolean windowIsCreated = false;
    protected static Stage window = null;

    protected void createWindow(String messageToLog, String FXMLurl)  {

        if(!windowIsCreated) {
            windowIsCreated = true;
            try {

                System.out.println(messageToLog);
                window = new Stage();
                FXMLLoader loader = new FXMLLoader();
                loader.setController(this);
                Parent root = loader.load(DownloadsController.class.getResource(FXMLurl));

                window.setScene(new Scene(root));

                window.show();
                window.setOnCloseRequest((event)-> {windowIsCreated = false;
                window.close();
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
