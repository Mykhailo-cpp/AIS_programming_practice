package com.academic.AIS.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;


@Schema(description = "Request to create a new grade for a student")
public class CreateGradeRequest {

    @Schema(
            description = "Subject assignment ID",
            example = "1",
            required = true
    )
    @NotNull(message = "Assignment ID is required")
    private Integer assignmentId;

    @Schema(
            description = "Student ID receiving the grade",
            example = "1",
            required = true
    )
    @NotNull(message = "Student ID is required")
    private Integer studentId;

    @Schema(
            description = "Grade value on 0-10 scale",
            example = "8",
            required = true,
            minimum = "0",
            maximum = "10"
    )
    @NotNull(message = "Grade value is required")
    @Min(value = 0, message = "Grade must be at least 0")
    @Max(value = 10, message = "Grade must be at most 10")
    private Integer gradeValue;

    @Schema(
            description = "Optional teacher comments about the grade",
            example = "Excellent work on final exam",
            maxLength = 500
    )
    @Size(max = 500, message = "Comments must not exceed 500 characters")
    private String comments;

    public CreateGradeRequest() {}

    public Integer getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Integer assignmentId) {
        this.assignmentId = assignmentId;
    }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    public Integer getGradeValue() { return gradeValue; }
    public void setGradeValue(Integer gradeValue) {
        this.gradeValue = gradeValue;
    }
    public String getComments() { return comments; }
    public void setComments(String comments) {
        this.comments = comments;
    }
}
