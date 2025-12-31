package com.academic.AIS.service;

import com.academic.AIS.exception.DuplicateResourceException;
import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.exception.UnauthorizedException;
import com.academic.AIS.exception.ValidationException;
import com.academic.AIS.model.*;
import com.academic.AIS.repository.GradeRepository;
import com.academic.AIS.repository.StudentRepository;
import com.academic.AIS.repository.SubjectAssignmentRepository;
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
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectAssignmentRepository assignmentRepository;

    @InjectMocks
    private GradeService gradeService;

    private Student testStudent;
    private Teacher testTeacher;
    private StudyGroup testGroup;
    private Subject testSubject;
    private SubjectAssignment testAssignment;
    private Grade testGrade;

    @BeforeEach
    void setUp() {
        User studentUser = new User("john", "password", "STUDENT");
        User teacherUser = new User("jane", "password", "TEACHER");

        testStudent = new Student(studentUser, "John", "Doe", "john@example.com");
        setStudentId(testStudent, 1);

        testTeacher = new Teacher(teacherUser, "Jane", "Smith", "jane@example.com");
        setTeacherId(testTeacher, 1);

        testGroup = new StudyGroup("CS-101", 2024);
        setGroupId(testGroup, 1);

        testSubject = new Subject("Mathematics", "MATH101", 5, "Basic Math");

        testStudent.setGroup(testGroup);

        testAssignment = new SubjectAssignment(testSubject, testTeacher, testGroup, "2024/2025", "Fall");
        testGrade = new Grade(testStudent, testAssignment, 8, "Good work");
    }

    private void setStudentId(Student student, Integer id) {
        try {
            java.lang.reflect.Field field = Student.class.getDeclaredField("studentId");
            field.setAccessible(true);
            field.set(student, id);
        } catch (Exception e) {

        }
    }

    private void setTeacherId(Teacher teacher, Integer id) {
        try {
            java.lang.reflect.Field field = Teacher.class.getDeclaredField("teacherId");
            field.setAccessible(true);
            field.set(teacher, id);
        } catch (Exception e) {

        }
    }

    private void setGroupId(StudyGroup group, Integer id) {
        try {
            java.lang.reflect.Field field = StudyGroup.class.getDeclaredField("groupId");
            field.setAccessible(true);
            field.set(group, id);
        } catch (Exception e) {

        }
    }

    @Test
    void enterGrade_ValidData_ReturnsGrade() {

        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(testAssignment));
        when(gradeRepository.existsByStudentAndAssignment(1, 1)).thenReturn(false);
        when(gradeRepository.save(any(Grade.class))).thenReturn(testGrade);


        Grade result = gradeService.enterGrade(1, 1, 1, 8, "Good work");


        assertNotNull(result);
        assertEquals(8, result.getGradeValue());
        assertEquals("Good work", result.getComments());

        verify(studentRepository).findById(1);
        verify(assignmentRepository).findById(1);
        verify(gradeRepository).existsByStudentAndAssignment(1, 1);
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void enterGrade_StudentNotFound_ThrowsResourceNotFoundException() {

        when(studentRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.enterGrade(1, 999, 1, 8, "Good work")
        );


        assertTrue(exception.getMessage().contains("Student"));
        assertTrue(exception.getMessage().contains("999"));
        verify(studentRepository).findById(999);
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void enterGrade_AssignmentNotFound_ThrowsResourceNotFoundException() {

        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(assignmentRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.enterGrade(1, 1, 999, 8, "Good work")
        );


        assertTrue(exception.getMessage().contains("Assignment"));
        assertTrue(exception.getMessage().contains("999"));
        verify(assignmentRepository).findById(999);
    }

    @Test
    void enterGrade_TeacherNotAssigned_ThrowsUnauthorizedException() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(testAssignment));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> gradeService.enterGrade(999, 1, 1, 8, "Good work")
        );

        assertTrue(exception.getMessage().contains("not assigned"));
    }

    @Test
    void enterGrade_StudentNotInGroup_ThrowsValidationException() {

        StudyGroup differentGroup = new StudyGroup("CS-102", 2024);
        setGroupId(differentGroup, 2); // Set a different group ID

        Student studentInDifferentGroup = new Student(new User("alice", "pwd", "STUDENT"),
                "Alice", "Johnson", "alice@example.com");
        setStudentId(studentInDifferentGroup, 2);
        studentInDifferentGroup.setGroup(differentGroup);

        when(studentRepository.findById(2)).thenReturn(Optional.of(studentInDifferentGroup));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(testAssignment));


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> gradeService.enterGrade(1, 2, 1, 8, "Good work")
        );

        assertEquals("Student is not in the group for this subject", exception.getMessage());
    }

    @Test
    void enterGrade_GradeAlreadyExists_ThrowsDuplicateResourceException() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(testAssignment));
        when(gradeRepository.existsByStudentAndAssignment(1, 1)).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> gradeService.enterGrade(1, 1, 1, 8, "Good work")
        );

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void enterGrade_GradeValueTooLow_ThrowsValidationException() {

        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(testAssignment));
        when(gradeRepository.existsByStudentAndAssignment(1, 1)).thenReturn(false);


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> gradeService.enterGrade(1, 1, 1, -1, "Bad grade")
        );

        assertEquals("Grade must be between 0 and 10", exception.getMessage());
    }

    @Test
    void enterGrade_GradeValueTooHigh_ThrowsValidationException() {

        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(assignmentRepository.findById(1)).thenReturn(Optional.of(testAssignment));
        when(gradeRepository.existsByStudentAndAssignment(1, 1)).thenReturn(false);


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> gradeService.enterGrade(1, 1, 1, 11, "Too high")
        );

        assertEquals("Grade must be between 0 and 10", exception.getMessage());
    }

    @Test
    void updateGrade_ValidData_ReturnsUpdatedGrade() {

        when(gradeRepository.findById(1)).thenReturn(Optional.of(testGrade));
        when(gradeRepository.save(any(Grade.class))).thenReturn(testGrade);


        Grade result = gradeService.updateGrade(1, 1, 9, "Excellent improvement");


        assertNotNull(result);
        verify(gradeRepository).findById(1);
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void updateGrade_GradeNotFound_ThrowsResourceNotFoundException() {
        when(gradeRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.updateGrade(999, 1, 9, "Update")
        );

        assertTrue(exception.getMessage().contains("Grade"));
        assertTrue(exception.getMessage().contains("999"));
        verify(gradeRepository).findById(999);
    }

    @Test
    void updateGrade_WrongTeacher_ThrowsUnauthorizedException() {

        when(gradeRepository.findById(1)).thenReturn(Optional.of(testGrade));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> gradeService.updateGrade(1, 999, 9, "Update")
        );

        assertEquals("You can only edit grades you assigned", exception.getMessage());
    }

    @Test
    void updateGrade_InvalidGradeValue_ThrowsValidationException() {
        when(gradeRepository.findById(1)).thenReturn(Optional.of(testGrade));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> gradeService.updateGrade(1, 1, 15, "Invalid")
        );

        assertEquals("Grade must be between 0 and 10", exception.getMessage());
    }

    @Test
    void deleteGrade_ValidData_ReturnsDeletedGrade() {
        when(gradeRepository.findById(1)).thenReturn(Optional.of(testGrade));
        doNothing().when(gradeRepository).deleteById(1);

        Grade result = gradeService.deleteGrade(1, 1);

        assertNotNull(result);
        verify(gradeRepository).findById(1);
        verify(gradeRepository).deleteById(1);
    }

    @Test
    void deleteGrade_GradeNotFound_ThrowsResourceNotFoundException() {
        when(gradeRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.deleteGrade(999, 1)
        );

        assertTrue(exception.getMessage().contains("Grade"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void deleteGrade_WrongTeacher_ThrowsUnauthorizedException() {

        when(gradeRepository.findById(1)).thenReturn(Optional.of(testGrade));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> gradeService.deleteGrade(1, 999)
        );

        assertEquals("You can only delete grades you assigned", exception.getMessage());
        verify(gradeRepository, never()).deleteById(anyInt());
    }

    @Test
    void getTeacherGrades_ReturnsGradeList() {
        List<Grade> grades = Arrays.asList(testGrade, testGrade);
        when(gradeRepository.findByTeacher_TeacherId(1)).thenReturn(grades);

        List<Grade> result = gradeService.getTeacherGrades(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gradeRepository).findByTeacher_TeacherId(1);
    }

    @Test
    void getGradesForTeacherSubject_ReturnsGradeList() {
        List<Grade> grades = Arrays.asList(testGrade);
        when(gradeRepository.findByTeacherAndSubject(1, 1)).thenReturn(grades);

        List<Grade> result = gradeService.getGradesForTeacherSubject(1, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(gradeRepository).findByTeacherAndSubject(1, 1);
    }

    @Test
    void getGradesByAssignment_ReturnsGradeList() {
        List<Grade> grades = Arrays.asList(testGrade, testGrade);
        when(gradeRepository.findByAssignment_AssignmentIdOrderByGradeDateDesc(1)).thenReturn(grades);

        List<Grade> result = gradeService.getGradesByAssignment(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gradeRepository).findByAssignment_AssignmentIdOrderByGradeDateDesc(1);
    }
}