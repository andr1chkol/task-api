package com.andr1chkol.taskapi.dto;

import com.andr1chkol.taskapi.model.Role;
import com.andr1chkol.taskapi.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Registered user information")
public class RegisterResponse {
    @Schema(
            description = "Unique user identifier",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final Long id;

    @Schema(
            description = "User email",
            example = "example@mail.com"
    )
    private final String email;

    @Schema(
            description = "User role",
            example = "USER"
    )
    private final Role role;

    @Schema(
            description = "User creation timestamp",
            example = "2026-08-13T18:30:00Z",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final Instant createdAt;

    public RegisterResponse(Long id, String email, Role role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static RegisterResponse from(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
