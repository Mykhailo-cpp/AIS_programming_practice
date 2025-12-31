package com.academic.AIS.service;

import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.model.SubjectAssignment;
import com.academic.AIS.model.Teacher;
import com.academic.AIS.model.User;
import com.academic.AIS.repository.GradeRepository;
import com.academic.AIS.repository.SubjectAssignmentRepository;
import com.academic.AIS.repository.TeacherRepository;
import com.academic.AIS.repository.UserRepository;
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
class TeacherManagementServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;

    @Mock
    private GradeRepository gradeRepository;

    @InjectMocks
    private TeacherManagementService teacherManagementService;

    private Teacher testTeacher;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("jane", "password", "TEACHER");
        testTeacher = new Teacher(testUser, "Jane", "Smith", "jane@example.com");
    }

    @Test
    void createTeacher_ValidData_ReturnsTeacher() {

        when(authenticationService.registerTeacher(
                "Jane", "Smith", "jane@example.com"
        )).thenReturn(testTeacher);


        Teacher result = teacherManagementService.createTeacher("Jane", "Smith", "jane@example.com");


        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("jane@example.com", result.getEmail());

        verify(authenticationService).registerTeacher("Jane", "Smith", "jane@example.com");
    }

    @Test
    void updateTeacher_ValidData_ReturnsUpdatedTeacher() {

        when(teacherRepository.findById(1)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);


        Teacher result = teacherManagementService.updateTeacher(
                1, "Jane", "Doe", "jane.doe@example.com"
        );


        assertNotNull(result);
        verify(teacherRepository).findById(1);
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void updateTeacher_TeacherNotFound_ThrowsResourceNotFoundException() {

        when(teacherRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> teacherManagementService.updateTeacher(999, "Jane", "Doe", "jane@example.com")
        );

        assertEquals("Teacher not found with id: '999'", exception.getMessage());
        verify(teacherRepository).findById(999);
        verify(teacherRepository, never()).save(any(Teacher.class));
    }

    @Test
    void deleteTeacher_ValidId_DeletesTeacher() {

        when(teacherRepository.existsById(1)).thenReturn(true);
        doNothing().when(teacherRepository).deleteById(1);
        doNothing().when(userRepository).deleteById(1);


        teacherManagementService.deleteTeacher(1);


        verify(teacherRepository).existsById(1);
        verify(teacherRepository).deleteById(1);
        verify(userRepository).deleteById(1);
    }

    @Test
    void deleteTeacher_TeacherNotFound_ThrowsResourceNotFoundException() {

        when(teacherRepository.existsById(999)).thenReturn(false);


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> teacherManagementService.deleteTeacher(999)
        );

        assertEquals("Teacher not found with id: '999'", exception.getMessage());
        verify(teacherRepository).existsById(999);
        verify(teacherRepository, never()).deleteById(anyInt());
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    void getAllTeachers_ReturnsTeacherList() {

        Teacher teacher2 = new Teacher(
                new User("john", "pwd", "TEACHER"),
                "John", "Doe", "john@example.com"
        );
        List<Teacher> teachers = Arrays.asList(testTeacher, teacher2);
        when(teacherRepository.findAll()).thenReturn(teachers);


        List<Teacher> result = teacherManagementService.getAllTeachers();


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(teacherRepository).findAll();
    }

    @Test
    void getTeacherById_ValidId_ReturnsTeacher() {

        when(teacherRepository.findById(1)).thenReturn(Optional.of(testTeacher));


        Teacher result = teacherManagementService.getTeacherById(1);


        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("jane@example.com", result.getEmail());
        verify(teacherRepository).findById(1);
    }

    @Test
    void getTeacherById_TeacherNotFound_ThrowsResourceNotFoundException() {

        when(teacherRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> teacherManagementService.getTeacherById(999)
        );

        assertEquals("Teacher not found with id: '999'", exception.getMessage());
        verify(teacherRepository).findById(999);
    }

    @Test
    void getTeacherByUsername_ValidUsername_ReturnsTeacher() {

        when(teacherRepository.findByUsername("jane")).thenReturn(Optional.of(testTeacher));


        Optional<Teacher> result = teacherManagementService.getTeacherByUsername("jane");


        assertTrue(result.isPresent());
        assertEquals("Jane", result.get().getFirstName());
        verify(teacherRepository).findByUsername("jane");
    }

    @Test
    void getTeacherByUsername_TeacherNotFound_ReturnsEmpty() {

        when(teacherRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());


        Optional<Teacher> result = teacherManagementService.getTeacherByUsername("nonexistent");


        assertFalse(result.isPresent());
        verify(teacherRepository).findByUsername("nonexistent");
    }

    @Test
    void getTeacherAssignments_ReturnsAssignmentList() {

        SubjectAssignment assignment1 = mock(SubjectAssignment.class);
        SubjectAssignment assignment2 = mock(SubjectAssignment.class);
        List<SubjectAssignment> assignments = Arrays.asList(assignment1, assignment2);

        when(subjectAssignmentRepository.findByTeacher_TeacherId(1)).thenReturn(assignments);


        List<SubjectAssignment> result = teacherManagementService.getTeacherAssignments(1);


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(subjectAssignmentRepository).findByTeacher_TeacherId(1);
    }

    @Test
    void getTeacherAssignments_NoAssignments_ReturnsEmptyList() {

        when(subjectAssignmentRepository.findByTeacher_TeacherId(1)).thenReturn(Arrays.asList());


        List<SubjectAssignment> result = teacherManagementService.getTeacherAssignments(1);


        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(subjectAssignmentRepository).findByTeacher_TeacherId(1);
    }

    @Test
    void countTeacherGrades_ReturnsCount() {

        when(gradeRepository.countByTeacher(1)).thenReturn(15L);


        Long result = teacherManagementService.countTeacherGrades(1);


        assertNotNull(result);
        assertEquals(15L, result);
        verify(gradeRepository).countByTeacher(1);
    }

    @Test
    void countTeacherGrades_NoGrades_ReturnsZero() {

        when(gradeRepository.countByTeacher(1)).thenReturn(0L);


        Long result = teacherManagementService.countTeacherGrades(1);


        assertNotNull(result);
        assertEquals(0L, result);
        verify(gradeRepository).countByTeacher(1);
    }
}