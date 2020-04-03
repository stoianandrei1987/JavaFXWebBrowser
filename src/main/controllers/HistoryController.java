package main.controllers;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import main.HistoryItem;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class HistoryController extends Controller implements Initializable {

    private LocalDateTime searchDate;
    private Map<Integer, HistoryItem> listViewItemsMap = new HashMap<>();

    @FXML
    javafx.scene.control.ListView historyList;

    @FXML
    Label dateLabel;

    public void createWindow()  {
        super.createWindow("Creating history window!", "/history.fxml");

    }

    @FXML
    public void visitPressed(ActionEvent event) {
        if(historyList.getSelectionModel().getSelectedItem()!=null) {
            main.Main.loadPage(listViewItemsMap.get(historyList.getSelectionModel().getSelectedIndex()).getUri());
            windowIsCreated = false;
            window.close();

        }
    }

    public boolean checkSameDate(LocalDateTime ldt1, LocalDateTime ldt2) {
        if(ldt1.toLocalDate().equals(ldt2.toLocalDate())) return true;
        return false;
    }

    public void fillList(LocalDateTime ldt){
        listViewItemsMap.clear();
        historyList.getItems().clear();
        List<HistoryItem> lhi = main.Main.getHList().stream().
                filter(item -> {return checkSameDate(item.getCreatedAt(), ldt);}).
                collect(Collectors.toList());
        dateLabel.setText(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        int count = -1;
        for(HistoryItem hi : lhi) {
            count++;
            listViewItemsMap.put(count, hi);
            historyList.getItems().add(hi.getTitle());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchDate = LocalDateTime.now();
        fillList(searchDate);
        window.setMinWidth(500);
        window.initModality(Modality.WINDOW_MODAL);
        window.setResizable(false);

        /*
        window.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                historyList.setMinWidth(newValue.doubleValue()*0.75);
            }
        });
        *
         */

    }

    public void leftArrowClicked(MouseEvent mouseEvent) {
        searchDate = searchDate.minusDays(1);
        fillList(searchDate);
    }

    public void rightArrowClicked(MouseEvent mouseEvent) {
        if(searchDate.toLocalDate().isBefore(LocalDate.now())) searchDate = searchDate.plusDays(1);
        fillList(searchDate);
    }

    public void closePressed(ActionEvent event) {
        windowIsCreated = false;
        window.close();
    }

    public void removePressed(ActionEvent event)
    {
        System.out.println("Remove : " + historyList.getSelectionModel().getSelectedIndex());
    }
}

