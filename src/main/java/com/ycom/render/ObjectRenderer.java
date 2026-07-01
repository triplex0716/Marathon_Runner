package com.ycom.render;

import javafx.scene.canvas.GraphicsContext;

public interface ObjectRenderer {
    void render(GraphicsContext gc, RenderSnapshot snapshot, Projection p, Camera cam);
}
