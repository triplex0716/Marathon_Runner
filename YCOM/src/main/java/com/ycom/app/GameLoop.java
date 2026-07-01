package com.ycom.app;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.Scene;
import com.ycom.core.Config;
import com.ycom.core.TimeManager;
import com.ycom.state.GameStateManager;
import com.ycom.system.InputSystem;
import com.ycom.resource.AssetManager;
import com.ycom.resource.AudioManager;
import javafx.scene.canvas.GraphicsContext;

public class GameLoop extends AnimationTimer {
    private long lastTime = 0;
    private double accumulator = 0.0;

    private final GameStateManager stateManager;
    private final InputSystem inputSystem;
    private final Canvas canvas;

    public GameLoop(Canvas canvas, Scene scene) {
        this.canvas = canvas;
        AssetManager.init();
        AudioManager.init();
        TimeManager.init();

        inputSystem = new InputSystem(scene);
        stateManager = new GameStateManager(canvas, inputSystem);
    }

    @Override
    public void handle(long now) {
        if (lastTime == 0) {
            lastTime = now;
            return;
        }

        double frameTime = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;
        if (frameTime > 0.25) {
            frameTime = 0.25;
        }

        accumulator += frameTime;

        double fixedDt = TimeManager.getFixedDt();
        int maxUpdates = 5;
        int updates = 0;
        while (accumulator >= fixedDt && updates < maxUpdates) {
            inputSystem.update();
            stateManager.update(fixedDt);
            accumulator -= fixedDt;
            updates++;
        }
        if (accumulator >= fixedDt) {
            accumulator = 0; // Prevent death spiral if we hit maxUpdates
        }

        GraphicsContext gc = getGraphicsContext();
        stateManager.render();
        gc.restore();
    }

    private GraphicsContext getGraphicsContext() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        double scaleX = canvas.getWidth() / Config.LOGICAL_WIDTH;
        double scaleY = canvas.getHeight() / Config.LOGICAL_HEIGHT;
        double renderScale = Math.min(scaleX, scaleY);

        double offsetX = (canvas.getWidth() - Config.LOGICAL_WIDTH * renderScale) / 2.0;
        double offsetY = (canvas.getHeight() - Config.LOGICAL_HEIGHT * renderScale) / 2.0;

        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(renderScale, renderScale);

        gc.beginPath();
        gc.rect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        gc.clip();
        return gc;
    }
}
