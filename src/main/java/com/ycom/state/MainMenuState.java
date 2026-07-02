package com.ycom.state;

import com.ycom.account.Account;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.resource.AudioManager;
import com.ycom.system.InputSystem;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Text;

public class MainMenuState implements GameState {
    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;

    private double time = 0.0;

    public MainMenuState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }

    @Override
    public void onEnter() {
        AudioManager.stopBGM();
        time = 0.0;
    }

    @Override
    public void onExit() {}

    @Override
    public void update(double dt) {
        time += dt;

        double mx = input.getMouseX();
        double my = input.getMouseY();
        boolean clicked = input.isMouseJustClicked();

        if (clicked) {
            if (btnEasy().contains(mx, my)) startGame(Config.Difficulty.EASY);
            else if (btnMedium().contains(mx, my)) startGame(Config.Difficulty.MEDIUM);
            else if (btnHard().contains(mx, my)) startGame(Config.Difficulty.HARD);
            else if (Session.isLoggedIn() && btnShop().contains(mx, my)) gsm.setState(StateId.SHOP);
            else if (btnInstruction().contains(mx, my)) gsm.setState(StateId.INSTRUCTION);
            else if (btnIdentity().contains(mx, my)) {
                if (!Session.isGuest()) Session.logout();
                gsm.setState(StateId.LOGIN);
            }
        }
    }

    private void startGame(Config.Difficulty diff) {
        GameState state = gsm.getState(StateId.PLAYING);
        if (state instanceof PlayingState playingState) {
            playingState.resetGame(diff);
        }
        gsm.setState(StateId.PLAYING);
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        UIUtils.drawBackgroundAndTrack(gc, time);
        drawTitle(gc);

        // 4. Buttons
        UIUtils.drawNeoButton(gc, input, btnEasy());
        UIUtils.drawNeoButton(gc, input, btnMedium());
        UIUtils.drawNeoButton(gc, input, btnHard());
        
        UIUtils.drawNeoButton(gc, input, btnInstruction());
        if (Session.isLoggedIn()) {
            UIUtils.drawNeoButton(gc, input, btnShop());
        }
        UIUtils.drawNeoButton(gc, input, btnIdentity());

        drawUserInfo(gc);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawTitle(GraphicsContext gc) {
        // The title "YOU CAN'T OUTRUN ME!" is now built into the background image,
        // so we just draw the subtitle here on the left, centered above the buttons.
        double cx = 290;
        double cy = 450;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 28));
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(6.0);
        gc.strokeText("SELECT DIFFICULTY", cx, cy);
        gc.setFill(UIUtils.WHITE);
        gc.fillText("SELECT DIFFICULTY", cx, cy);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawUserInfo(GraphicsContext gc) {
        if (Session.isGuest()) return;
        Account acc = Session.current();
        
        String info = String.format("%s | Coins: %d | Caps: %d", acc.getUsername(), acc.getCoins(), acc.getCapsules());
        Font font = Font.font("Courier New", FontWeight.BOLD, 18);
        
        Text textHelper = new Text(info);
        textHelper.setFont(font);
        double textW = textHelper.getLayoutBounds().getWidth();
        
        double w = textW + 24; // 12px padding on each side
        double h = 48;
        double x = Config.LOGICAL_WIDTH - w - 15;
        double y = 15;

        // Shadow
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 4, y + 4, w, h);

        gc.setFill(UIUtils.WHITE);
        gc.fillRect(x, y, w, h);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(4.0);
        gc.strokeRect(x, y, w, h);

        gc.setFill(UIUtils.BORDER);
        gc.setFont(font);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(info, x + 12, y + 30);
    }

    // Button definitions
    private UIUtils.NeoBtn btnEasy() { return new UIUtils.NeoBtn(90, 500, 400, 75, UIUtils.CYAN, "EASY", 32); }
    private UIUtils.NeoBtn btnMedium() { return new UIUtils.NeoBtn(90, 590, 400, 75, UIUtils.YELLOW, "MEDIUM", 32); }
    private UIUtils.NeoBtn btnHard() { return new UIUtils.NeoBtn(90, 680, 400, 75, UIUtils.RED, "HARD", 32); }
    
    private UIUtils.NeoBtn btnInstruction() { 
        return new UIUtils.NeoBtn(90, 780, 240, 60, UIUtils.WHITE, "HOW TO PLAY", 22);
    }
    
    private UIUtils.NeoBtn btnShop() { 
        return new UIUtils.NeoBtn(350, 780, 140, 60, UIUtils.WHITE, "SHOP", 22); 
    }
    
    private UIUtils.NeoBtn btnIdentity() { 
        String text = Session.isGuest() ? "LOGIN" : "LOGOUT";
        return new UIUtils.NeoBtn(15, 0, 144, 48, UIUtils.PINK, text, 18); 
    }
}
