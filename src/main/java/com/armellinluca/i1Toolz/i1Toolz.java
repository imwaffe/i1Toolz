package com.armellinluca.i1Toolz;

import com.armellinluca.i1Toolz.Controllers.MainController;
import com.armellinluca.i1Toolz.Helpers.FXResizeHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class i1Toolz extends Application {

    private double xOffset = 0;
    private double yOffset = 0;
    private final static int RESIZEABLE_PADDING = 5;
    private static String openedProjectFilename = null;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/fxml/mainWindow.fxml"));
        Parent root = mainLoader.load();
        primaryStage.setTitle("com/armellin/i1Toolz/i1Toolz");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon-16.png")));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon-32.png")));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon-64.png")));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon-128.png")));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon-256.png")));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon-512.png")));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(root));

        if(Screen.getPrimary().getDpi() < 120){
            primaryStage.setHeight(500);
            primaryStage.setWidth(700);
            primaryStage.setMaximized(true);
        } else {
            primaryStage.setHeight(900);
            primaryStage.setWidth(1600);
        }

        root.setOnMousePressed(event -> {
            if(!primaryStage.isMaximized()) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        root.setOnMouseDragged(event -> {
            if(!primaryStage.isMaximized()) {
                double w = primaryStage.getWidth();
                double h = primaryStage.getHeight();
                if (xOffset > RESIZEABLE_PADDING * 2 && yOffset > RESIZEABLE_PADDING * 2 && w - event.getSceneX() > RESIZEABLE_PADDING * 2 && h - event.getSceneY() > RESIZEABLE_PADDING * 2) {
                    primaryStage.setX(event.getScreenX() - xOffset);
                    primaryStage.setY(event.getScreenY() - yOffset);
                }
            }
        });

        new FXResizeHelper(primaryStage,RESIZEABLE_PADDING,RESIZEABLE_PADDING);

        primaryStage.show();

        if(openedProjectFilename != null)
            ((MainController)mainLoader.getController()).openProject(new File(openedProjectFilename));
    }


    public static void main(String[] args) {
        if(args.length>0){
            openedProjectFilename = args[0];
        }
        launch(args);
    }
}
