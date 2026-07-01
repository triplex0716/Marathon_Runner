package com.ycom.world;

import java.util.ArrayList;
import java.util.List;
import com.ycom.entity.Coin;
import com.ycom.entity.EntityUpdateContext;
import com.ycom.entity.GameObject;
import com.ycom.entity.Player;
import com.ycom.render.RenderFrame;
import com.ycom.render.RenderSnapshot;
import com.ycom.system.InputSystem;
import com.ycom.core.Config;
import com.ycom.event.EventBus;

public class GameWorld {
    private final Player player;
    private final List<GameObject> objects = new ArrayList<>();
    private final List<GameObject> newObjects = new ArrayList<>();
    private volatile RenderFrame renderFrame;
    private EntityUpdateContext context;

    public GameWorld() {
        player = new Player();
        renderFrame = RenderFrame.initial(RenderSnapshot.from(player));
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
                if (obj.getZ() < player.getZ() - Config.OBJECT_DESPAWN_BEHIND_DISTANCE) {
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
        publishRenderFrame(fixedDt);
    }

    private void publishRenderFrame(double fixedDt) {
        List<RenderSnapshot> currentObjects = new ArrayList<>(objects.size());
        for (GameObject obj : objects) {
            if (obj.isActive()) {
                currentObjects.add(RenderSnapshot.from(obj));
            }
        }
        RenderFrame previous = renderFrame;
        renderFrame = new RenderFrame(
                previous.player(1.0),
                RenderSnapshot.from(player),
                previous.currentObjects(),
                currentObjects,
                System.nanoTime(),
                Math.round(fixedDt * 1_000_000_000.0)
        );
    }

    private boolean isInMagnetRange(GameObject obj) {
        double dz = obj.getZ() - player.getZ();
        if (dz < -Config.MAGNET_RANGE_BEHIND_DISTANCE || dz > Config.MAGNET_RANGE_AHEAD_DISTANCE) {
            return false;
        }
        double dx = player.getX() - obj.getX();
        double dy = player.getY() + Config.MAGNET_TARGET_Y_OFFSET - obj.getY();
        double distSq = dx * dx + dy * dy + dz * dz;
        return distSq <= Config.MAGNET_ATTRACT_RADIUS * Config.MAGNET_ATTRACT_RADIUS;
    }

    private void pullTowardPlayer(GameObject obj, double worldDt) {
        double dx = player.getX() - obj.getX();
        double dy = player.getY() + Config.MAGNET_TARGET_Y_OFFSET - obj.getY();
        double dz = player.getZ() - obj.getZ();
        double dist = Math.max(0.001, Math.sqrt(dx * dx + dy * dy + dz * dz));
        double speed = Math.max(Config.MAGNET_PULL_MIN_SPEED, Config.BASE_SPEED * Config.MAGNET_PULL_SPEED_MULTIPLIER);
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

    public RenderFrame renderFrame() {
        return renderFrame;
    }

    public EntityUpdateContext context() {
        return context;
    }
}
