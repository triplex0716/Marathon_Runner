package com.ycom.system;

import com.ycom.core.Config;
import com.ycom.entity.GameObject;
import com.ycom.entity.Player;
import com.ycom.event.EventBus;
import com.ycom.resource.AudioManager;
import com.ycom.world.GameWorld;

public class ReviveService {

    public boolean canOfferRevive(Player player, ScoreSystem score) {
        return player.hasRevival() || score.getCoins() >= currentCoinReviveCost(player);
    }

    public int currentCoinReviveCost(Player player) {
        int used = player.coinRevivesUsed();
        int[] costs = Config.COIN_REVIVE_COSTS;
        if (used >= costs.length) {
            return Integer.MAX_VALUE;
        }
        return costs[used];
    }

    public boolean useCapsule(Player player, GameWorld world, EventBus eventBus, EffectSystem effectSystem) {
        if (!player.hasRevival()) return false;
        player.consumeRevival();
        doRevive(player, world, eventBus, effectSystem);
        return true;
    }

    public boolean useCoins(Player player, ScoreSystem score, GameWorld world, EventBus eventBus, EffectSystem effectSystem) {
        int cost = currentCoinReviveCost(player);
        if (score.trySpendCoins(cost)) {
            player.onCoinRevive();
            doRevive(player, world, eventBus, effectSystem);
            return true;
        }
        return false;
    }

    private void doRevive(Player player, GameWorld world, EventBus eventBus, EffectSystem effectSystem) {
        clearNearbyObstacles(world, player.getZ());
        player.revive();
        effectSystem.addEffect(new com.ycom.system.effect.ReviveInvincibleEffect(Config.REVIVE_INVINCIBLE_DURATION), new com.ycom.entity.EntityUpdateContext(null, eventBus, player, 0, world));
        AudioManager.playSfx("invincible");
        AudioManager.playBGM();
    }

    public void clearNearbyObstacles(GameWorld world, double playerZ) {
        double radius = Config.REVIVE_CLEAR_RADIUS;
        for (GameObject obj : world.getObjects()) {
            if (obj.kind() != GameObject.ObjectKind.OBSTACLE) continue;
            double dz = obj.getZ() - playerZ;
            if (dz >= -Config.REVIVE_CLEAR_BEHIND_DISTANCE && dz <= radius) {
                obj.deactivate();
            }
        }
    }
}
