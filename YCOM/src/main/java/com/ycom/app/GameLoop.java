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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameLoop extends AnimationTimer {
    private final GameStateManager stateManager;
    private final InputSystem inputSystem;
    private final Canvas canvas;
    private final ScheduledExecutorService physicsExecutor;
    private final double fixedDt;

    public GameLoop(Canvas canvas, Scene scene) {
        this.canvas = canvas;
        AssetManager.init();
        AudioManager.init();
        TimeManager.init();

        inputSystem = new InputSystem(scene);
        stateManager = new GameStateManager(canvas, inputSystem);
        fixedDt = TimeManager.getFixedDt();
        physicsExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "ycom-physics");
            thread.setDaemon(true);
            return thread;
        });
        long periodNanos = Math.round(fixedDt * 1_000_000_000.0);
        physicsExecutor.scheduleAtFixedRate(this::updatePhysics, 0L, periodNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void handle(long now) {
        GraphicsContext gc = getGraphicsContext();
        stateManager.render();
        gc.restore();
    }

    @Override
    public void stop() {
        physicsExecutor.shutdownNow();
        super.stop();
    }

    private void updatePhysics() {
        try {
            inputSystem.update();
            stateManager.update(fixedDt);
        } catch (Throwable t) {
            t.printStackTrace();
        }
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
