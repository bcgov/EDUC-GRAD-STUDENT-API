package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.ExamSpecialCaseCode;
import ca.bc.gov.educ.api.gradstudent.model.mapper.ExamSpecialCaseCodeMapper;
import ca.bc.gov.educ.api.gradstudent.repository.ExamSpecialCaseCodeRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ExamSpecialCaseCodeService {

    private static final ExamSpecialCaseCodeMapper mapper = ExamSpecialCaseCodeMapper.mapper;
    private final ExamSpecialCaseCodeRepository examSpecialCaseCodeRepository;

    public List<ExamSpecialCaseCode> findAll() {
        return examSpecialCaseCodeRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder")).stream().map(mapper::toStructure).toList();
    }

    public ExamSpecialCaseCode findBySpecialCaseCode(String examSpecialCaseCode) {
        return examSpecialCaseCodeRepository.findById(examSpecialCaseCode)
                .map(mapper::toStructure)
                .orElse(null);
    }
}
