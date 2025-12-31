package com.academic.AIS.service;

import com.academic.AIS.exception.DuplicateResourceException;
import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.exception.ValidationException;
import com.academic.AIS.model.StudyGroup;
import com.academic.AIS.repository.StudyGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GroupManagementService {

    private static final Logger logger = LoggerFactory.getLogger(GroupManagementService.class);

    private final StudyGroupRepository studyGroupRepository;

    @Autowired
    public GroupManagementService(StudyGroupRepository studyGroupRepository) {
        this.studyGroupRepository = studyGroupRepository;
    }

    public StudyGroup createGroup(String groupName, Integer year) {
        logger.info("Creating study group: {} for year {}", groupName, year);

        validateGroupData(groupName, year);

        if (studyGroupRepository.existsByGroupName(groupName)) {
            logger.warn("Attempt to create group with existing name: {}", groupName);
            throw new DuplicateResourceException("StudyGroup", "groupName", groupName);
        }

        StudyGroup group = new StudyGroup(groupName, year);
        group = studyGroupRepository.save(group);

        logger.info("Study group created successfully with ID: {}", group.getGroupId());
        return group;
    }

    public StudyGroup updateGroup(Integer groupId, String groupName, Integer year) {
        logger.info("Updating study group ID: {} with name: {}, year: {}", groupId, groupName, year);

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> {
                    logger.error("Study group not found with ID: {}", groupId);
                    return new ResourceNotFoundException("StudyGroup", "id", groupId);
                });

        validateGroupData(groupName, year);

        studyGroupRepository.findByGroupName(groupName).ifPresent(existingGroup -> {
            if (!existingGroup.getGroupId().equals(groupId)) {
                logger.warn("Group name {} already exists for different group", groupName);
                throw new DuplicateResourceException("StudyGroup", "groupName", groupName);
            }
        });

        group.setGroupName(groupName);
        group.setYear(year);
        group = studyGroupRepository.save(group);

        logger.info("Study group updated successfully: {}", group.getGroupId());
        return group;
    }

    public void deleteGroup(Integer groupId) {
        logger.info("Attempting to delete study group ID: {}", groupId);

        if (!studyGroupRepository.existsById(groupId)) {
            logger.error("Cannot delete - study group not found with ID: {}", groupId);
            throw new ResourceNotFoundException("StudyGroup", "id", groupId);
        }

        StudyGroup group = studyGroupRepository.findById(groupId).get();
        int studentCount = group.getStudents() != null ? group.getStudents().size() : 0;

        if (studentCount > 0) {
            logger.warn("Deleting group {} which has {} students", groupId, studentCount);
        }

        studyGroupRepository.deleteById(groupId);
        logger.info("Study group deleted successfully: {}", groupId);
    }

    public List<StudyGroup> getAllGroups() {
        logger.debug("Retrieving all study groups");
        List<StudyGroup> groups = studyGroupRepository.findAll();
        logger.debug("Found {} study groups", groups.size());
        return groups;
    }

    public StudyGroup getGroupById(Integer groupId) {
        logger.debug("Retrieving study group by ID: {}", groupId);
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> {
                    logger.error("Study group not found with ID: {}", groupId);
                    return new ResourceNotFoundException("StudyGroup", "id", groupId);
                });
    }

    public StudyGroup getGroupByName(String groupName) {
        logger.debug("Retrieving study group by name: {}", groupName);
        return studyGroupRepository.findByGroupName(groupName)
                .orElseThrow(() -> {
                    logger.error("Study group not found with name: {}", groupName);
                    return new ResourceNotFoundException("StudyGroup", "groupName", groupName);
                });
    }

    public int getStudentCount(Integer groupId) {
        StudyGroup group = getGroupById(groupId);
        int count = group.getStudents() != null ? group.getStudents().size() : 0;
        logger.debug("Group {} has {} students", groupId, count);
        return count;
    }

    public boolean existsByName(String groupName) {
        return studyGroupRepository.existsByGroupName(groupName);
    }

    private void validateGroupData(String groupName, Integer year) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new ValidationException("Group name is required");
        }

        if (groupName.length() > 50) {
            throw new ValidationException("Group name must not exceed 50 characters");
        }

        if (year == null) {
            throw new ValidationException("Year is required");
        }

        if (year < 2000 || year > 2100) {
            throw new ValidationException("Year must be between 2000 and 2100, got: " + year);
        }
    }
}