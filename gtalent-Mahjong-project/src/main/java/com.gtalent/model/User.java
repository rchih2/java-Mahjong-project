package com.gtalent.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String password;
    private int score;
    private int wins;
    private int losses;
    private LocalDateTime registrationTime;

    public User() {
        this.score = 25000; // 預設分數
        this.wins = 0;
        this.losses = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public LocalDateTime getRegistrationTime() { return registrationTime; }
    public void setRegistrationTime(LocalDateTime registrationTime) { this.registrationTime = registrationTime; }
}
