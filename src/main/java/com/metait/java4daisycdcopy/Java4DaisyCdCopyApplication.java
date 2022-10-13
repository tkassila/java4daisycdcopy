package com.metait.java4daisycdcopy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Java4DaisyCdCopyApplication extends Application {
    Java4DaisyCdCopyController controller;
    @Override
    public void start(Stage stage) throws IOException {

        // getClass().getResource(
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("java4daisycdcopy-view.fxml"));
        controller = new Java4DaisyCdCopyController();
        try {
            Parent loadedroot = fxmlLoader.load();
            Scene scene = new Scene(loadedroot, 620, 540);
            stage.setTitle("Copy daisy cd into dir");
            controller.setMainStage(stage);
            fxmlLoader.setController(controller);
            stage.setScene(scene);
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Error: " +e.getMessage());
        }

//        FXMLLoader fxmlLoader = new FXMLLoader(Java4DaisyCdCopyApplication.class.getResource("java4daisycdcopy-view.fxml"));
    }

    public static void main(String[] args) {
        launch();
    }
}