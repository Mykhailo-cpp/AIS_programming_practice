package com.academic.AIS.repository;

import com.academic.AIS.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Integer> {

    @Query("SELECT g FROM Grade g " +
            "JOIN FETCH g.assignment a " +
            "JOIN FETCH a.subject " +
            "JOIN FETCH a.teacher " +
            "WHERE g.student.studentId = :studentId " +
            "ORDER BY g.gradeDate DESC")
    List<Grade> findByStudentWithDetails(@Param("studentId") Integer studentId);

    @Query("SELECT g FROM Grade g " +
            "WHERE g.assignment.teacher.teacherId = :teacherId")
    List<Grade> findByTeacher_TeacherId(@Param("teacherId") Integer teacherId);

    @Query("SELECT g FROM Grade g " +
            "JOIN FETCH g.student " +
            "WHERE g.assignment.subject.subjectId = :subjectId " +
            "AND g.assignment.teacher.teacherId = :teacherId " +
            "ORDER BY g.student.lastName, g.student.firstName")
    List<Grade> findByTeacherAndSubject(@Param("teacherId") Integer teacherId,
                                        @Param("subjectId") Integer subjectId);

    @Query("SELECT COUNT(g) FROM Grade g WHERE g.assignment.teacher.teacherId = :teacherId")
    Long countByTeacher(@Param("teacherId") Integer teacherId);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END " +
            "FROM Grade g WHERE g.student.studentId = :studentId " +
            "AND g.assignment.assignmentId = :assignmentId")
    boolean existsByStudentAndAssignment(@Param("studentId") Integer studentId,
                                         @Param("assignmentId") Integer assignmentId);

    List<Grade> findByAssignment_AssignmentIdOrderByGradeDateDesc(Integer assignmentId);

    Long countByStudent_StudentId(Integer studentId);

    List<Grade> findByStudent_StudentId(Integer studentId);

    @Query("SELECT g FROM Grade g " +
            "WHERE g.student.studentId = :studentId " +
            "AND g.assignment.subject.subjectId = :subjectId " +
            "ORDER BY g.gradeDate DESC")
    List<Grade> findByStudent_StudentIdAndAssignment_Subject_SubjectId(
            @Param("studentId") Integer studentId,
            @Param("subjectId") Integer subjectId);

}