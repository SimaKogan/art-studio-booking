package com.artstudio.booking.user;

import com.artstudio.booking.common.ApiException;
import com.artstudio.booking.security.JwtService;
import com.artstudio.booking.user.AuthDtos.AuthResponse;
import com.artstudio.booking.user.AuthDtos.LoginRequest;
import com.artstudio.booking.user.AuthDtos.RegisterRequest;
import com.artstudio.booking.user.AuthDtos.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("An account with this email already exists");
        }
        User user = users.save(new User(email, encoder.encode(req.password()), req.fullName().trim(), Role.USER));
        return new AuthResponse(jwt.issue(user), UserDto.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        return new AuthResponse(jwt.issue(user), UserDto.from(user));
    }

    @Transactional(readOnly = true)
    public UserDto me(String email) {
        return users.findByEmailIgnoreCase(email)
                .map(UserDto::from)
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
