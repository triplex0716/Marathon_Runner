package com.ycom.account;

public class Account {
    public String username;
    public String password;
    public int coins;
    public int capsules;
    public int highScore;

    public Account(String username, String password, int coins, int capsules, int highScore) {
        this.username = username;
        this.password = password;
        this.coins = coins;
        this.capsules = capsules;
        this.highScore = highScore;
    }
}
