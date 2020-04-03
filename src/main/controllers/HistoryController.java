package main.controllers;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import main.HistoryItem;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class HistoryController extends Controller implements Initializable {

    private LocalDateTime searchDate;

    @FXML
    javafx.scene.control.ListView historyList;

    @FXML
    Label dateLabel;

    public void createWindow()  {
        super.createWindow("Creating history window!", "/history.fxml");

    }

    @FXML
    public void visitPressed(ActionEvent event) {

    }

    public boolean checkSameDate(LocalDateTime ldt1, LocalDateTime ldt2) {
        if(ldt1.getDayOfYear()==ldt2.getDayOfYear() && ldt1.getMonthValue()==ldt2.getMonthValue() &&
        ldt1.getYear()==ldt2.getYear()) return true;
        return false;
    }

    public void fillList(LocalDateTime ldt){
        List<HistoryItem> lhi = main.Main.getHList().stream().
                filter(item -> {return checkSameDate(item.getCreatedAt(), ldt);}).
                collect(Collectors.toList());
   //     dateLabel.setText(new SimpleDateFormat("dd-MM-yyyy").format(searchDate));
        for(HistoryItem hi : lhi) {
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
}

