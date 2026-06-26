package com.ycom.state;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import com.ycom.account.Account;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.resource.AssetManager;
import com.ycom.resource.AudioManager;
import com.ycom.system.InputSystem;

public class MainMenuState implements GameState {
    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;

    public MainMenuState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }
    
    @Override
    public void onEnter() {
        AudioManager.stopBGM();
    }

    @Override public void onExit() {}

    @Override
    public void update(double dt) {
        if (!input.isMouseJustClicked()) {
            return;
        }

        if (identityButton().contains(input.getMouseX(), input.getMouseY())) {
            if (!Session.isGuest()) Session.logout();
            gsm.setState("LOGIN");
            return;
        }

        for (int i = 0; i < Config.Difficulty.values().length; i++) {
            ButtonRect rect = buttonRect(i);
            if (rect.contains(input.getMouseX(), input.getMouseY())) {
                startGame(Config.Difficulty.values()[i]);
                return;
            }
        }
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        if (AssetManager.background() != null) {
            gc.drawImage(AssetManager.background(), 0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
//            gc.setFill(Color.rgb(0, 0, 0, 0.58));
            gc.setFill(Color.rgb(124,183,213,0.58));
            gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        } else {
            gc.setFill(Color.web("#101820"));
            gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 78));
        gc.fillText("YOU CAN'T OUTRUN ME!", Config.LOGICAL_WIDTH/2 - 450, Config.LOGICAL_HEIGHT/2 - 100);

        gc.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < Config.Difficulty.values().length; i++) {
            Config.Difficulty difficulty = Config.Difficulty.values()[i];
            ButtonRect rect = buttonRect(i);
            boolean hovered = rect.contains(input.getMouseX(), input.getMouseY());
            drawDifficultyButton(gc, rect, difficulty, hovered);
//            drawDifficultyButton(gc, rect, difficulty);
        }
        gc.setTextAlign(TextAlignment.LEFT);

        drawIdentityBar(gc);
    }

    private void drawIdentityBar(GraphicsContext gc) {
        ButtonRect btn = identityButton();
        String info;
        String label;
        if (Session.isGuest()) {
            info = "GUEST MODE";
            label = "LOGIN";
        } else {
            Account acc = Session.current();
            info = acc.username + "   coins: " + acc.coins + "   caps: " + acc.capsules;
            label = "LOGOUT";
        }

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        gc.setFill(Color.rgb(0, 0, 0, 0.45));
        gc.fillRoundRect(btn.x - 760.0, btn.y, 740.0, btn.h, 8.0, 8.0);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(info, btn.x - 30.0, btn.y + 40.0);

        boolean hovered = btn.contains(input.getMouseX(), input.getMouseY());
        if (hovered) {
            gc.setFill(Color.rgb(255, 255, 255, 0.22));
            gc.fillRoundRect(btn.x, btn.y, btn.w, btn.h, 8.0, 8.0);
        }
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(btn.x, btn.y, btn.w, btn.h, 8.0, 8.0);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        gc.fillText(label, btn.x + btn.w / 2.0, btn.y + 40.0);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private ButtonRect identityButton() {
        double w = 180.0;
        double h = 60.0;
        return new ButtonRect(Config.LOGICAL_WIDTH - w - 30.0, 30.0, w, h);
    }

    private void startGame(Config.Difficulty difficulty) {
        GameState state = gsm.getState("PLAYING");
        if (state instanceof PlayingState playingState) {
            playingState.resetGame(difficulty);
        }
        gsm.setState("PLAYING");
    }

    private void drawDifficultyButton(GraphicsContext gc, ButtonRect rect, Config.Difficulty difficulty,boolean hovered) {
        Color fill = switch (difficulty) {
            case EASY -> Color.rgb(41, 120, 82, 0.88);
            case MEDIUM -> Color.rgb(42, 86, 145, 0.88);
            case HARD -> Color.rgb(150, 50, 46, 0.88);
        };
        if (hovered) {
            gc.setFill(fill);
            gc.fillRoundRect(rect.x, rect.y, rect.w, rect.h, 8.0, 8.0);
        }
        gc.setStroke(Color.rgb(255, 255, 255, 0.85));
        gc.setLineWidth(3.0);
        gc.strokeRoundRect(rect.x, rect.y, rect.w, rect.h, 8.0, 8.0);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 44.0));
        gc.fillText(difficulty.label, rect.x + rect.w / 2.0, rect.y + 70.0);
    }

    private ButtonRect buttonRect(int index) {
        double w = 620.0;
        double h = 112.0;
        double x = (Config.LOGICAL_WIDTH - w) / 2.0;
        double y = Config.LOGICAL_HEIGHT / 2.0 + 38.0 + index * 150.0;
        return new ButtonRect(x, y, w, h);
    }

    private record ButtonRect(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }
}
