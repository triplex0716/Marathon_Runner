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

        // 竖屏窗口：高度取屏幕 90%，宽度按逻辑画面比例算，保持竖屏不变形、几乎无黑边
        double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
        double windowHeight = Math.min(Config.LOGICAL_HEIGHT, screenHeight * 0.9);
        double windowWidth = windowHeight * (Config.LOGICAL_WIDTH / Config.LOGICAL_HEIGHT);

        Scene scene = new Scene(root, windowWidth, windowHeight);
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
