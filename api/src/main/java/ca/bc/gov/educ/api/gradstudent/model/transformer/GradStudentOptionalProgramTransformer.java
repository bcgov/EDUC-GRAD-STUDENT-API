package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class GradStudentOptionalProgramTransformer {

    @Autowired
    ModelMapper modelMapper;

    public StudentOptionalProgram transformToDTO (StudentOptionalProgramEntity gradStudentOptionalProgramEntity) {
    	StudentOptionalProgram gradStudentOptionalProgram = modelMapper.map(gradStudentOptionalProgramEntity, StudentOptionalProgram.class);
    	gradStudentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(gradStudentOptionalProgram.getOptionalProgramCompletionDate() != null ? gradStudentOptionalProgram.getOptionalProgramCompletionDate():null));
    	return gradStudentOptionalProgram;
    }

    public StudentOptionalProgram transformToDTO ( Optional<StudentOptionalProgramEntity> gradStudentOptionalProgramEntity ) {
    	StudentOptionalProgramEntity cae = new StudentOptionalProgramEntity();
        if (gradStudentOptionalProgramEntity.isPresent())
            cae = gradStudentOptionalProgramEntity.get();
        	
        StudentOptionalProgram gradStudentOptionalProgram = modelMapper.map(cae, StudentOptionalProgram.class);
        gradStudentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(gradStudentOptionalProgram.getOptionalProgramCompletionDate() != null ? gradStudentOptionalProgram.getOptionalProgramCompletionDate():null));
        return gradStudentOptionalProgram;
    }

	public List<StudentOptionalProgram> transformToDTO (Iterable<StudentOptionalProgramEntity> gradStudentOptionalProgramEntities ) {
		List<StudentOptionalProgram> gradStudentOptionalProgramList = new ArrayList<>();
        for (StudentOptionalProgramEntity gradStudentOptionalProgramEntity : gradStudentOptionalProgramEntities) {
        	StudentOptionalProgram gradStudentOptionalProgram = modelMapper.map(gradStudentOptionalProgramEntity, StudentOptionalProgram.class);
        	gradStudentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(gradStudentOptionalProgram.getOptionalProgramCompletionDate() != null ? gradStudentOptionalProgram.getOptionalProgramCompletionDate():null));
        	gradStudentOptionalProgramList.add(gradStudentOptionalProgram);
        }
        return gradStudentOptionalProgramList;
    }

    public StudentOptionalProgramEntity transformToEntity(StudentOptionalProgram gradStudentOptionalProgram) {
        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = modelMapper.map(gradStudentOptionalProgram, StudentOptionalProgramEntity.class);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(gradStudentOptionalProgram.getOptionalProgramCompletionDate() != null ?Date.valueOf(gradStudentOptionalProgram.getOptionalProgramCompletionDate()) : null);
        return gradStudentOptionalProgramEntity;
    }
}
