package com.andr1chkol.taskapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Successful login response")
public class LoginResponse {

    @Schema(
            description = "JWT access token",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    private final String accessToken;

    @Schema(
            description = "Authentication scheme",
            example = "Bearer"
    )
    private final String tokenType;

    public LoginResponse(String accessToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}
