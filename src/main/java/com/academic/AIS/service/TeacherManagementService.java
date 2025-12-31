package com.academic.AIS.service;

import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.model.SubjectAssignment;
import com.academic.AIS.model.Teacher;
import com.academic.AIS.repository.GradeRepository;
import com.academic.AIS.repository.SubjectAssignmentRepository;
import com.academic.AIS.repository.TeacherRepository;
import com.academic.AIS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TeacherManagementService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final SubjectAssignmentRepository subjectAssignmentRepository;
    private final GradeRepository gradeRepository;

    @Autowired
    public TeacherManagementService(TeacherRepository teacherRepository,
                                    UserRepository userRepository,
                                    AuthenticationService authenticationService,
                                    SubjectAssignmentRepository subjectAssignmentRepository,
                                    GradeRepository gradeRepository) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.subjectAssignmentRepository = subjectAssignmentRepository;
        this.gradeRepository = gradeRepository;
    }

    public Teacher createTeacher(String firstName, String lastName, String email) {
        return authenticationService.registerTeacher(firstName, lastName, email);
    }

    public Teacher updateTeacher(Integer teacherId, String firstName, String lastName, String email) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setEmail(email);

        return teacherRepository.save(teacher);
    }

    public void deleteTeacher(Integer teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("Teacher", "id", teacherId);
        }

        Integer userId = teacherId;
        teacherRepository.deleteById(teacherId);
        userRepository.deleteById(userId);
    }

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public Teacher getTeacherById(Integer teacherId) {
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
    }

    public Optional<Teacher> getTeacherByUsername(String username) {
        return teacherRepository.findByUsername(username);
    }

    public List<SubjectAssignment> getTeacherAssignments(Integer teacherId) {
        return subjectAssignmentRepository.findByTeacher_TeacherId(teacherId);
    }

    public Long countTeacherGrades(Integer teacherId) {
        return gradeRepository.countByTeacher(teacherId);
    }
}