package com.academic.AIS.service;

import com.academic.AIS.exception.DuplicateResourceException;
import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.exception.ValidationException;
import com.academic.AIS.model.StudyGroup;
import com.academic.AIS.model.Subject;
import com.academic.AIS.model.SubjectAssignment;
import com.academic.AIS.model.Teacher;
import com.academic.AIS.repository.StudyGroupRepository;
import com.academic.AIS.repository.SubjectAssignmentRepository;
import com.academic.AIS.repository.SubjectRepository;
import com.academic.AIS.repository.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AssignmentManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentManagementService.class);

    private final SubjectAssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final StudyGroupRepository studyGroupRepository;

    @Autowired
    public AssignmentManagementService(SubjectAssignmentRepository assignmentRepository,
                                       SubjectRepository subjectRepository,
                                       TeacherRepository teacherRepository,
                                       StudyGroupRepository studyGroupRepository) {
        this.assignmentRepository = assignmentRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.studyGroupRepository = studyGroupRepository;
    }

    public SubjectAssignment createAssignment(Integer subjectId, Integer teacherId,
                                              Integer groupId, String academicYear, String semester) {
        logger.info("Creating assignment: Subject={}, Teacher={}, Group={}, Year={}, Semester={}",
                subjectId, teacherId, groupId, academicYear, semester);

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> {
                    logger.error("Subject not found with ID: {}", subjectId);
                    return new ResourceNotFoundException("Subject", "id", subjectId);
                });

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> {
                    logger.error("Teacher not found with ID: {}", teacherId);
                    return new ResourceNotFoundException("Teacher", "id", teacherId);
                });

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> {
                    logger.error("Study group not found with ID: {}", groupId);
                    return new ResourceNotFoundException("StudyGroup", "id", groupId);
                });

        validateAssignmentData(academicYear, semester);

        if (assignmentRepository.findByAllFields(subjectId, teacherId, groupId, academicYear, semester).isPresent()) {
            logger.warn("Assignment already exists for Subject={}, Teacher={}, Group={}, Year={}, Semester={}",
                    subjectId, teacherId, groupId, academicYear, semester);
            throw new DuplicateResourceException("Assignment already exists for this combination");
        }

        SubjectAssignment assignment = new SubjectAssignment(subject, teacher, group, academicYear, semester);
        assignment = assignmentRepository.save(assignment);

        logger.info("Assignment created successfully with ID: {}", assignment.getAssignmentId());
        return assignment;
    }

    public SubjectAssignment updateAssignment(Integer assignmentId, Integer subjectId,
                                              Integer teacherId, Integer groupId,
                                              String academicYear, String semester) {
        logger.info("Updating assignment ID: {}", assignmentId);

        SubjectAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> {
                    logger.error("Assignment not found with ID: {}", assignmentId);
                    return new ResourceNotFoundException("Assignment", "id", assignmentId);
                });

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyGroup", "id", groupId));

        validateAssignmentData(academicYear, semester);

        assignment.setSubject(subject);
        assignment.setTeacher(teacher);
        assignment.setGroup(group);
        assignment.setAcademicYear(academicYear);
        assignment.setSemester(semester);

        assignment = assignmentRepository.save(assignment);
        logger.info("Assignment updated successfully: {}", assignmentId);
        return assignment;
    }

    public void deleteAssignment(Integer assignmentId) {
        logger.info("Attempting to delete assignment ID: {}", assignmentId);

        if (!assignmentRepository.existsById(assignmentId)) {
            logger.error("Cannot delete - assignment not found with ID: {}", assignmentId);
            throw new ResourceNotFoundException("Assignment", "id", assignmentId);
        }

        SubjectAssignment assignment = assignmentRepository.findById(assignmentId).get();
        int gradeCount = assignment.getGrades() != null ? assignment.getGrades().size() : 0;

        if (gradeCount > 0) {
            logger.warn("Deleting assignment {} which has {} grades", assignmentId, gradeCount);
        }

        assignmentRepository.deleteById(assignmentId);
        logger.info("Assignment deleted successfully: {}", assignmentId);
    }

    public void deleteAssignmentsBySubject(Integer subjectId) {
        logger.info("Deleting all assignments for subject ID: {}", subjectId);
        List<SubjectAssignment> assignments = assignmentRepository.findBySubject_SubjectId(subjectId);
        assignments.forEach(a -> deleteAssignment(a.getAssignmentId()));
        logger.info("Deleted {} assignments for subject {}", assignments.size(), subjectId);
    }

    public List<SubjectAssignment> getAllAssignments() {
        logger.debug("Retrieving all assignments");
        List<SubjectAssignment> assignments = assignmentRepository.findAll();
        logger.debug("Found {} assignments", assignments.size());
        return assignments;
    }

    public List<SubjectAssignment> getAssignmentsByTeacher(Integer teacherId) {
        logger.debug("Retrieving assignments for teacher ID: {}", teacherId);
        return assignmentRepository.findByTeacher_TeacherId(teacherId);
    }

    public List<SubjectAssignment> getAssignmentsBySubject(Integer subjectId) {
        logger.debug("Retrieving assignments for subject ID: {}", subjectId);
        return assignmentRepository.findBySubject_SubjectId(subjectId);
    }

    public List<SubjectAssignment> getAssignmentsByGroup(Integer groupId) {
        logger.debug("Retrieving assignments for group ID: {}", groupId);
        return assignmentRepository.findByGroup_GroupId(groupId);
    }

    public List<SubjectAssignment> getAssignmentsByAcademicYear(String academicYear) {
        logger.debug("Retrieving assignments for academic year: {}", academicYear);
        return assignmentRepository.findByAcademicYear(academicYear);
    }

    public SubjectAssignment getAssignmentById(Integer assignmentId) {
        logger.debug("Retrieving assignment by ID: {}", assignmentId);
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> {
                    logger.error("Assignment not found with ID: {}", assignmentId);
                    return new ResourceNotFoundException("Assignment", "id", assignmentId);
                });
    }

    public boolean isTeacherAssignedToSubject(Integer teacherId, Integer subjectId) {
        List<SubjectAssignment> assignments = assignmentRepository.findByTeacher_TeacherId(teacherId);
        return assignments.stream()
                .anyMatch(a -> a.getSubject().getSubjectId().equals(subjectId));
    }

    public int getTeacherAssignmentCount(Integer teacherId) {
        return getAssignmentsByTeacher(teacherId).size();
    }

    private void validateAssignmentData(String academicYear, String semester) {
        if (academicYear == null || academicYear.trim().isEmpty()) {
            throw new ValidationException("Academic year is required");
        }

        if (semester == null || semester.trim().isEmpty()) {
            throw new ValidationException("Semester is required");
        }

        if (!academicYear.matches("\\d{4}/\\d{4}")) {
            throw new ValidationException("Academic year must be in format YYYY/YYYY, e.g., '2024/2025'");
        }

        List<String> validSemesters = List.of("Fall", "Spring", "Summer", "Winter");
        if (!validSemesters.contains(semester)) {
            throw new ValidationException("Semester must be one of: " + String.join(", ", validSemesters));
        }
    }
}