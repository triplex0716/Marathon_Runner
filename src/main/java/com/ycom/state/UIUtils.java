package com.ycom.state;

import com.ycom.core.Config;
import com.ycom.resource.AssetManager;
import com.ycom.system.InputSystem;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class UIUtils {
    public static final Color BORDER = Color.web("#000000");
    public static final Color CYAN = Color.web("#01f7f2");
    public static final Color YELLOW = Color.web("#FFD700");
    public static final Color PINK = Color.web("#ff90e8");
    public static final Color RED = Color.web("#FF1C1C");
    public static final Color WHITE = Color.web("#ffffff");
    public static final Color GREEN = Color.web("#2ecc71");

    public record NeoBtn(double x, double y, double w, double h, Color color, String text, double fontSize) {
        public boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }

    public static void drawNeoButton(GraphicsContext gc, InputSystem input, NeoBtn b) {
        double mx = input.getMouseX();
        double my = input.getMouseY();
        boolean hover = b.contains(mx, my);

        double dx = 0, dy = 0;
        double shX = 6, shY = 6;
        if (hover) {
            dx = 3; dy = 3;
            shX = 3; shY = 3;
        }

        // Shadow
        gc.setFill(BORDER);
        gc.fillRect(b.x + shX, b.y + shY, b.w, b.h);

        // Fill
        Color fill = hover ? brighten(b.color, 0.15) : b.color;
        gc.setFill(fill);
        gc.fillRect(b.x + dx, b.y + dy, b.w, b.h);

        // Border
        gc.setStroke(BORDER);
        gc.setLineWidth(4.0);
        gc.strokeRect(b.x + dx, b.y + dy, b.w, b.h);

        // Text
        gc.setFill(BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, b.fontSize));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(b.text, b.x + dx + b.w / 2.0, b.y + dy + b.h / 2.0 + b.fontSize * 0.35);
    }

    public static Color brighten(Color c, double amt) {
        return new Color(
            Math.min(1.0, c.getRed() + amt),
            Math.min(1.0, c.getGreen() + amt),
            Math.min(1.0, c.getBlue() + amt),
            c.getOpacity()
        );
    }

    public static void drawBackgroundAndTrack(GraphicsContext gc, double time) {
        double H = Config.LOGICAL_HEIGHT;
        double W = Config.LOGICAL_WIDTH;

        // 1. Background
        String bg = com.ycom.resource.AssetManager.menuBg();
        if (com.ycom.resource.AssetManager.exists(bg)) {
            com.ycom.resource.AssetManager.draw(gc, bg, 0, 0, W, H);
        } else {
            gc.setFill(Color.web("#71c5cf")); // sky blue fallback
            gc.fillRect(0, 0, W, H);
        }

        // Overlay to darken slightly
        gc.setFill(Color.rgb(0, 0, 0, 0.15));
        gc.fillRect(0, 0, W, H);

        // 2. Track & Runners
        double trackY = H * 0.81;
        double trackH = H * 0.14;
        gc.setFill(Color.web("#e74c3c")); 
        gc.fillRect(0, trackY, W, trackH);

        // Thick black borders for track (Neo-Brutalist style)
        gc.setStroke(BORDER);
        gc.setLineWidth(6.0);
        gc.strokeLine(0, trackY, W, trackY);
        gc.strokeLine(0, trackY + trackH, W, trackY + trackH);
        
        // Dashed lane lines (black for retro contrast)
        gc.setStroke(BORDER);
        gc.setLineWidth(4.0);
        gc.setLineDashes(24, 16);
        gc.strokeLine(0, trackY + trackH * 0.33, W, trackY + trackH * 0.33);
        gc.strokeLine(0, trackY + trackH * 0.66, W, trackY + trackH * 0.66);
        gc.setLineDashes(0);

        // Runners
        String[] chars = {"cheryl", "dave", "matt", "mazz"};
        for (int i = 0; i < chars.length; i++) {
            double speed = 180.0 + i * 35.0;
            double x = (time * speed + i * 300.0) % (W + 200) - 100;
            int frame = (int)(time * 8) % 2 + 1;
            String sprite = AssetManager.runnerFrame(chars[i], frame);
            if (com.ycom.resource.AssetManager.exists(sprite)) {
                double sz = 75.0;
                com.ycom.resource.AssetManager.draw(gc, sprite, x, trackY + 10 + i * 18, sz, sz);
            }
        }

        // Green Footer
        double footerY = trackY + trackH;
        double footerH = H - footerY;
        gc.setFill(GREEN);
        gc.fillRect(0, footerY, W, footerH);

        // Footer Text - Stylized with stroke
        gc.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 26));
        gc.setStroke(BORDER);
        gc.setLineWidth(5.0);
        
        gc.setTextAlign(TextAlignment.LEFT);
        gc.strokeText("MADE BY GROUP 12", 30, footerY + footerH / 2 + 10);
        gc.setFill(YELLOW);
        gc.fillText("MADE BY GROUP 12", 30, footerY + footerH / 2 + 10);
        
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.strokeText("RUNNING GAVE ME A SECOND LIFE.", W - 30, footerY + footerH / 2 + 10);
        gc.setFill(WHITE);
        gc.fillText("RUNNING GAVE ME A SECOND LIFE.", W - 30, footerY + footerH / 2 + 10);
    }
}
