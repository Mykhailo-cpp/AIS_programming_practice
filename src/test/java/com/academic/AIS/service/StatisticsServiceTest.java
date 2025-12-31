package com.academic.AIS.service;

import com.academic.AIS.model.*;
import com.academic.AIS.repository.*;
import com.academic.AIS.service.StatisticsService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private SubjectAssignmentRepository assignmentRepository;

    @InjectMocks
    private StatisticsService statisticsService;

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
        setSubjectId(testSubject, 1);

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
            // Continue without ID
        }
    }

    private void setTeacherId(Teacher teacher, Integer id) {
        try {
            java.lang.reflect.Field field = Teacher.class.getDeclaredField("teacherId");
            field.setAccessible(true);
            field.set(teacher, id);
        } catch (Exception e) {
            // Continue without ID
        }
    }

    private void setGroupId(StudyGroup group, Integer id) {
        try {
            java.lang.reflect.Field field = StudyGroup.class.getDeclaredField("groupId");
            field.setAccessible(true);
            field.set(group, id);
        } catch (Exception e) {
            // Continue without ID
        }
    }

    private void setSubjectId(Subject subject, Integer id) {
        try {
            java.lang.reflect.Field field = Subject.class.getDeclaredField("subjectId");
            field.setAccessible(true);
            field.set(subject, id);
        } catch (Exception e) {
            // Continue without ID
        }
    }

    @Test
    void getSystemStatistics_ReturnsCorrectStatistics() {
        // Arrange
        when(studentRepository.count()).thenReturn(100L);
        when(teacherRepository.count()).thenReturn(20L);
        when(studyGroupRepository.count()).thenReturn(10L);
        when(subjectRepository.count()).thenReturn(15L);
        when(gradeRepository.count()).thenReturn(500L);
        when(assignmentRepository.count()).thenReturn(50L);

        // Act
        SystemStatistics result = statisticsService.getSystemStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getTotalStudents());
        assertEquals(20L, result.getTotalTeachers());
        assertEquals(10L, result.getTotalGroups());
        assertEquals(15L, result.getTotalSubjects());
        assertEquals(500L, result.getTotalGrades());
        assertEquals(50L, result.getTotalAssignments());

        verify(studentRepository).count();
        verify(teacherRepository).count();
        verify(studyGroupRepository).count();
        verify(subjectRepository).count();
        verify(gradeRepository).count();
        verify(assignmentRepository).count();
    }

    @Test
    void getSystemStatistics_EmptySystem_ReturnsZeros() {
        // Arrange
        when(studentRepository.count()).thenReturn(0L);
        when(teacherRepository.count()).thenReturn(0L);
        when(studyGroupRepository.count()).thenReturn(0L);
        when(subjectRepository.count()).thenReturn(0L);
        when(gradeRepository.count()).thenReturn(0L);
        when(assignmentRepository.count()).thenReturn(0L);

        // Act
        SystemStatistics result = statisticsService.getSystemStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getTotalStudents());
        assertEquals(0L, result.getTotalTeachers());
        assertEquals(0L, result.getTotalGroups());
        assertEquals(0L, result.getTotalSubjects());
        assertEquals(0L, result.getTotalGrades());
        assertEquals(0L, result.getTotalAssignments());
    }

    @Test
    void getTeacherStatistics_ReturnsCorrectStatistics() {
        // Arrange
        List<SubjectAssignment> assignments = Arrays.asList(testAssignment, testAssignment);
        List<Grade> grades = Arrays.asList(
                new Grade(testStudent, testAssignment, 8, "Good"),
                new Grade(testStudent, testAssignment, 9, "Excellent"),
                new Grade(testStudent, testAssignment, 7, "Good")
        );

        when(assignmentRepository.findByTeacher_TeacherId(1)).thenReturn(assignments);
        when(gradeRepository.countByTeacher(1)).thenReturn(3L);
        when(gradeRepository.findByTeacher_TeacherId(1)).thenReturn(grades);

        // Act
        TeacherStatistics result = statisticsService.getTeacherStatistics(1);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getTotalAssignments());
        assertEquals(1L, result.getTotalStudents()); // Only 1 unique student
        assertEquals(3L, result.getTotalGrades());
        assertEquals(8.0, result.getAverageGrade(), 0.01);

        verify(assignmentRepository).findByTeacher_TeacherId(1);
        verify(gradeRepository).countByTeacher(1);
        verify(gradeRepository, times(2)).findByTeacher_TeacherId(1);
    }

    @Test
    void getTeacherStatistics_NoGrades_ReturnsZeroAverage() {
        // Arrange
        when(assignmentRepository.findByTeacher_TeacherId(1)).thenReturn(Arrays.asList());
        when(gradeRepository.countByTeacher(1)).thenReturn(0L);
        when(gradeRepository.findByTeacher_TeacherId(1)).thenReturn(Arrays.asList());

        // Act
        TeacherStatistics result = statisticsService.getTeacherStatistics(1);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getTotalAssignments());
        assertEquals(0L, result.getTotalStudents());
        assertEquals(0L, result.getTotalGrades());
        assertEquals(0.0, result.getAverageGrade());
    }

    @Test
    void getStudentStatistics_ReturnsCorrectStatistics() {
        // Arrange
        List<Grade> grades = Arrays.asList(
                new Grade(testStudent, testAssignment, 8, "Good"),
                new Grade(testStudent, testAssignment, 4, "Poor"),
                new Grade(testStudent, testAssignment, 7, "Good"),
                new Grade(testStudent, testAssignment, 3, "Failed")
        );

        when(gradeRepository.countByStudent_StudentId(1)).thenReturn(4L);
        when(gradeRepository.findByStudentWithDetails(1)).thenReturn(grades);

        // Act
        StudentStatistics result = statisticsService.getStudentStatistics(1);

        // Assert
        assertNotNull(result);
        assertEquals(4L, result.getTotalGrades());
        assertEquals(5.5, result.getAverageGrade(), 0.01); // (8+4+7+3)/4
        assertEquals(2L, result.getPassingGrades()); // Grades >= 5
        assertEquals(2L, result.getFailingGrades()); // Grades < 5

        verify(gradeRepository).countByStudent_StudentId(1);
        verify(gradeRepository, times(2)).findByStudentWithDetails(1);
    }

    @Test
    void getStudentStatistics_NoGrades_ReturnsZeros() {
        // Arrange
        when(gradeRepository.countByStudent_StudentId(1)).thenReturn(0L);
        when(gradeRepository.findByStudentWithDetails(1)).thenReturn(Arrays.asList());

        // Act
        StudentStatistics result = statisticsService.getStudentStatistics(1);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getTotalGrades());
        assertEquals(0.0, result.getAverageGrade());
        assertEquals(0L, result.getPassingGrades());
        assertEquals(0L, result.getFailingGrades());
    }

    @Test
    void getStudentStatistics_AllPassingGrades_CorrectCounts() {
        // Arrange
        List<Grade> grades = Arrays.asList(
                new Grade(testStudent, testAssignment, 8, "Good"),
                new Grade(testStudent, testAssignment, 9, "Excellent"),
                new Grade(testStudent, testAssignment, 7, "Good")
        );

        when(gradeRepository.countByStudent_StudentId(1)).thenReturn(3L);
        when(gradeRepository.findByStudentWithDetails(1)).thenReturn(grades);

        // Act
        StudentStatistics result = statisticsService.getStudentStatistics(1);

        // Assert
        assertEquals(3L, result.getPassingGrades());
        assertEquals(0L, result.getFailingGrades());
    }

    @Test
    void getGroupStatistics_ReturnsCorrectStatistics() {
        // Arrange
        List<SubjectAssignment> assignments = Arrays.asList(testAssignment, testAssignment);

        Grade grade1 = new Grade(testStudent, testAssignment, 8, "Good");
        Grade grade2 = new Grade(testStudent, testAssignment, 9, "Excellent");
        List<Grade> allGrades = Arrays.asList(grade1, grade2);

        when(studentRepository.countByGroup_GroupId(1)).thenReturn(25L);
        when(assignmentRepository.findByGroup_GroupId(1)).thenReturn(assignments);
        when(gradeRepository.findAll()).thenReturn(allGrades);

        // Act
        GroupStatistics result = statisticsService.getGroupStatistics(1);

        // Assert
        assertNotNull(result);
        assertEquals(25L, result.getStudentCount());
        assertEquals(2L, result.getAssignmentCount());
        assertEquals(8.5, result.getAverageGrade(), 0.01); // (8+9)/2

        verify(studentRepository).countByGroup_GroupId(1);
        verify(assignmentRepository).findByGroup_GroupId(1);
        verify(gradeRepository).findAll();
    }

    @Test
    void getGroupStatistics_NoData_ReturnsZeros() {
        // Arrange
        when(studentRepository.countByGroup_GroupId(1)).thenReturn(0L);
        when(assignmentRepository.findByGroup_GroupId(1)).thenReturn(Arrays.asList());
        when(gradeRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        GroupStatistics result = statisticsService.getGroupStatistics(1);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getStudentCount());
        assertEquals(0L, result.getAssignmentCount());
        assertEquals(0.0, result.getAverageGrade());
    }

    @Test
    void getSubjectStatistics_ReturnsCorrectStatistics() {
        // Arrange
        StudyGroup group1 = new StudyGroup("CS-101", 2024);
        setGroupId(group1, 1);
        StudyGroup group2 = new StudyGroup("CS-102", 2024);
        setGroupId(group2, 2);

        Student student1 = new Student(new User("s1", "pwd", "STUDENT"), "S1", "L1", "s1@test.com");
        setStudentId(student1, 1);
        Student student2 = new Student(new User("s2", "pwd", "STUDENT"), "S2", "L2", "s2@test.com");
        setStudentId(student2, 2);

        group1.setStudents(Arrays.asList(student1));
        group2.setStudents(Arrays.asList(student2));

        SubjectAssignment assignment1 = new SubjectAssignment(testSubject, testTeacher, group1, "2024/2025", "Fall");
        SubjectAssignment assignment2 = new SubjectAssignment(testSubject, testTeacher, group2, "2024/2025", "Fall");
        List<SubjectAssignment> assignments = Arrays.asList(assignment1, assignment2);

        Grade grade1 = new Grade(student1, assignment1, 8, "Good");
        Grade grade2 = new Grade(student1, assignment1, 7, "Good");
        Grade grade3 = new Grade(student2, assignment2, 9, "Excellent");
        List<Grade> allGrades = Arrays.asList(grade1, grade2, grade3);

        when(assignmentRepository.findBySubject_SubjectId(1)).thenReturn(assignments);
        when(gradeRepository.findAll()).thenReturn(allGrades);

        // Act
        SubjectStatistics result = statisticsService.getSubjectStatistics(1);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getAssignmentCount());
        assertEquals(2L, result.getStudentCount()); // 2 unique students
        assertEquals(3L, result.getGradeCount());
        assertEquals(8.0, result.getAverageGrade(), 0.01); // (8+7+9)/3

        verify(assignmentRepository, times(2)).findBySubject_SubjectId(1);
        verify(gradeRepository, times(2)).findAll();
    }

    @Test
    void getSubjectStatistics_NoData_ReturnsZeros() {
        // Arrange
        when(assignmentRepository.findBySubject_SubjectId(1)).thenReturn(Arrays.asList());
        when(gradeRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        SubjectStatistics result = statisticsService.getSubjectStatistics(1);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getAssignmentCount());
        assertEquals(0L, result.getStudentCount());
        assertEquals(0L, result.getGradeCount());
        assertEquals(0.0, result.getAverageGrade());
    }

    @Test
    void getGradeDistribution_ReturnsCorrectDistribution() {
        // Arrange
        List<Grade> grades = Arrays.asList(
                new Grade(testStudent, testAssignment, 8, "Good"),
                new Grade(testStudent, testAssignment, 8, "Good"),
                new Grade(testStudent, testAssignment, 9, "Excellent"),
                new Grade(testStudent, testAssignment, 5, "Pass"),
                new Grade(testStudent, testAssignment, 3, "Fail")
        );

        when(gradeRepository.findAll()).thenReturn(grades);

        // Act
        Map<Integer, Long> result = statisticsService.getGradeDistribution();

        // Assert
        assertNotNull(result);
        assertEquals(11, result.size()); // 0-10
        assertEquals(0L, result.get(0));
        assertEquals(1L, result.get(3));
        assertEquals(1L, result.get(5));
        assertEquals(2L, result.get(8));
        assertEquals(1L, result.get(9));
        assertEquals(0L, result.get(10));

        verify(gradeRepository).findAll();
    }

    @Test
    void getGradeDistribution_NoGrades_ReturnsZeros() {
        // Arrange
        when(gradeRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        Map<Integer, Long> result = statisticsService.getGradeDistribution();

        // Assert
        assertNotNull(result);
        assertEquals(11, result.size());
        for (int i = 0; i <= 10; i++) {
            assertEquals(0L, result.get(i));
        }
    }

    @Test
    void getGradeDistribution_AllSameGrade_CorrectDistribution() {
        // Arrange
        List<Grade> grades = Arrays.asList(
                new Grade(testStudent, testAssignment, 10, "Perfect"),
                new Grade(testStudent, testAssignment, 10, "Perfect"),
                new Grade(testStudent, testAssignment, 10, "Perfect")
        );

        when(gradeRepository.findAll()).thenReturn(grades);

        // Act
        Map<Integer, Long> result = statisticsService.getGradeDistribution();

        // Assert
        assertEquals(3L, result.get(10));
        assertEquals(0L, result.get(9));
        assertEquals(0L, result.get(0));
    }
}