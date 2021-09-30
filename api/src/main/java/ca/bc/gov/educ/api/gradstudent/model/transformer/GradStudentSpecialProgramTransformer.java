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
public class GradStudentSpecialProgramTransformer {

    @Autowired
    ModelMapper modelMapper;

    public StudentOptionalProgram transformToDTO (StudentOptionalProgramEntity gradStudentSpecialProgramEntity) {
    	StudentOptionalProgram gradStudentSpecialProgram = modelMapper.map(gradStudentSpecialProgramEntity, StudentOptionalProgram.class);
    	gradStudentSpecialProgram.setSpecialProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(gradStudentSpecialProgram.getSpecialProgramCompletionDate() != null ? gradStudentSpecialProgram.getSpecialProgramCompletionDate():null));
    	return gradStudentSpecialProgram;
    }

    public StudentOptionalProgram transformToDTO ( Optional<StudentOptionalProgramEntity> gradStudentSpecialProgramEntity ) {
    	StudentOptionalProgramEntity cae = new StudentOptionalProgramEntity();
        if (gradStudentSpecialProgramEntity.isPresent())
            cae = gradStudentSpecialProgramEntity.get();
        	
        StudentOptionalProgram gradStudentSpecialProgram = modelMapper.map(cae, StudentOptionalProgram.class);
        gradStudentSpecialProgram.setSpecialProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(gradStudentSpecialProgram.getSpecialProgramCompletionDate() != null ? gradStudentSpecialProgram.getSpecialProgramCompletionDate():null));
        return gradStudentSpecialProgram;
    }

	public List<StudentOptionalProgram> transformToDTO (Iterable<StudentOptionalProgramEntity> gradStudentSpecialProgramEntities ) {
		List<StudentOptionalProgram> gradStudentSpecialProgramList = new ArrayList<>();
        for (StudentOptionalProgramEntity gradStudentSpecialProgramEntity : gradStudentSpecialProgramEntities) {
        	StudentOptionalProgram gradStudentSpecialProgram = modelMapper.map(gradStudentSpecialProgramEntity, StudentOptionalProgram.class);            
        	gradStudentSpecialProgram.setSpecialProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(gradStudentSpecialProgram.getSpecialProgramCompletionDate() != null ? gradStudentSpecialProgram.getSpecialProgramCompletionDate():null));
        	gradStudentSpecialProgramList.add(gradStudentSpecialProgram);
        }
        return gradStudentSpecialProgramList;
    }

    public StudentOptionalProgramEntity transformToEntity(StudentOptionalProgram gradStudentSpecialProgram) {
        StudentOptionalProgramEntity gradStudentSpecialProgramEntity = modelMapper.map(gradStudentSpecialProgram, StudentOptionalProgramEntity.class);
        gradStudentSpecialProgramEntity.setSpecialProgramCompletionDate(gradStudentSpecialProgram.getSpecialProgramCompletionDate() != null ?Date.valueOf(gradStudentSpecialProgram.getSpecialProgramCompletionDate()) : null);
        return gradStudentSpecialProgramEntity;
    }
}
