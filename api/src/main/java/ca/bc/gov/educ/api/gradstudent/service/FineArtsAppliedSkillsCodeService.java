package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.FineArtsAppliedSkillsCode;
import ca.bc.gov.educ.api.gradstudent.model.mapper.FineArtsAppliedSkillsCodeMapper;
import ca.bc.gov.educ.api.gradstudent.repository.FineArtsAppliedSkillsCodeRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FineArtsAppliedSkillsCodeService {

    private static final FineArtsAppliedSkillsCodeMapper mapper = FineArtsAppliedSkillsCodeMapper.mapper;
    private final FineArtsAppliedSkillsCodeRepository fineArtsAppliedSkillsCodeRepository;

    public List<FineArtsAppliedSkillsCode> findAll() {
        return fineArtsAppliedSkillsCodeRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder")).stream().map(mapper::toStructure).toList();
    }

    public FineArtsAppliedSkillsCode findByFineArtsAppliedSkillsCode(String fineArtsAppliedSkillsCode) {
        return fineArtsAppliedSkillsCodeRepository.findById(fineArtsAppliedSkillsCode)
                .map(mapper::toStructure)
                .orElse(null);
    }
}

