package com.ycom.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
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
        
        // 窗口按屏幕可视区的 90% 开，保持 16:9，避免窗口比屏幕大导致左/上边超出屏幕看不见；
        // 画面内容由 GameLoop 的 letterbox 自动等比缩放填满
        Rectangle2D vb = Screen.getPrimary().getVisualBounds();
        double winScale = Math.min(vb.getWidth() / Config.LOGICAL_WIDTH, vb.getHeight() / Config.LOGICAL_HEIGHT) * 0.9;
        double winW = Config.LOGICAL_WIDTH * winScale;
        double winH = Config.LOGICAL_HEIGHT * winScale;

        Scene scene = new Scene(root, winW, winH);
        primaryStage.setTitle("You Can't Outrun Me!");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
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
