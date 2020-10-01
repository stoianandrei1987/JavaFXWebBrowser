package main;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.controllers.DownloadsController;
import main.controllers.HistoryController;
import main.controllers.JSConsoleController;
import main.downloadtasks.Base64DownloadTask;
import main.downloadtasks.DownloadTask;
import main.downloadtasks.FileDownloadTask;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

import javax.xml.transform.TransformerException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Thread.currentThread;


public class Main extends Application {

    private static ObservableList<HistoryItem> historyItemObservableList;
    private Scene myScene;
    private static WebView view;
    private TextField textField;
    private HistoryItem currItem;
    // private ObservableList<HistoryItem> historyItemObservableList;
    private List<HistoryItem> backForwardList;
    private int backForwardIndex;
    private boolean backForwardWasPressed = false;
    private boolean incognitoMode;
    private Map<String, Long> visitedAddresses = new HashMap<>();
    private static ProgressBar progressBar = new ProgressBar();
    private static HBox bottomHBox = new HBox();
    private static Label loadLabel = new Label();
    private Stage primaryStageCopy;
    private static SimpleIntegerProperty numThreadsDownloading = new SimpleIntegerProperty(0);
    private static List<DownloadTask> downloads = new ArrayList<>();
    private static boolean progressBarBoundToThread = false;
    private static boolean mouseOverImage = false;
    private String mouseOverImageSrc = "";
    private static DownloadTask taskBoundToPb = null;
    private String initialDir = "C:\\Users\\Andrei\\Desktop\\testfolder";
    private static final Clipboard clipboard = Clipboard.getSystemClipboard();
    private static final ClipboardContent clipboardContent = new ClipboardContent();
    private static boolean viewwingPageSource = false;
    private static String extraCSSfix;

    public void initialize() {

        IOClass.createJSONS();
        backForwardIndex = -1;
        backForwardList = new ArrayList<>();
        backForwardWasPressed = false;
        incognitoMode = IOClass.getIncognito();
        historyItemObservableList = IOClass.getHistory();
        visitedAddresses = IOClass.getAddresses();
        HttpCManager.initialize();
        view = new WebView();
        view.setContextMenuEnabled(false);
        createContextMenu(view);
        extraCSSfix = new Scanner(Main.class.getClassLoader().getResourceAsStream("pagefix.css"), "UTF-8")
                .useDelimiter("\\A").next();
        view.getEngine().setUserStyleSheetLocation(Main.class.getClassLoader().
                getResource("pagefix.css").toExternalForm());
        numThreadsDownloading.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if (newValue.intValue() == 0) {
                    System.out.println("number of threads now zero");
                    bindProgressBarNormally();
                    progressBarBoundToThread = false;
                } else if (newValue.intValue() > oldValue.intValue()) {
                    System.out.println("increase in download threads");
                } else {
                    System.out.println("decrease in download threads");
                    if (taskBoundToPb.isDone()) {
                        System.out.println("switching pb to diff thread : " + downloads.stream().
                                filter(task -> ((!task.isDone()) && !task.isCancelled())).findFirst().get().getTaskID());
                        bindPbToDownloadTask(downloads.stream().filter(task -> (!task.isDone()) && (!task.isCancelled())).
                                findFirst().get());
                    }
                }
            }
        });

        currentThread().setPriority(6);
        Runnable writeThings = () -> {
            IOClass.writeAddresses(visitedAddresses);
            IOClass.writeHistory(historyItemObservableList);
        };

        //changed made for test
        if (incognitoMode == false) {
            ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
            service.scheduleWithFixedDelay(writeThings, 15, 60, TimeUnit.SECONDS);
        }

    }

    public static ObservableList<HistoryItem> getHList() {
        return historyItemObservableList;
    }

    public static List<DownloadTask> getDownloads() {
        return downloads;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        initialize();
        primaryStageCopy = primaryStage;
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

        loadLabel.setMinWidth(79);
        progressBar.setMinWidth(500);
        progressBar.setMaxSize(Double.MAX_VALUE, 20);

        view.getEngine().titleProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (viewwingPageSource) {

                } else if (currItem != null && newValue != null) {
                    backForwardIndex++;
                    currItem.setTitle(newValue);
                    currItem.setCreatedAt(LocalDateTime.now());
                    historyItemObservableList.add(currItem);
                    backForwardList.add(backForwardIndex, currItem);
                    if (!backForwardWasPressed) backForwardList = backForwardList.subList(0, backForwardIndex + 1);
                    backForwardWasPressed = false;

                    //testing
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

                if (!viewwingPageSource) {
                    currItem = new HistoryItem();
                    currItem.setUri(newValue);
                    textField.setText(newValue);
                    if (oldValue != null && !oldValue.equals("")
                            && !shortenURL(oldValue).equals(shortenURL(newValue)))
                        registerVisit(shortenURL(newValue));
                } else textField.setText("pagesource");

            }
        });

        view.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {

                if (newValue.toString().equals("RUNNING")) {

                }
                if (newValue.toString().equals("SUCCEEDED")) {
                    String ytJS = new Scanner(Main.class.getClassLoader().
                            getResourceAsStream("youtube.js"), "UTF-8").useDelimiter("\\A").next();
                    if(view.getEngine().getLocation().contains("www.youtube.com")) {
                        view.getEngine().executeScript(ytJS);
                    }

                    org.w3c.dom.events.EventListener mouseOverEventListener = new org.w3c.dom.events.EventListener() {
                        @Override
                        public void handleEvent(org.w3c.dom.events.Event evt) {
                            String src = ((Element) evt.getTarget()).getAttribute("src");
                            src = fixImgSrcURL(src);
                            mouseOverImage = true;
                            mouseOverImageSrc = src;
                            System.out.println("Mouse over image : " + mouseOverImageSrc);

                        }
                    };


                    org.w3c.dom.events.EventListener mouseOutEventListener = new org.w3c.dom.events.EventListener() {
                        @Override
                        public void handleEvent(org.w3c.dom.events.Event evt) {
                            mouseOverImage = false;
                            mouseOverImageSrc = "";
                        }

                    };

                    Document document = view.getEngine().getDocument();
                    NodeList nodeList = document.getElementsByTagName("img");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener("mouseover", mouseOverEventListener, false);
                        ((EventTarget) nodeList.item(i)).addEventListener("mouseout", mouseOutEventListener, false);
                    }

                }

                /*

                if(oldValue.toString().equals("READY") && newValue.toString().equals("SCHEDULED")) {


                    if (backForwardList.get(backForwardIndex).getUri().contains("www.google.")) {
                        System.out.println("ready -> running");
                        view.getEngine().
                                executeScript("document.getElementById(\"lb\").style.display=\"none\";");
                    };
                }


                 */

                if (oldValue.toString().equals("RUNNING") && newValue.toString().equals("CANCELLED"))
                    tryDownload(view.getEngine().getLocation());
            }
        });

        bindProgressBarNormally();
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
                reloadBtn = new Button(),
                downloadsBtn = new Button(),
                menuBtn = new Button(),
                historyBtn = new Button();

        Image backIcon = new Image(getClass().getResourceAsStream("/icons/back.png"));
        backBtn.setGraphic(new ImageView(backIcon));

        Image forwIcon = new Image(getClass().getResourceAsStream("/icons/forward.png"));
        forwBtn.setGraphic(new ImageView(forwIcon));

        Image stopIcon = new Image(getClass().getResourceAsStream("/icons/stop.png"));
        stopBtn.setGraphic(new ImageView(stopIcon));

        Image reloadIcon = new Image(getClass().getResourceAsStream("/icons/reload.png"));
        reloadBtn.setGraphic(new ImageView(reloadIcon));

        Image historyIcon = new Image(getClass().getResourceAsStream("/icons/history.png"));
        historyBtn.setGraphic(new ImageView(historyIcon));

        Image downloadsIcon = new Image(getClass().getResourceAsStream("/icons/downloads.png"));
        downloadsBtn.setGraphic(new ImageView(downloadsIcon));

        Image menuIcon = new Image(getClass().getResourceAsStream("/icons/menu.png"));
        menuBtn.setGraphic(new ImageView(menuIcon));

        for (Button b : Arrays.asList(backBtn, forwBtn, stopBtn, reloadBtn, historyBtn, downloadsBtn, menuBtn)) {
            b.getStyleClass().add("upper-btns");
        }


        stopBtn.setOnAction(event -> {
            view.getEngine().getLoadWorker().cancel();


            Platform.runLater(() -> {
            });
        });

        backBtn.setOnAction(event -> {
            if (viewwingPageSource) {
                viewwingPageSource = false;
                loadPage(backForwardList.get(backForwardIndex).getUri());
            } else if (backForwardIndex >= 1) {
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
            if (!viewwingPageSource)
                loadPage(backForwardList.get(backForwardIndex).getUri());
        });


        downloadsBtn.setOnAction(event -> {
            new DownloadsController().createWindow();
        });

        historyBtn.setOnAction(event -> {
            new HistoryController().createWindow();
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
                        if ((visitedAddresses.get(o1) - visitedAddresses.get(o2)) > 0) return -1;
                        else return 1;
                    }
                };
                return visitedAddresses.keySet().stream().filter(s -> s.contains(paramUserText)).
                        sorted(stringComparator).limit(5).collect(Collectors.toList());

            }
        });

        textField.setOnKeyPressed(textFieldHandler);
        //menuBtn.setAlignment(Pos.CENTER_RIGHT);
        primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                textField.setPrefWidth(newValue.doubleValue() * 0.77);
            }
        });

        view.getEngine().setOnAlert(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(event.getData());
            alert.showAndWait();
        });


        upperHBox.getChildren().addAll(backBtn, forwBtn, stopBtn, reloadBtn, textField, historyBtn, downloadsBtn, menuBtn);
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


    private String getSelectedText() {
        return (String) view.getEngine()
                .executeScript("window.getSelection().toString()");
    }

    private void createContextMenu(WebView webView) {
        String[] imgDownloadSrc = new String[1];
        ContextMenu contextMenu = new ContextMenu();
        MenuItem save = new MenuItem("Copy");
        save.setOnAction(e -> {
                    clipboardContent.clear();
                    clipboardContent.putString(getSelectedText());
                    clipboard.setContent(clipboardContent);
                }
        );
        MenuItem reload = new MenuItem("Reload");
        reload.setOnAction(e -> webView.getEngine().reload());
        MenuItem savePage = new MenuItem("Save Page");
        savePage.setOnAction(e -> {
            //String htmlCode = view.getEngine().getDocument().body().;
            Document d = view.getEngine().getDocument();
            String fileName = "page.html";
            FileChooser chooser = new FileChooser();
            chooser.setInitialDirectory(new File(initialDir));
            chooser.setInitialFileName(fileName);
            File f = chooser.showSaveDialog(primaryStageCopy);
            if (f != null)
                try {
                    IOClass.printDocument(d, new FileOutputStream(f));
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (TransformerException ex) {
                    ex.printStackTrace();
                }
        });

        MenuItem saveImage = new MenuItem("Save Image");
        saveImage.setOnAction(e -> tryDownload(imgDownloadSrc[0]));

        MenuItem viewSource = new MenuItem("View Source");
        viewSource.setOnAction(e -> {
            viewwingPageSource = true;
            view.getEngine().loadContent(PageSource.getPageSourceHTMLString(), "text/html");
        });

        MenuItem javascriptConsole = new MenuItem("JavaScript Console");
        javascriptConsole.setOnAction(e -> new JSConsoleController().createWindow());


        contextMenu.getItems().addAll(reload, savePage, viewSource, javascriptConsole);

        webView.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {


                if (mouseOverImage) contextMenu.getItems().add(saveImage);
                imgDownloadSrc[0] = mouseOverImageSrc;

                if (!viewwingPageSource)
                    contextMenu.show(webView, e.getScreenX(), e.getScreenY());

                if (!getSelectedText().equals("")) contextMenu.getItems().add(save);

            } else {
                contextMenu.getItems().remove(saveImage);
                contextMenu.getItems().remove(save);
                contextMenu.hide();
            }
        });
    }

    public static WebView getView() {
        return view;
    }

    private boolean tryDownload(String newValue) {
        System.out.println("trying to download : " + newValue);
        if (newValue.startsWith("www")) {
            if (isURL("https://" + newValue)) newValue = "https://" + newValue;
            else newValue = "http://" + newValue;
        }

        return tryDownload(newValue, true);
    }

    private boolean tryDownload(String newValue, boolean check) {

        String cd = "";
        /*
        try {
            Header cdh = HttpCManager.getClient().
                    execute(new HttpGet(newValue)).getFirstHeader("Content-Disposition");
            if(cdh!=null) cd = cdh.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!cd.equals("")) {
            System.out.println("Content-Disposition : "+cd);
        } */

        boolean start = false;
        boolean base64 = false;

        String[] downloadableExtensions = {".gif", ".bin", ".docx", ".doc", ".xls", ".zip", ".exe", ".rar", ".pdf", ".jar", ".png", ".jpg", ".gif"};
        if (check == true) {
            for (String ext : downloadableExtensions) if (newValue.endsWith(ext)) start = true;
            if (newValue.startsWith("data:image")) {
                start = true;
                base64 = true;
            }
        }
        if (check == false) start = true;

        if (start) {
            //begin download

            FileChooser chooser = new FileChooser();
            chooser.setInitialDirectory(new File(initialDir));
            if (!base64) chooser.setInitialFileName(newValue.substring(newValue.lastIndexOf("/") + 1));
            else chooser.setInitialFileName("base64image." + newValue.split(";")[0].split("/")[1]);
            File file = chooser.showSaveDialog(primaryStageCopy);
            if (file != null) {


                numThreadsDownloading.set(numThreadsDownloading.get() + 1);
                String taskID = "DOWNLOAD" + (downloads.size() + 1);
                DownloadTask fileDownloadTask =
                        base64 ? new Base64DownloadTask(newValue, file) : new FileDownloadTask(newValue, file, taskID);

                downloads.add(fileDownloadTask);


                System.out.println("Num threads downloading : " + numThreadsDownloading.get());
                if (!progressBarBoundToThread) {
                    bindPbToDownloadTask(fileDownloadTask);
                    progressBarBoundToThread = true;
                }
                new Thread(fileDownloadTask).start();
            }
            return true;
        }
        return false;
    }


    public static void bindPbToDownloadTask(DownloadTask fileDownloadTask) {
        System.out.println("Binding progress bar to : " + fileDownloadTask.getTaskID());
        loadLabel.setText("Downloading file!          ");
        progressBarBoundToThread = true;
        taskBoundToPb = fileDownloadTask;
        progressBar.progressProperty().bind(fileDownloadTask.progressProperty());
        bottomHBox.visibleProperty().bind(fileDownloadTask.runningProperty());
    }


    public void bindProgressBarNormally() {

        loadLabel.setText("Loading! Please wait!     ");
        progressBar.progressProperty().bind(view.getEngine().getLoadWorker().progressProperty());
        bottomHBox.visibleProperty().bind(view.getEngine().getLoadWorker().runningProperty());
    }

    public static SimpleIntegerProperty getNumthreadsDownloading() {
        return numThreadsDownloading;
    }

    public static void decreaseNumDownloadThreads() {
        numThreadsDownloading.set(numThreadsDownloading.get() - 1);
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

    private String fixImgSrcURL(String url) {
        int backwardsCount = 0;
        if (url.startsWith("data:image/")) {

        } else if (url.startsWith("http")) {
        } else if (url.startsWith("//")) {
            url = url.substring(2);
        } else if (url.startsWith("/")) {
            String rootLocation = view.getEngine().getLocation().split("//")[1].split("/")[0];
            url = rootLocation + url;
        } else if (url.startsWith("..")) {
            System.out.println("Working magic on :" + url);
            while (url.startsWith("..")) {
                url = url.substring(url.indexOf("/") + 1);
                backwardsCount++;
                System.out.println("-----Magic Done!-------");
            }
            String location = view.getEngine().getLocation();
            for (int i = 0; i < backwardsCount; i++) {
                location = location.substring(0, location.lastIndexOf("/"));
            }
            System.out.println("location : " + location);
            url = location + url;
        }
        if (url.contains("?")) url = url.split("g?")[0] + "g";
        return url;
    }

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
        if (url.contains("/")) url = url.substring(0, url.indexOf("/"));
        return url;
    }

    public void registerVisit(String url) {


        if (visitedAddresses.containsKey(url)) {
            Long nv = visitedAddresses.get(url);
            nv++;
            visitedAddresses.replace(url, nv);
        } else {
            visitedAddresses.put(url, (long) 1);
        }

    }

    public static void loadPage(String url) {

        //   registerVisit(url);

        Platform.runLater(() -> {
            //System.out.println(Main.class.getClassLoader().getResource("stylesheet.css").toString());

            view.getEngine().load(url);
            //  view.getEngine().getDocument().appendChild();
        });

    }


}
