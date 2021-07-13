package ca.bc.gov.educ.api.gradstudent.transformer;

import ca.bc.gov.educ.api.gradstudent.dto.GraduationStatus;
import ca.bc.gov.educ.api.gradstudent.entity.GraduationStatusEntity;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStatusApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class GraduationStatusTransformer {

    @Autowired
    ModelMapper modelMapper;
    
    @Autowired
    GradValidation validation;

    public GraduationStatus transformToDTO (GraduationStatusEntity gradStatusEntity) {
    	GraduationStatus gradStatus = modelMapper.map(gradStatusEntity, GraduationStatus.class);
    	gradStatus.setProgramCompletionDate(EducGradStatusApiUtils.parseDateFromString(gradStatusEntity.getProgramCompletionDate() != null ? gradStatusEntity.getProgramCompletionDate().toString():null));
    	return gradStatus;
    }

    public GraduationStatus transformToDTO ( Optional<GraduationStatusEntity> gradStatusEntity ) {
    	GraduationStatusEntity cae = new GraduationStatusEntity();
        if (gradStatusEntity.isPresent())
            cae = gradStatusEntity.get();
        	
        GraduationStatus gradStatus = modelMapper.map(cae, GraduationStatus.class);
        gradStatus.setProgramCompletionDate(EducGradStatusApiUtils.parseTraxDate(gradStatus.getProgramCompletionDate() != null ? gradStatus.getProgramCompletionDate():null));
        return gradStatus;
    }

	public List<GraduationStatus> transformToDTO (Iterable<GraduationStatusEntity> gradStatusEntities ) {
		List<GraduationStatus> gradStatusList = new ArrayList<>();
        for (GraduationStatusEntity gradStatusEntity : gradStatusEntities) {
        	GraduationStatus gradStatus = modelMapper.map(gradStatusEntity, GraduationStatus.class);            
        	gradStatus.setProgramCompletionDate(EducGradStatusApiUtils.parseTraxDate(gradStatus.getProgramCompletionDate() != null ? gradStatus.getProgramCompletionDate():null));
        	gradStatusList.add(gradStatus);
        }
        return gradStatusList;
    }

    public GraduationStatusEntity transformToEntity(GraduationStatus gradStatus) {
        GraduationStatusEntity gradStatusEntity = modelMapper.map(gradStatus, GraduationStatusEntity.class);
        Date programCompletionDate = null;
        try {
        	if(gradStatus.getProgramCompletionDate() != null) {
        		String pDate = gradStatus.getProgramCompletionDate();
        		if(gradStatus.getProgramCompletionDate().length() <= 7) {
        			pDate = EducGradStatusApiUtils.parsingTraxDate(gradStatus.getProgramCompletionDate());
        		}
        		programCompletionDate= Date.valueOf(pDate);
        	}
        }catch(Exception e) {
        	validation.addErrorAndStop("Invalid Date");
        }
        gradStatusEntity.setProgramCompletionDate(programCompletionDate);
        return gradStatusEntity;
    }
}
