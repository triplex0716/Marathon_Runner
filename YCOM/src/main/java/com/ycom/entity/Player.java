package com.ycom.entity;

import com.ycom.core.Config;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

public class Player extends Actor {
    public enum PlayerState {
        RUNNING,
        JUMPING,
        SLIDING,
        BOOSTED_INVINCIBLE
    }

    private PlayerState state = PlayerState.RUNNING;
    private double velocityY;
    private double slideTimer;
    private double magnetTimer;
    private double magnetMax;
    private double boostTimer;
    private double boostMax;
    private double scoreMultiplierTimer;
    private double scoreMultiplierMax;
    private double scoreMultiplier = 1.0;
    private int revivalCount;
    private int coinRevivesUsed;

    public Player() {
        super("player", 0.0, 0.0, 0.0, 1.25, Config.PLAYER_STANDING_HEIGHT, 1.0, Color.CORNFLOWERBLUE);
        setFramesPerSecond(12.0);
    }

    @Override
    public void update(double worldDt, EntityUpdateContext context) {
        super.update(worldDt, context);

        z += Config.BASE_SPEED * worldDt;
        updateInput(worldDt, context);
        updateVerticalMotion(worldDt);
        updateEffects(context.fixedDt());
    }

    private void updateInput(double worldDt, EntityUpdateContext context) {
        if ((context.input().isKeyJustPressed(KeyCode.LEFT) || context.input().isKeyJustPressed(KeyCode.A)) && lane > -1) {
            lane--;
        }
        if ((context.input().isKeyJustPressed(KeyCode.RIGHT) || context.input().isKeyJustPressed(KeyCode.D)) && lane < 1) {
            lane++;
        }

        targetX = lane * Config.LANE_WIDTH;
        x += (targetX - x) * Math.min(1.0, Config.LATERAL_SPEED * worldDt);

        boolean grounded = y <= 0.0001;
        if ((context.input().isKeyJustPressed(KeyCode.UP) || context.input().isKeyJustPressed(KeyCode.W)) && grounded) {
            velocityY = Config.JUMP_VELOCITY;
            clearSlide();
            state = PlayerState.JUMPING;
        }

        if ((context.input().isKeyJustPressed(KeyCode.DOWN) || context.input().isKeyJustPressed(KeyCode.S)) && grounded) {
            state = PlayerState.SLIDING;
            slideTimer = Config.SLIDE_DURATION;
            height = Config.PLAYER_SLIDING_HEIGHT;
            y = 0.0;
        }
    }

    private void updateVerticalMotion(double worldDt) {
        if (y > 0.0 || velocityY > 0.0) {
            velocityY += Config.GRAVITY * worldDt;
            y += velocityY * worldDt;
            if (y <= 0.0) {
                y = 0.0;
                velocityY = 0.0;
                if (!isSliding() && !isBoosted()) {
                    state = PlayerState.RUNNING;
                }
            }
        }
    }

    private void updateEffects(double realDt) {
        if (slideTimer > 0.0) {
            slideTimer -= realDt;
            if (slideTimer <= 0.0) {
                clearSlide();
            }
        }

        if (magnetTimer > 0.0) {
            magnetTimer -= realDt;
            if (magnetTimer <= 0.0) {
                magnetTimer = 0.0;
                magnetMax = 0.0;
            }
        }

        if (boostTimer > 0.0) {
            boostTimer -= realDt;
            state = PlayerState.BOOSTED_INVINCIBLE;
            if (boostTimer <= 0.0) {
                boostTimer = 0.0;
                boostMax = 0.0;
                state = y > 0.0 ? PlayerState.JUMPING : PlayerState.RUNNING;
            }
        }

        if (scoreMultiplierTimer > 0.0) {
            scoreMultiplierTimer -= realDt;
            if (scoreMultiplierTimer <= 0.0) {
                scoreMultiplierTimer = 0.0;
                scoreMultiplierMax = 0.0;
                scoreMultiplier = 1.0;
            }
        }
    }

    private void clearSlide() {
        slideTimer = 0.0;
        if (height != Config.PLAYER_STANDING_HEIGHT) {
            height = Config.PLAYER_STANDING_HEIGHT;
        }
        if (!isBoosted()) {
            state = y > 0.0 ? PlayerState.JUMPING : PlayerState.RUNNING;
        }
    }

    public void activateMagnet(double duration) {
        magnetTimer = Math.max(magnetTimer, duration);
        magnetMax = Math.max(magnetMax, duration);
    }

    public void activateBoost(double duration) {
        boostTimer = Math.max(boostTimer, duration);
        boostMax = Math.max(boostMax, duration);
        state = PlayerState.BOOSTED_INVINCIBLE;
    }

    public boolean hasMagnet() {
        return magnetTimer > 0.0;
    }

    public double magnetTimer() {
        return magnetTimer;
    }

    public double magnetMaxDuration() {
        return magnetMax;
    }

    public double boostMaxDuration() {
        return boostMax;
    }

    public double scoreMultiplierMaxDuration() {
        return scoreMultiplierMax;
    }

    public boolean isBoosted() {
        return boostTimer > 0.0;
    }

    public double boostTimer() {
        return boostTimer;
    }

    public boolean isSliding() {
        return slideTimer > 0.0;
    }

    public PlayerState state() {
        return state;
    }

    public void activateScoreMultiplier(double duration, double multiplier) {
        scoreMultiplierTimer = Math.max(scoreMultiplierTimer, duration);
        scoreMultiplierMax = Math.max(scoreMultiplierMax, duration);
        scoreMultiplier = Math.max(scoreMultiplier, multiplier);
    }

    public boolean hasScoreMultiplier() {
        return scoreMultiplierTimer > 0.0;
    }

    public double scoreMultiplierTimer() {
        return scoreMultiplierTimer;
    }

    public double currentScoreMultiplier() {
        return scoreMultiplier;
    }

    public void addRevival() {
        revivalCount++;
    }

    public boolean hasRevival() {
        return revivalCount > 0;
    }

    public int revivalCount() {
        return revivalCount;
    }

    public void consumeRevival() {
        if (revivalCount > 0) {
            revivalCount--;
        }
    }

    public int coinRevivesUsed() {
        return coinRevivesUsed;
    }

    public void onCoinRevive() {
        coinRevivesUsed++;
    }

    public void revive() {
        velocityY = 0.0;
        y = 0.0;
        slideTimer = 0.0;
        height = Config.PLAYER_STANDING_HEIGHT;
        state = PlayerState.RUNNING;
        activateBoost(Config.REVIVE_INVINCIBLE_DURATION);
    }
}
