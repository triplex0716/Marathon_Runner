package com.ycom.system;

import com.ycom.entity.Player;
import com.ycom.event.CoinCollectedEvent;
import com.ycom.event.EventBus;
import com.ycom.event.ScoreAddEvent;

public class ScoreSystem {
    private static volatile int highScore = 0;

    private final Player player;
    private volatile double distance = 0.0;
    private double lastPlayerZ = 0.0;
    private volatile int coinCount = 0;
    private volatile int runCoinsEarned = 0;
    private volatile int bonusScore = 0;

    public ScoreSystem(EventBus eventBus, Player player) {
        this.player = player;
        eventBus.subscribe(CoinCollectedEvent.class, event -> {
            coinCount++;
            runCoinsEarned++;
            bonusScore += event.value();
        });
        eventBus.subscribe(ScoreAddEvent.class, event -> bonusScore += event.points());
    }

    public void update(double dt, double playerZ) {
        double delta = Math.max(0.0, playerZ - lastPlayerZ);
        distance += delta * player.currentScoreMultiplier();
        lastPlayerZ = playerZ;
        highScore = Math.max(highScore, getScore());
    }

    public boolean trySpendCoins(int amount) {
        if (coinCount < amount) {
            return false;
        }
        coinCount -= amount;
        return true;
    }

    public int getCoins() {
        return coinCount;
    }

    public int getRunCoinsEarned() {
        return runCoinsEarned;
    }

    public void setCoins(int n) {
        coinCount = Math.max(0, n);
    }

    public int getScore() {
        return (int) distance + bonusScore;
    }

    public int getHighScore() {
        return highScore;
    }
}
