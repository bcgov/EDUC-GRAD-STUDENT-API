package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.HistoricStudentActivity;
import ca.bc.gov.educ.api.gradstudent.model.entity.HistoricStudentActivityEntity;
import ca.bc.gov.educ.api.gradstudent.model.mapper.HistoricStudentActivityMapper;
import ca.bc.gov.educ.api.gradstudent.repository.HistoricStudentActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HistoricStudentActivityService {

    private final HistoricStudentActivityRepository historicStudentActivityRepository;

    @Transactional(readOnly = true)
    public List<HistoricStudentActivity> getHistoricStudentActivities(UUID studentID) {
        log.debug("getHistoricStudentActivities: studentID = {}", studentID);
        List<HistoricStudentActivityEntity> entities = historicStudentActivityRepository.findByGraduationStudentRecordID(studentID);
        return entities.stream()
                .map(HistoricStudentActivityMapper.mapper::toStructure)
                .collect(Collectors.toList());
    }
}
