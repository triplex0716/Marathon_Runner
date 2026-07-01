package com.ycom.account;


public class Account {
    private String username;
    private String passwordHash;
    private int coins;
    private int capsules;
    private int highScore;

    public Account(String username, String passwordHash, int coins, int capsules, int highScore) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.coins = coins;
        this.capsules = capsules;
        this.highScore = highScore;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public int getCoins() { return coins; }
    public int getCapsules() { return capsules; }
    public int getHighScore() { return highScore; }

    public void addCoins(int amount) {
        if (amount > 0) this.coins += amount;
    }

    public boolean trySpendCoins(int amount) {
        if (amount > 0 && this.coins >= amount) {
            this.coins -= amount;
            return true;
        }
        return false;
    }

    public void addCapsules(int amount) {
        if (amount > 0) this.capsules += amount;
    }

    public boolean trySpendCapsule() {
        if (this.capsules > 0) {
            this.capsules--;
            return true;
        }
        return false;
    }

    public void tryUpdateHighScore(int score) {
        if (score > this.highScore) {
            this.highScore = score;
        }
    }

    public boolean checkPassword(String password) {
        return this.passwordHash.equals(password);
    }

}
