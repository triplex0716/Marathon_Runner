package com.ycom.core;

public class Config {
    public static final double LOGICAL_WIDTH = 1920.0;
    public static final double LOGICAL_HEIGHT = 1080.0;

    public static final double BASE_SPEED = 20.0;
    public static final double BASE_TIME_SCALE = 1.0;
    public static final double MAX_TIME_SCALE = 1.75;
    public static final double TIME_SCALE_STEP_INTERVAL = 8.0;
    public static final double TIME_SCALE_STEP_AMOUNT = 0.1;
    public static final double BOOST_WORLD_RATE = 1.8;
    public static final double BOOST_BGM_RATE = 1.8;
    public static final double BOOST_DURATION = 3.0;
    public static final double MAGNET_DURATION = 10.0;
    public static final double TREADMILL_DURATION = 10.0;
    public static final double SCORE_MULTIPLIER = 2.0;
    public static final double REVIVE_INVINCIBLE_DURATION = 5.0;
    public static final double REVIVE_CLEAR_RADIUS = 20.0;
    public static final int[] COIN_REVIVE_COSTS = { 300, 600, 1200 };
    public static final double BGM_VOLUME = 0.85;
    public static final double SFX_VOLUME = 0.38;

    public static final double CAMERA_Y = 4;
    public static final double CAMERA_OFFSET_Z = -8.0;
    public static final double FOCAL_LENGTH = 1000.0;

    public static final double LANE_WIDTH = 4.0;
    public static final double LATERAL_SPEED = 15.0;
    public static final double GRAVITY = -60.0;
    public static final double JUMP_VELOCITY = 20.0;
    public static final double SLIDE_DURATION = 0.75;
    public static final double PLAYER_STANDING_HEIGHT = 2.0;
    public static final double PLAYER_SLIDING_HEIGHT = 1.0;

    public enum Difficulty {
        EASY("Easy", 0.90, 15.5, 5.5, 0.16, 0.12, 0.06, 0.05, 0.05, 0.10, 45.0, 0.05, 0.02, 0.04),
        MEDIUM("Medium", 1.00, 14.0, 5.0, 0.24, 0.18, 0.14, 0.04, 0.04, 0.22, 25.0, 0.04, 0.015, 0.03),
        HARD("Hard", 1.15, 12.0, 4.0, 0.30, 0.24, 0.22, 0.03, 0.03, 0.36, 8.0, 0.03, 0.01, 0.02);

        public final String label;
        public final double initialTimeScale;
        public final double spawnBaseGap;
        public final double spawnRandomGap;
        public final double jumpObstacleChance;
        public final double slideObstacleChance;
        public final double laneBlockObstacleChance;
        public final double magnetChance;
        public final double energyDrinkChance;
        public final double lateExtraObstacleChance;
        public final double laneBlockUnlockSeconds;
        public final double treadmillChance;
        public final double revivalChance;
        public final double randomItemChance;

        Difficulty(
                String label,
                double initialTimeScale,
                double spawnBaseGap,
                double spawnRandomGap,
                double jumpObstacleChance,
                double slideObstacleChance,
                double laneBlockObstacleChance,
                double magnetChance,
                double energyDrinkChance,
                double lateExtraObstacleChance,
                double laneBlockUnlockSeconds,
                double treadmillChance,
                double revivalChance,
                double randomItemChance
        ) {
            this.label = label;
            this.initialTimeScale = initialTimeScale;
            this.spawnBaseGap = spawnBaseGap;
            this.spawnRandomGap = spawnRandomGap;
            this.jumpObstacleChance = jumpObstacleChance;
            this.slideObstacleChance = slideObstacleChance;
            this.laneBlockObstacleChance = laneBlockObstacleChance;
            this.magnetChance = magnetChance;
            this.energyDrinkChance = energyDrinkChance;
            this.lateExtraObstacleChance = lateExtraObstacleChance;
            this.laneBlockUnlockSeconds = laneBlockUnlockSeconds;
            this.treadmillChance = treadmillChance;
            this.revivalChance = revivalChance;
            this.randomItemChance = randomItemChance;
        }
    }

    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.MEDIUM;
}
