package com.andr1chkol.taskapi;

import com.andr1chkol.taskapi.dto.LoginRequest;
import com.andr1chkol.taskapi.dto.RegisterRequest;
import com.andr1chkol.taskapi.model.Role;
import com.andr1chkol.taskapi.model.User;
import com.andr1chkol.taskapi.repository.UserRepository;
import com.andr1chkol.taskapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "security.jwt.secret="
                + "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "security.jwt.expiration-ms=900000"
})
@AutoConfigureMockMvc
@Testcontainers
public class AuthRegistrationIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void register_whenRequestIsValid_persistsUserWithBCryptPassword() throws Exception {
        String rawPassword = "qwerty123";

        RegisterRequest registerRequest = new RegisterRequest("Test@Example.COM", rawPassword);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        User savedUser = userRepository
                .findByEmail("test@example.com")
                .orElseThrow();

        assertNotEquals(rawPassword, savedUser.getPassword());
        assertTrue(passwordEncoder.matches(
                rawPassword,
                savedUser.getPassword()
        ));
        assertEquals(Role.USER, savedUser.getRole());
    }

    @Test
    void login_whenCredentialsAreValid_returnsAccessToken() throws Exception {
        String rawPassword = "qwerty123";

        User user = new User("test@example.com", passwordEncoder.encode(rawPassword));

        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("Test@Example.COM", rawPassword);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_whenCredentialsAreInvalid_returnsUnauthorized() throws Exception {
        User user = new User("test@example.com", passwordEncoder.encode("correct_password"));

        LoginRequest loginRequest = new LoginRequest("test@example.com", "incorrect_password");

        userRepository.save(user);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.path").value("/auth/login"));
    }

    @Test
    void getTasks_whenTokenIsMissing_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/tasks"))
                .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void getTasks_whenTokenIsMalformed_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer malformed-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/tasks"));
    }

    @Test
    void getTasks_whenTokenIsValid_returnsOk() throws Exception {
        User user = new User("test@example.com", passwordEncoder.encode("qwerty123"));

        userRepository.save(user);
        String accessToken =
                jwtService.generateToken(user.getEmail());

        mockMvc.perform(get("/tasks").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void deniedEndpoint_whenUserIsAuthenticated_returnsForbidden() throws Exception {
        User user = new User(
                "test@example.com",
                passwordEncoder.encode("qwerty123")
        );

        userRepository.save(user);

        String accessToken =
                jwtService.generateToken(user.getEmail());

        mockMvc.perform(get("/not-allowed")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access is denied"))
                .andExpect(jsonPath("$.path").value("/not-allowed"));
    }
}
