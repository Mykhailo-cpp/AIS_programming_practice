package com.academic.AIS.controller.api;

import com.academic.AIS.dto.request.LoginRequest;
import com.academic.AIS.dto.response.AuthResponse;
import com.academic.AIS.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;


import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and token management")
public class ApiAuthController {

    private final AuthenticationService authenticationService;

    @Autowired
    public ApiAuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = """
                    Authenticate user credentials and receive JWT tokens.
                    
                    Default admin credentials:
                    - Username: `admin`
                    - Password: `admin123`
                    
                    After successful login, use the `accessToken` in the Authorization header:
                    `Authorization: Bearer <accessToken>`
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful - returns access and refresh tokens",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Login",
                                    value = """
                                            {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 3600,
                                                "username": "admin",
                                                "role": "ADMINISTRATOR"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed - invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Invalid Credentials",
                                    value = """
                                            {
                                                "status": 401,
                                                "error": "Unauthorized",
                                                "message": "Invalid username or password"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - validation error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                            {
                                                "status": 400,
                                                "error": "Validation Failed",
                                                "fieldErrors": {
                                                    "username": "Username is required",
                                                    "password": "Password is required"
                                                }
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Admin Login",
                                            value = """
                                                    {
                                                        "username": "admin",
                                                        "password": "admin123"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Teacher Login",
                                            value = """
                                                    {
                                                        "username": "john",
                                                        "password": "Doe"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Student Login",
                                            value = """
                                                    {
                                                        "username": "mykhailo",
                                                        "password": "Osadchuk"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authenticationService.authenticate(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = """
                    Get a new access token using a valid refresh token.
                    Send the refresh token in the Authorization header.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "501",
                    description = "Not yet implemented",
                    content = @Content
            )
    })
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("Authorization") String refreshToken) {
        throw new UnsupportedOperationException("Refresh token endpoint not yet implemented");
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = """
                    Logout the current user. For stateless JWT authentication,
                    this is typically handled client-side by removing the token.
                    
                    Optional: Can implement server-side token blacklist.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid token",
                    content = @Content
            )
    })
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        // For stateless JWT, logout is typically handled client-side
        // Optionally implement token blacklist here
        return ResponseEntity.ok().build();
    }
}