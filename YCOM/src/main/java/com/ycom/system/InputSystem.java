package com.ycom.system;

import com.ycom.core.Config;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

public class InputSystem {
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final Set<KeyCode> justPressed = new HashSet<>();
    private final Set<KeyCode> previousKeys = new HashSet<>();
    private final Scene scene;
    private boolean pendingMouseClick;
    private boolean mouseJustClicked;
    private double mouseX;
    private double mouseY;

    public InputSystem(Scene scene) {
        this.scene = scene;
        scene.setOnKeyPressed(e -> {
            synchronized (this) {
                activeKeys.add(e.getCode());
            }
        });
        scene.setOnKeyReleased(e -> {
            synchronized (this) {
                activeKeys.remove(e.getCode());
            }
        });
        scene.setOnMouseMoved(e -> updateMousePosition(e.getSceneX(), e.getSceneY()));
        scene.setOnMouseDragged(e -> updateMousePosition(e.getSceneX(), e.getSceneY()));



        scene.setOnMouseClicked(e -> {
            updateMousePosition(e.getSceneX(), e.getSceneY());
            synchronized (this) {
                pendingMouseClick = mouseX >= 0.0 && mouseX <= Config.LOGICAL_WIDTH
                        && mouseY >= 0.0 && mouseY <= Config.LOGICAL_HEIGHT;
            }
        });
    }

    public synchronized void update() {
        justPressed.clear();
        mouseJustClicked = pendingMouseClick;
        pendingMouseClick = false;
        for (KeyCode k : activeKeys) {
            if (!previousKeys.contains(k)) {
                justPressed.add(k);
            }
        }
        previousKeys.clear();
        previousKeys.addAll(activeKeys);
    }

    public synchronized boolean isKeyPressed(KeyCode code) {
        return activeKeys.contains(code);
    }

    public synchronized boolean isKeyJustPressed(KeyCode code) {
        return justPressed.contains(code);
    }

    public synchronized boolean isMouseJustClicked() {
        return mouseJustClicked;
    }

    public synchronized double getMouseX() {
        return mouseX;
    }

    public synchronized double getMouseY() {
        return mouseY;
    }

    private synchronized void updateMousePosition(double sceneX, double sceneY) {
        double scale = Math.min(scene.getWidth() / Config.LOGICAL_WIDTH, scene.getHeight() / Config.LOGICAL_HEIGHT);
        double offsetX = (scene.getWidth() - Config.LOGICAL_WIDTH * scale) / 2.0;
        double offsetY = (scene.getHeight() - Config.LOGICAL_HEIGHT * scale) / 2.0;
        mouseX = (sceneX - offsetX) / scale;
        mouseY = (sceneY - offsetY) / scale;
    }

    public synchronized void reset() {
        activeKeys.clear();
        previousKeys.clear();
        justPressed.clear();
        pendingMouseClick = false;
        mouseJustClicked = false;
    }
}
