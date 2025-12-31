package com.academic.AIS.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create or update a student")
public class CreateStudentRequest {

    @Schema(
            description = "Student's first name",
            example = "Alice",
            required = true,
            minLength = 2,
            maxLength = 100
    )
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @Schema(
            description = "Student's last name",
            example = "Smith",
            required = true,
            minLength = 2,
            maxLength = 100
    )
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @Schema(
            description = "Student's email address (must be unique)",
            example = "alice.smith@university.edu",
            required = true,
            format = "email"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(
            description = "Study group ID to assign student to (optional)",
            example = "1",
            nullable = true
    )
    private Integer groupId;

    public CreateStudentRequest() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
}