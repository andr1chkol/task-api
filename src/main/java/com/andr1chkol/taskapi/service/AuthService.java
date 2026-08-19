package com.andr1chkol.taskapi.service;

import com.andr1chkol.taskapi.dto.LoginRequest;
import com.andr1chkol.taskapi.dto.RegisterRequest;
import com.andr1chkol.taskapi.exception.EmailAlreadyExistsException;
import com.andr1chkol.taskapi.model.User;
import com.andr1chkol.taskapi.repository.UserRepository;
import com.andr1chkol.taskapi.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {
        String normalizedEmail = registerRequest.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }
        String passwordHash = passwordEncoder.encode(registerRequest.getPassword());
        User user = new User(normalizedEmail, passwordHash);
        return userRepository.save(user);
    }

    public Authentication authenticate(LoginRequest loginRequest) {
        String normalizedEmail = loginRequest.getEmail().trim().toLowerCase(Locale.ROOT);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(normalizedEmail, loginRequest.getPassword());

        return authenticationManager.authenticate(authenticationToken);
    }

    public String login(LoginRequest loginRequest) {
        Authentication authentication = authenticate(loginRequest);
        return jwtService.generateToken(authentication.getName());
    }
}
