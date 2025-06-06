package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.EquivalentOrChallengeCode;
import ca.bc.gov.educ.api.gradstudent.model.mapper.EquivalentOrChallengeCodeMapper;
import ca.bc.gov.educ.api.gradstudent.repository.EquivalentOrChallengeCodeRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EquivalentOrChallengeCodeService {

    private static final EquivalentOrChallengeCodeMapper mapper = EquivalentOrChallengeCodeMapper.mapper;
    private final EquivalentOrChallengeCodeRepository equivalentOrChallengeCodeRepository;

    public List<EquivalentOrChallengeCode> findAll() {
        return equivalentOrChallengeCodeRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder")).stream().map(mapper::toStructure).toList();
    }

    public EquivalentOrChallengeCode findByEquivalentOrChallengeCode(String equivalentOrChallengeCode) {
        return equivalentOrChallengeCodeRepository.findById(equivalentOrChallengeCode)
                .map(mapper::toStructure)
                .orElse(null);
    }
}

