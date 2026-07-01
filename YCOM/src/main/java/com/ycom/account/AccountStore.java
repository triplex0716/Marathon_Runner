package com.ycom.account;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountStore {
    public static final int MIN_USERNAME = 3;
    public static final int MAX_USERNAME = 16;
    public static final int MIN_PASSWORD = 6;
    public static final int MAX_PASSWORD = 32;

    private static final Path DIR = Paths.get(System.getProperty("user.home"), ".marathon_runner");
    private static final Path FILE = DIR.resolve("accounts.dat");
    private static final Path BACKUP = DIR.resolve("accounts.dat.bak");
    private static final Path LAST_USER_FILE = DIR.resolve("last_user.txt");

    private static final Map<String, Account> accounts = new LinkedHashMap<>();
    private static boolean loaded = false;

    public static synchronized void load() {
        accounts.clear();
        loaded = true;
        if (!Files.exists(FILE)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(FILE, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(";", -1);
                if (parts.length != 4 && parts.length != 5) {
                    System.err.println("AccountStore: skipped malformed line " + (i + 1));
                    continue;
                }
                try {
                    int high = parts.length == 5 ? Integer.parseInt(parts[4]) : 0;
                    Account acc = new Account(
                            parts[0],
                            parts[1],
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]),
                            high);
                    accounts.put(acc.getUsername(), acc);
                } catch (NumberFormatException ex) {
                    System.err.println("AccountStore: skipped line " + (i + 1) + " (bad number)");
                }
            }
        } catch (IOException ex) {
            System.err.println("AccountStore: load failed - " + ex.getMessage());
        }
    }

    public static synchronized Account register(String username, String password) {
        ensureLoaded();
        validateUsername(username);
        validatePassword(password);
        if (accounts.containsKey(username)) {
            throw new IllegalStateException("DUPLICATE");
        }
        Account acc = new Account(username, password, 0, 0, 0);
        accounts.put(username, acc);
        save();
        return acc;
    }

    public static synchronized Account authenticate(String username, String password) {
        ensureLoaded();
        Account acc = accounts.get(username);
        if (acc == null || !acc.checkPassword(password)) {
            return null;
        }
        return acc;
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(DIR);
            if (Files.exists(FILE)) {
                Files.copy(FILE, BACKUP, StandardCopyOption.REPLACE_EXISTING);
            }
            List<String> lines = new ArrayList<>(accounts.size());
            for (Account a : accounts.values()) {
                lines.add(a.getUsername() + ";" + a.getPasswordHash() + ";" + a.getCoins() + ";" + a.getCapsules() + ";" + a.getHighScore());
            }
            Files.write(FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.println("AccountStore: save failed - " + ex.getMessage());
        }
    }

    public static synchronized void saveLastUsername(String username) {
        try {
            Files.createDirectories(DIR);
            Files.write(LAST_USER_FILE, (username == null ? "" : username).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            System.err.println("AccountStore: saveLastUsername failed - " + ex.getMessage());
        }
    }

    public static synchronized String loadLastUsername() {
        if (!Files.exists(LAST_USER_FILE)) return "";
        try {
            String s = new String(Files.readAllBytes(LAST_USER_FILE), StandardCharsets.UTF_8).trim();
            return s;
        } catch (IOException ex) {
            return "";
        }
    }

    private static void ensureLoaded() {
        if (!loaded) load();
    }

    private static void validateUsername(String s) {
        if (s == null || s.isEmpty()) throw new IllegalArgumentException("EMPTY");
        if (s.length() < MIN_USERNAME) throw new IllegalArgumentException("USER_SHORT");
        if (s.length() > MAX_USERNAME) throw new IllegalArgumentException("TOO_LONG");
        if (s.indexOf(';') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("BAD_CHAR");
        }
    }

    private static void validatePassword(String s) {
        if (s == null || s.isEmpty()) throw new IllegalArgumentException("EMPTY");
        if (s.length() < MIN_PASSWORD) throw new IllegalArgumentException("SHORT");
        if (s.length() > MAX_PASSWORD) throw new IllegalArgumentException("TOO_LONG");
    }
}
