package org.exercise.core.dtos;

import java.util.UUID;

public record AuthResponse(UUID id, String username, String email, Boolean active, Integer balance, String accessToken) {
}
