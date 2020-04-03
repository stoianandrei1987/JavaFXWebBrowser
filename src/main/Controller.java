package main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class Controller {
    protected static boolean windowIsCreated = false;
    protected static Stage window = null;

    protected void init(){

    }

    public void createWindow(String messageToLog, String FXMLurl)  {

        if(!windowIsCreated) {
            windowIsCreated = true;
            try {

                System.out.println(messageToLog);
                Parent root = FXMLLoader.load(DownloadsController.class.getResource(FXMLurl));
                window = new Stage();
                window.setScene(new Scene(root));
                init();
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
