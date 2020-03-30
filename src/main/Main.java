package main;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Thread.currentThread;


public class Main extends Application {

    private Scene myScene;
    private WebView view;
    private TextField textField;
    private HistoryItem currItem;
    private ObservableList<HistoryItem> historyItemObservableList;
    private List<HistoryItem> backForwardList;
    private int backForwardIndex;
    private boolean backForwardWasPressed = false;
    private boolean incognitoMode = false;
    private Map<String, Long> visitedAddresses = new HashMap<>();


    public void initialize() {

        backForwardIndex = -1;
        backForwardList = new ArrayList<>();
        backForwardWasPressed = false;
        historyItemObservableList = IOClass.getHistory();
        visitedAddresses = IOClass.getAddresses();
        view = new WebView();
        currentThread().setPriority(6);

        Runnable writeThings = () -> {
            IOClass.writeAddresses(visitedAddresses);
            IOClass.writeHistory(historyItemObservableList);
        };

        if(incognitoMode == false) {
            ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
            service.scheduleWithFixedDelay(writeThings, 0, 60, TimeUnit.SECONDS);
        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        initialize();
        primaryStage.setTitle("Web Browser");

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

        Label loadLabel = new Label("Loading. Please Wait!   ");
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
                    if (!backForwardWasPressed) backForwardList = backForwardList.subList(0, backForwardIndex + 1);
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

                currItem = new HistoryItem();
                currItem.setUri(newValue);
                textField.setText(newValue);
                if(oldValue!=null && !shortenURL(oldValue).equals(shortenURL(newValue)))
                    registerVisit(shortenURL(newValue));



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

        Button backBtn = new Button(),
                forwBtn = new Button(),
                stopBtn = new Button(),
                reloadBtn = new Button();

        Image backIcon = new Image(getClass().getResourceAsStream("/icons/back.png"));
        backBtn.setGraphic(new ImageView(backIcon));

        Image forwIcon = new Image(getClass().getResourceAsStream("/icons/forward.png"));
        forwBtn.setGraphic(new ImageView(forwIcon));

        Image stopIcon = new Image(getClass().getResourceAsStream("/icons/stop.png"));
        stopBtn.setGraphic(new ImageView(stopIcon));

        Image reloadIcon = new Image(getClass().getResourceAsStream("/icons/reload.png"));
        reloadBtn.setGraphic(new ImageView(reloadIcon));

        for (Button b : Arrays.asList(backBtn, forwBtn, stopBtn, reloadBtn)) {
            b.getStyleClass().add("upper-btns");
        }


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
        upperHBox.setStyle("-fx-background-color: white;");


        textField = new TextField();
        TextFields.bindAutoCompletion(textField, new Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>>() {
            @Override
            public Collection<String> call(AutoCompletionBinding.ISuggestionRequest param) {

                String paramUserText = param.getUserText();
                Comparator<String> stringComparator = new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if((visitedAddresses.get(o1) - visitedAddresses.get(o2)) > 0) return -1;
                        else return 1;
                    }
                };
                return visitedAddresses.keySet().stream().filter(s -> s.contains(paramUserText)).
                        sorted(stringComparator).limit(5).collect(Collectors.toList());

            }
        });

        textField.setOnKeyPressed(textFieldHandler);
        upperHBox.getChildren().addAll(backBtn, forwBtn, stopBtn, reloadBtn, textField);
        GridPane.setConstraints(upperHBox, 0, 0, 1, 1);
        textField.setMinWidth(500);
        g.getChildren().add(upperHBox);
        GridPane.setHalignment(upperHBox, HPos.CENTER);

        primaryStage.setMinHeight(700);
        primaryStage.setMinWidth(700);
        myScene = new Scene(g);
        myScene.getStylesheets().add("stylesheet.css");
        primaryStage.setScene(myScene);

        primaryStage.show();
        primaryStage.setOnCloseRequest((e) -> {

      //      IOClass.writeAddresses(visitedAddresses);
      //      IOClass.writeHistory(historyItemObservableList);

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

    public String shortenURL(String url) {
        if (url.startsWith("https://")) url = url.substring(8);
        else url = url.substring(7);
        if(url.contains("/")) url = url.substring(0, url.indexOf("/"));
        return url;
    }

    public void registerVisit(String url) {

        if(visitedAddresses.containsKey(url)) {
            Long nv = visitedAddresses.get(url);
            nv++;
            visitedAddresses.replace(url, nv);
        } else {
            visitedAddresses.put(url, (long) 1);
        }

    }

    public void loadPage(String url) {

     //   registerVisit(url);
        Platform.runLater(() -> view.getEngine().load(url));

    }


}
