package i1Toolz;

import Helpers.FXResizeHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    private double xOffset = 0;
    private double yOffset = 0;
    private final static int RESIZEABLE_PADDING = 5;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/mainWindow.fxml"));
        primaryStage.setTitle("i1Toolz");

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(root, 1500, 770));

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
    }


    public static void main(String[] args) {
        launch(args);
    }
}
