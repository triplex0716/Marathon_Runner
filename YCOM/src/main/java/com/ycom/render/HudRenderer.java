package com.ycom.render;

import com.ycom.account.Account;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.core.TimeManager;
import com.ycom.entity.Player;
import com.ycom.resource.AssetManager;
import com.ycom.system.ScoreSystem;
import com.ycom.state.UIUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class HudRenderer {

    public void draw(GraphicsContext gc, Player player, ScoreSystem scoreSystem, com.ycom.system.EffectSystem effectSystem) {
        gc.setTextAlign(TextAlignment.LEFT);

        boolean topRunVisible = Session.isLoggedIn() && Session.current().getHighScore() > 0;
        double panelW = 440.0;
        double panelH = topRunVisible ? 240.0 : 190.0;
        double startX = 24.0;
        double startY = 24.0;

        gc.setFill(UIUtils.BORDER);
        gc.fillRect(startX + 8, startY + 8, panelW, panelH);
        gc.setFill(UIUtils.WHITE);
        gc.fillRect(startX, startY, panelW, panelH);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(6.0);
        gc.strokeRect(startX, startY, panelW, panelH);

        gc.setFill(UIUtils.BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 30.0));
        double y = startY + 50.0;
        gc.fillText("SCORE: " + scoreSystem.getScore(), startX + 24.0, y);
        y += 40.0;

        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 22.0));
        if (topRunVisible) {
            Account acc = Session.current();
            int gap = acc.getHighScore() - scoreSystem.getScore();
            if (gap > 0) {
                gc.setFill(UIUtils.BORDER);
                gc.fillText("TOP RUN: " + gap, startX + 24.0, y);
            } else {
                gc.setFill(UIUtils.RED);
                gc.fillText("NEW BEST! +" + (-gap), startX + 24.0, y);
                gc.setFill(UIUtils.BORDER);
            }
            y += 40.0;
        }

        int totalCoins = Session.current() != null ? Session.current().getCoins() : scoreSystem.getCoins();
        gc.fillText("COINS: " + scoreSystem.getRunCoinsEarned() + " (ALL: " + totalCoins + ")", startX + 24.0, y); y += 40.0;
        gc.fillText("SPEED: " + String.format("%.2fx", TimeManager.getWorldRate()), startX + 24.0, y); y += 40.0;
        gc.fillText("CAPSULES: " + player.revivalCount(), startX + 24.0, y);

        double size = 84.0;
        double stride = 104.0;
        double bcx = 32.0;
        double bcy = Config.LOGICAL_HEIGHT - 32.0 - size;

        for (com.ycom.system.effect.PowerUpEffect effect : effectSystem.activeEffects()) {
            Image icon = null;
            Color color = UIUtils.WHITE;
            switch(effect.id()) {
                case "magnet": icon = AssetManager.magnetIcon(); color = UIUtils.PINK; break;
                case "revive": icon = AssetManager.revivalIcon(); color = Color.rgb(255, 200, 50); break;
                case "boost": icon = AssetManager.spriteIcon(); color = UIUtils.PINK; break;
                case "score_multiplier": icon = AssetManager.treadmillIcon(); color = UIUtils.YELLOW; break;
            }
            drawBuffTimer(gc, icon, effect.duration(), effect.maxDuration(), color, bcx, bcy, size);
            bcy -= stride;
        }
    }

    private void drawBuffTimer(GraphicsContext gc, Image icon, double timer, double max, Color ringColor, double x, double y, double size) {
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 6, y + 6, size, size);
        gc.setFill(ringColor);
        gc.fillRect(x, y, size, size);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(4.0);
        gc.strokeRect(x, y, size, size);

        if (icon != null && icon.getWidth() > 0.0) {
            double iconSize = size * 0.66;
            gc.drawImage(icon, x + (size - iconSize) / 2.0, y + (size - iconSize) / 2.0, iconSize, iconSize);
        }

        gc.setFill(UIUtils.WHITE);
        gc.fillRect(x, y + size - 14, size, 24);
        gc.strokeRect(x, y + size - 14, size, 24);

        gc.setFill(UIUtils.BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 16.0));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.format("%.1fs", timer), x + size / 2.0, y + size + 3);
        gc.setTextAlign(TextAlignment.LEFT);
    }
}
