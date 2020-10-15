package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradStudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GradStudentService {

    @Autowired
    GradStudentRepository gradStudentRepository;

    public Optional<GradStudentEntity> getStudentByPen(String pen) {
        return gradStudentRepository.findById(pen);
    }
}
