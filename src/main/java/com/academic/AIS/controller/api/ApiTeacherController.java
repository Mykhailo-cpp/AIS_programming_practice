package com.academic.AIS.controller.api;

import com.academic.AIS.dto.mapper.GradeMapper;
import com.academic.AIS.dto.request.CreateGradeRequest;
import com.academic.AIS.dto.response.GradeResponse;
import com.academic.AIS.model.Grade;
import com.academic.AIS.model.SubjectAssignment;
import com.academic.AIS.model.Teacher;
import com.academic.AIS.service.GradeService;
import com.academic.AIS.service.TeacherManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
@Tag(
        name = "Teacher Management",
        description = "APIs for teacher operations including grade management, assignments, and statistics. Requires TEACHER role authentication."
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('TEACHER')")
@Validated
public class ApiTeacherController {

    private final TeacherManagementService teacherManagementService;
    private final GradeService gradeService;
    private final GradeMapper gradeMapper;

    @Autowired
    public ApiTeacherController(TeacherManagementService teacherManagementService, GradeService gradeService, GradeMapper gradeMapper) {
        this.teacherManagementService = teacherManagementService;
        this.gradeService = gradeService;
        this.gradeMapper = gradeMapper;
    }

    private Integer getCurrentTeacherId(Authentication authentication) {
        String username = authentication.getName();
        return teacherManagementService.getTeacherByUsername(username)
                .map(Teacher::getTeacherId)
                .orElseThrow(() -> new IllegalStateException("Teacher profile not found for user: " + username));
    }

    // ==================== ASSIGNMENTS ====================

    @GetMapping("/assignments")
    @Operation(
            summary = "Get teacher assignments",
            description = """
                    Retrieve all subject assignments for the currently authenticated teacher.
                    
                    **Returns:**
                    - List of assignments with subject, group, and student information
                    - Academic year and semester details
                    - Associated student list for each assignment
                    
                    **Use Case:** Display teacher's teaching schedule and assigned classes.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved assignments",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubjectAssignment.class),
                            examples = @ExampleObject(
                                    name = "Teacher Assignments",
                                    value = """
                                            [
                                                {
                                                    "assignmentId": 1,
                                                    "subject": {
                                                        "subjectId": 1,
                                                        "subjectName": "Mathematics",
                                                        "subjectCode": "MATH101",
                                                        "credits": 4
                                                    },
                                                    "teacher": {
                                                        "teacherId": 1,
                                                        "firstName": "John",
                                                        "lastName": "Doe"
                                                    },
                                                    "group": {
                                                        "groupId": 1,
                                                        "groupName": "CS-21",
                                                        "year": 2021,
                                                        "students": [
                                                            {
                                                                "studentId": 1,
                                                                "firstName": "Alice",
                                                                "lastName": "Smith"
                                                            }
                                                        ]
                                                    },
                                                    "academicYear": "2024/2025",
                                                    "semester": "Fall"
                                                }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have TEACHER role",
                    content = @Content
            )
    })
    public ResponseEntity<List<SubjectAssignment>> getAssignments(Authentication authentication) {
        Integer teacherId = getCurrentTeacherId(authentication);
        List<SubjectAssignment> assignments = teacherManagementService.getTeacherAssignments(teacherId);
        return ResponseEntity.ok(assignments);
    }

    // ==================== STATISTICS ====================

    @GetMapping("/statistics")
    @Operation(
            summary = "Get teacher statistics",
            description = """
                    Retrieve comprehensive teaching statistics for the authenticated teacher.
                    
                    **Includes:**
                    - Total number of subjects taught
                    - Total number of unique students
                    - Total grades entered
                    - Average grade across all subjects
                    
                    **Use Case:** Dashboard overview and performance metrics.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Teacher Statistics",
                                    value = """
                                            {
                                                "totalSubjects": 3,
                                                "totalStudents": 45,
                                                "totalGrades": 120,
                                                "averageGrade": 7.8
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content
            )
    })
    public ResponseEntity<Map<String, Object>> getStatistics(Authentication authentication) {
        Integer teacherId = getCurrentTeacherId(authentication);

        List<SubjectAssignment> assignments = teacherManagementService.getTeacherAssignments(teacherId);
        long totalGrades = teacherManagementService.countTeacherGrades(teacherId);

        int totalStudents = assignments.stream()
                .filter(a -> a != null && a.getGroup() != null && a.getGroup().getStudents() != null)
                .flatMapToInt(a -> a.getGroup().getStudents().stream().mapToInt(s -> 1))
                .sum();

        List<Grade> teacherGrades = gradeService.getTeacherGrades(teacherId);
        double averageGrade = teacherGrades.isEmpty() ? 0.0 :
                teacherGrades.stream().mapToDouble(Grade::getGradeValue).average().orElse(0.0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSubjects", assignments.size());
        stats.put("totalStudents", totalStudents);
        stats.put("totalGrades", totalGrades);
        stats.put("averageGrade", Math.round(averageGrade * 100.0) / 100.0);

        return ResponseEntity.ok(stats);
    }

    // ==================== GRADES ====================

    @GetMapping("/grades")
    @Operation(
            summary = "Get all grades",
            description = """
                    Retrieve all grades entered by the authenticated teacher across all subjects.
                    
                    **Returns:** Complete list of grades with student and assignment details.
                    
                    **Note:** This may return a large dataset. Consider using filtered endpoints for better performance.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Grades retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Grade.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<List<GradeResponse>> getAllGrades(
            Authentication authentication) {
        Integer teacherId = getCurrentTeacherId(authentication);
        List<Grade> grades = gradeService.getTeacherGrades(teacherId);

        List<GradeResponse> responses = grades.stream()
                .map(gradeMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/grades/subject/{subjectId}")
    @Operation(
            summary = "Get grades by subject",
            description = """
                    Retrieve all grades for a specific subject taught by the authenticated teacher.
                    
                    **Use Case:** View all grades for a particular course or subject.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject grades retrieved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subject not found or not taught by teacher",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<List<Grade>> getGradesBySubject(
            @Parameter(description = "Subject ID", required = true, example = "1")
            @PathVariable Integer subjectId,
            Authentication authentication) {
        Integer teacherId = getCurrentTeacherId(authentication);
        List<Grade> grades = gradeService.getGradesForTeacherSubject(teacherId, subjectId);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/grades/assignment/{assignmentId}")
    @Operation(
            summary = "Get grades by assignment",
            description = """
                    Retrieve all grades for a specific subject-group assignment.
                    
                    **Validation:** Verifies that the teacher owns the specified assignment.
                    
                    **Use Case:** View grades for a specific class section.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Assignment grades retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Assignment not owned by teacher",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "status": 403,
                                                "error": "Forbidden",
                                                "message": "Assignment not found or access denied"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<List<Grade>> getGradesByAssignment(
            @Parameter(description = "Assignment ID", required = true, example = "1")
            @PathVariable Integer assignmentId,
            Authentication authentication) {
        Integer teacherId = getCurrentTeacherId(authentication);

        List<SubjectAssignment> assignments = teacherManagementService.getTeacherAssignments(teacherId);
        boolean ownsAssignment = assignments.stream()
                .anyMatch(a -> a.getAssignmentId().equals(assignmentId));

        if (!ownsAssignment) {
            throw new IllegalArgumentException("Assignment not found or access denied");
        }

        List<Grade> grades = gradeService.getGradesByAssignment(assignmentId);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/grades/{id}")
    @Operation(
            summary = "Get grade by ID",
            description = """
                    Retrieve a specific grade by its ID.
                    
                    **Validation:** Ensures the grade belongs to the authenticated teacher.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Grade retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "gradeId": 1,
                                                "gradeValue": 8.5,
                                                "comments": "Excellent work",
                                                "dateEntered": "2024-12-15T10:30:00",
                                                "student": {
                                                    "studentId": 1,
                                                    "firstName": "Alice",
                                                    "lastName": "Smith"
                                                },
                                                "assignment": {
                                                    "assignmentId": 1,
                                                    "subject": {
                                                        "subjectName": "Mathematics"
                                                    }
                                                }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Grade not found or access denied",
                    content = @Content
            )
    })
    public ResponseEntity<Grade> getGradeById(
            @Parameter(description = "Grade ID", required = true, example = "1")
            @PathVariable Integer id,
            Authentication authentication) {
        Integer teacherId = getCurrentTeacherId(authentication);

        List<Grade> teacherGrades = gradeService.getTeacherGrades(teacherId);
        Grade grade = teacherGrades.stream()
                .filter(g -> g.getGradeId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Grade not found or access denied"));

        return ResponseEntity.ok(grade);
    }

    @PostMapping("/grades")
    @Operation(
            summary = "Create grade",
            description = """
                    Enter a new grade for a student in an assigned subject.
                    
                    **Validation Rules:**
                    - Grade value must be between 0 and 10
                    - Teacher must own the assignment
                    - Student must be in the assignment's group
                    - No duplicate grades for the same student-assignment pair
                    
                    **Grade Scale:**
                    - 0-4: Failing
                    - 5-6: Satisfactory
                    - 7-8: Good
                    - 9-10: Excellent
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Grade created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Grade.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - validation failed",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "status": 400,
                                                "error": "Bad Request",
                                                "message": "Grade must be between 0 and 10",
                                                "fieldErrors": {
                                                    "gradeValue": "Grade must not exceed 10"
                                                }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - duplicate grade exists",
                    content = @Content
            )
    })
    public ResponseEntity<GradeResponse> createGrade(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Grade creation request",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateGradeRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "assignmentId": 1,
                                                "studentId": 1,
                                                "gradeValue": 8,
                                                "comments": "Excellent work on final exam"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody CreateGradeRequest request,
            Authentication authentication) {

        Integer teacherId = getCurrentTeacherId(authentication);

        Grade grade = gradeService.enterGrade(
                teacherId,
                request.getStudentId(),
                request.getAssignmentId(),
                request.getGradeValue(),
                request.getComments()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gradeMapper.toResponse(grade));
    }

    @PutMapping("/grades/{id}")
    @Operation(
            summary = "Update grade",
            description = """
                    Update an existing grade's value and comments.
                    
                    **Validation:**
                    - Grade must belong to the authenticated teacher
                    - New grade value must be between 0 and 10
                    
                    **Use Case:** Correct grading errors or update after re-evaluation.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Grade updated successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid grade value",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Grade not found or access denied",
                    content = @Content
            )
    })
    public ResponseEntity<GradeResponse> updateGrade(
            @Parameter(description = "Grade ID to update", required = true, example = "1")
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Grade update request",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "gradeValue": 9,
                                                "comments": "Improved significantly"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody CreateGradeRequest request,
            Authentication authentication) {

        Integer teacherId = getCurrentTeacherId(authentication);

        Grade grade = gradeService.updateGrade(
                id,
                teacherId,
                request.getGradeValue(),
                request.getComments()
        );

        return ResponseEntity.ok(gradeMapper.toResponse(grade));
    }

    @DeleteMapping("/grades/{id}")
    @Operation(
            summary = "Delete grade",
            description = """
                    Delete a grade entry.
                    
                    **Warning:** This action cannot be undone.
                    
                    **Validation:** Grade must belong to the authenticated teacher.
                    
                    **Use Case:** Remove incorrectly entered grades.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Grade deleted successfully - no content returned",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Grade not found or access denied",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "status": 404,
                                                "error": "Not Found",
                                                "message": "Grade with ID 999 not found or access denied"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - teacher does not own this grade",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteGrade(
            @Parameter(description = "Grade ID to delete", required = true, example = "1")
            @PathVariable @NotNull Integer id,
            Authentication authentication) {
        Integer teacherId = getCurrentTeacherId(authentication);
        gradeService.deleteGrade(id, teacherId);
        return ResponseEntity.noContent().build();
    }
}