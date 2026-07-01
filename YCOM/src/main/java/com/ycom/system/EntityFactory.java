package com.ycom.system;

import com.ycom.core.Config;
import com.ycom.entity.Coin;
import com.ycom.entity.EnergyDrink;
import com.ycom.entity.GameObject;
import com.ycom.entity.Magnet;
import com.ycom.entity.Obstacle;
import com.ycom.entity.RandomItem;
import com.ycom.entity.RevivalCapsule;
import com.ycom.entity.Treadmill;
import com.ycom.world.GameWorld;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class EntityFactory {
    
    public void createAndAddToWorld(EntitySpec spec, double baseZ, int laneShift, boolean mirror, GameWorld world) {
        int lane = spec.lane;
        if (mirror) lane = -lane;
        lane += laneShift;
        double x = lane * Config.LANE_WIDTH;
        double z = baseZ + spec.relZ;
        
        switch (spec.type) {
            case JUMP_OBSTACLE -> world.addObject(new Obstacle(
                    x, 0.0, z,
                    Config.JUMP_OBSTACLE_WIDTH, Config.JUMP_OBSTACLE_HEIGHT, Config.JUMP_OBSTACLE_DEPTH,
                    Color.ORANGE, Obstacle.AvoidMethod.JUMP));
            case SLIDE_OBSTACLE -> world.addObject(new Obstacle(
                    x, Config.SLIDE_OBSTACLE_Y, z,
                    Config.SLIDE_OBSTACLE_WIDTH, Config.SLIDE_OBSTACLE_HEIGHT, Config.SLIDE_OBSTACLE_DEPTH,
                    Color.CRIMSON, Obstacle.AvoidMethod.SLIDE));
            case LANE_BLOCK -> world.addObject(new Obstacle(
                    x, 0.0, z,
                    Config.LANE_BLOCK_WIDTH, Config.LANE_BLOCK_HEIGHT, Config.LANE_BLOCK_DEPTH,
                    Color.DARKRED, Obstacle.AvoidMethod.CHANGE_LANE));
            case CONTAINER_WITH_RAMP -> {
                world.addObject(new Obstacle(
                        x, 0.0, z,
                        Config.LANE_BLOCK_WIDTH, Config.LANE_BLOCK_HEIGHT, Config.LANE_BLOCK_DEPTH,
                        Color.DARKBLUE, Obstacle.AvoidMethod.CONTAINER));
                world.addObject(new Obstacle(
                        x, 0.0, z - Config.RAMP_Z_OFFSET,
                        Config.RAMP_WIDTH, Config.RAMP_HEIGHT, Config.RAMP_DEPTH,
                        Color.GRAY, Obstacle.AvoidMethod.RAMP));
            }
            case COIN -> {
                double y = spec.y > 0.0 ? spec.y : Config.DEFAULT_COIN_Y;
                world.addObject(new Coin(x, y, z));
            }
            case MAGNET -> world.addObject(new Magnet(x, Config.DEFAULT_MAGNET_Y, z));
            case ENERGY_DRINK -> world.addObject(new EnergyDrink(x, Config.DEFAULT_ENERGY_DRINK_Y, z));
            case TREADMILL -> world.addObject(new Treadmill(x, Config.DEFAULT_TREADMILL_Y, z));
            case REVIVAL_CAPSULE -> world.addObject(new RevivalCapsule(x, Config.DEFAULT_REVIVAL_CAPSULE_Y, z));
            case RANDOM_ITEM -> world.addObject(new RandomItem(x, Config.DEFAULT_RANDOM_ITEM_Y, z));
        }
    }
}
