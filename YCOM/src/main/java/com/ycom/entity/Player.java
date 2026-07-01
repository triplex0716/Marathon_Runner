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

    private volatile PlayerState state = PlayerState.RUNNING;
    private double velocityY;
    private volatile double slideTimer;
    private volatile boolean hasMagnet;
    private volatile boolean isBoosted;
    private volatile boolean isReviveInvincible;
    private volatile double scoreMultiplier = 1.0;
    private volatile int revivalCount;
    private int coinRevivesUsed;
    private volatile double floorY;

    public Player() {
        super(ObjectKind.PLAYER, 0.0, 0.0, 0.0,
                Config.PLAYER_WIDTH, Config.PLAYER_STANDING_HEIGHT, Config.PLAYER_DEPTH,
                Color.CORNFLOWERBLUE);
        setFramesPerSecond(Config.PLAYER_ANIMATION_FPS);
    }

    @Override
    public void update(double worldDt, EntityUpdateContext context) {
        super.update(worldDt, context);

        setZ(getZ() + Config.BASE_SPEED * worldDt);
        updateInput(worldDt, context);
        updateVerticalMotion(worldDt, context);
        updateEffects(context.fixedDt());
    }

    private void updateInput(double worldDt, EntityUpdateContext context) {
        if ((context.input().isKeyJustPressed(KeyCode.LEFT) || context.input().isKeyJustPressed(KeyCode.A))
                && lane > Config.MIN_LANE) {
            lane--;
        }
        if ((context.input().isKeyJustPressed(KeyCode.RIGHT) || context.input().isKeyJustPressed(KeyCode.D))
                && lane < Config.MAX_LANE) {
            lane++;
        }

        targetX = lane * Config.LANE_WIDTH;
        setX(getX() + (targetX - getX()) * Math.min(1.0, Config.LATERAL_SPEED * worldDt));

        boolean grounded = Math.abs(getY() - floorY) <= 0.0001;
        if ((context.input().isKeyJustPressed(KeyCode.UP) || context.input().isKeyJustPressed(KeyCode.W)) && grounded) {
            velocityY = Config.JUMP_VELOCITY;
            clearSlide();
            state = PlayerState.JUMPING;
        }

        if ((context.input().isKeyJustPressed(KeyCode.DOWN) || context.input().isKeyJustPressed(KeyCode.S)) && grounded) {
            state = PlayerState.SLIDING;
            slideTimer = Config.SLIDE_DURATION;
            setHeight(Config.PLAYER_SLIDING_HEIGHT);
            setY(0.0);
        }
    }

    private double calculateFloorY(EntityUpdateContext context) {
        double floorY = 0.0;
        for (GameObject obj : context.world().getObjects()) {
            if (!obj.isActive() || !(obj instanceof Obstacle obs)) continue;

            double halfW = obs.getWidth() / 2.0;
            if (getX() >= obs.getX() - halfW && getX() <= obs.getX() + halfW) {
                if (getZ() + getDepth() / 2.0 >= obs.getZ() - obs.getDepth() / 2.0 && getZ() - getDepth() / 2.0 <= obs.getZ() + obs.getDepth() / 2.0) {
                    if (obs.avoidMethod() == Obstacle.AvoidMethod.CONTAINER) {
                        floorY = Math.max(floorY, obs.getHeight());
                    } else if (obs.avoidMethod() == Obstacle.AvoidMethod.RAMP) {
                        double frontZ = obs.getZ() - obs.getDepth() / 2.0;
                        double clampZ = Math.max(frontZ, Math.min(obs.getZ() + obs.getDepth() / 2.0, getZ() + getDepth() / 2.0));
                        double progress = (clampZ - frontZ) / obs.getDepth();
                        // 坡度高度从前向后递增
                        double rY = progress * obs.getHeight();
                        floorY = Math.max(floorY, rY);
                    }
                }
            }
        }
        return floorY;
    }

    private void updateVerticalMotion(double worldDt, EntityUpdateContext context) {
        floorY = calculateFloorY(context);
        
        if (getY() > floorY || velocityY > 0.0) {
            velocityY += Config.GRAVITY * worldDt;
            setY(getY() + velocityY * worldDt);
            if (getY() <= floorY) {
                setY(floorY);
                velocityY = 0.0;
                if (!isSliding() && !isBoosted()) {
                    state = PlayerState.RUNNING;
                }
            }
        } else {
            if (getY() < floorY) {
                setY(floorY);
                velocityY = 0.0;
            }
            if (!isSliding() && !isBoosted()) {
                state = PlayerState.RUNNING;
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
    }

    private void clearSlide() {
        slideTimer = 0.0;
        if (getHeight() != Config.PLAYER_STANDING_HEIGHT) {
            setHeight(Config.PLAYER_STANDING_HEIGHT);
        }
        if (!isBoosted()) {
            state = Math.abs(getY() - floorY) > 0.0001 ? PlayerState.JUMPING : PlayerState.RUNNING;
        }
    }

    public void setHasMagnet(boolean val) { this.hasMagnet = val; }
    public boolean hasMagnet() { return hasMagnet; }

    public void setBoosted(boolean val) {
        this.isBoosted = val;
        if (val) {
            state = PlayerState.BOOSTED_INVINCIBLE;
        } else {
            state = Math.abs(getY() - floorY) > 0.0001 ? PlayerState.JUMPING : PlayerState.RUNNING;
        }
    }
    public boolean isBoosted() { return isBoosted; }

    public void setReviveInvincible(boolean val) { this.isReviveInvincible = val; }
    public boolean isReviveInvincible() { return isReviveInvincible; }

    public boolean isInvulnerable() { return isBoosted || isReviveInvincible; }

    public void setScoreMultiplier(double val) { this.scoreMultiplier = val; }
    public double currentScoreMultiplier() { return scoreMultiplier; }
    public boolean hasScoreMultiplier() { return scoreMultiplier > 1.0; }

    public boolean isSliding() {
        return slideTimer > 0.0;
    }

    public PlayerState state() {
        if (isBoosted) return PlayerState.BOOSTED_INVINCIBLE;
        return state;
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

    public void setRevivalCount(int n) {
        revivalCount = Math.max(0, n);
    }

    public int coinRevivesUsed() {
        return coinRevivesUsed;
    }

    public void onCoinRevive() {
        coinRevivesUsed++;
    }

    public void revive() {
        velocityY = 0.0;
        setY(0.0);
        slideTimer = 0.0;
        setHeight(Config.PLAYER_STANDING_HEIGHT);
        state = PlayerState.RUNNING;
        
    }

    public double floorY() {
        return floorY;
    }
}
