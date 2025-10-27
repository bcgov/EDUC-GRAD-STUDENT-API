package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.EdwGraduationSnapshot;
import ca.bc.gov.educ.api.gradstudent.model.entity.EdwGraduationSnapshotEntity;
import ca.bc.gov.educ.api.gradstudent.util.DateUtils;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class EDWGraduationStatusTransformer {

    private static final Logger logger = LoggerFactory.getLogger(EDWGraduationStatusTransformer.class);

    private static final String JSON_PARSING_ERROR = "Parsing Error: {}";

    @Autowired
    ModelMapper modelMapper;
    
    @Autowired
    GradValidation validation;

    public EdwGraduationSnapshot transformToDTO (EdwGraduationSnapshotEntity gradStatusEntity) {
        EdwGraduationSnapshot gradStatus = modelMapper.map(gradStatusEntity, EdwGraduationSnapshot.class);
        gradStatus.setGraduatedDate(EducGradStudentApiUtils.parseDateFromString(gradStatusEntity.getGraduatedDate() != null ?
                EducGradStudentApiUtils.formatDate(DateUtils.toDate(gradStatusEntity.getGraduatedDate())) : null));
        return gradStatus;
    }

    public EdwGraduationSnapshot transformToDTO (Optional<EdwGraduationSnapshotEntity> gradStatusEntity) {
        EdwGraduationSnapshotEntity cae = new EdwGraduationSnapshotEntity();
        if (gradStatusEntity.isPresent())
            cae = gradStatusEntity.get();

        EdwGraduationSnapshot gradStatus = modelMapper.map(cae, EdwGraduationSnapshot.class);
        gradStatus.setGraduatedDate(EducGradStudentApiUtils.parseDateFromString(cae.getGraduatedDate() != null ?
                EducGradStudentApiUtils.formatDate(DateUtils.toDate(cae.getGraduatedDate())) : null));
        return gradStatus;
    }

    public List<EdwGraduationSnapshot> transformToDTO (Iterable<EdwGraduationSnapshotEntity> gradStatusEntities ) {
        List<EdwGraduationSnapshot> gradStatusList = new ArrayList<>();
        for (EdwGraduationSnapshotEntity gradStatusEntity : gradStatusEntities) {
            EdwGraduationSnapshot gradStatus = modelMapper.map(gradStatusEntity, EdwGraduationSnapshot.class);
            gradStatus.setGraduatedDate(EducGradStudentApiUtils.parseDateFromString(gradStatusEntity.getGraduatedDate() != null ?
                    EducGradStudentApiUtils.formatDate(DateUtils.toDate(gradStatusEntity.getGraduatedDate())) : null));
            gradStatusList.add(gradStatus);
        }
        return gradStatusList;
    }

    public EdwGraduationSnapshotEntity transformToEntity(EdwGraduationSnapshot gradStatus) {
        EdwGraduationSnapshotEntity gradStatusEntity = modelMapper.map(gradStatus, EdwGraduationSnapshotEntity.class);
        LocalDate programCompletionDate = null;
        try {
            if (gradStatus.getGraduatedDate() != null) {
                String pDate = gradStatus.getGraduatedDate();
                if (gradStatus.getGraduatedDate().length() == 6) {
                    pDate = EducGradStudentApiUtils.parsingEdwDate(gradStatus.getGraduatedDate());
                } else if (gradStatus.getGraduatedDate().length() == 7) {
                    pDate = EducGradStudentApiUtils.parsingTraxDate(gradStatus.getGraduatedDate());
                }
                programCompletionDate = EducGradStudentApiUtils.parseLocalDate(pDate, EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
            }
        } catch(Exception e) {
            validation.addErrorAndStop("Invalid GraduatedDate");
        }
        gradStatusEntity.setGraduatedDate(programCompletionDate);
        return gradStatusEntity;
    }
}
