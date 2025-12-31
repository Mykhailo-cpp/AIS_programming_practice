package com.academic.AIS.service;

import com.academic.AIS.repository.StudentRepository;
import com.academic.AIS.repository.TeacherRepository;
import com.academic.AIS.repository.StudyGroupRepository;
import com.academic.AIS.repository.SubjectRepository;
import com.academic.AIS.repository.GradeRepository;
import com.academic.AIS.repository.SubjectAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;


@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;
    private final SubjectAssignmentRepository assignmentRepository;

    @Autowired
    public StatisticsService(StudentRepository studentRepository,
                             TeacherRepository teacherRepository,
                             StudyGroupRepository studyGroupRepository,
                             SubjectRepository subjectRepository,
                             GradeRepository gradeRepository,
                             SubjectAssignmentRepository assignmentRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.subjectRepository = subjectRepository;
        this.gradeRepository = gradeRepository;
        this.assignmentRepository = assignmentRepository;
    }


    public SystemStatistics getSystemStatistics() {
        logger.info("Calculating system statistics");

        long totalStudents = studentRepository.count();
        long totalTeachers = teacherRepository.count();
        long totalGroups = studyGroupRepository.count();
        long totalSubjects = subjectRepository.count();
        long totalGrades = gradeRepository.count();
        long totalAssignments = assignmentRepository.count();

        logger.info("System statistics calculated: Students={}, Teachers={}, Groups={}, Subjects={}, Grades={}, Assignments={}",
                totalStudents, totalTeachers, totalGroups, totalSubjects, totalGrades, totalAssignments);

        return new SystemStatistics(totalStudents, totalTeachers, totalGroups,
                totalSubjects, totalGrades, totalAssignments);
    }


    public TeacherStatistics getTeacherStatistics(Integer teacherId) {
        logger.debug("Calculating statistics for teacher ID: {}", teacherId);

        long assignmentCount = assignmentRepository.findByTeacher_TeacherId(teacherId).size();
        long gradeCount = gradeRepository.countByTeacher(teacherId);

        // Calculate unique students taught
        long studentCount = gradeRepository.findByTeacher_TeacherId(teacherId).stream()
                .map(grade -> grade.getStudent().getStudentId())
                .distinct()
                .count();

        // Calculate average grade given by teacher
        Double averageGrade = gradeRepository.findByTeacher_TeacherId(teacherId).stream()
                .mapToDouble(grade -> grade.getGradeValue())
                .average()
                .orElse(0.0);

        return new TeacherStatistics(assignmentCount, studentCount, gradeCount, averageGrade);
    }



    public StudentStatistics getStudentStatistics(Integer studentId) {
        logger.debug("Calculating statistics for student ID: {}", studentId);

        long totalGrades = gradeRepository.countByStudent_StudentId(studentId);

        Double averageGrade = gradeRepository.findByStudentWithDetails(studentId).stream()
                .mapToDouble(grade -> grade.getGradeValue())
                .average()
                .orElse(0.0);

        long passingGrades = gradeRepository.findByStudentWithDetails(studentId).stream()
                .filter(grade -> grade.getGradeValue() >= 5)
                .count();

        long failingGrades = totalGrades - passingGrades;

        return new StudentStatistics(totalGrades, averageGrade, passingGrades, failingGrades);
    }



    public GroupStatistics getGroupStatistics(Integer groupId) {
        logger.debug("Calculating statistics for group ID: {}", groupId);

        long studentCount = studentRepository.countByGroup_GroupId(groupId);
        long assignmentCount = assignmentRepository.findByGroup_GroupId(groupId).size();

        // Calculate average grade for the group
        Double averageGrade = gradeRepository.findAll().stream()
                .filter(grade -> grade.getStudent().getGroup() != null &&
                        grade.getStudent().getGroup().getGroupId().equals(groupId))
                .mapToDouble(grade -> grade.getGradeValue())
                .average()
                .orElse(0.0);

        return new GroupStatistics(studentCount, assignmentCount, averageGrade);
    }




    public SubjectStatistics getSubjectStatistics(Integer subjectId) {
        logger.debug("Calculating statistics for subject ID: {}", subjectId);

        long assignmentCount = assignmentRepository.findBySubject_SubjectId(subjectId).size();

        long studentCount = assignmentRepository.findBySubject_SubjectId(subjectId).stream()
                .flatMap(assignment -> assignment.getGroup().getStudents().stream())
                .distinct()
                .count();

        long gradeCount = gradeRepository.findAll().stream()
                .filter(grade -> grade.getAssignment().getSubject().getSubjectId().equals(subjectId))
                .count();

        Double averageGrade = gradeRepository.findAll().stream()
                .filter(grade -> grade.getAssignment().getSubject().getSubjectId().equals(subjectId))
                .mapToDouble(grade -> grade.getGradeValue())
                .average()
                .orElse(0.0);

        return new SubjectStatistics(assignmentCount, studentCount, gradeCount, averageGrade);
    }




    public Map<Integer, Long> getGradeDistribution() {
        logger.debug("Calculating grade distribution");

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 0; i <= 10; i++) {
            distribution.put(i, 0L);
        }

        gradeRepository.findAll().forEach(grade -> {
            Integer value = grade.getGradeValue();
            distribution.put(value, distribution.get(value) + 1);
        });

        return distribution;
    }

    // ==================== Inner Classes for Statistics ====================



    public static class SystemStatistics {
        private final long totalStudents;
        private final long totalTeachers;
        private final long totalGroups;
        private final long totalSubjects;
        private final long totalGrades;
        private final long totalAssignments;

        public SystemStatistics(long totalStudents, long totalTeachers, long totalGroups,
                                long totalSubjects, long totalGrades, long totalAssignments) {
            this.totalStudents = totalStudents;
            this.totalTeachers = totalTeachers;
            this.totalGroups = totalGroups;
            this.totalSubjects = totalSubjects;
            this.totalGrades = totalGrades;
            this.totalAssignments = totalAssignments;
        }

        public long getTotalStudents() {
            return totalStudents;
        }

        public long getTotalTeachers() {
            return totalTeachers;
        }

        public long getTotalGroups() {
            return totalGroups;
        }

        public long getTotalSubjects() {
            return totalSubjects;
        }

        public long getTotalGrades() {
            return totalGrades;
        }

        public long getTotalAssignments() {
            return totalAssignments;
        }
    }




    public static class TeacherStatistics {
        private final long totalAssignments;
        private final long totalStudents;
        private final long totalGrades;
        private final double averageGrade;

        public TeacherStatistics(long totalAssignments, long totalStudents,
                                 long totalGrades, double averageGrade) {
            this.totalAssignments = totalAssignments;
            this.totalStudents = totalStudents;
            this.totalGrades = totalGrades;
            this.averageGrade = averageGrade;
        }

        public long getTotalAssignments() {
            return totalAssignments;
        }

        public long getTotalStudents() {
            return totalStudents;
        }

        public long getTotalGrades() {
            return totalGrades;
        }

        public double getAverageGrade() {
            return averageGrade;
        }
    }



    public static class StudentStatistics {
        private final long totalGrades;
        private final double averageGrade;
        private final long passingGrades;
        private final long failingGrades;

        public StudentStatistics(long totalGrades, double averageGrade,
                                 long passingGrades, long failingGrades) {
            this.totalGrades = totalGrades;
            this.averageGrade = averageGrade;
            this.passingGrades = passingGrades;
            this.failingGrades = failingGrades;
        }

        public long getTotalGrades() {
            return totalGrades;
        }

        public double getAverageGrade() {
            return averageGrade;
        }

        public long getPassingGrades() {
            return passingGrades;
        }

        public long getFailingGrades() {
            return failingGrades;
        }
    }



    public static class GroupStatistics {
        private final long studentCount;
        private final long assignmentCount;
        private final double averageGrade;

        public GroupStatistics(long studentCount, long assignmentCount, double averageGrade) {
            this.studentCount = studentCount;
            this.assignmentCount = assignmentCount;
            this.averageGrade = averageGrade;
        }

        public long getStudentCount() {
            return studentCount;
        }

        public long getAssignmentCount() {
            return assignmentCount;
        }

        public double getAverageGrade() {
            return averageGrade;
        }
    }



    public static class SubjectStatistics {
        private final long assignmentCount;
        private final long studentCount;
        private final long gradeCount;
        private final double averageGrade;

        public SubjectStatistics(long assignmentCount, long studentCount,
                                 long gradeCount, double averageGrade) {
            this.assignmentCount = assignmentCount;
            this.studentCount = studentCount;
            this.gradeCount = gradeCount;
            this.averageGrade = averageGrade;
        }
        public long getAssignmentCount() {
            return assignmentCount;
        }

        public long getStudentCount() {
            return studentCount;
        }

        public long getGradeCount() {
            return gradeCount;
        }

        public double getAverageGrade() {
            return averageGrade;
        }
    }
}
