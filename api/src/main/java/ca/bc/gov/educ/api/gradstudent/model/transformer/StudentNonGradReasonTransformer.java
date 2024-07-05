package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentNonGradReason;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentNonGradReasonEntity;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class StudentNonGradReasonTransformer {

    private static final Logger logger = LoggerFactory.getLogger(StudentNonGradReasonTransformer.class);

    private static final String JSON_PARSING_ERROR = "Parsing Error: {}";

    @Autowired
    ModelMapper modelMapper;
    
    @Autowired
    GradValidation validation;

    public StudentNonGradReason transformToDTO (StudentNonGradReasonEntity gradStatusEntity) {
        StudentNonGradReason gradStatus = modelMapper.map(gradStatusEntity, StudentNonGradReason.class);
//        gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(gradStatusEntity.getProgramCompletionDate() != null ?
//                EducGradStudentApiUtils.formatDate(gradStatusEntity.getProgramCompletionDate()) : null));
        return gradStatus;
    }

    public StudentNonGradReason transformToDTO (Optional<StudentNonGradReasonEntity> gradStatusEntity) {
        StudentNonGradReasonEntity cae = new StudentNonGradReasonEntity();
        if (gradStatusEntity.isPresent())
            cae = gradStatusEntity.get();

        StudentNonGradReason gradStatus = modelMapper.map(cae, StudentNonGradReason.class);
//        gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(gradStatus.getProgramCompletionDate() != null ? gradStatus.getProgramCompletionDate():null));
        return gradStatus;
    }

    public List<StudentNonGradReason> transformToDTO (Iterable<StudentNonGradReasonEntity> gradStatusEntities ) {
        List<StudentNonGradReason> gradStatusList = new ArrayList<>();
        for (StudentNonGradReasonEntity gradStatusEntity : gradStatusEntities) {
            StudentNonGradReason gradStatus = modelMapper.map(gradStatusEntity, StudentNonGradReason.class);
//            gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(gradStatusEntity.getProgramCompletionDate() != null ? gradStatusEntity.getProgramCompletionDate().toString():null));
            gradStatusList.add(gradStatus);
        }
        return gradStatusList;
    }
}
