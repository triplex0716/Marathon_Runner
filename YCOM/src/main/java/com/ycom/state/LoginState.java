package com.ycom.state;

import com.ycom.account.Account;
import com.ycom.account.AccountStore;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.core.TimeManager;
import com.ycom.resource.AssetManager;
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

    private static final double FIELD_W = 620.0;
    private static final double FIELD_H = 84.0;
    private static final double BUTTON_W = 320.0;
    private static final double BUTTON_H = 100.0;

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
        if (!input.isMouseJustClicked()) {
            return;
        }
        double mx = input.getMouseX();
        double my = input.getMouseY();

        if (usernameField().contains(mx, my)) {
            focus = Focus.USERNAME;
            return;
        }
        if (passwordField().contains(mx, my)) {
            focus = Focus.PASSWORD;
            return;
        }
        if (mode == Mode.REGISTER && confirmField().contains(mx, my)) {
            focus = Focus.CONFIRM;
            return;
        }
        if (loginButton().contains(mx, my)) {
            onLoginButton();
            return;
        }
        if (registerButton().contains(mx, my)) {
            onRegisterButton();
            return;
        }
        if (guestButton().contains(mx, my)) {
            Session.enterAsGuest();
            gsm.setState("MENU");
        }
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        if (AssetManager.background() != null) {
            gc.drawImage(AssetManager.background(), 0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
            gc.setFill(Color.rgb(124, 183, 213, 0.58));
            gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        } else {
            gc.setFill(Color.web("#101820"));
            gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 78));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("YOU CAN'T OUTRUN ME!", Config.LOGICAL_WIDTH / 2.0, 200.0);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 30));
        gc.setFill(Color.rgb(255, 255, 255, 0.75));
        gc.fillText(mode == Mode.REGISTER ? "REGISTER MODE  (A-Z, a-z, 0-9 only; password >= 6)"
                                          : "LOGIN MODE  (A-Z, a-z, 0-9 only)",
                Config.LOGICAL_WIDTH / 2.0, 260.0);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 34));
        double labelX = (Config.LOGICAL_WIDTH - FIELD_W) / 2.0 - 30.0;
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.WHITE);
        gc.fillText("USER", labelX, usernameField().y + FIELD_H / 2.0 + 12.0);
        gc.fillText("PASS", labelX, passwordField().y + FIELD_H / 2.0 + 12.0);
        if (mode == Mode.REGISTER) {
            gc.fillText("CONF", labelX, confirmField().y + FIELD_H / 2.0 + 12.0);
        }

        drawField(gc, usernameField(), username.toString(), focus == Focus.USERNAME, false);
        drawField(gc, passwordField(), password.toString(), focus == Focus.PASSWORD, true);
        if (mode == Mode.REGISTER) {
            drawField(gc, confirmField(), confirm.toString(), focus == Focus.CONFIRM, true);
        }

        drawButton(gc, loginButton(), "LOGIN", Color.rgb(42, 86, 145, 0.88), mode == Mode.LOGIN);
        drawButton(gc, registerButton(), "REGISTER", Color.rgb(41, 120, 82, 0.88), mode == Mode.REGISTER);
        drawButton(gc, guestButton(), "GUEST", Color.rgb(120, 92, 36, 0.88), false);

        if (!errorMessage.isEmpty()) {
            gc.setFill(Color.rgb(255, 120, 120));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(errorMessage, Config.LOGICAL_WIDTH / 2.0, 940.0);
        }

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void onKeyTyped(KeyEvent e) {
        String ch = e.getCharacter();
        if (ch == null || ch.isEmpty()) return;
        char c = ch.charAt(0);

        if (c < 0x20 || c == 0x7F) return;
        if (!Character.isLetterOrDigit(c)) return;
        if (c > 0x7A) return;

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
        if (mode == Mode.LOGIN) {
            focus = (focus == Focus.USERNAME) ? Focus.PASSWORD : Focus.USERNAME;
        } else {
            focus = switch (focus) {
                case USERNAME -> Focus.PASSWORD;
                case PASSWORD -> Focus.CONFIRM;
                case CONFIRM -> Focus.USERNAME;
            };
        }
    }

    private void submitCurrentMode() {
        if (mode == Mode.LOGIN) tryLogin();
        else tryRegister();
    }

    private void onLoginButton() {
        if (mode == Mode.LOGIN) {
            tryLogin();
        } else {
            switchMode(Mode.LOGIN);
        }
    }

    private void onRegisterButton() {
        if (mode == Mode.REGISTER) {
            tryRegister();
        } else {
            switchMode(Mode.REGISTER);
        }
    }

    private void switchMode(Mode m) {
        mode = m;
        errorMessage = "";
        confirm.setLength(0);
        if (focus == Focus.CONFIRM) focus = Focus.USERNAME;
    }

    private void tryLogin() {
        if (username.length() == 0 || password.length() == 0) {
            errorMessage = "Username and password required.";
            return;
        }
        Account acc = AccountStore.authenticate(username.toString(), password.toString());
        if (acc == null) {
            errorMessage = "Wrong username or password.";
            password.setLength(0);
            return;
        }
        Session.login(acc);
        gsm.setState("MENU");
    }

    private void tryRegister() {
        if (username.length() == 0 || password.length() == 0 || confirm.length() == 0) {
            errorMessage = "All three fields are required.";
            return;
        }
        if (!password.toString().equals(confirm.toString())) {
            errorMessage = "Passwords do not match.";
            confirm.setLength(0);
            focus = Focus.CONFIRM;
            return;
        }
        try {
            Account acc = AccountStore.register(username.toString(), password.toString());
            Session.login(acc);
            gsm.setState("MENU");
        } catch (IllegalStateException dup) {
            errorMessage = "Username already taken.";
        } catch (IllegalArgumentException bad) {
            switch (bad.getMessage()) {
                case "SHORT" -> errorMessage = "Password must be at least 6 characters.";
                case "TOO_LONG" -> errorMessage = "Username/password too long.";
                case "BAD_CHAR" -> errorMessage = "Username has invalid characters.";
                default -> errorMessage = "Invalid username or password.";
            }
        }
    }

    private void drawField(GraphicsContext gc, ButtonRect r, String text, boolean focused, boolean mask) {
        gc.setFill(focused ? Color.rgb(255, 255, 255, 0.18) : Color.rgb(0, 0, 0, 0.32));
        gc.fillRoundRect(r.x, r.y, r.w, r.h, 8.0, 8.0);
        gc.setStroke(focused ? Color.rgb(255, 220, 90) : Color.rgb(255, 255, 255, 0.7));
        gc.setLineWidth(focused ? 4.0 : 2.5);
        gc.strokeRoundRect(r.x, r.y, r.w, r.h, 8.0, 8.0);

        String display = mask ? "*".repeat(text.length()) : text;
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.NORMAL, 38));
        gc.setTextAlign(TextAlignment.LEFT);
        double textY = r.y + r.h / 2.0 + 14.0;
        gc.fillText(display, r.x + 20.0, textY);

        if (focused) {
            boolean caretOn = ((long) (TimeManager.getElapsedTime() * 2)) % 2 == 0;
            if (caretOn) {
                double caretX = r.x + 20.0 + gc.getFont().getSize() * 0.52 * display.length();
                gc.setStroke(Color.rgb(255, 220, 90));
                gc.setLineWidth(3.0);
                gc.strokeLine(caretX, r.y + 14.0, caretX, r.y + r.h - 14.0);
            }
        }
    }

    private void drawButton(GraphicsContext gc, ButtonRect r, String label, Color fill, boolean active) {
        boolean hovered = r.contains(input.getMouseX(), input.getMouseY());
        if (active) {
            gc.setFill(fill);
            gc.fillRoundRect(r.x, r.y, r.w, r.h, 8.0, 8.0);
        } else if (hovered) {
            gc.setFill(Color.rgb(fillR(fill), fillG(fill), fillB(fill), 0.35));
            gc.fillRoundRect(r.x, r.y, r.w, r.h, 8.0, 8.0);
        }
        gc.setStroke(active ? Color.rgb(255, 220, 90) : Color.rgb(255, 255, 255, 0.85));
        gc.setLineWidth(active ? 4.5 : 3.0);
        gc.strokeRoundRect(r.x, r.y, r.w, r.h, 8.0, 8.0);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, r.x + r.w / 2.0, r.y + r.h / 2.0 + 14.0);
    }

    private static int fillR(Color c) { return (int) (c.getRed() * 255); }
    private static int fillG(Color c) { return (int) (c.getGreen() * 255); }
    private static int fillB(Color c) { return (int) (c.getBlue() * 255); }

    private ButtonRect usernameField() {
        return new ButtonRect((Config.LOGICAL_WIDTH - FIELD_W) / 2.0, 340.0, FIELD_W, FIELD_H);
    }

    private ButtonRect passwordField() {
        return new ButtonRect((Config.LOGICAL_WIDTH - FIELD_W) / 2.0, 450.0, FIELD_W, FIELD_H);
    }

    private ButtonRect confirmField() {
        return new ButtonRect((Config.LOGICAL_WIDTH - FIELD_W) / 2.0, 560.0, FIELD_W, FIELD_H);
    }

    private ButtonRect loginButton() {
        return buttonRowRect(0);
    }

    private ButtonRect registerButton() {
        return buttonRowRect(1);
    }

    private ButtonRect guestButton() {
        return buttonRowRect(2);
    }

    private ButtonRect buttonRowRect(int idx) {
        double gap = 40.0;
        double totalW = BUTTON_W * 3 + gap * 2;
        double startX = (Config.LOGICAL_WIDTH - totalW) / 2.0;
        return new ButtonRect(startX + (BUTTON_W + gap) * idx, 760.0, BUTTON_W, BUTTON_H);
    }

    private record ButtonRect(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }
}
