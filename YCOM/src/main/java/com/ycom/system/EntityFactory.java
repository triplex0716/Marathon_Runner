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
