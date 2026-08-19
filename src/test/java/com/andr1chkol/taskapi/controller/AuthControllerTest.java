package com.andr1chkol.taskapi.controller;

import com.andr1chkol.taskapi.dto.LoginRequest;
import com.andr1chkol.taskapi.dto.RegisterRequest;
import com.andr1chkol.taskapi.exception.EmailAlreadyExistsException;
import com.andr1chkol.taskapi.model.Role;
import com.andr1chkol.taskapi.model.User;
import com.andr1chkol.taskapi.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_whenRequestIsInvalid_returnsBadRequestAndDoesNotCallService() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("invalid", "short");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/auth/register"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.fieldErrors.password").value("Password must contain between 8 and 72 characters"));

        verifyNoInteractions(authService);
    }

    @Test
    void register_whenRequestIsValid_returnsCreatedAndSafeUserResponse() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "qwerty123");

        User user = new User("test@example.com", "bcrypt-hash");
        ReflectionTestUtils.setField(user, "id", 1L);

        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.role").value(Role.USER.name()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_whenEmailAlreadyExists_returnsConflict() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "qwerty123");

        when(authService.register(any(RegisterRequest.class))).thenThrow(new EmailAlreadyExistsException());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email is already registered"))
                .andExpect(jsonPath("$.path").value("/auth/register"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void login_whenRequestIsValid_returnsAccessToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "qwerty123");

        when(authService.login(any(LoginRequest.class))).thenReturn("generated-jwt");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken")
                        .value("generated-jwt"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_whenRequestIsInvalid_returnsBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest("invalid", "short");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/auth/login"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.fieldErrors.password").value("Password must contain between 8 and 72 characters"));

        verifyNoInteractions(authService);
    }

    @Test
    void login_whenCredentialsAreInvalid_returnsUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrong-password");

        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException(
                "Internal authentication message"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"))
                .andExpect(jsonPath("$.path").value("/auth/login"));

        verify(authService).login(any(LoginRequest.class));
    }
}
