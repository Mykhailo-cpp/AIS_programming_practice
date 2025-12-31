package com.academic.AIS.dto.mapper;

import com.academic.AIS.dto.response.StudentResponse;
import com.academic.AIS.model.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }

        return new StudentResponse(
                student.getStudentId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getUsername(),
                student.getGroup()
        );
    }
}