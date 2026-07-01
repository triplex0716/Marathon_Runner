package com.ycom.system;

import com.ycom.core.Config;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class ChunkLibrary {
    private ChunkLibrary() {}

    private static final EnumSet<Config.Difficulty> ALL_DIFF = EnumSet.allOf(Config.Difficulty.class);
    private static final EnumSet<Config.Difficulty> MED_HARD = EnumSet.of(Config.Difficulty.MEDIUM, Config.Difficulty.HARD);
    private static final EnumSet<Config.Difficulty> HARD_ONLY = EnumSet.of(Config.Difficulty.HARD);

    private static final double[] ARC_DZ = {-3.0, -2.25, -1.5, -0.75, 0.0, 0.75, 1.5, 2.25, 3.0};
    private static final double[] ARC_Y = {2.67, 2.86, 3.12, 3.27, 3.33, 3.27, 3.12, 2.86, 2.67};

    public static final Chunk MIXED_SAFE_ROW = new Chunk(
            "MIXED_SAFE_ROW",
            ALL_DIFF,
            6.0,
            50,
            0.0,
            List.of(
                    new EntitySpec(2.0, -1, EntityType.JUMP_OBSTACLE),
                    new EntitySpec(0.0, 1, EntityType.COIN),
                    new EntitySpec(2.0, 1, EntityType.COIN),
                    new EntitySpec(4.0, 1, EntityType.COIN)
            ),
            false, true, false
    );

    public static final Chunk COIN_HIGHWAY_3LANE = new Chunk(
            "COIN_HIGHWAY_3LANE",
            ALL_DIFF,
            18.0,
            25,
            0.0,
            buildCoinHighway(),
            false, false, false
    );

    public static final Chunk CONTAINER_CLIMB = new Chunk(
            "CONTAINER_CLIMB",
            ALL_DIFF,
            17.0,
            20,
            0.0,
            List.of(
                    new EntitySpec(7.5, 0, EntityType.CONTAINER_WITH_RAMP),
                    new EntitySpec(6.0, 0, 3.3, EntityType.COIN),
                    new EntitySpec(8.0, 0, 3.3, EntityType.COIN),
                    new EntitySpec(10.0, 0, 3.3, EntityType.COIN)
            ),
            true, true, false
    );

    public static final Chunk SLIDE_TUNNEL = new Chunk(
            "SLIDE_TUNNEL",
            MED_HARD,
            24.0,
            15,
            20.0,
            buildSlideTunnel(),
            true, true, false
    );

    public static final Chunk JUMP_STAIRS = new Chunk(
            "JUMP_STAIRS",
            MED_HARD,
            32.0,
            15,
            25.0,
            buildJumpStairs(),
            true, true, false
    );

    public static final Chunk MAGNET_BAIT = new Chunk(
            "MAGNET_BAIT",
            ALL_DIFF,
            22.0,
            15,
            0.0,
            buildMagnetBait(),
            false, false, false
    );

    public static final Chunk LANE_BLOCK_FORK = new Chunk(
            "LANE_BLOCK_FORK",
            MED_HARD,
            14.0,
            15,
            30.0,
            List.of(
                    new EntitySpec(4.0, 0, EntityType.LANE_BLOCK),
                    new EntitySpec(4.0, -1, EntityType.ENERGY_DRINK),
                    new EntitySpec(4.0, 1, EntityType.MAGNET),
                    new EntitySpec(8.0, -1, EntityType.COIN),
                    new EntitySpec(8.0, 1, EntityType.COIN)
            ),
            false, true, false
    );

    public static final Chunk OBSTACLE_GAUNTLET = new Chunk(
            "OBSTACLE_GAUNTLET",
            HARD_ONLY,
            22.0,
            10,
            70.0,
            List.of(
                    new EntitySpec(2.0, 0, EntityType.JUMP_OBSTACLE),
                    new EntitySpec(10.0, 0, EntityType.SLIDE_OBSTACLE),
                    new EntitySpec(18.0, 0, EntityType.JUMP_OBSTACLE)
            ),
            true, true, false
    );

    public static final Chunk SINGLE_LANE_COIN_STRING = new Chunk(
            "SINGLE_LANE_COIN_STRING",
            ALL_DIFF,
            10.0,
            25,
            0.0,
            List.of(
                    new EntitySpec(0.0, 0, EntityType.COIN),
                    new EntitySpec(2.0, 0, EntityType.COIN),
                    new EntitySpec(4.0, 0, EntityType.COIN),
                    new EntitySpec(6.0, 0, EntityType.COIN),
                    new EntitySpec(8.0, 0, EntityType.COIN)
            ),
            true, true, false
    );

    public static final Chunk COIN_CURVE = new Chunk(
            "COIN_CURVE",
            ALL_DIFF,
            14.0,
            20,
            0.0,
            List.of(
                    new EntitySpec(0.0, -1, EntityType.COIN),
                    new EntitySpec(2.0, -1, EntityType.COIN),
                    new EntitySpec(4.0, 0, EntityType.COIN),
                    new EntitySpec(6.0, 0, EntityType.COIN),
                    new EntitySpec(8.0, 0, EntityType.COIN),
                    new EntitySpec(10.0, 1, EntityType.COIN),
                    new EntitySpec(12.0, 1, EntityType.COIN)
            ),
            false, true, false
    );

    public static final Chunk JUMP_COIN_REWARD = new Chunk(
            "JUMP_COIN_REWARD",
            ALL_DIFF,
            8.0,
            20,
            0.0,
            List.of(
                    new EntitySpec(2.0, 0, EntityType.JUMP_OBSTACLE),
                    new EntitySpec(4.0, 0, EntityType.COIN),
                    new EntitySpec(5.5, 0, EntityType.COIN),
                    new EntitySpec(7.0, 0, EntityType.COIN)
            ),
            true, true, false
    );

    public static final Chunk SLIDE_COIN_REWARD = new Chunk(
            "SLIDE_COIN_REWARD",
            ALL_DIFF,
            8.0,
            20,
            0.0,
            List.of(
                    new EntitySpec(2.0, 0, EntityType.SLIDE_OBSTACLE),
                    new EntitySpec(4.0, 0, EntityType.COIN),
                    new EntitySpec(5.5, 0, EntityType.COIN),
                    new EntitySpec(7.0, 0, EntityType.COIN)
            ),
            true, true, false
    );

    public static final Chunk LANE_WEAVE = new Chunk(
            "LANE_WEAVE",
            MED_HARD,
            20.0,
            20,
            15.0,
            List.of(
                    new EntitySpec(2.0, -1, EntityType.JUMP_OBSTACLE),
                    new EntitySpec(10.0, 1, EntityType.JUMP_OBSTACLE),
                    new EntitySpec(18.0, 0, EntityType.JUMP_OBSTACLE),
                    new EntitySpec(2.0, 1, EntityType.COIN),
                    new EntitySpec(10.0, -1, EntityType.COIN),
                    new EntitySpec(18.0, 1, EntityType.COIN)
            ),
            false, true, false
    );

    public static final Chunk TRAIN_GAP = new Chunk(
            "TRAIN_GAP",
            MED_HARD,
            10.0,
            15,
            20.0,
            List.of(
                    new EntitySpec(2.0, -1, EntityType.LANE_BLOCK),
                    new EntitySpec(2.0, 1, EntityType.LANE_BLOCK),
                    new EntitySpec(2.0, 0, EntityType.COIN),
                    new EntitySpec(4.0, 0, EntityType.COIN),
                    new EntitySpec(6.0, 0, EntityType.COIN),
                    new EntitySpec(8.0, 0, EntityType.COIN)
            ),
            false, false, false
    );

    public static final Chunk ZIG_ZAG_OBSTACLES = new Chunk(
            "ZIG_ZAG_OBSTACLES",
            MED_HARD,
            20.0,
            15,
            25.0,
            List.of(
                    new EntitySpec(2.0, -1, EntityType.SLIDE_OBSTACLE),
                    new EntitySpec(10.0, 0, EntityType.JUMP_OBSTACLE),
                    new EntitySpec(18.0, 1, EntityType.SLIDE_OBSTACLE)
            ),
            false, true, false
    );

    public static final Chunk TRIPLE_JUMP_BARRIER = new Chunk(
            "TRIPLE_JUMP_BARRIER",
            MED_HARD,
            10.0,
            10,
            30.0,
            buildTripleJumpBarrier(),
            false, false, false
    );

    public static final Chunk TRIPLE_SLIDE_BARRIER = new Chunk(
            "TRIPLE_SLIDE_BARRIER",
            MED_HARD,
            8.0,
            10,
            30.0,
            buildTripleSlideBarrier(),
            false, false, false
    );

    public static final Chunk TRAIN_CENTER_HURDLES = new Chunk(
            "TRAIN_CENTER_HURDLES",
            MED_HARD,
            12.0,
            10,
            20.0,
            List.of(
                    new EntitySpec(4.0, 0, EntityType.LANE_BLOCK),
                    new EntitySpec(5.0, -1, EntityType.JUMP_OBSTACLE),
                    new EntitySpec(5.0, 1, EntityType.SLIDE_OBSTACLE)
            ),
            false, true, false
    );

    public static final Chunk TRAIN_CENTER_DOUBLE_JUMP = new Chunk(
            "TRAIN_CENTER_DOUBLE_JUMP",
            MED_HARD,
            12.0,
            10,
            20.0,
            List.of(
                    new EntitySpec(4.0, 0, EntityType.LANE_BLOCK),
                    new EntitySpec(5.0, -1, EntityType.JUMP_OBSTACLE),
                    new EntitySpec(5.0, 1, EntityType.JUMP_OBSTACLE)
            ),
            false, true, false
    );

    public static final Chunk TRAIN_CENTER_DOUBLE_SLIDE = new Chunk(
            "TRAIN_CENTER_DOUBLE_SLIDE",
            MED_HARD,
            12.0,
            10,
            20.0,
            List.of(
                    new EntitySpec(4.0, 0, EntityType.LANE_BLOCK),
                    new EntitySpec(5.0, -1, EntityType.SLIDE_OBSTACLE),
                    new EntitySpec(5.0, 1, EntityType.SLIDE_OBSTACLE)
            ),
            false, true, false
    );

    public static final Chunk ENERGY_DRINK_PICKUP = new Chunk(
            "ENERGY_DRINK_PICKUP",
            ALL_DIFF,
            6.0,
            30,
            0.0,
            List.of(new EntitySpec(3.0, 0, EntityType.ENERGY_DRINK)),
            true, true, true
    );

    public static final Chunk MAGNET_PICKUP = new Chunk(
            "MAGNET_PICKUP",
            ALL_DIFF,
            6.0,
            30,
            0.0,
            List.of(new EntitySpec(3.0, 0, EntityType.MAGNET)),
            true, true, true
    );

    public static final Chunk TREADMILL_PICKUP = new Chunk(
            "TREADMILL_PICKUP",
            ALL_DIFF,
            6.0,
            30,
            0.0,
            List.of(new EntitySpec(3.0, 0, EntityType.TREADMILL)),
            true, true, true
    );

    public static final Chunk REVIVAL_PICKUP = new Chunk(
            "REVIVAL_PICKUP",
            ALL_DIFF,
            6.0,
            15,
            0.0,
            List.of(new EntitySpec(3.0, 0, EntityType.REVIVAL_CAPSULE)),
            true, true, true
    );

    public static final Chunk RANDOM_BOX_PICKUP = new Chunk(
            "RANDOM_BOX_PICKUP",
            ALL_DIFF,
            6.0,
            20,
            0.0,
            List.of(new EntitySpec(3.0, 0, EntityType.RANDOM_ITEM)),
            true, true, true
    );

    public static final List<Chunk> ALL = List.of(
            MIXED_SAFE_ROW,
            COIN_HIGHWAY_3LANE,
            CONTAINER_CLIMB,
            SLIDE_TUNNEL,
            JUMP_STAIRS,
            MAGNET_BAIT,
            LANE_BLOCK_FORK,
            OBSTACLE_GAUNTLET,
            SINGLE_LANE_COIN_STRING,
            COIN_CURVE,
            JUMP_COIN_REWARD,
            SLIDE_COIN_REWARD,
            LANE_WEAVE,
            TRAIN_GAP,
            ZIG_ZAG_OBSTACLES,
            TRIPLE_JUMP_BARRIER,
            TRIPLE_SLIDE_BARRIER,
            TRAIN_CENTER_HURDLES,
            TRAIN_CENTER_DOUBLE_JUMP,
            TRAIN_CENTER_DOUBLE_SLIDE,
            ENERGY_DRINK_PICKUP,
            MAGNET_PICKUP,
            TREADMILL_PICKUP,
            REVIVAL_PICKUP,
            RANDOM_BOX_PICKUP
    );

    private static List<EntitySpec> buildCoinHighway() {
        List<EntitySpec> list = new ArrayList<>();
        for (int lane = -1; lane <= 1; lane++) {
            for (int i = 0; i < 8; i++) {
                list.add(new EntitySpec(i * 2.0, lane, EntityType.COIN));
            }
        }
        return list;
    }

    private static void addJumpArc(List<EntitySpec> list, double obstacleZ, int lane) {
        for (int i = 0; i < ARC_DZ.length; i++) {
            list.add(new EntitySpec(obstacleZ + ARC_DZ[i], lane, ARC_Y[i], EntityType.COIN));
        }
    }

    private static void addSlideLine(List<EntitySpec> list, double obstacleZ, int lane) {
        for (double dz = -3.0; dz <= 3.0; dz += 1.0) {
            list.add(new EntitySpec(obstacleZ + dz, lane, 0.4, EntityType.COIN));
        }
    }

    private static List<EntitySpec> buildJumpStairs() {
        List<EntitySpec> list = new ArrayList<>();
        for (double z : new double[]{2.0, 10.0, 18.0, 26.0}) {
            list.add(new EntitySpec(z, 0, EntityType.JUMP_OBSTACLE));
            addJumpArc(list, z, 0);
        }
        return list;
    }

    private static List<EntitySpec> buildSlideTunnel() {
        List<EntitySpec> list = new ArrayList<>();
        for (double z : new double[]{2.0, 10.0, 18.0}) {
            list.add(new EntitySpec(z, 0, EntityType.SLIDE_OBSTACLE));
            addSlideLine(list, z, 0);
        }
        return list;
    }

    private static List<EntitySpec> buildTripleJumpBarrier() {
        List<EntitySpec> list = new ArrayList<>();
        for (int lane = -1; lane <= 1; lane++) {
            list.add(new EntitySpec(4.0, lane, EntityType.JUMP_OBSTACLE));
            addJumpArc(list, 4.0, lane);
        }
        return list;
    }

    private static List<EntitySpec> buildTripleSlideBarrier() {
        List<EntitySpec> list = new ArrayList<>();
        for (int lane = -1; lane <= 1; lane++) {
            list.add(new EntitySpec(4.0, lane, EntityType.SLIDE_OBSTACLE));
            addSlideLine(list, 4.0, lane);
        }
        return list;
    }

    private static List<EntitySpec> buildMagnetBait() {
        List<EntitySpec> list = new ArrayList<>();
        list.add(new EntitySpec(2.0, 0, EntityType.MAGNET));
        for (int lane = -1; lane <= 1; lane++) {
            for (int i = 0; i < 8; i++) {
                list.add(new EntitySpec(6.0 + i * 2.0, lane, EntityType.COIN));
            }
        }
        return list;
    }
}
