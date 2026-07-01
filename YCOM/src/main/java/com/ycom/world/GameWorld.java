package com.ycom.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import com.ycom.entity.Coin;
import com.ycom.entity.EntityUpdateContext;
import com.ycom.entity.GameObject;
import com.ycom.entity.Player;
import com.ycom.system.InputSystem;
import com.ycom.core.Config;
import com.ycom.event.EventBus;

public class GameWorld {
    private final Player player;
    private final List<GameObject> objects = new ArrayList<>();
    private final List<GameObject> newObjects = new ArrayList<>();
    private EntityUpdateContext context;

    public GameWorld() {
        player = new Player();
    }

    public void addObject(GameObject obj) {
        newObjects.add(obj);
    }

    public void update(double worldDt, double fixedDt, InputSystem input, EventBus eventBus) {
        context = new EntityUpdateContext(input, eventBus, player, fixedDt, this);
        objects.addAll(newObjects);
        newObjects.clear();

        player.update(worldDt, context);

        for (GameObject obj : objects) {
            if (obj.isActive()) {
                obj.update(worldDt, context);
                if (obj.getZ() < player.getZ() - 35.0) {
                    obj.deactivate();
                }

                if (obj.isActive() && obj instanceof Coin) {
                    Coin coin = (Coin) obj;
                    if (!coin.attracted && player.hasMagnet() && isInMagnetRange(coin)) {
                        coin.attracted = true;
                    }
                    if (coin.attracted) {
                        pullTowardPlayer(coin, worldDt);
                    }
                }
            }
        }
        objects.removeIf(obj -> !obj.isActive());
    }

    private boolean isInMagnetRange(GameObject obj) {
        double dz = obj.getZ() - player.getZ();
        if (dz < -1.0 || dz > 38.0) {
            return false;
        }
        double dx = player.getX() - obj.getX();
        double dy = player.getY() + 0.8 - obj.getY();
        double distSq = dx * dx + dy * dy + dz * dz;
        return distSq <= 45.0 * 45.0;
    }

    private void pullTowardPlayer(GameObject obj, double worldDt) {
        double dx = player.getX() - obj.getX();
        double dy = player.getY() + 0.8 - obj.getY();
        double dz = player.getZ() - obj.getZ();
        double dist = Math.max(0.001, Math.sqrt(dx * dx + dy * dy + dz * dz));
        double speed = Math.max(36.0, Config.BASE_SPEED * 2.0);
        obj.setX(obj.getX() + (dx / dist) * speed * worldDt);
        obj.setY(obj.getY() + (dy / dist) * speed * worldDt);
        obj.setZ(obj.getZ() + (dz / dist) * speed * worldDt);
    }

    public Player getPlayer() {
        return player;
    }

    public List<GameObject> getObjects() {
        return objects;
    }

    public EntityUpdateContext context() {
        return context;
    }
}
