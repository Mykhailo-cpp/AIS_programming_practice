package com.academic.AIS.service;

import com.academic.AIS.exception.DuplicateResourceException;
import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.exception.UnauthorizedException;
import com.academic.AIS.exception.ValidationException;
import com.academic.AIS.model.Grade;
import com.academic.AIS.model.Student;
import com.academic.AIS.model.SubjectAssignment;
import com.academic.AIS.repository.GradeRepository;
import com.academic.AIS.repository.StudentRepository;
import com.academic.AIS.repository.SubjectAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectAssignmentRepository assignmentRepository;

    @Autowired
    public GradeService(GradeRepository gradeRepository,
                        StudentRepository studentRepository,
                        SubjectAssignmentRepository assignmentRepository) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public Grade enterGrade(Integer teacherId, Integer studentId, Integer assignmentId,
                            Integer gradeValue, String comments) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        SubjectAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));

        if (!assignment.getTeacher().getTeacherId().equals(teacherId)) {
            throw new UnauthorizedException("You are not assigned to teach this subject");
        }

        if (student.getGroup() == null ||
                !student.getGroup().getGroupId().equals(assignment.getGroup().getGroupId())) {
            throw new ValidationException("Student is not in the group for this subject");
        }

        if (gradeRepository.existsByStudentAndAssignment(studentId, assignmentId)) {
            throw new DuplicateResourceException("Grade already exists for this student and assignment. Use update instead.");
        }

        if (gradeValue < 0 || gradeValue > 10) {
            throw new ValidationException("Grade must be between 0 and 10");
        }

        Grade grade = new Grade(student, assignment, gradeValue, comments);
        return gradeRepository.save(grade);
    }

    public Grade updateGrade(Integer gradeId, Integer teacherId,
                             Integer newGradeValue, String newComments) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", gradeId));

        if (!grade.getAssignment().getTeacher().getTeacherId().equals(teacherId)) {
            throw new UnauthorizedException("You can only edit grades you assigned");
        }

        if (newGradeValue < 0 || newGradeValue > 10) {
            throw new ValidationException("Grade must be between 0 and 10");
        }

        grade.setGradeValue(newGradeValue);
        grade.setComments(newComments);
        grade.setGradeDate(LocalDate.now());

        return gradeRepository.save(grade);
    }

    public Grade deleteGrade(Integer gradeId, Integer teacherId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", gradeId));

        if (!grade.getAssignment().getTeacher().getTeacherId().equals(teacherId)) {
            throw new UnauthorizedException("You can only delete grades you assigned");
        }

        Grade deletedGrade = grade;
        gradeRepository.deleteById(gradeId);

        return deletedGrade;
    }

    public List<Grade> getTeacherGrades(Integer teacherId) {
        return gradeRepository.findByTeacher_TeacherId(teacherId);
    }

    public List<Grade> getGradesForTeacherSubject(Integer teacherId, Integer subjectId) {
        return gradeRepository.findByTeacherAndSubject(teacherId, subjectId);
    }

    public List<Grade> getGradesByAssignment(Integer assignmentId) {
        return gradeRepository.findByAssignment_AssignmentIdOrderByGradeDateDesc(assignmentId);
    }
}