package com.ruddlesdin;

/**
 * Created by p_ruddlesdin on 21/03/2017.
 */
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class NewController implements Initializable{

    @FXML private Button btnNewClose;
    @FXML private Button btnCreateOrder;
    @FXML private ListView<String> livNewProduct;
    @FXML private TextField txtNewProduct;
    @FXML private TextField txtNewShopOrder;
    @FXML private TextField txtNewRotation1;
    @FXML private TextField txtNewRotation2;
    @FXML private TextField txtNewRotation3;
    @FXML private TextField txtChosenProduct;
    @FXML private TextField txtWare;
    @FXML private TextField txtProdLineNr;
    @FXML private TextField txtSAPcode;

    private ObservableList<String> wareList;
    private SortedList<String> sortedWareList;
    private String prodSearchString = "";
    private FirebirdConnect fc2;
    private FirebirdConnect fc3;
    private String sqlWare = "SELECT WARENR FROM TBLWARE";
    private String sqlFilter = "SELECT WARENR FROM TBLWARE WHERE WARENR STARTING WITH (UPPER('";
    private String sqlSearchEnd = "'))";

    private Boolean checkProduct = false;
    private Boolean checkShopOrder = false;
    private Boolean checkRotation1 = false;
    private Boolean checkRotation2 = false;
    private Boolean checkRotation3 = false;


    private MainController mainController;
    private int orderNr;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setRedStyle();
        setCreateBtnDisable();
        getData(sqlWare);
        txtNewProduct.addEventFilter(KeyEvent.KEY_TYPED,maxLength(14));
        txtNewShopOrder.addEventFilter(KeyEvent.KEY_TYPED,maxLength(5));
        txtNewRotation1.addEventFilter(KeyEvent.KEY_TYPED,maxLength(2));
        txtNewRotation2.addEventFilter(KeyEvent.KEY_TYPED,maxLength(4));
        txtNewRotation3.addEventFilter(KeyEvent.KEY_TYPED,maxLength(2));
    }

    NewController(MainController mc) {
        this.mainController = mc;
    }

    public void createNewOrder() { // Called from btnCreateOrder in New.fxml
        fc3 = new FirebirdConnect(mainController,mainController.getIpAddress(), mainController.getDbPath());
        orderNr = fc3.createNewOrder(txtChosenProduct.getText().trim(),txtNewRotation1.getText()+txtNewRotation2.getText()+txtNewRotation3.getText(),txtNewShopOrder.getText());
        if (orderNr < 300000) {
            System.out.println("Error creating order");
        }
        mainController.filter();
        fc3.FBConnectionClose();
        mainController.setSelectedOrder(orderNr);
        closeNew();
    }

    private EventHandler<KeyEvent> maxLength(final Integer i) {
        return arg0 -> {
            TextField tx = (TextField) arg0.getSource();
            if (tx.getText().length() >= i) {
                arg0.consume();
            }
        };
    }

    public void closeNew() {
        // get a handle to the stage
        Stage stage = (Stage) btnNewClose.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    private void setRedStyle() {
        txtChosenProduct.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
        txtNewShopOrder.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
        txtNewRotation1.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
        txtNewRotation2.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
        txtNewRotation3.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
    }

    private void setCreateBtnDisable() {
        btnCreateOrder.setDisable(true);
    }

    private void getData(String warenr) {
        fc2 = new FirebirdConnect(mainController, mainController.getIpAddress(), mainController.getDbPath());
        wareList = fc2.FBWarenr(warenr);
        fc2.FBConnectionClose();
        populateList();
    }

    private void populateList() {
        sortedWareList = null;
        sortedWareList = new SortedList<>(wareList);
        sortedWareList.setComparator((arg0, arg1) -> arg0.compareToIgnoreCase(arg1));
        livNewProduct.setItems(sortedWareList);
        livNewProduct.refresh();
    }

    public void insertChosenProduct() {
        txtChosenProduct.setText(livNewProduct.getSelectionModel().getSelectedItem());
        checkProduct();
    }

    public void productSearch() {
        prodSearchString = txtNewProduct.getText();
        getData(sqlFilter + prodSearchString + sqlSearchEnd);
    }

    private void checkProduct() {
        if(txtChosenProduct.getText().isEmpty()) {
            txtChosenProduct.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            checkProduct = false;
        } else {
            txtChosenProduct.setStyle("-fx-border-color: gray ; -fx-border-width: 1px ;");
            checkProduct = true;
        }
        if(checkProduct && checkShopOrder && checkRotation1 && checkRotation2 && checkRotation3) {
            btnCreateOrder.setDisable(false);
        } else {
            btnCreateOrder.setDisable(true);
        }
    }

    public void checkShopOrder() {
        if(txtNewShopOrder.getText().length() <= 4) {
            txtNewShopOrder.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            checkShopOrder = false;
        } else {
            txtNewShopOrder.setStyle("-fx-border-color: gray ; -fx-border-width: 1px ;");
            checkShopOrder = true;
        }
        if(checkProduct && checkShopOrder && checkRotation1 && checkRotation2 && checkRotation3) {
            btnCreateOrder.setDisable(false);
        } else {
            btnCreateOrder.setDisable(true);
        }
    }

    public void checkRotation1() {
        if(txtNewRotation1.getText().length() <= 1) {
            txtNewRotation1.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            checkRotation1 = false;
        } else {
            txtNewRotation1.setStyle("-fx-border-color: gray ; -fx-border-width: 1px ;");
            checkRotation1 = true;
        }
        if(checkProduct && checkShopOrder && checkRotation1 && checkRotation2 && checkRotation3) {
            btnCreateOrder.setDisable(false);
        } else {
            btnCreateOrder.setDisable(true);
        }
    }

    public void checkRotation2() {
        if(txtNewRotation2.getText().length() <= 3) {
            txtNewRotation2.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            checkRotation2 = false;
        } else {
            txtNewRotation2.setStyle("-fx-border-color: gray ; -fx-border-width: 1px ;");
            checkRotation2 = true;
        }
        if(checkProduct && checkShopOrder && checkRotation1 && checkRotation2 && checkRotation3) {
            btnCreateOrder.setDisable(false);
        } else {
            btnCreateOrder.setDisable(true);
        }
    }

    public void checkRotation3() {
        if(txtNewRotation3.getText().length() <= 1) {
            txtNewRotation3.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            checkRotation3 = false;
        } else {
            txtNewRotation3.setStyle("-fx-border-color: gray ; -fx-border-width: 1px ;");
            checkRotation3 = true;
        }
        if(checkProduct && checkShopOrder && checkRotation1 && checkRotation2 && checkRotation3) {
            btnCreateOrder.setDisable(false);
        } else {
            btnCreateOrder.setDisable(true);
        }
    }
}
