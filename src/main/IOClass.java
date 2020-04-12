package main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
        File properties = new File(pathToDir + "\\application.properties");

        try {
            if (!json1.exists()) json1.createNewFile();
            if (!json2.exists()) json2.createNewFile();
            if (!properties.exists()) {
                if (properties.createNewFile()) {
                    writeStringToFile("incognito=false", properties.getCanonicalPath());
                }


            }
            pathToVisists = json1.getCanonicalPath();
            pathToHistory = json2.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean getIncognito() {
        Properties p = new Properties();
        try {
            p.load(new FileReader(pathToDir + "\\application.properties"));
            if (p.getProperty("incognito").equals("true")) return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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

    public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

}
