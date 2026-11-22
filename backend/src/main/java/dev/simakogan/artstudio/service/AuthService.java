package dev.simakogan.artstudio.service;

import dev.simakogan.artstudio.domain.Role;
import dev.simakogan.artstudio.domain.User;
import dev.simakogan.artstudio.dto.Dtos.AuthResponse;
import dev.simakogan.artstudio.dto.Dtos.LoginRequest;
import dev.simakogan.artstudio.dto.Dtos.RegisterRequest;
import dev.simakogan.artstudio.dto.Dtos.UserDto;
import dev.simakogan.artstudio.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder encoder, TokenService tokens) {
        this.users = users;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw Errors.conflict("An account with this email already exists");
        }
        User user = users.save(new User(email, encoder.encode(req.password()), req.fullName().trim(), Role.USER));
        return new AuthResponse(tokens.issue(user), Mappers.user(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> Errors.unauthorized("Wrong email or password"));
        return new AuthResponse(tokens.issue(user), Mappers.user(user));
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        return users.findById(userId).map(Mappers::user).orElseThrow(() -> Errors.notFound("User"));
    }
}
