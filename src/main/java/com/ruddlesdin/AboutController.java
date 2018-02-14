package com.ruddlesdin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by p_ruddlesdin on 21/03/2017.
 */
public class AboutController  implements Initializable {
    @FXML
    Button btnAboutClose;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    public void closeAbout() {
        // get a handle to the stage
        Stage stage = (Stage) btnAboutClose.getScene().getWindow();
        // do what you have to do
        stage.close();
    }
}
