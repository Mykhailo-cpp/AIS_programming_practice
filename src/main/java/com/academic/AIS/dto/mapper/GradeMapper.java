package com.academic.AIS.dto.mapper;

import com.academic.AIS.dto.response.GradeResponse;
import com.academic.AIS.model.Grade;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {

    public GradeResponse toResponse(Grade grade) {
        if (grade == null) {
            return null;
        }

        GradeResponse response = new GradeResponse();
        response.setGradeId(grade.getGradeId());
        response.setGradeValue(grade.getGradeValue());
        response.setGradeLevel(grade.getGradeLevel());
        response.setGradeDate(grade.getGradeDate());
        response.setComments(grade.getComments());

        if (grade.getStudent() != null) {
            response.setStudentId(grade.getStudent().getStudentId());
            response.setStudentName(grade.getStudent().getFullName());
        }

        if (grade.getAssignment() != null && grade.getAssignment().getSubject() != null) {
            response.setSubjectId(grade.getAssignment().getSubject().getSubjectId());
            response.setSubjectName(grade.getAssignment().getSubject().getSubjectName());
            response.setSubjectCode(grade.getAssignment().getSubject().getSubjectCode());
        }

        if (grade.getAssignment() != null && grade.getAssignment().getTeacher() != null) {
            response.setTeacherName(grade.getAssignment().getTeacher().getFullName());
        }

        return response;
    }
}
