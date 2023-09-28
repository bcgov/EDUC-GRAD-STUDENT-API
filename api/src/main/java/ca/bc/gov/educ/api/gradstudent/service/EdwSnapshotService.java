package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.EdwGraduationSnapshot;
import ca.bc.gov.educ.api.gradstudent.model.entity.EdwGraduationSnapshotEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.EDWGraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.StudentNonGradReasonTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.EdwGraduationSnapshotRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentNonGradReasonRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants.SECOND_DEFAULT_DATE_FORMAT;

@Service
public class EdwSnapshotService {

    private static final Logger logger = LoggerFactory.getLogger(EdwSnapshotService.class);

    final EdwGraduationSnapshotRepository edwGraduationSnapshotRepository;

    final EDWGraduationStatusTransformer edwGraduationStatusTransformer;

    final EducGradStudentApiConstants constants;

    final WebClient webClient;
    final GradValidation validation;

    @Autowired
    public EdwSnapshotService(
                EdwGraduationSnapshotRepository edwGraduationSnapshotRepository,
                EDWGraduationStatusTransformer edwGraduationStatusTransformer,
                StudentNonGradReasonRepository studentNonGradReasonRepository,
                StudentNonGradReasonTransformer studentNonGradReasonTransformer,
                EducGradStudentApiConstants constants,
                WebClient webClient,
                GradValidation validation) {
        this.constants = constants;
        this.webClient = webClient;
        this.validation = validation;
        this.edwGraduationSnapshotRepository = edwGraduationSnapshotRepository;
        this.edwGraduationStatusTransformer = edwGraduationStatusTransformer;
    }

    public EdwGraduationSnapshot retrieve(Integer gradYear, String pen) {
        return edwGraduationStatusTransformer.transformToDTO(edwGraduationSnapshotRepository.findByGradYearAndPen(gradYear, pen));
    }

    public List<EdwGraduationSnapshot> retrieveAll(Integer gradYear) {
        return edwGraduationStatusTransformer.transformToDTO(edwGraduationSnapshotRepository.findByGradYear(gradYear));
    }

    public List<EdwGraduationSnapshot> retrieveByPage(Integer gradYear, Pageable pageable) {
        return edwGraduationStatusTransformer.transformToDTO(edwGraduationSnapshotRepository.findByGradYear(gradYear, pageable));
    }

    public Integer countAllByGradYear(Integer gradYear) {
        return edwGraduationSnapshotRepository.countAllByGradYear(gradYear);
    }

    @Transactional
    public EdwGraduationSnapshot saveEdwGraduationSnapshot(EdwGraduationSnapshot toBeSaved) {
        Optional<EdwGraduationSnapshotEntity> optional = edwGraduationSnapshotRepository.findByGradYearAndPen(toBeSaved.getGradYear(), toBeSaved.getPen());
        EdwGraduationSnapshotEntity toBeSavedEntity = edwGraduationStatusTransformer.transformToEntity(toBeSaved);
        // populate timestamp
        toBeSavedEntity.setRunDate(LocalDate.now());
        String sessionDate = toBeSavedEntity.getGradYear().toString() + "/09/01";
        toBeSavedEntity.setSessionDate(EducGradStudentApiUtils.parseLocalDate(sessionDate, SECOND_DEFAULT_DATE_FORMAT));

        EdwGraduationSnapshotEntity savedEntity;
        if (optional.isPresent()) { // update
            EdwGraduationSnapshotEntity entity = optional.get();
            BeanUtils.copyProperties(toBeSavedEntity, entity, "gradYear", "pen");
            savedEntity = edwGraduationSnapshotRepository.saveAndFlush(entity);
        } else { // create
            savedEntity = edwGraduationSnapshotRepository.saveAndFlush(toBeSavedEntity);
        }
        return edwGraduationStatusTransformer.transformToDTO(savedEntity);
    }
}
