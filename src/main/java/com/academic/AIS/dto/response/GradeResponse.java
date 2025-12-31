package com.academic.AIS.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Grade information response")
public class GradeResponse {

    @Schema(description = "Grade ID", example = "1")
    private Integer gradeId;

    @Schema(description = "Student ID", example = "1")
    private Integer studentId;

    @Schema(description = "Student's full name", example = "Alice Smith")
    private String studentName;

    @Schema(description = "Subject ID", example = "1")
    private Integer subjectId;

    @Schema(description = "Subject name", example = "Mathematics")
    private String subjectName;

    @Schema(description = "Subject code", example = "MATH101")
    private String subjectCode;

    @Schema(description = "Grade value (0-10)", example = "8")
    private Integer gradeValue;

    @Schema(
            description = "Grade level description",
            example = "Good",
            allowableValues = {"Fail", "Satisfactory", "Good", "Excellent"}
    )
    private String gradeLevel;

    @Schema(description = "Date when grade was entered", example = "2024-12-30")
    private LocalDate gradeDate;

    @Schema(description = "Teacher's comments", example = "Good work")
    private String comments;

    @Schema(description = "Teacher's full name", example = "John Doe")
    private String teacherName;

    public GradeResponse() {}

    // Getters and setters
    public Integer getGradeId() { return gradeId; }
    public void setGradeId(Integer gradeId) { this.gradeId = gradeId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public Integer getGradeValue() { return gradeValue; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }
    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
    public LocalDate getGradeDate() { return gradeDate; }
    public void setGradeDate(LocalDate gradeDate) { this.gradeDate = gradeDate; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
}