package com.academic.AIS.service;

import com.academic.AIS.exception.DuplicateResourceException;
import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.exception.ValidationException;
import com.academic.AIS.model.StudyGroup;
import com.academic.AIS.repository.StudyGroupRepository;
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
class GroupManagementServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @InjectMocks
    private GroupManagementService groupManagementService;

    private StudyGroup testGroup;

    @BeforeEach
    void setUp() {
        testGroup = new StudyGroup("CS-101", 2024);
        setGroupId(testGroup, 1);
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
    void createGroup_ValidData_ReturnsGroup() {

        when(studyGroupRepository.existsByGroupName("CS-101")).thenReturn(false);
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(testGroup);


        StudyGroup result = groupManagementService.createGroup("CS-101", 2024);


        assertNotNull(result);
        assertEquals("CS-101", result.getGroupName());
        assertEquals(2024, result.getYear());

        verify(studyGroupRepository).existsByGroupName("CS-101");
        verify(studyGroupRepository).save(any(StudyGroup.class));
    }

    @Test
    void createGroup_GroupNameExists_ThrowsDuplicateResourceException() {
        when(studyGroupRepository.existsByGroupName("CS-101")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> groupManagementService.createGroup("CS-101", 2024)
        );

        assertTrue(exception.getMessage().contains("StudyGroup"));
        assertTrue(exception.getMessage().contains("groupName"));
    }

    @Test
    void createGroup_NullGroupName_ThrowsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> groupManagementService.createGroup(null, 2024)
        );

        assertEquals("Group name is required", exception.getMessage());
    }

    @Test
    void createGroup_EmptyGroupName_ThrowsValidationException() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> groupManagementService.createGroup("   ", 2024)
        );

        assertEquals("Group name is required", exception.getMessage());
    }

    @Test
    void createGroup_GroupNameTooLong_ThrowsValidationException() {

        String longName = "A".repeat(51);


        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> groupManagementService.createGroup(longName, 2024)
        );

        assertEquals("Group name must not exceed 50 characters", exception.getMessage());
    }

    @Test
    void createGroup_NullYear_ThrowsValidationException() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> groupManagementService.createGroup("CS-101", null)
        );

        assertEquals("Year is required", exception.getMessage());
    }

    @Test
    void createGroup_YearTooLow_ThrowsValidationException() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> groupManagementService.createGroup("CS-101", 1999)
        );

        assertTrue(exception.getMessage().contains("Year must be between 2000 and 2100"));
    }

    @Test
    void createGroup_YearTooHigh_ThrowsValidationException() {

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> groupManagementService.createGroup("CS-101", 2101)
        );

        assertTrue(exception.getMessage().contains("Year must be between 2000 and 2100"));
    }

    @Test
    void updateGroup_ValidData_ReturnsUpdatedGroup() {

        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));
        when(studyGroupRepository.findByGroupName("CS-102")).thenReturn(Optional.empty());
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(testGroup);


        StudyGroup result = groupManagementService.updateGroup(1, "CS-102", 2025);


        assertNotNull(result);
        verify(studyGroupRepository).findById(1);
        verify(studyGroupRepository).findByGroupName("CS-102");
        verify(studyGroupRepository).save(any(StudyGroup.class));
    }

    @Test
    void updateGroup_GroupNotFound_ThrowsResourceNotFoundException() {

        when(studyGroupRepository.findById(999)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> groupManagementService.updateGroup(999, "CS-102", 2025)
        );


        assertTrue(exception.getMessage().contains("StudyGroup"));
        assertTrue(exception.getMessage().contains("999"));
        verify(studyGroupRepository).findById(999);
    }

    @Test
    void updateGroup_NameConflictWithDifferentGroup_ThrowsDuplicateResourceException() {

        StudyGroup differentGroup = new StudyGroup("CS-102", 2024);
        setGroupId(differentGroup, 2); // Set different ID

        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));
        when(studyGroupRepository.findByGroupName("CS-102")).thenReturn(Optional.of(differentGroup));


        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> groupManagementService.updateGroup(1, "CS-102", 2024)
        );


        assertTrue(exception.getMessage().contains("StudyGroup"));
        assertTrue(exception.getMessage().contains("groupName"));
    }

    @Test
    void updateGroup_SameNameForSameGroup_Success() {

        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));
        when(studyGroupRepository.findByGroupName("CS-101")).thenReturn(Optional.of(testGroup));
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(testGroup);


        StudyGroup result = groupManagementService.updateGroup(1, "CS-101", 2024);


        assertNotNull(result);
        verify(studyGroupRepository).save(any(StudyGroup.class));
    }

    @Test
    void deleteGroup_ValidId_DeletesGroup() {

        when(studyGroupRepository.existsById(1)).thenReturn(true);
        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));
        doNothing().when(studyGroupRepository).deleteById(1);


        groupManagementService.deleteGroup(1);


        verify(studyGroupRepository).existsById(1);
        verify(studyGroupRepository).deleteById(1);
    }

    @Test
    void deleteGroup_GroupNotFound_ThrowsResourceNotFoundException() {

        when(studyGroupRepository.existsById(999)).thenReturn(false);


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> groupManagementService.deleteGroup(999)
        );


        assertTrue(exception.getMessage().contains("StudyGroup"));
        assertTrue(exception.getMessage().contains("999"));
        verify(studyGroupRepository, never()).deleteById(anyInt());
    }

    @Test
    void getAllGroups_ReturnsGroupList() {

        List<StudyGroup> groups = Arrays.asList(testGroup, new StudyGroup("CS-102", 2024));
        when(studyGroupRepository.findAll()).thenReturn(groups);


        List<StudyGroup> result = groupManagementService.getAllGroups();


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(studyGroupRepository).findAll();
    }

    @Test
    void getGroupById_ValidId_ReturnsGroup() {

        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));


        StudyGroup result = groupManagementService.getGroupById(1);


        assertNotNull(result);
        assertEquals("CS-101", result.getGroupName());
        verify(studyGroupRepository).findById(1);
    }

    @Test
    void getGroupById_GroupNotFound_ThrowsResourceNotFoundException() {
        when(studyGroupRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> groupManagementService.getGroupById(999)
        );


        assertTrue(exception.getMessage().contains("StudyGroup"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void getGroupByName_ValidName_ReturnsGroup() {

        when(studyGroupRepository.findByGroupName("CS-101")).thenReturn(Optional.of(testGroup));


        StudyGroup result = groupManagementService.getGroupByName("CS-101");


        assertNotNull(result);
        assertEquals("CS-101", result.getGroupName());
        verify(studyGroupRepository).findByGroupName("CS-101");
    }

    @Test
    void getGroupByName_GroupNotFound_ThrowsResourceNotFoundException() {

        when(studyGroupRepository.findByGroupName("NONEXISTENT")).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> groupManagementService.getGroupByName("NONEXISTENT")
        );


        assertTrue(exception.getMessage().contains("StudyGroup"));
        assertTrue(exception.getMessage().contains("NONEXISTENT"));
    }

    @Test
    void getStudentCount_ValidGroup_ReturnsCount() {

        when(studyGroupRepository.findById(1)).thenReturn(Optional.of(testGroup));


        int result = groupManagementService.getStudentCount(1);


        assertEquals(0, result);
        verify(studyGroupRepository).findById(1);
    }

    @Test
    void existsByName_NameExists_ReturnsTrue() {

        when(studyGroupRepository.existsByGroupName("CS-101")).thenReturn(true);

        boolean result = groupManagementService.existsByName("CS-101");


        assertTrue(result);
        verify(studyGroupRepository).existsByGroupName("CS-101");
    }

    @Test
    void existsByName_NameDoesNotExist_ReturnsFalse() {

        when(studyGroupRepository.existsByGroupName("NONEXISTENT")).thenReturn(false);


        boolean result = groupManagementService.existsByName("NONEXISTENT");


        assertFalse(result);
        verify(studyGroupRepository).existsByGroupName("NONEXISTENT");
    }
}