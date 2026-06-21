package com.ycom.event;

public record GameOverEvent(String reason) implements GameEvent {
}
