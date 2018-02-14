package com.ruddlesdin;

/**
 * Created by p_ruddlesdin on 21/03/2017.
 */
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SupervisorController  implements Initializable{

    @FXML
    private Button btnSupervisorUnlock;
    @FXML
    private Button btnSupervisorClose;
    @FXML
    private TextField txtSupervisorPassword;
    @FXML
    private TextField txtSupervisorMessage;
    @FXML
    private TextField txtSucceedMessage;

    Supervisor sup = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setSupMessageLocked();
        setPWtxtRed();
    }

    public SupervisorController(Supervisor supervisor) {
        sup = supervisor;
    }

    public void closeSupervisor() {
        // get a handle to the stage
        Stage stage = (Stage) btnSupervisorClose.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    public void setPWtxtRed() {
        txtSupervisorPassword.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
    }

    public void setSupMessageLocked() {
        txtSupervisorMessage.setStyle("-fx-text-inner-color: red;-fx-background-color: transparent ; -fx-background-insets: 0px ;");
        txtSupervisorMessage.setText("Locked !");
        txtSucceedMessage.setStyle("-fx-text-inner-color: red; -fx-font-weight: bold;-fx-background-color: transparent ; -fx-background-insets: 0px ;");
        btnSupervisorClose.setText("Close");
    }

    public void setSupMessageUnlocked() {
        txtSupervisorMessage.setStyle("-fx-text-inner-color: green;-fx-background-color: transparent ; -fx-background-insets: 0px ;");
        txtSupervisorMessage.setText("Unlocked :)");
        txtSucceedMessage.setStyle("-fx-font-weight: bold");
        txtSucceedMessage.setStyle("-fx-text-inner-color: green; -fx-font-weight: bold;-fx-background-color: transparent ; -fx-background-insets: 0px ;");
        txtSucceedMessage.setText("Authorised to proceed ...");
        btnSupervisorClose.setText("Proceed");
    }

    public void setSupMessageText(String message) {
        txtSupervisorMessage.setText(message);
    }

    public void checkPwdText() {
        if (txtSupervisorPassword.getText().isEmpty()) {
            txtSupervisorPassword.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
        } else {
            txtSupervisorPassword.setStyle("-fx-border-color: gray ; -fx-border-width: 1px ;");
        }
    }

    public void testPassword() {
        if(sup.isConfig()) {
            if (new PasswordConfig().checkPasswordExists(txtSupervisorPassword.getText())) {
                setSupMessageUnlocked();
                sup.setSupervisorResult(true);

            } else {
                setSupMessageLocked();
                txtSucceedMessage.setText("Authorisation Failed");
                sup.setSupervisorResult(false);
            }
        } else {
            if (new Password().checkPasswordExists(txtSupervisorPassword.getText())) {
                setSupMessageUnlocked();
                sup.setSupervisorResult(true);

            } else {
                setSupMessageLocked();
                txtSucceedMessage.setText("Authorisation Failed");
                sup.setSupervisorResult(false);
            }
        }
    }
}
