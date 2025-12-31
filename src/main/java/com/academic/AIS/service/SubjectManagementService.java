package com.academic.AIS.service;

import com.academic.AIS.exception.DuplicateResourceException;
import com.academic.AIS.exception.ResourceNotFoundException;
import com.academic.AIS.exception.ValidationException;
import com.academic.AIS.model.Subject;
import com.academic.AIS.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class SubjectManagementService {

    private final SubjectRepository subjectRepository;

    @Autowired
    public SubjectManagementService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public Subject createSubject(String subjectName, String subjectCode,
                                 Integer credits, String description) {
        validateSubjectData(subjectName, subjectCode, credits);

        if (subjectRepository.findBySubjectCode(subjectCode).isPresent()) {
            throw new DuplicateResourceException("Subject", "subjectCode", subjectCode);
        }

        Subject subject = new Subject(subjectName, subjectCode, credits, description);
        return subjectRepository.save(subject);
    }

    public Subject updateSubject(Integer subjectId, String subjectName,
                                 String subjectCode, Integer credits, String description) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        validateSubjectData(subjectName, subjectCode, credits);

        subject.setSubjectName(subjectName);
        subject.setSubjectCode(subjectCode);
        subject.setCredits(credits);
        subject.setDescription(description);

        return subjectRepository.save(subject);
    }

    public void deleteSubject(Integer subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject", "id", subjectId);
        }
        subjectRepository.deleteById(subjectId);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubjectById(Integer subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));
    }

    private void validateSubjectData(String subjectName, String subjectCode, Integer credits) {
        if (subjectName == null || subjectName.trim().isEmpty()) {
            throw new ValidationException("Subject name is required");
        }
        if (subjectCode == null || subjectCode.trim().isEmpty()) {
            throw new ValidationException("Subject code is required");
        }
        if (credits == null || credits < 0) {
            throw new ValidationException("Invalid credits");
        }
    }
}