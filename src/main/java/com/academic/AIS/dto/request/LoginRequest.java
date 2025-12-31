package com.academic.AIS.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login credentials")
public class LoginRequest {

    @Schema(
            description = "Username for authentication",
            example = "admin",
            required = true,
            minLength = 1
    )
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(
            description = "User password",
            example = "admin",
            required = true,
            minLength = 1,
            format = "password"
    )
    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}