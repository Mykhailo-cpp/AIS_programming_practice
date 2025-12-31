package com.academic.AIS.controller.api;

import com.academic.AIS.dto.request.CreateStudentRequest;
import com.academic.AIS.dto.response.StudentResponse;
import com.academic.AIS.service.StudentManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/students")
@Tag(name = "Student Management", description = "Admin APIs for managing student accounts and enrollment")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class ApiAdminStudentController {

    private final StudentManagementService studentManagementService;

    @Autowired
    public ApiAdminStudentController(StudentManagementService studentManagementService) {
        this.studentManagementService = studentManagementService;
    }

    @GetMapping
    @Operation(
            summary = "Get all students",
            description = "Retrieve a list of all students in the system with their details including group assignments"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of students",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have ADMINISTRATOR role",
                    content = @Content
            )
    })
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        List<StudentResponse> students = studentManagementService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get student by ID",
            description = "Retrieve detailed information about a specific student by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved student",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Student not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<StudentResponse> getStudentById(
            @Parameter(description = "Student ID", required = true, example = "1")
            @PathVariable Integer id) {
        StudentResponse student = studentManagementService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @PostMapping
    @Operation(
            summary = "Create a new student",
            description = "Create a new student account. Auto-generates username and password based on first name and last name."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Student created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<StudentResponse> createStudent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Student creation request with required fields",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateStudentRequest.class)
                    )
            )
            @Valid @RequestBody CreateStudentRequest request) {
        StudentResponse student = studentManagementService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(student);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update student information",
            description = "Update an existing student's personal information and group assignment"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Student updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Student not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<StudentResponse> updateStudent(
            @Parameter(description = "Student ID to update", required = true, example = "1")
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated student information",
                    required = true
            )
            @Valid @RequestBody CreateStudentRequest request) {
        StudentResponse student = studentManagementService.updateStudent(id, request);
        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a student",
            description = "Permanently delete a student account from the system. This action cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Student deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Student not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cannot delete student - may have associated grades",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<Void> deleteStudent(
            @Parameter(description = "Student ID to delete", required = true, example = "1")
            @PathVariable Integer id) {
        studentManagementService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{studentId}/group/{groupId}")
    @Operation(
            summary = "Assign student to a group",
            description = "Assign a student to a specific study group"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Student assigned to group successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Student or group not found",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<StudentResponse> assignToGroup(
            @Parameter(description = "Student ID", required = true, example = "1")
            @PathVariable Integer studentId,
            @Parameter(description = "Group ID to assign", required = true, example = "1")
            @PathVariable Integer groupId) {
        StudentResponse student = studentManagementService.assignStudentToGroup(studentId, groupId);
        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{studentId}/group")
    @Operation(
            summary = "Remove student from group",
            description = "Remove a student from their current study group"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Student removed from group successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Student not found",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<StudentResponse> removeFromGroup(
            @Parameter(description = "Student ID", required = true, example = "1")
            @PathVariable Integer studentId) {
        StudentResponse student = studentManagementService.removeStudentFromGroup(studentId);
        return ResponseEntity.ok(student);
    }
}