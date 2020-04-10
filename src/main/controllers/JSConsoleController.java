package main.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebEngine;
import main.Main;
import netscape.javascript.JSObject;
import org.w3c.dom.traversal.NodeFilter;

import java.net.URL;
import java.util.ResourceBundle;


public class JSConsoleController extends Controller implements Initializable {

    private static SimpleStringProperty simple = new SimpleStringProperty();
    private String lastText = "";
    private final JavaBridge bridge = new JavaBridge();
    @FXML
    TextArea console;

    @FXML
    TextArea log;

    static TextArea logToGet;

    public static TextArea getLog(){
        return logToGet;
    }


    public void createWindow(){
        super.createWindow("Creating JSConsoleWindow", "/console.fxml");
        window.setTitle("JSConsole");
        window.setResizable(false);

    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        WebEngine engine = Main.getView().getEngine();
     //   log.setFocusTraversable(false);
     //   log.setEditable(false);



        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("java", bridge);
                engine.executeScript("console.log = function(message)\n" +
                        "{\n" +
                        "    java.log(message);\n" +
                        "};");
            }
        });

        log.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                log.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
                //use Double.MIN_VALUE to scroll to the top
            }
        });

        logToGet = log;

    }

    public void closeWindow(ActionEvent event) {
        windowIsCreated = false;
        window.close();
    }

    public void runScript(ActionEvent event) {
        Main.getView().getEngine().executeScript(console.getText());

    }
}
