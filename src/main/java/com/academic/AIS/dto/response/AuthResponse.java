package com.academic.AIS.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response with JWT tokens")
public class AuthResponse {

    @Schema(
            description = "JWT access token for API authentication",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;

    @Schema(
            description = "JWT refresh token for obtaining new access tokens",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String refreshToken;

    @Schema(
            description = "Token type (always Bearer)",
            example = "Bearer",
            defaultValue = "Bearer"
    )
    private String tokenType = "Bearer";

    @Schema(
            description = "Authenticated username",
            example = "admin"
    )
    private String username;

    @Schema(
            description = "User role",
            example = "ADMINISTRATOR",
            allowableValues = {"ADMINISTRATOR", "TEACHER", "STUDENT"}
    )
    private String role;

    @Schema(
            description = "User ID",
            example = "1"
    )
    private Integer userId;

    @Schema(
            description = "User's display name",
            example = "John Doe"
    )
    private String displayName;

    public AuthResponse(String accessToken, String refreshToken, String username,
                        String role, Integer userId, String displayName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
        this.userId = userId;
        this.displayName = displayName;
    }

    // Getters and setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}