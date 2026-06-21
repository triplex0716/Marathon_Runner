package com.ycom.system;

import com.ycom.event.CoinCollectedEvent;
import com.ycom.event.EventBus;
import com.ycom.event.ScoreAddEvent;

public class ScoreSystem {
    private static int highScore = 0;

    private double distance = 0.0;
    private int coinCount = 0;
    private int bonusScore = 0;

    public ScoreSystem(EventBus eventBus) {
        eventBus.subscribe(CoinCollectedEvent.class, event -> {
            coinCount++;
            bonusScore += event.value();
        });
        eventBus.subscribe(ScoreAddEvent.class, event -> bonusScore += event.points());
    }

    public void update(double dt, double playerZ) {
        distance = playerZ;
        highScore = Math.max(highScore, getScore());
    }

    public int getCoins() {
        return coinCount;
    }

    public int getScore() {
        return (int) distance + bonusScore;
    }

    public int getHighScore() {
        return highScore;
    }
}
