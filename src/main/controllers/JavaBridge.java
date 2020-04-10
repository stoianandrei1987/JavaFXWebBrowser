package main.controllers;

import javafx.scene.control.TextArea;

public class JavaBridge {

    /*
    private static boolean sWasShown = false;
    private static String s = "";

    public static boolean getWasShown() {
        return sWasShown;
    }

    public static String getS(){
        if(sWasShown == false) {
            sWasShown = true;
            return s;
        } else return "";
    }

     */

    public void log(String text) {
        JSConsoleController.getLog().setEditable(false);
        JSConsoleController.getLog().appendText(text+"\n");
        text+="";

    }

}
