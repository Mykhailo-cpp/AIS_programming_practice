package com.academic.AIS.service;

import com.academic.AIS.exception.DuplicateResourceException;
import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.exception.ValidationException;
import com.academic.AIS.model.Subject;
import com.academic.AIS.repository.SubjectRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectManagementServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private SubjectManagementService subjectManagementService;

    private Subject testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new Subject("Mathematics", "MATH101", 5, "Basic Mathematics");
    }

    @Test
    void createSubject_ValidData_ReturnsSubject() {

        when(subjectRepository.findBySubjectCode("MATH101")).thenReturn(Optional.empty());
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);


        Subject result = subjectManagementService.createSubject(
                "Mathematics", "MATH101", 5, "Basic Mathematics"
        );


        assertNotNull(result);
        assertEquals("Mathematics", result.getSubjectName());
        assertEquals("MATH101", result.getSubjectCode());
        assertEquals(5, result.getCredits());
        assertEquals("Basic Mathematics", result.getDescription());

        verify(subjectRepository).findBySubjectCode("MATH101");
        verify(subjectRepository).save(any(Subject.class));
    }

    @Test
    void createSubject_SubjectCodeExists_ThrowsDuplicateResourceException() {

        when(subjectRepository.findBySubjectCode("MATH101")).thenReturn(Optional.of(testSubject));


        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> subjectManagementService.createSubject("Mathematics", "MATH101", 5, "Description")
        );

        assertEquals("Subject already exists with subjectCode: 'MATH101'", exception.getMessage());
        verify(subjectRepository).findBySubjectCode("MATH101");
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void createSubject_NullSubjectName_ThrowsValidationException() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> subjectManagementService.createSubject(null, "MATH101", 5, "Description")
        );

        assertEquals("Subject name is required", exception.getMessage());
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void createSubject_EmptySubjectName_ThrowsValidationException() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> subjectManagementService.createSubject("   ", "MATH101", 5, "Description")
        );

        assertEquals("Subject name is required", exception.getMessage());
    }

    @Test
    void createSubject_NullSubjectCode_ThrowsValidationException() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> subjectManagementService.createSubject("Mathematics", null, 5, "Description")
        );

        assertEquals("Subject code is required", exception.getMessage());
    }

    @Test
    void createSubject_EmptySubjectCode_ThrowsValidationException() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> subjectManagementService.createSubject("Mathematics", "   ", 5, "Description")
        );

        assertEquals("Subject code is required", exception.getMessage());
    }

    @Test
    void createSubject_NullCredits_ThrowsValidationException() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> subjectManagementService.createSubject("Mathematics", "MATH101", null, "Description")
        );

        assertEquals("Invalid credits", exception.getMessage());
    }

    @Test
    void createSubject_NegativeCredits_ThrowsValidationException() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> subjectManagementService.createSubject("Mathematics", "MATH101", -1, "Description")
        );

        assertEquals("Invalid credits", exception.getMessage());
    }

    @Test
    void updateSubject_ValidData_ReturnsUpdatedSubject() {

        when(subjectRepository.findById(1)).thenReturn(Optional.of(testSubject));
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);


        Subject result = subjectManagementService.updateSubject(
                1, "Advanced Mathematics", "MATH201", 6, "Advanced topics"
        );


        assertNotNull(result);
        verify(subjectRepository).findById(1);
        verify(subjectRepository).save(any(Subject.class));
    }

    @Test
    void updateSubject_SubjectNotFound_ThrowsResourceNotFoundException() {

        when(subjectRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> subjectManagementService.updateSubject(999, "Math", "MATH101", 5, "Description")
        );

        assertEquals("Subject not found with id: '999'", exception.getMessage());
        verify(subjectRepository).findById(999);
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void updateSubject_InvalidData_ThrowsValidationException() {

        when(subjectRepository.findById(1)).thenReturn(Optional.of(testSubject));


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> subjectManagementService.updateSubject(1, "", "MATH101", 5, "Description")
        );

        assertEquals("Subject name is required", exception.getMessage());
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void deleteSubject_ValidId_DeletesSubject() {

        when(subjectRepository.existsById(1)).thenReturn(true);
        doNothing().when(subjectRepository).deleteById(1);


        subjectManagementService.deleteSubject(1);


        verify(subjectRepository).existsById(1);
        verify(subjectRepository).deleteById(1);
    }

    @Test
    void deleteSubject_SubjectNotFound_ThrowsResourceNotFoundException() {

        when(subjectRepository.existsById(999)).thenReturn(false);


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> subjectManagementService.deleteSubject(999)
        );

        assertEquals("Subject not found with id: '999'", exception.getMessage());
        verify(subjectRepository).existsById(999);
        verify(subjectRepository, never()).deleteById(anyInt());
    }

    @Test
    void getAllSubjects_ReturnsSubjectList() {

        List<Subject> subjects = Arrays.asList(
                testSubject,
                new Subject("Physics", "PHYS101", 4, "Basic Physics")
        );
        when(subjectRepository.findAll()).thenReturn(subjects);


        List<Subject> result = subjectManagementService.getAllSubjects();


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(subjectRepository).findAll();
    }

    @Test
    void getSubjectById_ValidId_ReturnsSubject() {

        when(subjectRepository.findById(1)).thenReturn(Optional.of(testSubject));


        Subject result = subjectManagementService.getSubjectById(1);


        assertNotNull(result);
        assertEquals("Mathematics", result.getSubjectName());
        assertEquals("MATH101", result.getSubjectCode());
        verify(subjectRepository).findById(1);
    }

    @Test
    void getSubjectById_SubjectNotFound_ThrowsResourceNotFoundException() {

        when(subjectRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> subjectManagementService.getSubjectById(999)
        );

        assertEquals("Subject not found with id: '999'", exception.getMessage());
        verify(subjectRepository).findById(999);
    }
}