package com.ycom.system;

import com.ycom.core.Config;
import com.ycom.core.TimeManager;
import com.ycom.entity.*;
import com.ycom.world.GameWorld;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnSystem {
    private final GameWorld world;
    private final Config.Difficulty difficulty;
    private final Random rand = new Random();
    private double nextSpawnZ = 50.0;

    private static class VirtualEntity {
        enum Type { COIN, OBSTACLE, TRAIN, MAGNET, ENERGY_DRINK, REVIVAL_CAPSULE, TREADMILL, RANDOM_ITEM }
        Type type;
        double x, y, z;
        double width, height, depth;
        Color color;
        Obstacle.AvoidMethod avoidMethod;
        double speed;
        boolean hasRamp;

        VirtualEntity(Type type, double x, double y, double z, double width, double height, double depth) {
            this.type = type;
            this.x = x; this.y = y; this.z = z;
            this.width = width; this.height = height; this.depth = depth;
        }
    }

    public SpawnSystem(GameWorld world, Config.Difficulty difficulty) {
        this.world = world;
        this.difficulty = difficulty == null ? Config.DEFAULT_DIFFICULTY : difficulty;
    }

    public void update(double playerZ) {
        while (playerZ + 240.0 > nextSpawnZ) {
            double length = spawnNextChunk(nextSpawnZ);
            nextSpawnZ += length;
        }
    }

    private double spawnNextChunk(double startZ) {
        double currentSpeed = Config.BASE_SPEED * TimeManager.getWorldRate();
        double chunkLength = 70.0; // Default chunk length: 70 meters

        List<VirtualEntity> virtuals = new ArrayList<>();

        double elapsed = TimeManager.getElapsedTime();
        int choice = choosePrefabByDDA(elapsed);
        
        switch (choice) {
            case 0 -> buildStraightCoins(virtuals, startZ, chunkLength);
            case 1 -> buildJumpOrSlideHurdles(virtuals, startZ, chunkLength, elapsed > 300.0);
            case 2 -> buildChokePointWarning(virtuals, startZ, chunkLength);
            case 3 -> buildTrainCombo(virtuals, startZ, chunkLength, elapsed > 60.0);
            case 4 -> buildMidAirLaneChange(virtuals, startZ, chunkLength);
            case 5 -> buildConsecutiveTrainRooftops(virtuals, startZ, chunkLength);
            default -> buildTunnelBlindSpot(virtuals, startZ, chunkLength);
        }

        adjustObstacleDistances(virtuals, currentSpeed);

        for (VirtualEntity ve : virtuals) {
            if (ve.z + ve.depth / 2.0 + 10.0 - startZ > chunkLength) {
                chunkLength = ve.z + ve.depth / 2.0 + 10.0 - startZ;
            }
        }

        for (VirtualEntity ve : virtuals) {
            switch (ve.type) {
                case COIN -> world.addObject(new Coin(ve.x, ve.y, ve.z));
                case OBSTACLE -> world.addObject(new Obstacle(ve.x, ve.y, ve.z, ve.width, ve.height, ve.depth, ve.color, ve.avoidMethod));
                case TRAIN -> {
                    // Container (Train body)
                    world.addObject(new Obstacle(ve.x, 0.0, ve.z, 2.4, 3.0, 7.0, ve.color, Obstacle.AvoidMethod.CONTAINER));
                    if (ve.hasRamp) {
                        // Ramp in front
                        world.addObject(new Obstacle(ve.x, 0.0, ve.z - 5.5, 2.4, 3.0, 4.0, Color.GRAY, Obstacle.AvoidMethod.RAMP));
                    }
                }
                case MAGNET -> world.addObject(new Magnet(ve.x, ve.y, ve.z));
                case ENERGY_DRINK -> world.addObject(new EnergyDrink(ve.x, ve.y, ve.z));
                case REVIVAL_CAPSULE -> world.addObject(new RevivalCapsule(ve.x, ve.y, ve.z));
                case TREADMILL -> world.addObject(new Treadmill(ve.x, ve.y, ve.z));
                case RANDOM_ITEM -> world.addObject(new RandomItem(ve.x, ve.y, ve.z));
            }
        }

        return chunkLength;
    }

    private int choosePrefabByDDA(double elapsedSeconds) {
        double r = rand.nextDouble();
        if (elapsedSeconds < 60.0) {
            if (r < 0.20) return 0;
            if (r < 0.50) return 1;
            if (r < 0.60) return 2;
            if (r < 0.70) return 3;
            return 5;
        } else if (elapsedSeconds < 300.0) {
            if (r < 0.05) return 0;
            if (r < 0.20) return 1;
            if (r < 0.30) return 2;
            if (r < 0.45) return 3;
            if (r < 0.55) return 4;
            if (r < 0.90) return 5;
            return 6;
        } else {
            if (r < 0.05) return 0;
            if (r < 0.20) return 1;
            if (r < 0.35) return 3;
            if (r < 0.45) return 4;
            if (r < 0.80) return 5;
            if (r < 0.90) return 2;
            return 6;
        }
    }

    private void buildStraightCoins(List<VirtualEntity> virtuals, double startZ, double length) {
        int lane = rand.nextInt(3) - 1;
        double x = lane * Config.LANE_WIDTH;
        for (double z = startZ + 5.0; z < startZ + length - 5.0; z += 3.0) {
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, x, 0.65, z, 0.7, 0.7, 0.7));
        }
    }

    private void buildJumpOrSlideHurdles(List<VirtualEntity> virtuals, double startZ, double length, boolean highSpeed) {
        int lane = rand.nextInt(3) - 1;
        double x = lane * Config.LANE_WIDTH;

        double step = highSpeed ? 15.0 : 25.0;
        int index = 0;
        for (double z = startZ + 10.0; z < startZ + length - 10.0; z += step) {
            boolean isJump = (index % 2 == 0);
            if (isJump) {
                virtuals.add(createObstacle(x, 0.0, z, 2.1, 0.9, 1.2, Color.BROWN, Obstacle.AvoidMethod.JUMP));
                for (int i = 0; i < 5; i++) {
                    double cz = z - 4.0 + i * 2.0;
                    double cy = 0.65 + Math.sin((i / 4.0) * Math.PI) * 1.8;
                    virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, x, cy, cz, 0.7, 0.7, 0.7));
                }
            } else {
                virtuals.add(createObstacle(x, 1.25, z, 2.2, 1.0, 1.1, Color.SILVER, Obstacle.AvoidMethod.SLIDE));
                for (int i = 0; i < 5; i++) {
                    double cz = z - 4.0 + i * 2.0;
                    if (Math.abs(cz - z) > 0.5) {
                        virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, x, 0.65, cz, 0.7, 0.7, 0.7));
                    }
                }
            }
            index++;
        }
    }

    private void buildChokePointWarning(List<VirtualEntity> virtuals, double startZ, double length) {
        int openLane = rand.nextInt(3) - 1;
        double openX = openLane * Config.LANE_WIDTH;
        double obstacleZ = startZ + 20.0;

        for (int l = -1; l <= 1; l++) {
            if (l != openLane) {
                double x = l * Config.LANE_WIDTH;
                virtuals.add(createObstacle(x, 0.0, obstacleZ, 2.4, 3.0, 1.2, Color.YELLOW, Obstacle.AvoidMethod.CHANGE_LANE));
            }
        }

        for (double z = startZ + 5.0; z < startZ + length - 5.0; z += 3.0) {
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, openX, 0.65, z, 0.7, 0.7, 0.7));
        }

        int riskLane = openLane == 1 ? 0 : openLane + 1;
        double riskX = riskLane * Config.LANE_WIDTH;
        
        double roll = rand.nextDouble();
        if (roll < 0.3) {
            // Most dangerous item placement (Pill in the blocked lane)
            virtuals.add(new VirtualEntity(VirtualEntity.Type.REVIVAL_CAPSULE, riskX, 0.6, obstacleZ - 10.0, 1.0, 1.0, 1.0));
        } else if (roll < 0.7) {
            VirtualEntity.Type item = rand.nextBoolean() ? VirtualEntity.Type.TREADMILL : VirtualEntity.Type.RANDOM_ITEM;
            virtuals.add(new VirtualEntity(item, riskX, 0.6, obstacleZ - 10.0, 1.0, 1.0, 1.0));
        } else {
            VirtualEntity.Type item = rand.nextBoolean() ? VirtualEntity.Type.MAGNET : VirtualEntity.Type.ENERGY_DRINK;
            virtuals.add(new VirtualEntity(item, riskX, 0.6, obstacleZ - 10.0, 1.0, 1.0, 1.0));
        }
    }

    private void buildTrainCombo(List<VirtualEntity> virtuals, double startZ, double length, boolean hasDynamic) {
        double trainX = 0.0;
        double tZ1 = startZ + 30.0;
        virtuals.add(createTrain(trainX, tZ1, 0.0, true, Color.SLATEGRAY));

        VirtualEntity.Type trainItem = rand.nextDouble() < 0.5 ? VirtualEntity.Type.TREADMILL : (rand.nextBoolean() ? VirtualEntity.Type.MAGNET : VirtualEntity.Type.ENERGY_DRINK);
        virtuals.add(new VirtualEntity(
            trainItem,
            trainX, 3.4, tZ1 + 5.0, 1.0, 1.0, 1.0
        ));

        for (int i = 0; i < 5; i++) {
            double cz = tZ1 - 5.0 + i * 3.0;
            double cy = 0.65 + (i / 4.0) * 2.8;
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, trainX, cy, cz, 0.7, 0.7, 0.7));
        }

        double sideX1 = -Config.LANE_WIDTH;
        double sideX2 = Config.LANE_WIDTH;

        if (hasDynamic && rand.nextDouble() < 0.7) {
            double movingTrainX = rand.nextBoolean() ? sideX1 : sideX2;
            virtuals.add(createTrain(movingTrainX, startZ + 55.0, 15.0, false, Color.DARKBLUE));
            
            double safeSideX = (movingTrainX == sideX1) ? sideX2 : sideX1;
            for (double cz = startZ + 15.0; cz < startZ + length - 5.0; cz += 3.0) {
                virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, safeSideX, 0.65, cz, 0.7, 0.7, 0.7));
            }
        } else {
            virtuals.add(createObstacle(sideX1, 0.0, startZ + 20.0, 2.1, 0.9, 1.2, Color.BROWN, Obstacle.AvoidMethod.JUMP));
            virtuals.add(createObstacle(sideX2, 1.25, startZ + 25.0, 2.2, 1.0, 1.1, Color.SILVER, Obstacle.AvoidMethod.SLIDE));
        }
    }

    private void buildMidAirLaneChange(List<VirtualEntity> virtuals, double startZ, double length) {
        double leftX = -Config.LANE_WIDTH;
        virtuals.add(createTrain(leftX, startZ + 15.0, 0.0, true, Color.SLATEGRAY));

        double centerX = 0.0;
        virtuals.add(createObstacle(centerX, 0.0, startZ + 16.0, 2.4, 3.0, 1.2, Color.YELLOW, Obstacle.AvoidMethod.CHANGE_LANE));

        virtuals.add(createTrain(centerX, startZ + 35.0, 0.0, false, Color.SLATEGRAY));

        for (int i = 0; i < 3; i++) {
            double cy = 0.65 + (i / 2.0) * 2.8;
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, leftX, cy, startZ + i * 3.0, 0.7, 0.7, 0.7));
        }
        virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, leftX, 3.45, startZ + 9.0, 0.7, 0.7, 0.7));
        
        for (int i = 0; i < 3; i++) {
            double progress = (i + 1) / 4.0;
            double coinX = leftX + progress * Config.LANE_WIDTH;
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, coinX, 3.8, startZ + 14.0 + i * 3.0, 0.7, 0.7, 0.7));
        }

        // Place a pill (Revival Capsule) at the peak of the mid-air lane change jump (extreme risk!)
        if (rand.nextDouble() < 0.5) {
            virtuals.add(new VirtualEntity(VirtualEntity.Type.REVIVAL_CAPSULE, leftX + 0.5 * Config.LANE_WIDTH, 4.2, startZ + 18.0, 1.0, 1.0, 1.0));
        }

        for (int i = 0; i < 4; i++) {
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, centerX, 3.45, startZ + 25.0 + i * 3.0, 0.7, 0.7, 0.7));
        }
    }

    private void buildConsecutiveTrainRooftops(List<VirtualEntity> virtuals, double startZ, double length) {
        double z1 = startZ + 10.0;
        virtuals.add(createTrain(-Config.LANE_WIDTH, z1, 0.0, true, Color.SLATEGRAY));

        double z2 = startZ + 25.0;
        virtuals.add(createTrain(0.0, z2, 0.0, false, Color.DARKBLUE));

        double z3 = startZ + 40.0;
        virtuals.add(createTrain(Config.LANE_WIDTH, z3, 0.0, false, Color.SLATEGRAY));

        double z4 = startZ + 55.0;
        virtuals.add(createTrain(0.0, z4, 0.0, false, Color.DARKBLUE));

        for (int i = 0; i < 4; i++) {
            double cz = z1 - 10.0 + i * 3.0; 
            double cy = 0.65 + (i / 3.0) * 2.8;
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, -Config.LANE_WIDTH, cy, cz, 0.7, 0.7, 0.7));
        }
        virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, -Config.LANE_WIDTH, 3.45, z1, 0.7, 0.7, 0.7));

        for (int i = 0; i < 3; i++) {
            double progress = (i + 1) / 4.0;
            double coinX = -Config.LANE_WIDTH + progress * Config.LANE_WIDTH;
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, coinX, 3.45, z1 + 5.0 + i * 3.0, 0.7, 0.7, 0.7));
        }

        virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, 0.0, 3.45, z2, 0.7, 0.7, 0.7));

        for (int i = 0; i < 3; i++) {
            double progress = (i + 1) / 4.0;
            double coinX = progress * Config.LANE_WIDTH;
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, coinX, 3.45, z2 + 5.0 + i * 3.0, 0.7, 0.7, 0.7));
        }

        virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, Config.LANE_WIDTH, 3.45, z3, 0.7, 0.7, 0.7));

        for (int i = 0; i < 3; i++) {
            double progress = (i + 1) / 4.0;
            double coinX = Config.LANE_WIDTH - progress * Config.LANE_WIDTH;
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, coinX, 3.45, z3 + 5.0 + i * 3.0, 0.7, 0.7, 0.7));
        }

        virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, 0.0, 3.45, z4, 0.7, 0.7, 0.7));
        virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, 0.0, 3.45, z4 + 5.0, 0.7, 0.7, 0.7));

        for (double cz = startZ + 3.0; cz < startZ + 25.0; cz += 4.0) {
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, Config.LANE_WIDTH, 0.65, cz, 0.7, 0.7, 0.7));
        }
        for (int i = 0; i < 3; i++) {
            double progress = (i + 1) / 4.0;
            double coinX = Config.LANE_WIDTH - progress * 2.0 * Config.LANE_WIDTH;
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, coinX, 0.65, startZ + 24.0 + i * 3.0, 0.7, 0.7, 0.7));
        }
        for (double cz = startZ + 33.0; cz < startZ + length - 5.0; cz += 4.0) {
            virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, -Config.LANE_WIDTH, 0.65, cz, 0.7, 0.7, 0.7));
        }

        if (rand.nextDouble() < 0.6) {
            VirtualEntity.Type topItem = rand.nextDouble() < 0.4 ? VirtualEntity.Type.RANDOM_ITEM : (rand.nextBoolean() ? VirtualEntity.Type.TREADMILL : VirtualEntity.Type.MAGNET);
            virtuals.add(new VirtualEntity(
                topItem,
                0.0, 3.4, z4 + 8.0, 1.0, 1.0, 1.0
            ));
        }
    }

    private void buildTunnelBlindSpot(List<VirtualEntity> virtuals, double startZ, double length) {
        for (int i = 0; i < 4; i++) {
            double az = startZ + 10.0 + i * 6.0;
            virtuals.add(createObstacle(0.0, 2.5, az, 12.0, 1.2, 0.5, Color.DARKGRAY, Obstacle.AvoidMethod.SLIDE));
        }

        int blindLane = rand.nextInt(3) - 1;
        double bx = blindLane * Config.LANE_WIDTH;
        boolean lowHurdle = rand.nextBoolean();
        if (lowHurdle) {
            virtuals.add(createObstacle(bx, 0.0, startZ + 35.0, 2.1, 0.9, 1.2, Color.BROWN, Obstacle.AvoidMethod.JUMP));
        } else {
            virtuals.add(createObstacle(bx, 1.25, startZ + 35.0, 2.2, 1.0, 1.1, Color.SILVER, Obstacle.AvoidMethod.SLIDE));
        }

        for (double z = startZ + 5.0; z < startZ + length - 5.0; z += 3.0) {
            if (z < startZ + 32.0) {
                virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, 0.0, 0.65, z, 0.7, 0.7, 0.7));
            } else {
                int safeLane = (blindLane == 1) ? 0 : blindLane + 1;
                virtuals.add(new VirtualEntity(VirtualEntity.Type.COIN, safeLane * Config.LANE_WIDTH, 0.65, z, 0.7, 0.7, 0.7));
            }
        }

        // Place a pill directly behind the blind spot obstacle (high risk of hitting the obstacle while grabbing it)
        if (rand.nextDouble() < 0.4) {
            virtuals.add(new VirtualEntity(VirtualEntity.Type.REVIVAL_CAPSULE, bx, lowHurdle ? 2.5 : 0.6, startZ + 43.0, 1.0, 1.0, 1.0));
        }
    }

    private VirtualEntity createObstacle(double x, double y, double z, double w, double h, double d, Color color, Obstacle.AvoidMethod avoid) {
        VirtualEntity ve = new VirtualEntity(VirtualEntity.Type.OBSTACLE, x, y, z, w, h, d);
        ve.color = color;
        ve.avoidMethod = avoid;
        return ve;
    }

    private VirtualEntity createTrain(double x, double z, double speed, boolean hasRamp, Color color) {
        VirtualEntity ve = new VirtualEntity(VirtualEntity.Type.TRAIN, x, 0.0, z, 2.2, 2.8, 20.0);
        ve.speed = speed;
        ve.hasRamp = hasRamp;
        ve.color = color;
        return ve;
    }

    private void adjustObstacleDistances(List<VirtualEntity> entities, double speed) {
        if (entities.isEmpty()) return;
        entities.sort((a, b) -> Double.compare(a.z, b.z));

        double tJump = 0.8;
        double tSlide = 0.75;
        double tLane = 0.3;

        double lastActionZ = -999.0;
        double lastActionDuration = 0.0;

        for (int i = 0; i < entities.size(); i++) {
            VirtualEntity ve = entities.get(i);
            if (ve.type != VirtualEntity.Type.OBSTACLE && ve.type != VirtualEntity.Type.TRAIN) {
                continue;
            }

            double currentDuration = 0.0;
            if (ve.type == VirtualEntity.Type.TRAIN) {
                currentDuration = tLane;
            } else {
                if (ve.avoidMethod == Obstacle.AvoidMethod.JUMP) {
                    currentDuration = tJump;
                } else if (ve.avoidMethod == Obstacle.AvoidMethod.SLIDE) {
                    currentDuration = tSlide;
                } else {
                    currentDuration = tLane;
                }
            }

            if (lastActionZ > -900.0) {
                double requiredDistance = speed * lastActionDuration;
                double actualDistance = ve.z - lastActionZ;
                if (actualDistance < requiredDistance) {
                    double shift = requiredDistance - actualDistance;
                    for (int j = i; j < entities.size(); j++) {
                        entities.get(j).z += shift;
                    }
                }
            }
            lastActionZ = ve.z;
            lastActionDuration = currentDuration;
        }
    }
}
