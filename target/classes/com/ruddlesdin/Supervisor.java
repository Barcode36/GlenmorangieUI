package com.ruddlesdin;

/**
 * Created by p_ruddlesdin on 21/03/2017.
 */
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Supervisor {

    private boolean supervisorResult = false;
    private boolean config;

    public Supervisor(boolean config) {
        supervisorResult = false;
        this.config = config;
    }

    public boolean isConfig() {
        return config;
    }

    public boolean isSupervisorResult() {
        return supervisorResult;
    }

    public void setSupervisorResult(boolean supervisorResult) {
        this.supervisorResult = supervisorResult;
    }

    public boolean supervisorTest() {
        try {
            SupervisorController sup = new SupervisorController(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ruddlesdin/Supervisor.fxml"));
            loader.setController(sup);
            Parent root = (Parent) loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
            Stage SupervisorStage = new Stage();
            SupervisorStage.initModality(Modality.APPLICATION_MODAL);
            SupervisorStage.setTitle("Supervisor");
            SupervisorStage.setScene(scene);
            SupervisorStage.initStyle(StageStyle.UNDECORATED);
            SupervisorStage.setUserData(this);
            SupervisorStage.showAndWait();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return supervisorResult;
    }

}