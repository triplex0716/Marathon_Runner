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
    private final double fixedDt;
    private final long fixedNanos;
    
    // FPS counting variables
    private long lastNanos = 0;
    private long fpsTimer = 0;
    private int frameCount = 0;
    private double currentFps = 0.0;
    private String fpsString = "FPS: 0.0";
    private static final javafx.scene.text.Font FPS_FONT = javafx.scene.text.Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 20);
    
    // Multithreading
    public static final java.util.concurrent.atomic.AtomicReference<com.ycom.core.PhysicsSnapshot> snapshotRef = new java.util.concurrent.atomic.AtomicReference<>();
    private final ScheduledExecutorService physicsExecutor;

    public GameLoop(Canvas canvas, Scene scene) {
        this.canvas = canvas;
        AssetManager.init();
        AudioManager.init();
        TimeManager.init();

        inputSystem = new InputSystem(scene);
        stateManager = new GameStateManager(canvas, inputSystem);
        fixedDt = TimeManager.getFixedDt();
        fixedNanos = Math.round(fixedDt * 1_000_000_000.0);
        physicsExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "ycom-physics");
            thread.setDaemon(true);
            return thread;
        });
        physicsExecutor.scheduleAtFixedRate(this::updatePhysics, 0L, fixedNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void handle(long now) {
        if (lastNanos == 0) {
            lastNanos = now;
            fpsTimer = now;
            frameCount = 0;
            return;
        } 
        
        long elapsed = now - lastNanos;
        lastNanos = now;
        
        // Prevent death spiral if game hangs
        if (elapsed > 250_000_000L) {
            elapsed = 250_000_000L;
        }
        
        frameCount++;
        if (now - fpsTimer >= 1_000_000_000L) {
            currentFps = (double) frameCount * 1_000_000_000L / (now - fpsTimer);
            fpsString = String.format("FPS: %.1f", currentFps);
            frameCount = 0;
            fpsTimer = now;
        }
        GraphicsContext gc = getGraphicsContext();
        stateManager.render();
        gc.restore();
        
        if (Config.SHOW_FPS) {
            GraphicsContext rootGc = canvas.getGraphicsContext2D();
            rootGc.save();
            rootGc.setTransform(1, 0, 0, 1, 0, 0); // Reset transform to absolute window coordinates
            rootGc.setFill(javafx.scene.paint.Color.LIMEGREEN);
            rootGc.setFont(FPS_FONT);
            rootGc.fillText(fpsString, canvas.getWidth() - 140, 30);
            rootGc.restore();
        }
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
            if (stateManager.getState(com.ycom.state.StateId.PLAYING) instanceof com.ycom.state.PlayingState p) {
                if (p.isPhysicsUpdating()) {
                    snapshotRef.set(p.createPhysicsSnapshot());
                }
            }
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
