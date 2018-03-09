package com.ruddlesdin;

/**
 * Created by p_ruddlesdin on 21/03/2017.
 */

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;

public class OrderController implements Initializable{

    @FXML private AnchorPane ancMain;

    @FXML private Button btnOrderClose;
    @FXML private Button btnDelete;
    @FXML private Button btnOrderStart;
    @FXML private Button btnOrderStop;
    @FXML private Button btnUpdateLaser;
    @FXML private Button btnUpdateCaseLabeller;
    @FXML private Button btnUpdateLogopak;
    @FXML private Button btnPrintToDesktop;
    @FXML private Button btnPartPallet;
    @FXML private Button btnClearQueue;

    @FXML private CheckBox chxCaseLabellerA;
    @FXML private CheckBox chxCaseLabellerB;
    @FXML private CheckBox chxFlexiLine;
    @FXML private TabPane tabPane;
    @FXML private Tab tabOrder;
    @FXML private Tab tabProduct;
    @FXML private TextField txtWareNr;
    @FXML private TextField txtStatus;
    @FXML private TextField txtLine;
    @FXML private TextField txtSSCC;
    @FXML private TextField txtShopOrder;
    @FXML private TextField txtQDOrderNr;
    @FXML private TextField txtRotation;
    @FXML private CheckBox chxRepack;
    @FXML private TextField txtRepackBatch;
    @FXML private DatePicker txtRepackDate;
    @FXML private ComboBox<String> cbxRepackYear;
    @FXML private TextField txtPartPallet;
    @FXML private TextField txtCaseCount;
    @FXML private TextField txtThinking;
    @FXML private Button btnRestore;

    @FXML private TextField txtGMIEcode;
    @FXML private TextField txtServiceDescription;
    @FXML private TextField txtSizeText;
    @FXML private TextField txtSAPcode;
    @FXML private TextField txtEAN;
    @FXML private TextField txtGTIN;
    @FXML private TextField txtWeight;
    @FXML private TextField txtLabelCode;
    @FXML private TextField txtBottlesPerCase;
    @FXML private TextField txtCaseQuantity;
    @FXML private TextField txtLayersOnPallet;
    @FXML private TextField txtLaserTemplate;
    @FXML private TextField txtCaseTemplate;
    @FXML private TextField txtPalletTemplate;
    @FXML private TextField txtOrderBanner;
    @FXML private TextField txtEditSSCC;
    @FXML private ProgressIndicator pgiThinking;

    private MainController mainController;
    private Orders selectedOrder;
    private FirebirdConnect fc;
    private ObservableList<OpenOrder> list;
    private Supervisor sup;
    private Logs log;
    private String status;
    private Boolean checkEditSSCCLength = false;
    private Boolean checkRepackBatchLength = false;

    final static String DATE_FORMAT = "dd/MM/yyyy";


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log = new Logs();
        fc = new FirebirdConnect(mainController, mainController.getIpAddress(), mainController.getDbPath());
        getData();
        populateWindow();
        setButtonStates();
        txtEditSSCC.addEventFilter(KeyEvent.KEY_TYPED,maxLength(6));
	txtRepackBatch.addEventFilter(KeyEvent.KEY_TYPED,maxLength(5));
        setComboBoxSSCCYear();
        initialiseDatePickerFormat();
        pgiThinking.setVisible(false);
    }

    private void initialiseDatePickerFormat() {
        System.out.println("InitialiseDatePickerFormat()");
        String pattern = "dd/MM/yyyy";

        txtRepackDate.setPromptText(pattern.toLowerCase());
        txtRepackDate.setConverter(new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
    }

    OrderController(MainController mc, Orders selectedOrder) {
        this.mainController = mc;
        this.selectedOrder = selectedOrder;
    }

    private void refresh() {
        getData();
        populateWindow();
        setButtonStates();
    }

    private EventHandler<KeyEvent> maxLength(final Integer i) {
        return arg0 -> {

            TextField tx = (TextField) arg0.getSource();
            if (tx.getText().length() >= i) {
                arg0.consume();
            }
        };
    }

    public void checkSSCCLength() {
        // Test if SSCC is less than 6 chars or if it is not all numbers
        if(txtEditSSCC.getText().length() < 6 || txtEditSSCC.getText().matches(".*\\D.*")) {
            // Set red border
            txtEditSSCC.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            checkEditSSCCLength = false;
        } else {
            // set normal border
            txtEditSSCC.setStyle("-fx-border-color: gray ; -fx-border-width: 1px ;");
            checkEditSSCCLength = true;
        }
		if(checkEditSSCCLength) {
			btnOrderStart.setDisable(false);
		} else {
			btnOrderStart.setDisable(true);
		}
    }

    public void checkRepackBatchLength() {
    	if(chxRepack.isSelected()) {
			if (txtRepackBatch.getText().length() < 5 || txtRepackBatch.getText().matches(".*\\D.*")) {
				// Set red border
				txtRepackBatch.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
				checkRepackBatchLength = false;
			} else {
				// set normal border
				txtRepackBatch.setStyle("-fx-border-color: gray ; -fx-border-width: 1px ;");
				checkRepackBatchLength = true;
			}
			if(checkRepackBatchLength) {
				btnOrderStart.setDisable(false);
			} else {
				btnOrderStart.setDisable(true);
			}
		}
	}

    private void setButtonStates() {
        status = fc.getStatus(String.valueOf(selectedOrder.getOrderProductionNr()));
        switch (status) {
            case "READY":
                btnOrderStart.setDisable(false);
                btnOrderStop.setDisable(true);
                btnUpdateLaser.setDisable(true);
                btnUpdateCaseLabeller.setDisable(true);
                btnUpdateLogopak.setDisable(true);
                btnPrintToDesktop.setDisable(true);
                btnPartPallet.setDisable(true);
                txtCaseCount.setDisable(false);
                txtPartPallet.setDisable(true);
				if (!mainController.getProductionLine().equals("Caledonian")) {
					chxCaseLabellerA.setVisible(false);
					chxCaseLabellerA.setDisable(true);
					chxCaseLabellerB.setVisible(false);
					chxCaseLabellerB.setDisable(true);
					chxFlexiLine.setVisible(false);
					chxFlexiLine.setDisable(true);
					btnClearQueue.setVisible(false);
					btnClearQueue.setDisable(true);
				} else {
					chxCaseLabellerA.setVisible(true);
					chxCaseLabellerA.setDisable(true);
					chxCaseLabellerB.setVisible(true);
					chxCaseLabellerB.setDisable(true);
					chxFlexiLine.setVisible(true);
					chxFlexiLine.setDisable(true);
					btnClearQueue.setVisible(true);
                    btnClearQueue.setDisable(true);
				}
                btnDelete.setDisable(false);
                cbxRepackYear.setDisable(false);
                chxRepack.setDisable(false);
                txtRepackDate.setDisable(false);
                txtRepackBatch.setDisable(false);
                btnRestore.setDisable(true);
                break;
            case "STARTED":
                btnOrderStart.setDisable(true);
                btnOrderStop.setDisable(false);
                btnUpdateLaser.setDisable(false);
                btnUpdateCaseLabeller.setDisable(false);
                btnUpdateLogopak.setDisable(false);
                btnPrintToDesktop.setDisable(false);
                btnPartPallet.setDisable(false);
                txtCaseCount.setDisable(false);
                txtPartPallet.setDisable(false);
		if (!mainController.getProductionLine().equals("Caledonian")) {
                    chxCaseLabellerA.setVisible(false);
                    chxCaseLabellerA.setDisable(true);
                    chxCaseLabellerB.setVisible(false);
                    chxCaseLabellerB.setDisable(true);
                    chxFlexiLine.setVisible(false);
                    chxFlexiLine.setDisable(true);
                    btnClearQueue.setVisible(false);
                    btnClearQueue.setDisable(true);
		} else {
                    chxCaseLabellerA.setVisible(true);
                    chxCaseLabellerA.setDisable(false);
                    chxCaseLabellerB.setVisible(true);
                    chxCaseLabellerB.setDisable(false);
                    chxFlexiLine.setVisible(true);
                    chxFlexiLine.setDisable(false);
                    btnClearQueue.setVisible(true);
                    btnClearQueue.setDisable(false);
                }
                btnDelete.setDisable(true);
                cbxRepackYear.setDisable(true);
                chxRepack.setDisable(true);
                txtRepackDate.setDisable(true);
                txtRepackBatch.setDisable(true);
                btnRestore.setDisable(false);
                break;
            default:
                btnOrderStart.setDisable(true);
                btnOrderStop.setDisable(true);
                btnUpdateLaser.setDisable(true);
                btnUpdateCaseLabeller.setDisable(true);
                btnUpdateLogopak.setDisable(true);
                btnPrintToDesktop.setDisable(true);
                btnPartPallet.setDisable(true);
                txtCaseCount.setDisable(true);
                txtPartPallet.setDisable(true);
				if (!mainController.getProductionLine().equals("Caledonian")) {
					chxCaseLabellerA.setVisible(false);
					chxCaseLabellerA.setDisable(true);
					chxCaseLabellerB.setVisible(false);
					chxCaseLabellerB.setDisable(true);
					chxFlexiLine.setVisible(false);
					chxFlexiLine.setDisable(true);
					btnClearQueue.setVisible(false);
					btnClearQueue.setDisable(true);
				} else {
					chxCaseLabellerA.setVisible(true);
					chxCaseLabellerA.setDisable(true);
					chxCaseLabellerB.setVisible(true);
					chxCaseLabellerB.setDisable(true);
					chxFlexiLine.setVisible(true);
					chxFlexiLine.setDisable(true);
					btnClearQueue.setVisible(true);
					btnClearQueue.setDisable(true);
				}
                chxRepack.setDisable(true);
                txtRepackDate.setDisable(true);
                txtRepackBatch.setDisable(true);
                btnDelete.setDisable(false);
                cbxRepackYear.setDisable(true);
                txtEditSSCC.setDisable(true);
                btnRestore.setDisable(false);
                break;
        }
    }
    public void populateWindow() {
        txtWareNr.setText(list.get(0).getWarenr());
        txtStatus.setText(list.get(0).getStatus());
        txtLine.setText(list.get(0).getProductionLineNr());
        txtShopOrder.setText(list.get(0).getExtra1());
        txtQDOrderNr.setText(list.get(0).getOrderProductionNr());
        txtRotation.setText(list.get(0).getBatchLotNr().substring(0,2) + "/" + list.get(0).getBatchLotNr().substring(2,6) + "/" + list.get(0).getBatchLotNr().substring(6,8));
        if(list.get(0).getRepack().equals("1")) {
            chxRepack.setSelected(true);
        } else if (list.get(0).getRepack().equals(null)) {
            chxRepack.setSelected(false);
        } else {
            chxRepack.setSelected(false);
        }
        String repackBatchTest = list.get(0).getRepackBatch();
        if(repackBatchTest == null || repackBatchTest.isEmpty()) {
            txtRepackBatch.setText("99999");
        } else {
            txtRepackBatch.setText(list.get(0).getRepackBatch());
        }
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(list.get(0).getRepackDate() == null || list.get(0).getRepackDate().equals("0") || list.get(0).getRepackDate().isEmpty() || list.get(0).getRepackDate().equals("99/99/9999")){
            txtRepackDate.setValue(null);
        } else {
            LocalDate date = LocalDate.parse(list.get(0).getRepackDate(),dtf);
            txtRepackDate.setValue(date);
        }
        cbxRepackYear.setValue(list.get(0).getRepackYear());

        txtGMIEcode.setText(list.get(0).getWarenr());
        txtServiceDescription.setText(list.get(0).getLabeltext());
        txtSizeText.setText(list.get(0).getLabeltext2());
        txtSAPcode.setText(list.get(0).getServicedescription());
        txtEAN.setText(list.get(0).getBarcodec());
        txtGTIN.setText(list.get(0).getBarcoded());
        txtWeight.setText(list.get(0).getGramweightperdpack());
        txtLabelCode.setText(list.get(0).getLabeltext3());
        txtBottlesPerCase.setText(list.get(0).getDtocratio());
        txtCaseQuantity.setText(list.get(0).getTtodratio());
        txtLayersOnPallet.setText(list.get(0).getDtolayerratio());
        txtLaserTemplate.setText(list.get(0).getBarcodeclabel());
        txtCaseTemplate.setText(list.get(0).getBarcodedlabel());
        txtPalletTemplate.setText(list.get(0).getBarcodetlabel());
        txtSSCC.setText(fc.FBgetSSCC());
        txtEditSSCC.setText(fc.FBgetSSCC());
        mainController.getSSCC();
		if(!mainController.getProductionLine().equals("Caledonian")) {
			btnClearQueue.setDisable(true);
		} else {
			btnClearQueue.setDisable(false);
		}
    }

    public void getData() {
        list = fc.getOrderData(selectedOrder.getOrderProductionNr());
        fc.FBConnectionClose();
    }


    public void deleteOrder() { // Called from btnDelete in Order.fxml
        sup = new Supervisor(false);
        if(sup.supervisorTest()) {
            int orderNr = Integer.parseInt(txtQDOrderNr.getText());
           // fc = new FirebirdConnect(mainController);
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {// perform any updates to the UI on the FX Application Thread:
                    // code that updates UI
                    Platform.runLater(() -> fc.deleteOrder(orderNr));
                    return null;
                }
            };
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
            mainController.filter();
            mainController.setBanner("Order deleted","green");
            orderBanner("Order deleted","green");
            closeOrder();
        }
    }

    public void closeOrder() {
        // get a handle to the stage
        Stage stage = (Stage) btnOrderClose.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    private void setComboBoxSSCCYear() {
        cbxRepackYear.getItems().clear();

        cbxRepackYear.getItems().addAll(
                "14","15","16","17","18","19","20","21","22","23","24","25","26","27");
        //cbxRepackYear.getSelectionModel().select("18");
        cbxRepackYear.getSelectionModel().select(getCurrentYY());
        
    }

    public static String getCurrentYY() {
    SimpleDateFormat sdfDate = new SimpleDateFormat("yy");//yy
    Date now = new Date();
    String strDate = sdfDate.format(now);
    return strDate;
    }
    
    public void startOrder() { // Called from btnOrderStart in Order.fxml

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {// perform any updates to the UI on the FX Application Thread:
                // code that updates UI
                Platform.runLater(() -> setThinkingOn());
                startOrderButtonPressed();
                setThinkingOff();
                return null;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();

    }

    private void setThinkingOn(){
        pgiThinking.setVisible(true);
    }

    private void setThinkingOff() {
        pgiThinking.setVisible(false);
    }


    private void startOrderButtonPressed() {
        String repack;
        //Stage stage = (Stage) btnOrderClose.getScene().getWindow();
        //stage.getScene().getWindow().getScene();
        log.userLog("Start Order button pressed\r\n");
        log.appLog("Start Order button pressed\r\n");
        String QDOrderNr = txtQDOrderNr.getText();
        //fc = new FirebirdConnect(mainController);
        if(chxRepack.isSelected()) {
            repack = "1";
        } else {
            repack = "0";
        }
        String repackDate;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
	LocalDate date = txtRepackDate.getValue();
	if (date != null) {
            repackDate = formatter.format(date);
            if (!isDateValid(repackDate)) {
		repackDate = "01/01/1970";
            }
	} else {
            repackDate = "0";
	}
        String repackBatch = txtRepackBatch.getText();
        String repackYear = cbxRepackYear.getValue().toString();
        String caseCount = txtCaseCount.getText();
        if (caseCount.isEmpty()) {
            caseCount = "1";
        }
        String editSSCC = txtEditSSCC.getText();
        String partPallet = txtPartPallet.getText();
        String shopOrder = txtShopOrder.getText();
        int result = fc.startOrder(QDOrderNr,repack,repackBatch,repackDate,repackYear,caseCount,editSSCC,partPallet,shopOrder);
        if(result == 0) {
            mainController.setBanner("Order " + QDOrderNr + " started","green");
            orderBanner("Order " + QDOrderNr + " started","green");
            getData();
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {// perform any updates to the UI on the FX Application Thread:
                    // code that updates UI
                    Platform.runLater(() -> populateWindow());
                    return null;
                }
            };
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
            mainController.fullRefresh();
        } else {
            mainController.setBanner("An order is already running","red");
        }
        refresh();
    }

    public static boolean isDateValid(String date)
    {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            System.out.println (e.getMessage()) ;
            return false;
        }
    }

    private void orderBanner(String message, String colour) {
        txtOrderBanner.setStyle("-fx-text-inner-color: " + colour + ";");
        txtOrderBanner.setText(message);
    }

    public void stopOrder() { // Called from btnOrderStop in Order.fxml
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {// perform any updates to the UI on the FX Application Thread:
                // code that updates UI
                Platform.runLater(() -> setThinkingOn());
                stopOrderButtonPressed();
                setThinkingOff();
                return null;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private void stopOrderButtonPressed() {
        log.userLog("Stop Order button pressed\r\n");
        String QDOrderNr = txtQDOrderNr.getText();
        //fc = new FirebirdConnect(mainController);
        System.out.println("txtQDOrderNr = " + QDOrderNr);
        int result = fc.stopOrder(QDOrderNr);
        if(result == 0) {
            mainController.setBanner("Order " + QDOrderNr + " stopped","green");
            orderBanner("Order " + QDOrderNr + " stopped","green");
            getData();
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {// perform any updates to the UI on the FX Application Thread:
                    // code that updates UI
                    Platform.runLater(() -> populateWindow());
                    return null;
                }
            };
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
            mainController.fullRefresh();
        } else {
            mainController.setBanner("This order is NOT running","red");
            orderBanner("This order is NOT running","red");
        }
	System.out.println("Stop button tasks done");
	log.userLog("Stop button tasks done\r\n");
	System.out.println("Status = " + list.get(0).getStatus());
        refresh();
        System.out.println("Status = " + list.get(0).getStatus());
        System.out.println("Refresh done");
    }

    public void setResources() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDate date = txtRepackDate.getValue();
        String repack;
        if (chxRepack.isSelected()) {
            repack = "1";
        } else {
            repack = "0";
        }
        String repackDate;
        if (txtRepackDate.getValue() == null) {
            repackDate = "0";
        } else {
            repackDate = formatter.format(date);
            if (isDateValid(repackDate)) {
                repackDate = formatter.format(date);
            } else {
                repackDate = "01/01/1970";
            }
        }
        String repackBatch = txtRepackBatch.getText();
        String repackYear = cbxRepackYear.getValue().toString();
        String caseCount = txtCaseCount.getText();
        String editSSCC = txtEditSSCC.getText();
        String partPallet = txtPartPallet.getText();
        String shopOrder = txtShopOrder.getText();
        fc.FBConnection();
        fc.setResourses(repack, repackBatch, repackDate, repackYear, caseCount, editSSCC, partPallet, shopOrder);
        fc.FBConnectionClose();
    }

    public void updateLaser() { //Called from btnUpdateLaser in Order.fxml
        setResources();
        String editSSCC = txtEditSSCC.getText();
        String orderNr = txtQDOrderNr.getText();
        sup = new Supervisor(false);
        if(sup.supervisorTest()) {
            log.userLog("user pressed the Update Laser button\r\n");
            log.appLog("user pressed the Update Laser button\r\n");
            //fc = new FirebirdConnect(mainController);
            System.out.println("Update Laser Button Pressed");
            fc.updateLaser(orderNr,editSSCC);
        } else {
            log.userLog("Wrong Password Entered");
            mainController.setBanner("Wrong Supervisor Password !","RED");
        }

    }

    public void updateCaseLabeller() { //Called from btnCaseLabeller in Order.fxml
        setResources();
        String editSSCC = txtEditSSCC.getText();
        String orderNr = txtQDOrderNr.getText();
        log.userLog("user pressed the Update Case Labeller button\r\n");
        log.appLog("user pressed the Update Case Labeller button\r\n");
        System.out.println("Update Case Labeller Button Pressed");
        fc.updateCaseLabeller(orderNr,editSSCC);
        mainController.fullRefresh();
    }

    public void updateLogopak() { //Called from btnUpdateLogopak in Order.fxml
        setResources();
        String editSSCC = txtEditSSCC.getText();
        String orderNr = txtQDOrderNr.getText();
        log.userLog("user pressed the Update Logopak button\r\n");
        log.appLog("user pressed the Update Logopak button\r\n");
        System.out.println("Update LogoPak Button Pressed");
        fc.updateLogoPak(orderNr,editSSCC);
    }

    public void printToDesktop() { //Called from btnPrintToDesktop in Order.fxml
        setResources();
        String editSSCC = txtEditSSCC.getText();
        String orderNr = txtQDOrderNr.getText();
        log.userLog("user pressed the Print To Desktop button\r\n");
        log.appLog("user pressed the Print To Desktop button\r\n");
        System.out.println("Print To Desktop Button Pressed");
        fc.printToDesktop(orderNr,editSSCC);
    }

    public void partPallet() { //Called from btnPartPallet in Order.fxml
        setResources();
        String editSSCC = txtEditSSCC.getText();
        String orderNr = txtQDOrderNr.getText();
        log.userLog("user pressed the Part Pallet button\r\n");
        log.appLog("user pressed the Part Pallet button\r\n");
        System.out.println("Part Pallet Button Pressed");
        fc.partPallet(orderNr,editSSCC);
    }

    public void clearQueue() { //Called from btnClearQueue in Order.fxml
	String editSSCC = txtEditSSCC.getText();
	String orderNr = txtQDOrderNr.getText();
	sup = new Supervisor(false);
	if (sup.supervisorTest()) {
            log.userLog("user pressed the Clear Queue button\r\n");
            log.appLog("user pressed the Clear Queue button\r\n");
            System.out.println("Clear Queue Button Pressed");
            fc.clearQueue(orderNr, editSSCC);
	} else {
            log.userLog("Wrong Password Entered");
            mainController.setBanner("Wrong Supervisor Password !", "RED");
	}
    }
        
    public void restore() { //Called from btnRestore in Order.fxml
	String orderNr = txtQDOrderNr.getText();
	log.userLog("user pressed the Restore button\r\n");
	log.appLog("user pressed the Restore button\r\n");
	System.out.println("Restore Button Pressed");
	int result = fc.restore(orderNr);
	if (result == 0) {
            mainController.setBanner("Order " + orderNr + " restored", "green");
            orderBanner("Order " + orderNr + " restored", "green");
            getData();
            Task<Void> task = new Task<Void>() {
		@Override protected Void call() throws Exception {// perform any updates to the UI on the FX Application Thread:
		// code that updates UI
		Platform.runLater(() -> populateWindow());
		return null;
            }
	};
	Thread th = new Thread(task);
	th.setDaemon(true);
	th.start();
	setButtonStates();
	mainController.fullRefresh();
	} else {
            mainController.setBanner("Could not restore order", "red");
            orderBanner("Could not restore order", "red");
	}
        startOrderButtonPressed();
    }
}
