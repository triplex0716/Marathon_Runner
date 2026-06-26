package com.ycom.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.ycom.account.AccountStore;
import com.ycom.core.Config;

public class GameApp extends Application {
    private GameLoop gameLoop;

    @Override
    public void start(Stage primaryStage) {
        AccountStore.load();
        Canvas canvas = new Canvas();
        StackPane root = new StackPane(canvas);
        
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        
        Scene scene = new Scene(root, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        primaryStage.setTitle("You Can't Outrun Me!");
        primaryStage.setScene(scene);
        primaryStage.show();

        gameLoop = new GameLoop(canvas, scene);
        gameLoop.start();
    }

    @Override
    public void stop() throws Exception {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
