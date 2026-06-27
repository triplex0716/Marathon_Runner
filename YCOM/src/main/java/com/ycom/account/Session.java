package com.ycom.account;

public class Session {
    private static final Account GUEST = new Account("__guest__", "", 0, 0, 0);
    private static Account current = null;

    public static void login(Account acc) {
        current = acc;
        AccountStore.saveLastUsername(acc.username);
    }

    public static void enterAsGuest() {
        GUEST.coins = 0;
        GUEST.capsules = 0;
        current = GUEST;
    }

    public static void logout() {
        current = null;
    }

    public static Account current() {
        return current;
    }

    public static boolean isGuest() {
        return current == GUEST;
    }

    public static boolean isLoggedIn() {
        return current != null && current != GUEST;
    }
}
