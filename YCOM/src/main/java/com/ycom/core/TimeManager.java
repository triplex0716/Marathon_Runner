package com.ycom.core;

public class TimeManager {
    private static double difficultyTimeScale = Config.BASE_TIME_SCALE;
    private static double boostWorldRate = 0.0;
    private static double audioRate = Config.BASE_TIME_SCALE;
    private static double elapsedTime = 0.0;
    private static Config.Difficulty difficulty = Config.DEFAULT_DIFFICULTY;

    public static void init() {
        reset();
    }

    public static void update(double dt) {
        elapsedTime += dt;

        double stepCount = Math.floor(elapsedTime / Config.TIME_SCALE_STEP_INTERVAL);
        difficultyTimeScale = clamp(
                difficulty.initialTimeScale + stepCount * Config.TIME_SCALE_STEP_AMOUNT,
                difficulty.initialTimeScale,
                Config.MAX_TIME_SCALE
        );

        if (!isBoostActive()) {
            audioRate = difficultyTimeScale;
        }
    }

    public static double getFixedDt() {
        return Config.FIXED_TIMESTEP_SECONDS;
    }

    public static double getWorldRate() {
        return boostWorldRate > 0.0 ? boostWorldRate : difficultyTimeScale;
    }

    public static double getDifficultyTimeScale() {
        return difficultyTimeScale;
    }

    public static double getAudioRate() {
        return audioRate;
    }

    public static double getElapsedTime() {
        return elapsedTime;
    }

    public static double getScaledDeltaTime(double dt) {
        return dt * getWorldRate();
    }

    public static void activateBoost(double duration, double worldRate, double bgmRate) {
        if (duration <= 0.0) {
            return;
        }
        boostWorldRate = clamp(worldRate, 0.0, Config.BOOST_WORLD_RATE);
        audioRate = clamp(bgmRate, 0.0, Config.BOOST_BGM_RATE);
    }

    public static void clearBoost() {
        boostWorldRate = 0.0;
        audioRate = difficultyTimeScale;
    }

    public static double getBoostRemaining() {
        return 0.0;
    }

    public static boolean isBoostActive() {
        return boostWorldRate > 0.0;
    }

    public static void reset() {
        reset(Config.DEFAULT_DIFFICULTY);
    }

    public static void reset(Config.Difficulty selectedDifficulty) {
        difficulty = selectedDifficulty == null ? Config.DEFAULT_DIFFICULTY : selectedDifficulty;
        difficultyTimeScale = difficulty.initialTimeScale;
        boostWorldRate = 0.0;
        audioRate = difficultyTimeScale;
        elapsedTime = 0.0;
    }

    public static Config.Difficulty getDifficulty() {
        return difficulty;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
