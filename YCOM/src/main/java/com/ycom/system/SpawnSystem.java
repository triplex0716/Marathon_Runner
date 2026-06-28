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
import java.util.Random;

public class SpawnSystem {
    private final GameWorld world;
    private final Config.Difficulty difficulty;
    private final Random rand = new Random();
    private double nextSpawnZ = 50.0;

    public SpawnSystem(GameWorld world, Config.Difficulty difficulty) {
        this.world = world;
        this.difficulty = difficulty == null ? Config.DEFAULT_DIFFICULTY : difficulty;
    }

    public void update(double playerZ) {
        while (playerZ + 230.0 > nextSpawnZ) {
            spawnRow(nextSpawnZ);
            double density = Math.min(1.0, (TimeManager.getElapsedTime() / 120.0));
            nextSpawnZ += Math.max(7.5, difficulty.spawnBaseGap - density * 3.0 + rand.nextDouble() * difficulty.spawnRandomGap);
        }
    }

    private void spawnRow(double z) {
        int lane = rand.nextInt(3) - 1;
        double x = lane * Config.LANE_WIDTH;

        double roll = rand.nextDouble();
        double elapsed = TimeManager.getElapsedTime();

        double laneBlockChance = elapsed >= difficulty.laneBlockUnlockSeconds ? difficulty.laneBlockObstacleChance : 0.0;
        double jumpThreshold = difficulty.jumpObstacleChance;
        double slideThreshold = jumpThreshold + difficulty.slideObstacleChance;
        double blockThreshold = slideThreshold + laneBlockChance;
        double magnetThreshold = blockThreshold + difficulty.magnetChance;
        double energyThreshold = magnetThreshold + difficulty.energyDrinkChance;
        double treadmillThreshold = energyThreshold + difficulty.treadmillChance;
        double revivalThreshold = treadmillThreshold + difficulty.revivalChance;
        double randomItemThreshold = revivalThreshold + difficulty.randomItemChance;

        if (roll < jumpThreshold) {
            world.addObject(new Obstacle(x, 0.0, z, 2.1, 1.0, 1.2, Color.ORANGE, Obstacle.AvoidMethod.JUMP));
        } else if (roll < slideThreshold) {
            world.addObject(new Obstacle(x, 1.25, z, 2.2, 1.0, 1.1, Color.CRIMSON, Obstacle.AvoidMethod.SLIDE));
        } else if (roll < blockThreshold) {
            if (rand.nextBoolean()) {
                world.addObject(new Obstacle(x, 0.0, z, 2.4, 3.0, 7.0, Color.DARKRED, Obstacle.AvoidMethod.CHANGE_LANE));
            } else {
                world.addObject(new Obstacle(x, 0.0, z, 2.4, 3.0, 7.0, Color.DARKBLUE, Obstacle.AvoidMethod.CONTAINER));
                world.addObject(new Obstacle(x, 0.0, z - 5.5, 2.4, 3.0, 4.0, Color.GRAY, Obstacle.AvoidMethod.RAMP));
            }
        } else if (roll < magnetThreshold) {
            world.addObject(new Magnet(x, 0.6, z));
        } else if (roll < energyThreshold) {
            world.addObject(new EnergyDrink(x, 0.4, z));
        } else if (roll < treadmillThreshold) {
            world.addObject(new Treadmill(x, 0.45, z));
        } else if (roll < revivalThreshold) {
            world.addObject(new RevivalCapsule(x, 0.55, z));
        } else if (roll < randomItemThreshold) {
            world.addObject(new RandomItem(x, 0.5, z));
        } else {
            spawnCoinLine(lane, z);
        }

        if (elapsed > 70.0 && rand.nextDouble() < difficulty.lateExtraObstacleChance) {
            int secondLane = pickDifferentLane(lane);
            double secondX = secondLane * Config.LANE_WIDTH;
            world.addObject(new Obstacle(secondX, 0.0, z + 2.5, 2.1, 1.0, 1.2, Color.DARKORANGE, Obstacle.AvoidMethod.JUMP));
        }
    }

    private void spawnCoinLine(int lane, double z) {
        double x = lane * Config.LANE_WIDTH;
        for (int i = 0; i < 5; i++) {
            world.addObject(new Coin(x, 0.65, z + i * 2.0));
        }
    }

    private int pickDifferentLane(int lane) {
        int secondLane;
        do {
            secondLane = rand.nextInt(3) - 1;
        } while (secondLane == lane);
        return secondLane;
    }
}
