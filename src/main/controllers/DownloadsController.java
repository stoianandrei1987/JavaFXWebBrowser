package main.controllers;


import main.controllers.Controller;

public class DownloadsController extends Controller {

    public void createWindow()  {
        super.createWindow("Creating downloads window!", "/downloads.fxml");
    }

}
