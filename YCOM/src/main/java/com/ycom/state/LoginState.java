package com.ycom.state;

import com.ycom.account.Account;
import com.ycom.account.AccountStore;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.system.InputSystem;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class LoginState implements GameState {
    private enum Mode { LOGIN, REGISTER }
    private enum Focus { USERNAME, PASSWORD, CONFIRM }

    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;

    private final StringBuilder username = new StringBuilder();
    private final StringBuilder password = new StringBuilder();
    private final StringBuilder confirm = new StringBuilder();
    private Mode mode = Mode.LOGIN;
    private Focus focus = Focus.USERNAME;
    private String errorMessage = "";

    private final javafx.event.EventHandler<KeyEvent> typedHandler = this::onKeyTyped;
    private Scene attachedScene;

    private double time = 0.0;

    public LoginState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }

    @Override
    public void onEnter() {
        AccountStore.load();
        username.setLength(0);
        password.setLength(0);
        confirm.setLength(0);
        mode = Mode.LOGIN;
        errorMessage = "";
        time = 0.0;

        String last = AccountStore.loadLastUsername();
        if (last != null && !last.isEmpty() && last.length() <= AccountStore.MAX_USERNAME) {
            username.append(last);
            focus = Focus.PASSWORD;
        } else {
            focus = Focus.USERNAME;
        }

        attachedScene = canvas.getScene();
        if (attachedScene != null) {
            attachedScene.addEventFilter(KeyEvent.KEY_TYPED, typedHandler);
        }
    }

    @Override
    public void onExit() {
        if (attachedScene != null) {
            attachedScene.removeEventFilter(KeyEvent.KEY_TYPED, typedHandler);
            attachedScene = null;
        }
    }

    @Override
    public void update(double dt) {
        time += dt;

        if (input.isKeyJustPressed(KeyCode.BACK_SPACE)) {
            StringBuilder target = focusedBuffer();
            if (target.length() > 0) target.deleteCharAt(target.length() - 1);
        }
        if (input.isKeyJustPressed(KeyCode.TAB)) {
            cycleFocus();
        }
        if (input.isKeyJustPressed(KeyCode.ENTER)) {
            submitCurrentMode();
            return;
        }

        if (!input.isMouseJustClicked()) return;
        double mx = input.getMouseX();
        double my = input.getMouseY();

        if (fieldRect(0).contains(mx, my)) focus = Focus.USERNAME;
        else if (fieldRect(1).contains(mx, my)) focus = Focus.PASSWORD;
        else if (mode == Mode.REGISTER && fieldRect(2).contains(mx, my)) focus = Focus.CONFIRM;
        
        else if (btnLogin().contains(mx, my)) {
            if (mode == Mode.LOGIN) tryLogin(); else switchMode(Mode.LOGIN);
        }
        else if (btnRegister().contains(mx, my)) {
            if (mode == Mode.REGISTER) tryRegister(); else switchMode(Mode.REGISTER);
        }
        else if (btnGuest().contains(mx, my)) {
            Session.enterAsGuest();
            gsm.setState(StateId.MENU);
        }
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        UIUtils.drawBackgroundAndTrack(gc, time);
        
        drawFormContent(gc);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawFormContent(GraphicsContext gc) {
        String titleText = mode == Mode.LOGIN ? "LOGIN MODE" : "REGISTER MODE";
        gc.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 36));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(6.0);
        gc.strokeText(titleText, 90, 460);
        gc.setFill(UIUtils.YELLOW);
        gc.fillText(titleText, 90, 460);

        drawNeoField(gc, "USERNAME", username.toString(), focus == Focus.USERNAME, false, fieldRect(0));
        drawNeoField(gc, "PASSWORD", password.toString(), focus == Focus.PASSWORD, true, fieldRect(1));
        
        if (mode == Mode.REGISTER) {
            drawNeoField(gc, "CONFIRM", confirm.toString(), focus == Focus.CONFIRM, true, fieldRect(2));
        }

        UIUtils.drawNeoButton(gc, input, btnLogin());
        UIUtils.drawNeoButton(gc, input, btnRegister());
        UIUtils.drawNeoButton(gc, input, btnGuest());

        if (!errorMessage.isEmpty()) {
            gc.setFill(UIUtils.RED);
            gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 24));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setStroke(UIUtils.BORDER);
            gc.setLineWidth(4.0);
            gc.strokeText(errorMessage, 90, mode == Mode.REGISTER ? 950 : 860);
            gc.fillText(errorMessage, 90, mode == Mode.REGISTER ? 950 : 860);
        }
    }

    private void drawNeoField(GraphicsContext gc, String label, String text, boolean focused, boolean mask, ButtonRect r) {
        // Shadow
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(r.x + 6, r.y + 6, r.w, r.h);

        // Fill
        gc.setFill(focused ? UIUtils.brighten(UIUtils.WHITE, -0.1) : UIUtils.WHITE);
        gc.fillRect(r.x, r.y, r.w, r.h);

        // Border
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(4.0);
        gc.strokeRect(r.x, r.y, r.w, r.h);

        // Label
        gc.setFill(UIUtils.BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(label, r.x, r.y - 8);

        // Text
        String display = mask ? "*".repeat(text.length()) : text;
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 30));
        gc.fillText(display + (focused && (System.currentTimeMillis() / 500 % 2 == 0) ? "_" : ""), r.x + 15, r.y + r.h / 2.0 + 10);
    }

    private void onKeyTyped(KeyEvent e) {
        String ch = e.getCharacter();
        if (ch == null || ch.isEmpty()) return;
        char c = ch.charAt(0);

        if (c < 0x20 || c == 0x7F || !Character.isLetterOrDigit(c) || c > 0x7A) return;

        StringBuilder target = focusedBuffer();
        int max = (focus == Focus.USERNAME) ? AccountStore.MAX_USERNAME : AccountStore.MAX_PASSWORD;
        if (target.length() >= max) return;
        target.append(c);
    }

    private StringBuilder focusedBuffer() {
        return switch (focus) {
            case USERNAME -> username;
            case PASSWORD -> password;
            case CONFIRM -> confirm;
        };
    }

    private void cycleFocus() {
        if (mode == Mode.LOGIN) focus = (focus == Focus.USERNAME) ? Focus.PASSWORD : Focus.USERNAME;
        else focus = switch (focus) {
            case USERNAME -> Focus.PASSWORD;
            case PASSWORD -> Focus.CONFIRM;
            case CONFIRM -> Focus.USERNAME;
        };
    }

    private void submitCurrentMode() {
        if (mode == Mode.LOGIN) tryLogin(); else tryRegister();
    }

    private void switchMode(Mode m) {
        mode = m;
        errorMessage = "";
        confirm.setLength(0);
        if (focus == Focus.CONFIRM) focus = Focus.USERNAME;
    }

    private void tryLogin() {
        if (username.length() == 0 || password.length() == 0) {
            errorMessage = "Username and password required."; return;
        }
        Account acc = AccountStore.authenticate(username.toString(), password.toString());
        if (acc == null) {
            errorMessage = "Wrong username or password."; password.setLength(0); return;
        }
        Session.login(acc);
        gsm.setState(StateId.MENU);
    }

    private void tryRegister() {
        if (username.length() == 0 || password.length() == 0 || confirm.length() == 0) {
            errorMessage = "All three fields are required."; return;
        }
        if (!password.toString().equals(confirm.toString())) {
            errorMessage = "Passwords do not match."; confirm.setLength(0); focus = Focus.CONFIRM; return;
        }
        try {
            Account acc = AccountStore.register(username.toString(), password.toString());
            Session.login(acc);
            gsm.setState(StateId.MENU);
        } catch (IllegalStateException dup) {
            errorMessage = "Username already taken.";
        } catch (IllegalArgumentException bad) {
            errorMessage = bad.getMessage();
        }
    }

    private ButtonRect fieldRect(int row) {
        double w = 450, h = 60;
        double x = 90;
        double y = 500 + row * 90;
        return new ButtonRect(x, y, w, h);
    }

    private UIUtils.NeoBtn btnLogin() {
        double w = 215, h = 60;
        double x = 90;
        double y = mode == Mode.REGISTER ? 790 : 700;
        Color c = mode == Mode.LOGIN ? UIUtils.CYAN : UIUtils.WHITE;
        return new UIUtils.NeoBtn(x, y, w, h, c, "LOGIN", 24);
    }

    private UIUtils.NeoBtn btnRegister() {
        double w = 215, h = 60;
        double x = 90 + 235;
        double y = mode == Mode.REGISTER ? 790 : 700;
        Color c = mode == Mode.REGISTER ? UIUtils.CYAN : UIUtils.WHITE;
        return new UIUtils.NeoBtn(x, y, w, h, c, "REGISTER", 24);
    }

    private UIUtils.NeoBtn btnGuest() {
        double w = 450, h = 60;
        double x = 90;
        double y = mode == Mode.REGISTER ? 870 : 780;
        return new UIUtils.NeoBtn(x, y, w, h, UIUtils.PINK, "GUEST", 24);
    }

    private record ButtonRect(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }
}
