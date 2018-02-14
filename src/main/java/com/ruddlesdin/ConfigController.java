package com.ruddlesdin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by p_ruddlesdin on 21/03/2017.
 */
public class ConfigController implements Initializable {
    @FXML Button btnConfigClose;
    @FXML ComboBox<String> cbxLine;
    @FXML TextField txtIPAddress;
    @FXML TextField txtDBPath;

    MainController mainController;
    ObservableList<String> lines = FXCollections.observableArrayList("Caledonian", "FlexiLine", "Finishing", "Miniatures");
    private static final String CONFIG_FILE_PATH = "C:\\ProgramData\\QuickDesignUI\\";
    private static final String CONFIG_FILE_PATH_END = ".xml";
    private String jarPath;
    private String selectedLine;
    private String ipAddress;
    private String dbPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbxLine.setItems(lines);
		jarPath = mainController.getJarName();
		readXMLFile();
    }

    public ConfigController(MainController mainController) {
        this.mainController = mainController;
    }

    public void selectedLine() {
        selectedLine = cbxLine.getValue().toString();
    }
    public void getIPAddress() { ipAddress = txtIPAddress.getText().toString(); }
    public void getDBPath() { dbPath = txtDBPath.getText().toString(); }

    public void closeConfig() {
        updateXMLFile();
        // get a handle to the stage
        Stage stage = (Stage) btnConfigClose.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    public boolean checkConfigFileExists() {
        boolean result;
        File f = new File(CONFIG_FILE_PATH + jarPath + CONFIG_FILE_PATH_END);
        if(f.exists() && !f.isDirectory()) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    public void updateXMLFile() {
        selectedLine = cbxLine.getValue().toString();
        getIPAddress();
        getDBPath();
        boolean fileExists = checkConfigFileExists();
        if(fileExists) {
            System.out.println("File Exists");
            appendXMLFile(selectedLine,ipAddress,dbPath);
        } else {
            System.out.println("File Does Not Exist");
            createXMLFile(selectedLine,ipAddress,dbPath);
        }
        mainController.checkFBConnection();
        if(mainController.isConnected()) {
            mainController.fullRefresh();
        }
    }

    public void readXMLFile() {
        boolean fileExists = checkConfigFileExists();
        if(fileExists) {
            System.out.println("Config File Exists");

            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
                Document doc = dbBuilder.parse(new File(CONFIG_FILE_PATH + jarPath + CONFIG_FILE_PATH_END));

                doc.getDocumentElement().normalize();

                // Get the element by tag name
                String Line = doc.getElementsByTagName("Line").item(0).getTextContent();
                cbxLine.setValue(Line);
                mainController.setLineBanner(Line,"Blue");
                String IPAddress = doc.getElementsByTagName("IPAddress").item(0).getTextContent();
                txtIPAddress.setText(IPAddress);
                String DBPath = doc.getElementsByTagName("DBPath").item(0).getTextContent();
                txtDBPath.setText(DBPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Config File Does NOT Exist");
            createXMLFile("Caledonian","10.7.66.99", "D:/Var/db/UTF-8/Glenmorangie.FDB");
        }
    }

    public void appendXMLFile(String selectedLine, String ipAddress, String dbPath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
            Document doc = dbBuilder.parse(CONFIG_FILE_PATH + jarPath + CONFIG_FILE_PATH_END);

            // Get the element by tag name
            Node Line = doc.getElementsByTagName("Line").item(0);
            Line.setTextContent(selectedLine);

            Node IPAddress = doc.getElementsByTagName("IPAddress").item(0);
            IPAddress.setTextContent(ipAddress);

            Node DBPath = doc.getElementsByTagName("DBPath").item(0);
            DBPath.setTextContent(dbPath);


            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(CONFIG_FILE_PATH + jarPath + CONFIG_FILE_PATH_END));
            transformer.transform(source, result);

            //Output to console for testing
            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(source, consoleResult);
            mainController.setBanner("Config changed successfully", "Green");

        } catch (Exception e) {
            e.printStackTrace();
        }
        mainController.checkFBConnection();
        if(mainController.isConnected()) {
            mainController.fullRefresh();
        }
    }
    public void createXMLFile(String selectedLine, String ipAddress, String dbPath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
            Document doc = dbBuilder.newDocument();

            //root element
            Element rootElement = doc.createElement("QuickDesignUI");
            doc.appendChild(rootElement);

            //subRoot element
            Element Config = doc.createElement("Config");
            rootElement.appendChild(Config);

            //subRoot element
            Element Line = doc.createElement("Line");
            Line.appendChild(doc.createTextNode(selectedLine));
            Config.appendChild(Line);
            //subRoot element
            Element IPAddress = doc.createElement("IPAddress");
            IPAddress.appendChild(doc.createTextNode(ipAddress));
            Config.appendChild(IPAddress);
            //subRoot element
            Element DBPath = doc.createElement("DBPath");
            DBPath.appendChild(doc.createTextNode(dbPath));
            Config.appendChild(DBPath);

            //Write the content into the xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(CONFIG_FILE_PATH + jarPath + CONFIG_FILE_PATH_END));
            transformer.transform(source, result);

            //Output to console for testing
            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(source, consoleResult);

        } catch (Exception e) {
            e.printStackTrace();
        }
        mainController.setBanner("Config file created.","Green");
        mainController.checkFBConnection();
        if(mainController.isConnected()) {
            mainController.fullRefresh();
        }
    }
}
