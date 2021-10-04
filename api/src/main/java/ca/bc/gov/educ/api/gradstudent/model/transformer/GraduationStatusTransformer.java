package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
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

    public GraduationStudentRecord transformToDTO (GraduationStudentRecordEntity gradStatusEntity) {
    	GraduationStudentRecord gradStatus = modelMapper.map(gradStatusEntity, GraduationStudentRecord.class);
    	gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(gradStatusEntity.getProgramCompletionDate() != null ? gradStatusEntity.getProgramCompletionDate().toString():null));
    	return gradStatus;
    }

    public GraduationStudentRecord transformToDTO ( Optional<GraduationStudentRecordEntity> gradStatusEntity ) {
    	GraduationStudentRecordEntity cae = new GraduationStudentRecordEntity();
        if (gradStatusEntity.isPresent())
            cae = gradStatusEntity.get();
        	
        GraduationStudentRecord gradStatus = modelMapper.map(cae, GraduationStudentRecord.class);
        gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(gradStatus.getProgramCompletionDate() != null ? gradStatus.getProgramCompletionDate():null));
        return gradStatus;
    }

	public List<GraduationStudentRecord> transformToDTO (Iterable<GraduationStudentRecordEntity> gradStatusEntities ) {
		List<GraduationStudentRecord> gradStatusList = new ArrayList<>();
        for (GraduationStudentRecordEntity gradStatusEntity : gradStatusEntities) {
        	GraduationStudentRecord gradStatus = new GraduationStudentRecord();
        	gradStatus.setStudentID(gradStatusEntity.getStudentID());
        	gradStatus.setProgram(gradStatusEntity.getProgram());
        	gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(gradStatusEntity.getProgramCompletionDate() != null ? gradStatusEntity.getProgramCompletionDate().toString():null));
        	gradStatusList.add(gradStatus);
        }
        return gradStatusList;
    }

    public GraduationStudentRecordEntity transformToEntity(GraduationStudentRecord gradStatus) {
        GraduationStudentRecordEntity gradStatusEntity = modelMapper.map(gradStatus, GraduationStudentRecordEntity.class);
        Date programCompletionDate = null;
        try {
        	if(gradStatus.getProgramCompletionDate() != null) {
        		String pDate = gradStatus.getProgramCompletionDate();
        		if(gradStatus.getProgramCompletionDate().length() <= 7) {
        			pDate = EducGradStudentApiUtils.parsingTraxDate(gradStatus.getProgramCompletionDate());
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
