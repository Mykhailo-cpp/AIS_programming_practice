package com.academic.AIS.service;

import com.academic.AIS.exception.DuplicateResourceException;
import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.exception.ValidationException;
import com.academic.AIS.model.StudyGroup;
import com.academic.AIS.model.Subject;
import com.academic.AIS.model.SubjectAssignment;
import com.academic.AIS.model.Teacher;
import com.academic.AIS.model.User;
import com.academic.AIS.repository.StudyGroupRepository;
import com.academic.AIS.repository.SubjectAssignmentRepository;
import com.academic.AIS.repository.SubjectRepository;
import com.academic.AIS.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentManagementServiceTest {

    @Mock
    private SubjectAssignmentRepository assignmentRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private StudyGroupRepository studyGroupRepository;

    @InjectMocks
    private AssignmentManagementService assignmentManagementService;

    private Subject testSubject;
    private Teacher testTeacher;
    private StudyGroup testGroup;
    private SubjectAssignment testAssignment;

    @BeforeEach
    void setUp() {
        testSubject = new Subject("Mathematics", "MATH101", 5, "Basic Math");
        User teacherUser = new User("jane", "password", "TEACHER");
        testTeacher = new Teacher(teacherUser, "Jane", "Smith", "jane@example.com");
        testGroup = new StudyGroup("CS-101", 2024);
        testAssignment = new SubjectAssignment(
                testSubject, testTeacher, testGroup, "2024/2025", "Fall"
        );
    }

    @Test
    void createAssignment_ValidData_ReturnsAssignment() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(testSubject));
        when(teacherRepository.findById(1)).thenReturn(Optional.of(testTeacher));
        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));
        when(assignmentRepository.findByAllFields(1, 1, 1, "2024/2025", "Fall"))
                .thenReturn(Optional.empty());
        when(assignmentRepository.save(any(SubjectAssignment.class))).thenReturn(testAssignment);

        SubjectAssignment result = assignmentManagementService.createAssignment(
                1, 1, 1, "2024/2025", "Fall"
        );

        assertNotNull(result);
        verify(assignmentRepository).save(any(SubjectAssignment.class));
    }

    @Test
    void createAssignment_SubjectNotFound_ThrowsResourceNotFoundException() {
        when(subjectRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> assignmentManagementService.createAssignment(999, 1, 1, "2024/2025", "Fall")
        );

        assertTrue(exception.getMessage().contains("Subject"));
        assertTrue(exception.getMessage().contains("999"));
        verify(assignmentRepository, never()).save(any(SubjectAssignment.class));
    }

    @Test
    void createAssignment_TeacherNotFound_ThrowsResourceNotFoundException() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(testSubject));
        when(teacherRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> assignmentManagementService.createAssignment(1, 999, 1, "2024/2025", "Fall")
        );

        assertTrue(exception.getMessage().contains("Teacher"));
    }

    @Test
    void createAssignment_GroupNotFound_ThrowsResourceNotFoundException() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(testSubject));
        when(teacherRepository.findById(1)).thenReturn(Optional.of(testTeacher));
        when(studyGroupRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> assignmentManagementService.createAssignment(1, 1, 999, "2024/2025", "Fall")
        );

        assertTrue(exception.getMessage().contains("StudyGroup"));
    }

    @Test
    void createAssignment_AssignmentAlreadyExists_ThrowsDuplicateResourceException() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(testSubject));
        when(teacherRepository.findById(1)).thenReturn(Optional.of(testTeacher));
        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));
        when(assignmentRepository.findByAllFields(1, 1, 1, "2024/2025", "Fall"))
                .thenReturn(Optional.of(testAssignment));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> assignmentManagementService.createAssignment(1, 1, 1, "2024/2025", "Fall")
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(assignmentRepository, never()).save(any(SubjectAssignment.class));
    }

    @Test
    void createAssignment_NullAcademicYear_ThrowsValidationException() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(testSubject));
        when(teacherRepository.findById(1)).thenReturn(Optional.of(testTeacher));
        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> assignmentManagementService.createAssignment(1, 1, 1, null, "Fall")
        );

        assertEquals("Academic year is required", exception.getMessage());
    }

    @Test
    void createAssignment_InvalidAcademicYearFormat_ThrowsValidationException() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(testSubject));
        when(teacherRepository.findById(1)).thenReturn(Optional.of(testTeacher));
        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> assignmentManagementService.createAssignment(1, 1, 1, "2024", "Fall")
        );

        assertTrue(exception.getMessage().contains("format YYYY/YYYY"));
    }

    @Test
    void createAssignment_InvalidSemester_ThrowsValidationException() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(testSubject));
        when(teacherRepository.findById(1)).thenReturn(Optional.of(testTeacher));
        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> assignmentManagementService.createAssignment(1, 1, 1, "2024/2025", "Invalid")
        );

        assertTrue(exception.getMessage().contains("Semester must be one of"));
    }

    @Test
    void deleteAssignment_AssignmentNotFound_ThrowsResourceNotFoundException() {
        when(assignmentRepository.existsById(999)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> assignmentManagementService.deleteAssignment(999)
        );

        assertTrue(exception.getMessage().contains("Assignment"));
        verify(assignmentRepository, never()).deleteById(anyInt());
    }

    @Test
    void getAssignmentById_AssignmentNotFound_ThrowsResourceNotFoundException() {
        when(assignmentRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> assignmentManagementService.getAssignmentById(999)
        );

        assertTrue(exception.getMessage().contains("Assignment"));
    }
}