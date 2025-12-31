package com.academic.AIS.service;

import com.academic.AIS.dto.request.CreateStudentRequest;
import com.academic.AIS.dto.response.StudentResponse;
import com.academic.AIS.dto.mapper.StudentMapper;
import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.model.Student;
import com.academic.AIS.model.StudyGroup;
import com.academic.AIS.model.User;
import com.academic.AIS.repository.StudentRepository;
import com.academic.AIS.repository.StudyGroupRepository;
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
class StudentManagementServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentManagementService studentManagementService;

    private Student testStudent;
    private StudyGroup testGroup;
    private CreateStudentRequest testRequest;
    private StudentResponse testResponse;

    @BeforeEach
    void setUp() {
        User user = new User("john", "password", "STUDENT");
        testStudent = new Student(user, "John", "Doe", "john@example.com");
        setStudentId(testStudent, 1);

        testGroup = new StudyGroup("CS-101", 2024);
        setGroupId(testGroup, 1);

        testRequest = new CreateStudentRequest();
        testRequest.setFirstName("John");
        testRequest.setLastName("Doe");
        testRequest.setEmail("john@example.com");
        testRequest.setGroupId(1);

        testResponse = new StudentResponse();
        testResponse.setStudentId(1);
        testResponse.setFirstName("John");
        testResponse.setLastName("Doe");
        testResponse.setEmail("john@example.com");
    }

    private void setStudentId(Student student, Integer id) {
        try {
            java.lang.reflect.Field field = Student.class.getDeclaredField("studentId");
            field.setAccessible(true);
            field.set(student, id);
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
    void createStudent_ValidDataWithGroup_ReturnsStudentResponse() {

        when(authenticationService.registerStudent(
                "John", "Doe", "john@example.com"
        )).thenReturn(testStudent);
        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);
        when(studentMapper.toResponse(any(Student.class))).thenReturn(testResponse);


        StudentResponse result = studentManagementService.createStudent(testRequest);


        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john@example.com", result.getEmail());

        verify(authenticationService).registerStudent("John", "Doe", "john@example.com");
        verify(studentRepository).findById(1);
        verify(studyGroupRepository).findById(1);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void createStudent_ValidDataWithoutGroup_ReturnsStudentResponse() {

        testRequest.setGroupId(null);
        when(authenticationService.registerStudent(
                "John", "Doe", "john@example.com"
        )).thenReturn(testStudent);
        when(studentMapper.toResponse(any(Student.class))).thenReturn(testResponse);


        StudentResponse result = studentManagementService.createStudent(testRequest);


        assertNotNull(result);
        verify(authenticationService).registerStudent("John", "Doe", "john@example.com");
        verify(studyGroupRepository, never()).findById(anyInt());
        verify(studentMapper).toResponse(any(Student.class));
    }

    @Test
    void createStudent_GroupNotFound_ThrowsResourceNotFoundException() {

        when(authenticationService.registerStudent(
                "John", "Doe", "john@example.com"
        )).thenReturn(testStudent);
        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(studyGroupRepository.findById(999)).thenReturn(Optional.empty());

        testRequest.setGroupId(999);


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentManagementService.createStudent(testRequest)
        );

        assertEquals("StudyGroup not found with id: '999'", exception.getMessage());
        verify(authenticationService).registerStudent("John", "Doe", "john@example.com");
        verify(studentRepository).findById(1);
    }

    @Test
    void updateStudent_ValidData_ReturnsUpdatedStudent() {

        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);
        when(studentMapper.toResponse(any(Student.class))).thenReturn(testResponse);


        StudentResponse result = studentManagementService.updateStudent(1, testRequest);


        assertNotNull(result);
        verify(studentRepository, times(2)).findById(1); // Called twice: once in update, once in assignStudentToGroup
        verify(studentRepository, times(2)).save(any(Student.class)); // Called twice: once in update, once in assignStudentToGroup
        verify(studyGroupRepository).findById(1);
    }

    @Test
    void updateStudent_StudentNotFound_ThrowsResourceNotFoundException() {

        when(studentRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentManagementService.updateStudent(999, testRequest)
        );

        assertEquals("Student not found with id: '999'", exception.getMessage());
        verify(studentRepository).findById(999);
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void updateStudent_RemoveFromGroup_Success() {

        testRequest.setGroupId(null);
        testStudent.setGroup(testGroup);

        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);
        when(studentMapper.toResponse(any(Student.class))).thenReturn(testResponse);


        StudentResponse result = studentManagementService.updateStudent(1, testRequest);


        assertNotNull(result);
        verify(studentRepository, times(2)).save(any(Student.class));
        verify(studyGroupRepository, never()).findById(anyInt());
    }

    @Test
    void deleteStudent_ValidId_DeletesStudent() {

        when(studentRepository.existsById(1)).thenReturn(true);
        doNothing().when(studentRepository).deleteById(1);
        doNothing().when(userRepository).deleteById(1);


        studentManagementService.deleteStudent(1);


        verify(studentRepository).existsById(1);
        verify(studentRepository).deleteById(1);
        verify(userRepository).deleteById(1);
    }

    @Test
    void deleteStudent_StudentNotFound_ThrowsResourceNotFoundException() {

        when(studentRepository.existsById(999)).thenReturn(false);


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentManagementService.deleteStudent(999)
        );

        assertEquals("Student not found with id: '999'", exception.getMessage());
        verify(studentRepository).existsById(999);
        verify(studentRepository, never()).deleteById(anyInt());
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    void getAllStudents_ReturnsStudentList() {

        List<Student> students = Arrays.asList(testStudent, testStudent);
        when(studentRepository.findAll()).thenReturn(students);
        when(studentMapper.toResponse(any(Student.class))).thenReturn(testResponse);


        List<StudentResponse> result = studentManagementService.getAllStudents();


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(studentRepository).findAll();
        verify(studentMapper, times(2)).toResponse(any(Student.class));
    }

    @Test
    void getStudentById_ValidId_ReturnsStudent() {

        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(studentMapper.toResponse(any(Student.class))).thenReturn(testResponse);


        StudentResponse result = studentManagementService.getStudentById(1);


        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(studentRepository).findById(1);
        verify(studentMapper).toResponse(testStudent);
    }

    @Test
    void getStudentById_StudentNotFound_ThrowsResourceNotFoundException() {

        when(studentRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentManagementService.getStudentById(999)
        );

        assertEquals("Student not found with id: '999'", exception.getMessage());
        verify(studentRepository).findById(999);
    }

    @Test
    void assignStudentToGroup_ValidData_ReturnsStudentResponse() {

        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);
        when(studentMapper.toResponse(any(Student.class))).thenReturn(testResponse);


        StudentResponse result = studentManagementService.assignStudentToGroup(1, 1);


        assertNotNull(result);
        verify(studentRepository).findById(1);
        verify(studyGroupRepository).findById(1);
        verify(studentRepository).save(any(Student.class));
        verify(studentMapper).toResponse(any(Student.class));
    }

    @Test
    void assignStudentToGroup_StudentNotFound_ThrowsResourceNotFoundException() {

        when(studentRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentManagementService.assignStudentToGroup(999, 1)
        );

        assertEquals("Student not found with id: '999'", exception.getMessage());
        verify(studyGroupRepository, never()).findById(anyInt());
    }

    @Test
    void assignStudentToGroup_GroupNotFound_ThrowsResourceNotFoundException() {

        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(studyGroupRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentManagementService.assignStudentToGroup(1, 999)
        );

        assertEquals("StudyGroup not found with id: '999'", exception.getMessage());
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void removeStudentFromGroup_ValidId_ReturnsStudentResponse() {

        testStudent.setGroup(testGroup);
        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);
        when(studentMapper.toResponse(any(Student.class))).thenReturn(testResponse);


        StudentResponse result = studentManagementService.removeStudentFromGroup(1);


        assertNotNull(result);
        assertNull(testStudent.getGroup());
        verify(studentRepository).findById(1);
        verify(studentRepository).save(any(Student.class));
        verify(studentMapper).toResponse(any(Student.class));
    }

    @Test
    void removeStudentFromGroup_StudentNotFound_ThrowsResourceNotFoundException() {

        when(studentRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentManagementService.removeStudentFromGroup(999)
        );

        assertEquals("Student not found with id: '999'", exception.getMessage());
        verify(studentRepository).findById(999);
        verify(studentRepository, never()).save(any(Student.class));
    }
}