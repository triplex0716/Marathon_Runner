package com.ycom.render;

import com.ycom.entity.GameObject;
import java.util.EnumMap;
import java.util.Map;

public class RendererRegistry {
    private final Map<GameObject.ObjectKind, ObjectRenderer> renderers = new EnumMap<>(GameObject.ObjectKind.class);

    public void register(GameObject.ObjectKind kind, ObjectRenderer renderer) {
        renderers.put(kind, renderer);
    }

    public ObjectRenderer getRenderer(GameObject.ObjectKind kind) {
        return renderers.get(kind);
    }
}
