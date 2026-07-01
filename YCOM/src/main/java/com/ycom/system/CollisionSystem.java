package com.ycom.system;

import com.ycom.entity.CollisionEvent;
import com.ycom.world.GameWorld;
import com.ycom.entity.Player;
import com.ycom.entity.GameObject;
import com.ycom.event.EventBus;
import java.util.List;

public class CollisionSystem {
    private final GameWorld world;
    private final EventBus eventBus;

    public CollisionSystem(GameWorld world, EventBus eventBus) {
        this.world = world;
        this.eventBus = eventBus;
    }

    public void update() {
        Player player = world.getPlayer();
        List<GameObject> objects = world.getObjects();

        for (GameObject obj : objects) {
            if (!obj.isActive()) {
                continue;
            }

            if (player.collidesWith(obj)) {
                obj.onCollision(new CollisionEvent(obj, player), world.context());
                player.onCollision(new CollisionEvent(player, obj), world.context());
            }
        }
    }
}
