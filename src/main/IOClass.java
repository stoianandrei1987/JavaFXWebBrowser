package main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class IOClass {

    private static String pathToDir;
    private static String pathToVisists;
    private static String pathToHistory;

    public static void createJSONS() {

        pathToDir = System.getProperty("user.home") + "\\Appdata\\Local\\JWFXBrowser";
        pathToHistory = pathToDir + "\\history.json";
        pathToVisists = pathToDir + "\\visits.json";

        File jfdir = new File(pathToDir);
        if (!jfdir.exists()) jfdir.mkdirs();
        File json1 = new File(pathToVisists);
        File json2 = new File(pathToHistory);

        try {
            if (!json1.exists()) json1.createNewFile();
            if (!json2.exists()) json2.createNewFile();
            pathToVisists = json1.getCanonicalPath();
            pathToHistory = json2.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Map<String, Long> getAddresses() {
        Type mapType = new TypeToken<Map<String, Long>>() {
        }.getType();
        String json = readStringFromFile(pathToVisists);
        Map<String, Long> map = new Gson().fromJson(json, mapType);
        if (map == null) return new HashMap<String, Long>();
        else return map;
    }


    public static ObservableList<HistoryItem> getHistory() {
        Type arrayListType = new TypeToken<ArrayList<HistoryItem>>() {
        }.getType();
        String json = readStringFromFile(pathToHistory);
        ArrayList<HistoryItem> al = new Gson().fromJson(json, arrayListType);
        if (al == null) return FXCollections.observableArrayList();
        else return FXCollections.observableArrayList(al);


    }


    public static void writeAddresses(Map<String, Long> addresses) {

        String json = new Gson().toJson(addresses);
        writeStringToFile(json, pathToVisists);

    }


    public static void writeHistory(ObservableList<HistoryItem> observableList) {

        ArrayList<HistoryItem> list = new ArrayList<>(observableList.subList(0, observableList.size() - 1));
        String json = new Gson().toJson(list);
        writeStringToFile(json, pathToHistory);

    }

    private static String readStringFromFile(String filename) {


        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            return reader.lines()
                    .collect(Collectors.joining(System.lineSeparator()));

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }


    }

    private static void writeStringToFile(String string, String filename) {

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(string);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
