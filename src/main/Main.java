package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;


public class Main extends Application {

    private Scene myScene;
    private WebView view;
    private TextField textField;
    private HistoryItem currItem;
    private ObservableList<HistoryItem> historyItemObservableList;
    private List<HistoryItem> backForwardList;
    private int backForwardIndex;
    private boolean backForwardWasPressed = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        backForwardIndex = -1;
        backForwardList = new ArrayList<>();
        backForwardWasPressed = false;

        historyItemObservableList = FXCollections.observableArrayList();
        //   Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("Web Browser");
        view = new WebView();
        Platform.runLater(() ->
                view.getEngine().load("https://www.google.ro")
        );

        GridPane g = new GridPane();
        RowConstraints r1 = new RowConstraints(),
                r2 = new RowConstraints(),
                r3 = new RowConstraints();
        r1.setMinHeight(50);
        r1.setMaxHeight(50);
        r2.setMinHeight(700 - 20 - 50);
        r3.setMinHeight(20);
        r3.setMaxHeight(20);

        Label loadLabel = new Label("Loading. Please Wait!");
        ProgressBar progressBar = new ProgressBar();
        loadLabel.setMinWidth(79);
        progressBar.setMinWidth(500);
        progressBar.setMaxSize(Double.MAX_VALUE, 20);
        HBox bottomHBox = new HBox();

        view.getEngine().titleProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (currItem != null && newValue != null) {
                    backForwardIndex++;
                    currItem.setTitle(newValue);
                    currItem.setCreatedAt(LocalDateTime.now());
                    historyItemObservableList.add(currItem);
                    backForwardList.add(backForwardIndex, currItem);
                    if(!backForwardWasPressed) backForwardList = backForwardList.subList(0, backForwardIndex + 1);
                    backForwardWasPressed = false;

                    //deletes penultimate entry if it is squal to final entry
                    if (backForwardIndex >= 1 &&
                            backForwardList.get(backForwardIndex).
                                    equals(backForwardList.get(backForwardIndex - 1))) {
                        backForwardList.remove(backForwardIndex - 1);
                        backForwardIndex--;
                    }

                }
            }
        });


        view.getEngine().locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (true) {
                    currItem = new HistoryItem();
                    currItem.setUri(newValue);
                    textField.setText(newValue);


                }
            }
        });



        progressBar.progressProperty().bind(view.getEngine().getLoadWorker().progressProperty());
        bottomHBox.visibleProperty().bind(view.getEngine().getLoadWorker().runningProperty());

        bottomHBox.setPadding(new Insets(5, 0, 5, 30));

        bottomHBox.setAlignment(Pos.CENTER_LEFT);
        bottomHBox.getChildren().addAll(loadLabel, progressBar);
        GridPane.setConstraints(bottomHBox, 0, 2, 1, 1);
        g.getChildren().add(bottomHBox);

        ColumnConstraints c = new ColumnConstraints();
        c.setPercentWidth(100);

        g.getColumnConstraints().add(c);
        g.getRowConstraints().addAll(r1, r2, r3);
        GridPane.setConstraints(view, 0, 1, 1, 1);
        g.getChildren().add(view);

        Button backBtn = new Button("B"),
                forwBtn = new Button("F"),
                stopBtn = new Button("S"),
                reloadBtn = new Button("R");

        stopBtn.setOnAction(event -> {
            view.getEngine().getLoadWorker().cancel();


            Platform.runLater(() -> {
            });
        });

        backBtn.setOnAction(event -> {
            if (backForwardIndex >= 1) {
                backForwardWasPressed = true;
                backForwardIndex--;
                loadPage(backForwardList.get(backForwardIndex).getUri());
            }
        });

        forwBtn.setOnAction(event -> {
            if (backForwardIndex <= backForwardList.size() - 2) {
                backForwardWasPressed = true;
                backForwardIndex++;
                loadPage(backForwardList.get(backForwardIndex).getUri());
            }
        });

        reloadBtn.setOnAction(event -> {
            loadPage(backForwardList.get(backForwardIndex).getUri());
        });

        HBox upperHBox = new HBox();
        upperHBox.setAlignment(Pos.CENTER_LEFT);
        upperHBox.setSpacing(20);
        upperHBox.setPadding(new Insets(0, 50, 0, 50));


        textField = new TextField();
        textField.setOnKeyPressed(textFieldHandler);
        upperHBox.getChildren().addAll(backBtn, forwBtn, stopBtn, reloadBtn, textField);
        GridPane.setConstraints(upperHBox, 0, 0, 1, 1);
        textField.setMinWidth(500);
        g.getChildren().add(upperHBox);
        GridPane.setHalignment(upperHBox, HPos.CENTER);

        primaryStage.setMinHeight(700);
        primaryStage.setMinWidth(700);
        myScene = new Scene(g);
        primaryStage.setScene(myScene);

        primaryStage.show();
        primaryStage.setOnCloseRequest((e) -> {
            Platform.exit();
            System.exit(0);

        });
    }


    public static void main(String[] args) {
        launch(args);
    }


    EventHandler<KeyEvent> textFieldHandler = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {

            if (event.getCode() == KeyCode.ESCAPE) {
                ((TextField) event.getSource()).selectAll();
            }

            if (event.getCode() == KeyCode.ENTER) {

                backForwardList = backForwardList.subList(0, backForwardIndex + 1);
                String url = ((TextField) event.getSource()).getText();
                boolean stillNotFound = true;

                if (isURL(url)) {
                    loadPage(url);
                    stillNotFound = false;
                }

                if (url.startsWith("www") && stillNotFound) {
                    if (isURL("https://" + url)) {
                        loadPage("https://" + url);
                        stillNotFound = false;
                    }
                    if (isURL("http://" + url) && stillNotFound) {
                        loadPage("http://" + url);
                        stillNotFound = false;
                    }
                } else {
                    if (isURL("https://www." + url)) {
                        loadPage("https://www." + url);
                        stillNotFound = false;
                    }
                    if (isURL("http://www." + url) && stillNotFound) {
                        loadPage("http://www." + url);
                        stillNotFound = false;
                    }
                }

                if (stillNotFound) loadPage("https://google.ro/search?q=" + url);
            }

        }
    };

    public boolean isURL(String url) {
        try {
            (new java.net.URL(url)).openStream().close();
            return true;
        } catch (Exception ex) {
        }
        return false;
    }

    public void loadPage(String url) {
        Platform.runLater(() -> view.getEngine().load(url));
    }


}
