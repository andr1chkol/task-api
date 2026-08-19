package com.andr1chkol.taskapi.service;

import com.andr1chkol.taskapi.dto.LoginRequest;
import com.andr1chkol.taskapi.dto.RegisterRequest;
import com.andr1chkol.taskapi.exception.EmailAlreadyExistsException;
import com.andr1chkol.taskapi.model.Role;
import com.andr1chkol.taskapi.model.User;
import com.andr1chkol.taskapi.repository.UserRepository;
import com.andr1chkol.taskapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Test
    void register_whenRequestIsValid_normalizesEmailHashesPasswordAndSavesUser() {
        RegisterRequest registerRequest = new RegisterRequest("  Test@example.COM  ", "qwerty123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register(registerRequest);

        assertEquals("test@example.com", result.getEmail());
        assertEquals("bcrypt-hash", result.getPassword());
        assertNotEquals("qwerty123", result.getPassword());
        assertEquals(Role.USER, result.getRole());
        assertNotNull(result.getCreatedAt());

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(result);
    }

    @Test
    void register_whenEmailAlreadyExists_throwsEmailAlreadyExistsExceptionAndDoesNotSaveUser() {
        RegisterRequest registerRequest = new RegisterRequest("  Test@example.COM  ", "qwerty123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        EmailAlreadyExistsException exception =
                assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerRequest));

        assertEquals("Email is already registered", exception.getMessage());

        verify(userRepository).existsByEmail("test@example.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_whenCredentialsAreProvided_delegatesToAuthenticationManager() {
        LoginRequest loginRequest =
                new LoginRequest("  Test@Example.COM  ", "qwerty123");

        Authentication expectedAuthentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(expectedAuthentication);

        Authentication result = authService.authenticate(loginRequest);

        assertSame(expectedAuthentication, result);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(
                        UsernamePasswordAuthenticationToken.class
                );

        verify(authenticationManager).authenticate(tokenCaptor.capture());

        UsernamePasswordAuthenticationToken authenticationRequest =
                tokenCaptor.getValue();

        assertEquals(
                "test@example.com",
                authenticationRequest.getPrincipal()
        );
        assertEquals(
                "qwerty123",
                authenticationRequest.getCredentials()
        );
        assertFalse(authenticationRequest.isAuthenticated());

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void authenticate_whenCredentialsAreInvalid_throwsBadCredentialsException() {
        LoginRequest loginRequest =
                new LoginRequest("test@example.com", "wrong-password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticate(loginRequest)
        );

        assertEquals("Bad credentials", exception.getMessage());

        verify(authenticationManager).authenticate(any(Authentication.class));
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void login_whenAuthenticationSucceed_generatesTokenForAuthenticatedUser() {
        LoginRequest loginRequest =
                new LoginRequest("test@example.com", "qwerty123");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(jwtService.generateToken("test@example.com")).thenReturn("generated-jwt");

        String result = authService.login(loginRequest);

        assertEquals("generated-jwt", result);

        verify(authenticationManager)
                .authenticate(any(Authentication.class));
        verify(jwtService)
                .generateToken("test@example.com");
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void login_whenAuthenticationFails_doesNotGenerateToken() {
        LoginRequest loginRequest =
                new LoginRequest("test@example.com", "qwerty123");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        verify(authenticationManager)
                .authenticate(any(Authentication.class));
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepository, passwordEncoder);
    }

}
