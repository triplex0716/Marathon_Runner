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
        context = new EntityUpdateContext(input, eventBus, player, fixedDt);
        objects.addAll(newObjects);
        newObjects.clear();

        player.update(worldDt, context);

        Iterator<GameObject> it = objects.iterator();
        while (it.hasNext()) {
            GameObject obj = it.next();
            if (obj.active) {
                obj.update(worldDt, context);
                if (obj.z < player.z - 35.0) {
                    obj.active = false;
                }

                if (obj.active && obj instanceof Coin) {
                    Coin coin = (Coin) obj;
                    if (!coin.attracted && player.hasMagnet() && isInMagnetRange(coin)) {
                        coin.attracted = true;
                    }
                    if (coin.attracted) {
                        pullTowardPlayer(coin, worldDt);
                    }
                }
            } else {
                it.remove();
            }
        }
    }

    private boolean isInMagnetRange(GameObject obj) {
        double dz = obj.z - player.z;
        if (dz < -1.0 || dz > 38.0) {
            return false;
        }
        double dx = player.x - obj.x;
        double dy = player.y + 0.8 - obj.y;
        double distSq = dx * dx + dy * dy + dz * dz;
        return distSq <= 45.0 * 45.0;
    }

    private void pullTowardPlayer(GameObject obj, double worldDt) {
        double dx = player.x - obj.x;
        double dy = player.y + 0.8 - obj.y;
        double dz = player.z - obj.z;
        double dist = Math.max(0.001, Math.sqrt(dx * dx + dy * dy + dz * dz));
        double speed = Math.max(36.0, Config.BASE_SPEED * 2.0);
        obj.x += (dx / dist) * speed * worldDt;
        obj.y += (dy / dist) * speed * worldDt;
        obj.z += (dz / dist) * speed * worldDt;
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
