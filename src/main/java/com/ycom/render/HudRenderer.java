package com.ycom.render;

import com.ycom.account.Account;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.state.UIUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import java.text.DecimalFormat;

public class HudRenderer {
    private static final Font SCORE_FONT = Font.font("Arial Black", FontWeight.BOLD, 30.0);
    private static final Font HUD_FONT = Font.font("Arial Black", FontWeight.BOLD, 22.0);
    private static final Font TIMER_FONT = Font.font("Arial Black", FontWeight.BOLD, 16.0);
    private static final DecimalFormat SPEED_FORMAT = new DecimalFormat("0.00x");
    private static final DecimalFormat TIMER_FORMAT = new DecimalFormat("0.0s");

    public void draw(GraphicsContext gc, com.ycom.core.PhysicsSnapshot snap) {
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
        gc.setFont(SCORE_FONT);
        double y = startY + 50.0;
        gc.fillText("SCORE: " + snap.score(), startX + 24.0, y);
        y += 40.0;

        gc.setFont(HUD_FONT);
        if (topRunVisible) {
            Account acc = Session.current();
            int gap = acc.getHighScore() - snap.score();
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

        int totalCoins = Session.current() != null ? Session.current().getCoins() : snap.coins();
        gc.fillText("COINS: " + snap.coins() + " (ALL: " + totalCoins + ")", startX + 24.0, y); y += 40.0;
        gc.fillText("SPEED: " + SPEED_FORMAT.format(snap.worldRate()), startX + 24.0, y); y += 40.0;
        gc.fillText("CAPSULES: " + snap.revivalCount(), startX + 24.0, y);

        double size = 84.0;
        double stride = 104.0;
        double bcx = 32.0;
        double bcy = Config.LOGICAL_HEIGHT - 32.0 - size;

        for (com.ycom.system.effect.PowerUpEffect effect : snap.effects()) {
            String icon = null;
            Color color = UIUtils.WHITE;
            switch(effect.id()) {
                case "magnet": icon = "magnet"; color = UIUtils.PINK; break;
                case "revive": icon = "revival"; color = Color.rgb(255, 200, 50); break;
                case "boost": icon = "sprite"; color = UIUtils.PINK; break;
                case "score_multiplier": icon = "treadmill"; color = UIUtils.YELLOW; break;
            }
            drawBuffTimer(gc, icon, effect.duration(), effect.maxDuration(), color, bcx, bcy, size);
            bcy -= stride;
        }
    }

    private void drawBuffTimer(GraphicsContext gc, String icon, double timer, double max, Color ringColor, double x, double y, double size) {
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 6, y + 6, size, size);
        gc.setFill(ringColor);
        gc.fillRect(x, y, size, size);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(4.0);
        gc.strokeRect(x, y, size, size);

        if (icon != null) {
            double iconSize = size * 0.66;
            com.ycom.resource.AssetManager.draw(gc, icon, x + (size - iconSize) / 2.0, y + (size - iconSize) / 2.0, iconSize, iconSize);
        }

        gc.setFill(UIUtils.WHITE);
        gc.fillRect(x, y + size - 14, size, 24);
        gc.strokeRect(x, y + size - 14, size, 24);

        gc.setFill(UIUtils.BORDER);
        gc.setFont(TIMER_FONT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(TIMER_FORMAT.format(timer), x + size / 2.0, y + size + 3);
        gc.setTextAlign(TextAlignment.LEFT);
    }
}
