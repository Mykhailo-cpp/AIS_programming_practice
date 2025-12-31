package com.academic.AIS.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ));

    }

    private Info apiInfo() {
        return new Info()
                .title("Academic Information System API")
                .description("""
                        RESTful API for Academic Information System (AIS).
                        \s
                        ## Features
                        - JWT-based authentication
                        - Role-based access control (ADMINISTRATOR, TEACHER, STUDENT)
                        - Student, Teacher, Subject, and Grade management
                        - Complete CRUD operations
                        \s
                        ## Authentication
                        1. Login via `/api/auth/login` with username and password
                        2. Copy the `accessToken` from the response
                        3. Click the "Authorize" button (🔒) at the top of this page
                        4. Enter: `Bearer <your-access-token>`
                        5. Click "Authorize" and "Close"
                        \s
                        ## Roles & Permissions
                        - **ADMINISTRATOR**: Full system access
                        - Manage students, teachers, subjects, groups
                        - View system statistics
                        - Access all endpoints under `/api/admin/*`
                        \s
                        - **TEACHER**: Grade management
                        - View assigned subjects and students
                        - Create, update, and delete grades
                        - Access endpoints under `/api/teacher/*`
                        \s
                        - **STUDENT**: Read-only access
                        - View own grades and information
                        - Limited endpoints access
                        \s
                        ## Error Codes
                        - **400 Bad Request**: Invalid input data or validation errors
                        - **401 Unauthorized**: Missing or invalid JWT token
                        - **403 Forbidden**: Insufficient permissions for the operation
                        - **404 Not Found**: Requested resource does not exist
                        - **409 Conflict**: Resource conflict (e.g., duplicate email)
                        - **500 Internal Server Error**: Unexpected server error
                        \s
                        ## Rate Limiting
                        API requests may be rate limited. Check response headers for limits.
                        """)
                .version("1.0.0");

    }
}