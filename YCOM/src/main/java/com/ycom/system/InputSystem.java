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
        scene.setOnKeyPressed(e -> activeKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));
        scene.setOnMouseMoved(e -> updateMousePosition(e.getSceneX(), e.getSceneY()));
        scene.setOnMouseDragged(e -> updateMousePosition(e.getSceneX(), e.getSceneY()));



        scene.setOnMouseClicked(e -> {
            updateMousePosition(e.getSceneX(), e.getSceneY());
            pendingMouseClick = mouseX >= 0.0 && mouseX <= Config.LOGICAL_WIDTH
                    && mouseY >= 0.0 && mouseY <= Config.LOGICAL_HEIGHT;
        });
    }

    public void update() {
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

    public boolean isKeyPressed(KeyCode code) {
        return activeKeys.contains(code);
    }

    public boolean isKeyJustPressed(KeyCode code) {
        return justPressed.contains(code);
    }

    public boolean isMouseJustClicked() {
        return mouseJustClicked;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    private void updateMousePosition(double sceneX, double sceneY) {
        double scale = Math.min(scene.getWidth() / Config.LOGICAL_WIDTH, scene.getHeight() / Config.LOGICAL_HEIGHT);
        double offsetX = (scene.getWidth() - Config.LOGICAL_WIDTH * scale) / 2.0;
        double offsetY = (scene.getHeight() - Config.LOGICAL_HEIGHT * scale) / 2.0;
        mouseX = (sceneX - offsetX) / scale;
        mouseY = (sceneY - offsetY) / scale;
    }

    public void reset() {
        activeKeys.clear();
        previousKeys.clear();
        justPressed.clear();
        pendingMouseClick = false;
        mouseJustClicked = false;
    }
}
