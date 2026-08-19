package com.andr1chkol.taskapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required for login")
public class LoginRequest {
    @Schema(
            description = "Email",
            example = "example@mail.com"
    )
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email must not be blank")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Schema(
            description = "Password",
            example = "qwerty123",
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 72, message = "Password must contain between 8 and 72 characters")
    private String password;

    protected LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
