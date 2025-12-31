package com.academic.AIS.service;

import com.academic.AIS.dto.request.CreateStudentRequest;
import com.academic.AIS.dto.response.StudentResponse;
import com.academic.AIS.dto.mapper.StudentMapper;
import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.model.Student;
import com.academic.AIS.model.StudyGroup;
import com.academic.AIS.repository.StudentRepository;
import com.academic.AIS.repository.StudyGroupRepository;
import com.academic.AIS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentManagementService {

    private final StudentRepository studentRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final StudentMapper studentMapper;

    @Autowired
    public StudentManagementService(StudentRepository studentRepository,
                                    StudyGroupRepository studyGroupRepository,
                                    UserRepository userRepository,
                                    AuthenticationService authenticationService,
                                    StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.studentMapper = studentMapper;
    }

    public StudentResponse createStudent(CreateStudentRequest request) {
        Student student = authenticationService.registerStudent(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail()
        );

        if (request.getGroupId() != null) {
            assignStudentToGroup(student.getStudentId(), request.getGroupId());
        }

        return studentMapper.toResponse(student);
    }

    public StudentResponse updateStudent(Integer studentId, CreateStudentRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setEmail(request.getEmail());

        student = studentRepository.save(student);

        if (request.getGroupId() != null) {
            assignStudentToGroup(studentId, request.getGroupId());
        } else {
            removeStudentFromGroup(studentId);
        }

        return studentMapper.toResponse(student);
    }

    public void deleteStudent(Integer studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }

        Integer userId = studentId;
        studentRepository.deleteById(studentId);
        userRepository.deleteById(userId);
    }

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public StudentResponse getStudentById(Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        return studentMapper.toResponse(student);
    }

    public StudentResponse assignStudentToGroup(Integer studentId, Integer groupId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyGroup", "id", groupId));

        student.setGroup(group);
        student = studentRepository.save(student);

        return studentMapper.toResponse(student);
    }

    public StudentResponse removeStudentFromGroup(Integer studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        student.setGroup(null);
        student = studentRepository.save(student);

        return studentMapper.toResponse(student);
    }
}