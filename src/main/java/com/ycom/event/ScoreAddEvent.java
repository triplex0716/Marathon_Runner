package com.ycom.event;

public record ScoreAddEvent(int points, String reason) implements GameEvent {
}
