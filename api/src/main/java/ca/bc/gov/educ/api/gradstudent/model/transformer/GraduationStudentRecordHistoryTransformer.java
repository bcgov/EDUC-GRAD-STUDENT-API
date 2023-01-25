package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecordHistory;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
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
public class GraduationStudentRecordHistoryTransformer {

    @Autowired
    ModelMapper modelMapper;
    
    @Autowired
    GradValidation validation;

    public GraduationStudentRecordHistory transformToDTO (GraduationStudentRecordHistoryEntity graduationStudentRecordHistoryEntity) {
    	GraduationStudentRecordHistory graduationStudentRecordHistory = modelMapper.map(graduationStudentRecordHistoryEntity, GraduationStudentRecordHistory.class);
    	graduationStudentRecordHistory.setProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(graduationStudentRecordHistoryEntity.getProgramCompletionDate() != null ? graduationStudentRecordHistoryEntity.getProgramCompletionDate().toString():null));
    	return graduationStudentRecordHistory;
    }

    public GraduationStudentRecordHistory transformToDTO ( Optional<GraduationStudentRecordHistoryEntity> graduationStudentRecordHistoryEntity ) {
    	GraduationStudentRecordHistoryEntity cae = new GraduationStudentRecordHistoryEntity();
        if (graduationStudentRecordHistoryEntity.isPresent())
            cae = graduationStudentRecordHistoryEntity.get();
        	
        GraduationStudentRecordHistory graduationStudentRecordHistory = modelMapper.map(cae, GraduationStudentRecordHistory.class);
        graduationStudentRecordHistory.setProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(graduationStudentRecordHistory.getProgramCompletionDate() != null ? graduationStudentRecordHistory.getProgramCompletionDate():null));
        return graduationStudentRecordHistory;
    }

	public List<GraduationStudentRecordHistory> transformToDTO (Iterable<GraduationStudentRecordHistoryEntity> graduationStudentRecordHistoryEntities ) {
		List<GraduationStudentRecordHistory> graduationStudentRecordHistoryList = new ArrayList<>();
        for (GraduationStudentRecordHistoryEntity graduationStudentRecordHistoryEntity : graduationStudentRecordHistoryEntities) {
            GraduationStudentRecordHistory graduationStudentRecordHistory = modelMapper.map(graduationStudentRecordHistoryEntity, GraduationStudentRecordHistory.class);
        	graduationStudentRecordHistory.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStudentRecordHistoryEntity.getProgramCompletionDate(), "yyyy/MM"));
        	graduationStudentRecordHistoryList.add(graduationStudentRecordHistory);
        }
        return graduationStudentRecordHistoryList;
    }

    public GraduationStudentRecordHistoryEntity transformToEntity(GraduationStudentRecordHistory graduationStudentRecordHistory) {
        GraduationStudentRecordHistoryEntity graduationStudentRecordHistoryEntity = modelMapper.map(graduationStudentRecordHistory, GraduationStudentRecordHistoryEntity.class);
        Date programCompletionDate = null;
        try {
        	if(graduationStudentRecordHistory.getProgramCompletionDate() != null) {
        		String pDate = graduationStudentRecordHistory.getProgramCompletionDate();
        		if(graduationStudentRecordHistory.getProgramCompletionDate().length() <= 7) {
        			pDate = EducGradStudentApiUtils.parsingTraxDate(graduationStudentRecordHistory.getProgramCompletionDate());
        		}
        		programCompletionDate= Date.valueOf(pDate);
        	}
        }catch(Exception e) {
        	validation.addErrorAndStop("Invalid Date");
        }
        graduationStudentRecordHistoryEntity.setProgramCompletionDate(programCompletionDate);
        return graduationStudentRecordHistoryEntity;
    }
}
