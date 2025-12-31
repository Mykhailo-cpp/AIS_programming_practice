package com.academic.AIS.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.academic.AIS.model.StudyGroup;

@Schema(description = "Student information response")
public class StudentResponse {

    @Schema(description = "Student ID", example = "1")
    private Integer studentId;

    @Schema(description = "First name", example = "Alice")
    private String firstName;

    @Schema(description = "Last name", example = "Smith")
    private String lastName;

    @Schema(description = "Email address", example = "alice.smith@university.edu")
    private String email;

    @Schema(description = "Username for login", example = "alice")
    private String username;

    @Schema(description = "Assigned study group", nullable = true)
    private StudyGroup group;

    public StudentResponse() {}

    public StudentResponse(Integer studentId, String firstName, String lastName,
                           String email, String username, StudyGroup group) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.group = group;
    }

    @Schema(description = "Student's full name", example = "Alice Smith")
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Getters and setters
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public StudyGroup getGroup() { return group; }
    public void setGroup(StudyGroup group) { this.group = group; }
}