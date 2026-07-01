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

    private final EntityFactory entityFactory = new EntityFactory();

    private void spawnChunk(Chunk c, double baseZ) {
        int[] resolved = resolveLaneTransform(c);
        int shift = resolved[0];
        boolean mirror = resolved[1] == 1;

        for (EntitySpec spec : c.entities) {
            entityFactory.createAndAddToWorld(spec, baseZ, shift, mirror, world);
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
