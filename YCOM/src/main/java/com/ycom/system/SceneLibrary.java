package com.ycom.system;

import com.ycom.core.Config;
import java.util.EnumSet;
import java.util.List;

public final class SceneLibrary {
    private SceneLibrary() {}

    private static final EnumSet<Config.Difficulty> ALL_DIFF = EnumSet.allOf(Config.Difficulty.class);
    private static final EnumSet<Config.Difficulty> MED_HARD = EnumSet.of(Config.Difficulty.MEDIUM, Config.Difficulty.HARD);
    private static final EnumSet<Config.Difficulty> HARD_ONLY = EnumSet.of(Config.Difficulty.HARD);

    public static final Scene TUTORIAL_START = new Scene(
            "TUTORIAL_START",
            ALL_DIFF,
            0.0,
            List.of(
                    ChunkLibrary.JUMP_COIN_REWARD,
                    ChunkLibrary.SLIDE_COIN_REWARD,
                    ChunkLibrary.MIXED_SAFE_ROW
            )
    );

    public static final Scene URBAN_RUN = new Scene(
            "URBAN_RUN",
            ALL_DIFF,
            0.0,
            List.of(
                    ChunkLibrary.SINGLE_LANE_COIN_STRING,
                    ChunkLibrary.JUMP_COIN_REWARD,
                    ChunkLibrary.SLIDE_COIN_REWARD
            )
    );

    public static final Scene CITY_BLOCK = new Scene(
            "CITY_BLOCK",
            ALL_DIFF,
            0.0,
            List.of(
                    ChunkLibrary.SINGLE_LANE_COIN_STRING,
                    ChunkLibrary.MIXED_SAFE_ROW,
                    ChunkLibrary.JUMP_COIN_REWARD
            )
    );

    public static final Scene CONTAINER_YARD = new Scene(
            "CONTAINER_YARD",
            ALL_DIFF,
            0.0,
            List.of(
                    ChunkLibrary.CONTAINER_CLIMB,
                    ChunkLibrary.CONTAINER_CLIMB,
                    ChunkLibrary.MIXED_SAFE_ROW
            )
    );

    public static final Scene LANE_DANCE = new Scene(
            "LANE_DANCE",
            MED_HARD,
            15.0,
            List.of(
                    ChunkLibrary.LANE_WEAVE,
                    ChunkLibrary.TRAIN_GAP,
                    ChunkLibrary.COIN_CURVE
            )
    );

    public static final Scene ZIGZAG_RUN = new Scene(
            "ZIGZAG_RUN",
            MED_HARD,
            20.0,
            List.of(
                    ChunkLibrary.ZIG_ZAG_OBSTACLES,
                    ChunkLibrary.LANE_WEAVE,
                    ChunkLibrary.ZIG_ZAG_OBSTACLES
            )
    );

    public static final Scene TRAIN_YARD = new Scene(
            "TRAIN_YARD",
            MED_HARD,
            20.0,
            List.of(
                    ChunkLibrary.TRAIN_CENTER_HURDLES,
                    ChunkLibrary.TRAIN_CENTER_DOUBLE_JUMP,
                    ChunkLibrary.TRAIN_CENTER_DOUBLE_SLIDE
            )
    );

    public static final Scene OBSTACLE_RUSH = new Scene(
            "OBSTACLE_RUSH",
            MED_HARD,
            30.0,
            List.of(
                    ChunkLibrary.JUMP_STAIRS,
                    ChunkLibrary.SLIDE_TUNNEL,
                    ChunkLibrary.JUMP_STAIRS
            )
    );

    public static final Scene FORCED_ACTION = new Scene(
            "FORCED_ACTION",
            MED_HARD,
            30.0,
            List.of(
                    ChunkLibrary.TRIPLE_JUMP_BARRIER,
                    ChunkLibrary.SINGLE_LANE_COIN_STRING,
                    ChunkLibrary.TRIPLE_SLIDE_BARRIER
            )
    );

    public static final Scene MIXED_CHALLENGE = new Scene(
            "MIXED_CHALLENGE",
            MED_HARD,
            40.0,
            List.of(
                    ChunkLibrary.JUMP_COIN_REWARD,
                    ChunkLibrary.LANE_WEAVE,
                    ChunkLibrary.SLIDE_COIN_REWARD
            )
    );

    public static final Scene COIN_PARADISE = new Scene(
            "COIN_PARADISE",
            ALL_DIFF,
            60.0,
            List.of(
                    ChunkLibrary.COIN_HIGHWAY_3LANE,
                    ChunkLibrary.MAGNET_BAIT,
                    ChunkLibrary.COIN_HIGHWAY_3LANE
            )
    );

    public static final Scene BOSS_GAUNTLET = new Scene(
            "BOSS_GAUNTLET",
            HARD_ONLY,
            70.0,
            List.of(
                    ChunkLibrary.OBSTACLE_GAUNTLET,
                    ChunkLibrary.LANE_BLOCK_FORK,
                    ChunkLibrary.OBSTACLE_GAUNTLET
            )
    );

    public static final List<Scene> ALL = List.of(
            TUTORIAL_START,
            URBAN_RUN,
            CITY_BLOCK,
            CONTAINER_YARD,
            LANE_DANCE,
            ZIGZAG_RUN,
            TRAIN_YARD,
            OBSTACLE_RUSH,
            FORCED_ACTION,
            MIXED_CHALLENGE,
            COIN_PARADISE,
            BOSS_GAUNTLET
    );
}
