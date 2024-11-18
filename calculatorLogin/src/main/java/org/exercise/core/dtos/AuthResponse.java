package org.exercise.core.dtos;

public record AuthResponse(String id, String username, String email, Boolean active, Integer balance, String accessToken) {
}
