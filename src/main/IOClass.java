package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IOClass {

    public static Map<String, Long> getAddresses() {
        Type mapType = new TypeToken<Map<String, Long>>() {}.getType();
        String json = readStringFromFile("src/resources/json/visits.json");
        Map<String, Long> map = new Gson().fromJson(json, mapType);
        if(map==null) return new HashMap<String, Long>();
        else return map;
    }


    public static ObservableList<HistoryItem> getHistory() {
        Type arrayListType = new TypeToken<ArrayList<HistoryItem>>() {}.getType();
        String json = readStringFromFile("src/resources/json/history.json");
        ArrayList<HistoryItem> al = new Gson().fromJson(json, arrayListType);
        if(al==null) return FXCollections.observableArrayList();
        else return FXCollections.observableArrayList(al);


    }


    public static void writeAddresses(Map<String, Long> addresses) {

        String json =  new Gson().toJson(addresses);
        writeStringToFile(json, "src/resources/json/visits.json");
    }


    public static void writeHistory(ObservableList<HistoryItem> observableList) {


        ArrayList<HistoryItem> list = new ArrayList<>(observableList.subList(0, observableList.size() - 1));
        String json = new Gson().toJson(list);
        writeStringToFile(json, "src/resources/json/history.json");

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
