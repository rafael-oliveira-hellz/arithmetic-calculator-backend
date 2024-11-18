package org.exercise.core.interfaces;

import org.exercise.core.dtos.AuthResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    AuthResponse authenticate(String username, String password);
}

