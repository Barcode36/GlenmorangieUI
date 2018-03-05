package com.ruddlesdin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("QuickDesign UI");
            //primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.initStyle(StageStyle.DECORATED);
            primaryStage.setMinWidth(1091);
            primaryStage.setMinHeight(782);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
