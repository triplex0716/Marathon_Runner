package com.ycom.system;

import com.ycom.core.Config;
import com.ycom.core.TimeManager;
import com.ycom.entity.Coin;
import com.ycom.entity.EnergyDrink;
import com.ycom.entity.Magnet;
import com.ycom.entity.Obstacle;
import com.ycom.entity.RandomItem;
import com.ycom.entity.RevivalCapsule;
import com.ycom.entity.Treadmill;
import com.ycom.world.GameWorld;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SpawnSystem {
    private static final double CHUNK_BUFFER_GAP = 6.0;
    private static final double SPAWN_LOOKAHEAD = 230.0;

    private final GameWorld world;
    private final SceneScheduler scheduler;
    private final Random rand = new Random();
    private double nextSpawnZ = 50.0;

    public SpawnSystem(GameWorld world, Config.Difficulty difficulty) {
        this.world = world;
        Config.Difficulty diff = difficulty == null ? Config.DEFAULT_DIFFICULTY : difficulty;
        this.scheduler = new SceneScheduler(diff);
    }

    public void update(double playerZ) {
        while (playerZ + SPAWN_LOOKAHEAD > nextSpawnZ) {
            Chunk c = scheduler.next(TimeManager.getElapsedTime());
            spawnChunk(c, nextSpawnZ);
            nextSpawnZ += c.length + CHUNK_BUFFER_GAP;
        }
    }

    private void spawnChunk(Chunk c, double baseZ) {
        int[] resolved = resolveLaneTransform(c);
        int shift = resolved[0];
        boolean mirror = resolved[1] == 1;

        for (EntitySpec spec : c.entities) {
            int lane = spec.lane;
            if (mirror) lane = -lane;
            lane += shift;
            double x = lane * Config.LANE_WIDTH;
            double z = baseZ + spec.relZ;
            switch (spec.type) {
                case JUMP_OBSTACLE -> world.addObject(new Obstacle(
                        x, 0.0, z, 2.1, 1.0, 1.2, Color.ORANGE, Obstacle.AvoidMethod.JUMP));
                case SLIDE_OBSTACLE -> world.addObject(new Obstacle(
                        x, 1.25, z, 2.2, 1.0, 1.1, Color.CRIMSON, Obstacle.AvoidMethod.SLIDE));
                case LANE_BLOCK -> world.addObject(new Obstacle(
                        x, 0.0, z, 2.4, 3.0, 7.0, Color.DARKRED, Obstacle.AvoidMethod.CHANGE_LANE));
                case CONTAINER_WITH_RAMP -> {
                    world.addObject(new Obstacle(
                            x, 0.0, z, 2.4, 3.0, 7.0, Color.DARKBLUE, Obstacle.AvoidMethod.CONTAINER));
                    world.addObject(new Obstacle(
                            x, 0.0, z - 5.5, 2.4, 3.0, 4.0, Color.GRAY, Obstacle.AvoidMethod.RAMP));
                }
                case COIN -> {
                    double y = spec.y > 0.0 ? spec.y : 0.65;
                    world.addObject(new Coin(x, y, z));
                }
                case MAGNET -> world.addObject(new Magnet(x, 0.6, z));
                case ENERGY_DRINK -> world.addObject(new EnergyDrink(x, 0.4, z));
                case TREADMILL -> world.addObject(new Treadmill(x, 0.45, z));
                case REVIVAL_CAPSULE -> world.addObject(new RevivalCapsule(x, 0.55, z));
                case RANDOM_ITEM -> world.addObject(new RandomItem(x, 0.5, z));
            }
        }
    }

    private int[] resolveLaneTransform(Chunk c) {
        if (!c.laneShiftable && !c.mirrorable) {
            return new int[]{0, 0};
        }
        List<int[]> candidates = new ArrayList<>();
        if (c.laneShiftable && c.mirrorable) {
            for (int s = -1; s <= 1; s++) {
                candidates.add(new int[]{s, 0});
                candidates.add(new int[]{s, 1});
            }
        } else if (c.laneShiftable) {
            for (int s = -1; s <= 1; s++) candidates.add(new int[]{s, 0});
        } else {
            candidates.add(new int[]{0, 0});
            candidates.add(new int[]{0, 1});
        }
        Collections.shuffle(candidates, rand);
        for (int[] cand : candidates) {
            if (isValidTransform(c, cand[0], cand[1] == 1)) {
                return cand;
            }
        }
        return new int[]{0, 0};
    }

    private boolean isValidTransform(Chunk c, int shift, boolean mirror) {
        for (EntitySpec spec : c.entities) {
            int lane = spec.lane;
            if (mirror) lane = -lane;
            lane += shift;
            if (lane < -1 || lane > 1) return false;
        }
        return true;
    }
}
