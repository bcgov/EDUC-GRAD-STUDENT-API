package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecordHistory;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentOptionalProgramHistory;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramHistoryEntity;
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
public class StudentOptionalProgramHistoryTransformer {

    @Autowired
    ModelMapper modelMapper;
    
    @Autowired
    GradValidation validation;

    public StudentOptionalProgramHistory transformToDTO (StudentOptionalProgramHistoryEntity studentOptionalProgramHistoryEntity) {
        StudentOptionalProgramHistory studentOptionalProgramHistory = modelMapper.map(studentOptionalProgramHistoryEntity, StudentOptionalProgramHistory.class);
        studentOptionalProgramHistory.setOptionalProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(studentOptionalProgramHistory.getOptionalProgramCompletionDate() != null ? studentOptionalProgramHistory.getOptionalProgramCompletionDate().toString():null));
    	return studentOptionalProgramHistory;
    }

    public StudentOptionalProgramHistory transformToDTO ( Optional<StudentOptionalProgramHistoryEntity> studentOptionalProgramHistoryEntity ) {
        StudentOptionalProgramHistoryEntity cae = new StudentOptionalProgramHistoryEntity();
        if (studentOptionalProgramHistoryEntity.isPresent())
            cae = studentOptionalProgramHistoryEntity.get();

        StudentOptionalProgramHistory studentOptionalProgramHistory = modelMapper.map(cae, StudentOptionalProgramHistory.class);
        studentOptionalProgramHistory.setOptionalProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(cae.getStudentOptionalProgramData() != null ? cae.getOptionalProgramCompletionDate().toString():null));
        return studentOptionalProgramHistory;
    }

	public List<StudentOptionalProgramHistory> transformToDTO (Iterable<StudentOptionalProgramHistoryEntity> graduationStudentRecordHistoryEntities ) {
		List<StudentOptionalProgramHistory> graduationStudentRecordHistoryList = new ArrayList<>();
        for (StudentOptionalProgramHistoryEntity graduationStudentRecordHistoryEntity : graduationStudentRecordHistoryEntities) {
            StudentOptionalProgramHistory graduationStudentRecordHistory = new StudentOptionalProgramHistory();
            graduationStudentRecordHistory = modelMapper.map(graduationStudentRecordHistoryEntity, StudentOptionalProgramHistory.class);
        	graduationStudentRecordHistory.setOptionalProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(graduationStudentRecordHistoryEntity.getOptionalProgramCompletionDate() != null ? graduationStudentRecordHistoryEntity.getOptionalProgramCompletionDate().toString():null));
        	graduationStudentRecordHistoryList.add(graduationStudentRecordHistory);
        }
        return graduationStudentRecordHistoryList;
    }

    public StudentOptionalProgramHistoryEntity transformToEntity(StudentOptionalProgramHistory graduationStudentRecordHistory) {
        StudentOptionalProgramHistoryEntity graduationStudentRecordHistoryEntity = modelMapper.map(graduationStudentRecordHistory, StudentOptionalProgramHistoryEntity.class);
        Date programCompletionDate = null;
        try {
        	if(graduationStudentRecordHistory.getOptionalProgramCompletionDate() != null) {
        		String pDate = graduationStudentRecordHistory.getOptionalProgramCompletionDate();
        		if(graduationStudentRecordHistory.getOptionalProgramCompletionDate().length() <= 7) {
        			pDate = EducGradStudentApiUtils.parsingTraxDate(graduationStudentRecordHistory.getOptionalProgramCompletionDate());
        		}
        		programCompletionDate= Date.valueOf(pDate);
        	}
        }catch(Exception e) {
        	validation.addErrorAndStop("Invalid Date");
        }
        graduationStudentRecordHistoryEntity.setOptionalProgramCompletionDate(programCompletionDate);
        return graduationStudentRecordHistoryEntity;
    }
}
