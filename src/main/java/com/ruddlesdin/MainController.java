package com.ruddlesdin;

// Paul Ruddlesdin

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable{

    @FXML private TableView<Orders> tblOrders;
    @FXML private TableColumn<Orders, String> clmStatus;
    @FXML private TableColumn<Orders, String> clmStartDateTime;
    @FXML private TableColumn<Orders, String> clmShopOrder;
    @FXML private TableColumn<Orders, String> clmRotation;
    @FXML private TableColumn<Orders, String> clmGMIECode;
    @FXML private TableColumn<Orders, String> clmSAPCode;
    @FXML private TableColumn<Orders, Integer> clmLineName;
    @FXML private TableColumn<Orders, Integer> clmOrderProductionNr;

    @FXML private Button btnExit;
    @FXML private ToggleButton tbnReady;
    @FXML private ToggleButton tbnStarted;
    @FXML private ToggleButton tbnDone;
    @FXML private TextField txtMainBanner;
    @FXML private TextField txtCurrentSSCC;
    @FXML private TextField txtLineBanner;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbxSearch;

    @FXML private ImageView imgDB;
    @FXML private TextField txtMainDB;

    private ObservableList<Orders> list;
    private FirebirdConnect fc;
    private ObservableList<String> searchBy = FXCollections.observableArrayList("Spec", "Shop Order", "Rotation", "SAP Code");
    private Orders selectedOrder;
    private String productionLine;
    private String productionLineNr;
    private String ready = "SELECT STATUS, STARTDATETIME,EXTRA1, BATCHLOTNR, WARENR, SERVICEDESCRIPTION, PRODUCTIONLINENR, ORDERPRODUCTIONNR FROM TBLORDERPRODUCTION WHERE (STATUS = 6) AND (PRODUCTIONLINENR = '";
    private String started = "SELECT STATUS, STARTDATETIME,EXTRA1, BATCHLOTNR, WARENR, SERVICEDESCRIPTION, PRODUCTIONLINENR, ORDERPRODUCTIONNR  FROM TBLORDERPRODUCTION WHERE (STATUS = 7) AND (PRODUCTIONLINENR = '";
    private String done = "SELECT STATUS, STARTDATETIME,EXTRA1, BATCHLOTNR, WARENR, SERVICEDESCRIPTION, PRODUCTIONLINENR, ORDERPRODUCTIONNR  FROM TBLORDERPRODUCTION WHERE (STATUS = 8) AND (PRODUCTIONLINENR = '";
    private String readyStarted = "SELECT STATUS, STARTDATETIME,EXTRA1, BATCHLOTNR, WARENR, SERVICEDESCRIPTION, PRODUCTIONLINENR, ORDERPRODUCTIONNR  FROM TBLORDERPRODUCTION WHERE (STATUS = 6 OR STATUS = 7) AND (PRODUCTIONLINENR = '";
    private String readyDone = "SELECT STATUS, STARTDATETIME,EXTRA1, BATCHLOTNR, WARENR, SERVICEDESCRIPTION, PRODUCTIONLINENR, ORDERPRODUCTIONNR  FROM TBLORDERPRODUCTION WHERE (STATUS = 6 OR STATUS = 8) AND (PRODUCTIONLINENR = '";
    private String startedDone = "SELECT STATUS, STARTDATETIME,EXTRA1, BATCHLOTNR, WARENR, SERVICEDESCRIPTION, PRODUCTIONLINENR, ORDERPRODUCTIONNR  FROM TBLORDERPRODUCTION WHERE (STATUS = 7 OR STATUS = 8) AND (PRODUCTIONLINENR = '";
    private String all = "SELECT STATUS, STARTDATETIME,EXTRA1, BATCHLOTNR, WARENR, SERVICEDESCRIPTION, PRODUCTIONLINENR, ORDERPRODUCTIONNR  FROM TBLORDERPRODUCTION WHERE (PRODUCTIONLINENR = '";
    private String none = "SELECT STATUS, STARTDATETIME,EXTRA1, BATCHLOTNR, WARENR, SERVICEDESCRIPTION, PRODUCTIONLINENR, ORDERPRODUCTIONNR  FROM TBLORDERPRODUCTION WHERE (STATUS = 1) AND (PRODUCTIONLINENR = '";
    private String endSQL = "')";
    private Logs log;
    private Supervisor sup;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean connected;
    private String ipAddress;
    private String dbPath;
    private boolean connectionOK;
    private String jarName;

    private String CONFIG_FILE_PATH = "C:\\ProgramData\\QuickDesignUI\\";
	private String CONFIG_FILE_PATH_END = ".xml";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productionLine = "Caledonian";
        productionLineNr = "15";
		jarName();
        log= new Logs();
        log.appLog("Starting app\r\n");
        setConnected(false);
        dbCross();
        cbxSearch.setItems(searchBy);
        txtSearch.requestFocus();
        checkFBConnection();
        System.out.println("connectionOK = " + connectionOK);
        if(connectionOK) {
            fullRefresh();
        }
        cbxSearch.getSelectionModel().select(0);
        tblOrders.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

	public String getJarName() {
		return jarName;
	}

	protected void jarName() {
    	try {
    		String path = Orders.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
    		jarName = path.substring(path.lastIndexOf("/") + 1 ).replace(".jar","");
		} catch(Exception e) {
			System.out.println("exception reading Jar path");
		}
	}

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    void setBanner(String message, String colour) {
        txtMainBanner.setStyle("-fx-text-inner-color: " + colour + ";");
        txtMainBanner.setText(message);
    }

    void setLineBanner(String message, String colour) {
        txtLineBanner.setStyle("-fx-text-inner-color: " + colour + ";");
        txtLineBanner.setText(message);
    }

    public void getSSCC() {
        fc = new FirebirdConnect(this, ipAddress, dbPath);
        txtCurrentSSCC.setText(fc.FBgetSSCC());
    }

    String getProductionLine() {
        return productionLine;
    }

    public void checkFBConnection() {
        readXMLFile();
        boolean fileExists;
        File f = new File(CONFIG_FILE_PATH + jarName + CONFIG_FILE_PATH_END);
        fileExists = f.exists() && !f.isDirectory();
        if(fileExists) {
            fc = new FirebirdConnect(this, ipAddress, dbPath);
            connectionOK = fc.checkConnection();
            System.out.println("Test connection OK");
        }
    }

    void dbCross() {
        imgDB.setImage(new Image("/images/dbCross.png"));
        txtMainDB.setStyle("-fx-text-inner-color: red;");
        txtMainDB.setText("Database Connection Problem !");
        log.appLog("Database Connection Problem! \r\n");
    }

    void dbTick() {
        imgDB.setImage(new Image("/images/dbTick.png"));
        txtMainDB.setStyle("-fx-text-inner-color: green;");
        txtMainDB.setText("Connection OK");
    }

    void dbTestTick() {
        imgDB.setImage(new Image("/images/dbTick.png"));
        txtMainDB.setStyle("-fx-text-inner-color: green;");
        txtMainDB.setText("Connection Tested OK");
    }

    void dbTestCross() {
        imgDB.setImage(new Image("/images/dbCross.png"));
        txtMainDB.setStyle("-fx-text-inner-color: red;");
        txtMainDB.setText("Database Test Connection Problem !");
        log.appLog("Database Test Connection Problem! \r\n");
    }

    String getProductionLineNr(String lineName) {
        String prodLineNr;
        switch (lineName) {
            case "Caledonian":
                prodLineNr = "15";
                break;
            case "FlexiLine":
                prodLineNr = "17";
                break;
            case "Finishing":
                prodLineNr = "16";
                break;
            case "Miniatures":
                prodLineNr = "18";
                break;
            default:
                prodLineNr = "15";
                break;
        }
        System.out.println("ProductionLineNr = " + prodLineNr);
        return prodLineNr;
    }

    void fullRefresh() {
        productionLineNr = getProductionLineNr(productionLine);
        System.out.println("ProductionLineNr = " + productionLineNr + " and Production Line = " + productionLine);
        filter();
        getSSCC();
    }

    private void populateTable() {
		log.appLog("Populating table\r\n");
		//clmStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

		clmStatus.setCellFactory(new Callback<TableColumn<Orders, String>,
				TableCell<Orders, String>>()
		{
			@Override
			public TableCell<Orders, String> call(
					TableColumn<Orders, String> param) {
				return new TableCell<Orders, String>() {
					@Override
					protected void updateItem(String item, boolean empty) {
						if (!empty) {
							int currentIndex = indexProperty()
									.getValue() < 0 ? 0
									: indexProperty().getValue();
							String clmStatus = param
									.getTableView().getItems()
									.get(currentIndex).getStatus();
							setStyle("-fx-font-weight: bold");
							if (clmStatus.equals("READY")) {
								setTextFill(Color.WHITE);
								getStyleClass().add("clmStatusBold");
								setStyle("-fx-background-color: green");
								setText(clmStatus);
							} else if (clmStatus.equals("STARTED")){
								setTextFill(Color.BLACK);
								getStyleClass().add("clmStatusBold");
								setStyle("-fx-background-color: yellow");
								setText(clmStatus);
							} else if (clmStatus.equals("DONE")){
								setTextFill(Color.BLACK);
								getStyleClass().add("clmStatusBold");
								setStyle("-fx-background-color: gray");
								setText(clmStatus);
							} else {
								setTextFill(Color.WHITE);
								getStyleClass().add("clmStatusBold");
								setStyle("-fx-background-color: red");
								setText(clmStatus);
							}
						}
					}
				};
			}
		});

		clmStartDateTime.setCellValueFactory(new PropertyValueFactory<>("startDateTime"));
		clmShopOrder.setCellValueFactory(new PropertyValueFactory<>("extra1"));
		clmRotation.setCellValueFactory(new PropertyValueFactory<>("batchLotNr"));
		clmGMIECode.setCellValueFactory(new PropertyValueFactory<>("wareNr"));
		clmSAPCode.setCellValueFactory(new PropertyValueFactory<>("serviceDescription"));
		clmLineName.setCellValueFactory(new PropertyValueFactory<>("productionLineNr"));
		clmOrderProductionNr.setCellValueFactory(new PropertyValueFactory<>("orderProductionNr"));
		tblOrders.setItems(list);
	}


    public void select() {
        String sql1 = "SELECT STATUS, STARTDATETIME,EXTRA1, BATCHLOTNR, WARENR, SERVICEDESCRIPTION, PRODUCTIONLINENR, ORDERPRODUCTIONNR  FROM TBLORDERPRODUCTION";
        fc = new FirebirdConnect(this, ipAddress, dbPath);
        list = fc.FBSelect(sql1);
        fc.FBConnectionClose();
    }

    public void openOrder(MouseEvent event) {
        if (event.getClickCount() > 1) {
            selectedOrder = tblOrders.getSelectionModel().getSelectedItem();
            System.out.println("Selected item = " + selectedOrder.getOrderProductionNr());
            openOrderWindow();
        }
    }

    void setSelectedOrder(int orderProductionNr) {
        for(Orders l : list) {
            if (l.getOrderProductionNr() == orderProductionNr) {
                selectedOrder = l;
            }
        }
        tblOrders.getSelectionModel().clearSelection();
        tblOrders.requestFocus();
        tblOrders.getSelectionModel().select(selectedOrder);
        openOrderWindow();
    }

    private void openOrderWindow() {
        try {
			OrderController openOrder = new OrderController(this, selectedOrder);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ruddlesdin/Order.fxml"));
			loader.setController(openOrder);
			Parent root = loader.load();
			Stage OrderStage = new Stage();

			OrderStage.initModality(Modality.APPLICATION_MODAL);
			OrderStage.initStyle(StageStyle.UNDECORATED);
			root.setOnMousePressed(event -> {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			});
			root.setOnMouseDragged(event -> {
				OrderStage.setX(event.getScreenX() - xOffset);
				OrderStage.setY(event.getScreenY() - yOffset);
			});
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("Order.css").toExternalForm());
			OrderStage.setTitle("Order");
			OrderStage.setScene(scene);
			OrderStage.show();
		} catch (LoadException el) {
			el.printStackTrace();
			log.appLog(el.getCause().toString()); // Debug
        } catch(Exception e) {
            e.printStackTrace();
			log.appLog(e.getMessage().toString()); // Debug
        }
    }

    public void openAbout(ActionEvent event) {
        try {
            log.userLog("About button has been pressed\r\n");
            Parent root = FXMLLoader.load(getClass().getResource("/com/ruddlesdin/About.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
            Stage AboutStage = new Stage();
            AboutStage.initModality(Modality.APPLICATION_MODAL);
            AboutStage.setTitle("About");
            AboutStage.setScene(scene);
            AboutStage.initStyle(StageStyle.UNDECORATED);
            AboutStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void newOrder(ActionEvent event) {
        log.userLog("New Order button has been pressed\r\n");
        try {
            txtMainBanner.setStyle("-fx-text-inner-color: green;");
            txtMainBanner.setText("Supervisor Password Correct");
            NewController newController = new NewController(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ruddlesdin/New.fxml"));
            loader.setController(newController);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("newOrder.css").toExternalForm());
            Stage NewStage = new Stage();
            NewStage.setUserData(this);
            NewStage.initModality(Modality.APPLICATION_MODAL);
            NewStage.setTitle("New Order");
            NewStage.setScene(scene);
            NewStage.initStyle(StageStyle.UNDECORATED);
            NewStage.show();
        } catch(IOException e) {
            log.appLog(Arrays.toString(e.getStackTrace())+"\r\n");
            e.printStackTrace();
        }
    }

    public void openConfig(ActionEvent event) {
        sup = new Supervisor(true);
        if(sup.supervisorTest()) {
            try {
                log.userLog("Config button has been pressed\r\n");
                ConfigController openConfig = new ConfigController(this);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ruddlesdin/Config.fxml"));
                loader.setController(openConfig);
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
                Stage ConfigStage = new Stage();
                ConfigStage.initModality(Modality.APPLICATION_MODAL);
                ConfigStage.setTitle("Config");
                ConfigStage.setScene(scene);
                ConfigStage.initStyle(StageStyle.UNDECORATED);
                ConfigStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.userLog("Wrong Password Entered\r\n");
            txtMainBanner.setStyle("-fx-text-inner-color: red;");
            txtMainBanner.setText("Wrong Supervisor Password !");
        }
    }

    public void closeMain() {
        log.userLog("Main Close button has been pressed\r\n");
        // get a handle to the stage
        Stage stage = (Stage) btnExit.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
//		Removed due to a request from Barry to introduce decorated windows
//    public void minimise() {
//        log.userLog("Minimise button has been pressed\r\n");
//        Stage stage = (Stage) btnExit.getScene().getWindow();
//        stage.setIconified(true);
//    }

    private void getData(String filter) {
        fc = new FirebirdConnect(this, ipAddress, dbPath);
        list = fc.FBSelect(filter);
        fc.FBConnectionClose();
        populateTable();
    }

    public void filter() {
        if(tbnReady.isSelected() && !tbnStarted.isSelected() && !tbnDone.isSelected()) {
            getData(ready + productionLineNr + endSQL);
        } else if (!tbnReady.isSelected() && tbnStarted.isSelected() && !tbnDone.isSelected()) {
            getData(started + productionLineNr + endSQL);
        } else if (!tbnReady.isSelected() && !tbnStarted.isSelected() && tbnDone.isSelected()) {
            getData(done + productionLineNr + endSQL);
        } else if (tbnReady.isSelected() && tbnStarted.isSelected() && !tbnDone.isSelected()) {
            getData(readyStarted + productionLineNr + endSQL);
        } else if (tbnReady.isSelected() && !tbnStarted.isSelected() && tbnDone.isSelected()) {
            getData(readyDone + productionLineNr + endSQL);
        } else if (!tbnReady.isSelected() && tbnStarted.isSelected() && tbnDone.isSelected()) {
            getData(startedDone + productionLineNr + endSQL);
        } else if (tbnReady.isSelected() && tbnStarted.isSelected() && tbnDone.isSelected()){
            getData(all + productionLineNr + endSQL);
        } else {
            getData(none + productionLineNr + endSQL);
        }
    }

    public void search() {
        if(Objects.equals(cbxSearch.getValue(), "Spec")) {
            getData(all + productionLineNr + endSQL + " AND WARENR LIKE UPPER('"+ txtSearch.getText() + "%')");
        } else if (Objects.equals(cbxSearch.getValue(), "Shop Order")) {
            getData(all + productionLineNr + endSQL + " AND EXTRA1 LIKE UPPER('"+ txtSearch.getText() + "%')");
        } else if (Objects.equals(cbxSearch.getValue(), "Rotation")) {
            getData(all + productionLineNr + endSQL + " AND BATCHLOTNR LIKE UPPER('"+ txtSearch.getText() + "%')");
        } else if (Objects.equals(cbxSearch.getValue(), "SAP Code")) {
            getData(all + productionLineNr + endSQL + " AND SERVICEDESCRIPTION LIKE UPPER('"+ txtSearch.getText() + "%')");
        }
    }

    private void readXMLFile() {
        boolean fileExists;
        File f = new File(CONFIG_FILE_PATH + jarName + CONFIG_FILE_PATH_END);
        fileExists = f.exists() && !f.isDirectory();

        if(fileExists) {
            System.out.println("Config File Exists");

            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
                Document doc = dbBuilder.parse(new File(CONFIG_FILE_PATH + jarName + CONFIG_FILE_PATH_END));

                doc.getDocumentElement().normalize();

                // Get the element by tag name
                productionLine = doc.getElementsByTagName("Line").item(0).getTextContent();
                setLineBanner(productionLine, "Blue");
                ipAddress = doc.getElementsByTagName("IPAddress").item(0).getTextContent();
                dbPath = doc.getElementsByTagName("DBPath").item(0).getTextContent();
                System.out.println("Production Line is set to " + productionLine);
                System.out.println("IPAddrress is set to " + ipAddress);
                System.out.println("DBPath Line is set to " + dbPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Config File Does NOT Exist");
            createXMLFile("Caledonian","10.7.66.99", "D:\\Var\\db\\UTF-8\\Glenmorangie.FDB");
			txtMainBanner.setStyle("-fx-text-inner-color: red;");
			txtMainBanner.setText("First Time Use? Defaults set! Change config.");
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
            StreamResult result = new StreamResult(new File(CONFIG_FILE_PATH + jarName + CONFIG_FILE_PATH_END));
            transformer.transform(source, result);

            //Output to console for testing
            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(source, consoleResult);

        } catch (Exception e) {
            e.printStackTrace();
        }
        setBanner("Config file created.","Green");
    }
}

