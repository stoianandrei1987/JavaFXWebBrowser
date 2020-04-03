package main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class HistoryController extends Controller{

    @Override
    protected void init(){
        System.out.println("Init method is run on historyController");
    }

    public void createWindow()  {
        super.createWindow("Creating history window!", "/history.fxml");
    }

    @FXML
    public void visitPressed(ActionEvent event) {

    }


}
