package ca.bc.gov.educ.api.gradstudent.transformer;

import ca.bc.gov.educ.api.gradstudent.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.gradstudent.entity.GradStudentSpecialProgramEntity;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStatusApiUtils;
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

    public GradStudentSpecialProgram transformToDTO (GradStudentSpecialProgramEntity gradStudentSpecialProgramEntity) {
    	GradStudentSpecialProgram gradStudentSpecialProgram = modelMapper.map(gradStudentSpecialProgramEntity, GradStudentSpecialProgram.class);
    	gradStudentSpecialProgram.setSpecialProgramCompletionDate(EducGradStatusApiUtils.parseDateFromString(gradStudentSpecialProgram.getSpecialProgramCompletionDate() != null ? gradStudentSpecialProgram.getSpecialProgramCompletionDate():null));
    	return gradStudentSpecialProgram;
    }

    public GradStudentSpecialProgram transformToDTO ( Optional<GradStudentSpecialProgramEntity> gradStudentSpecialProgramEntity ) {
    	GradStudentSpecialProgramEntity cae = new GradStudentSpecialProgramEntity();
        if (gradStudentSpecialProgramEntity.isPresent())
            cae = gradStudentSpecialProgramEntity.get();
        	
        GradStudentSpecialProgram gradStudentSpecialProgram = modelMapper.map(cae, GradStudentSpecialProgram.class);
        gradStudentSpecialProgram.setSpecialProgramCompletionDate(EducGradStatusApiUtils.parseDateFromString(gradStudentSpecialProgram.getSpecialProgramCompletionDate() != null ? gradStudentSpecialProgram.getSpecialProgramCompletionDate():null));
        return gradStudentSpecialProgram;
    }

	public List<GradStudentSpecialProgram> transformToDTO (Iterable<GradStudentSpecialProgramEntity> gradStudentSpecialProgramEntities ) {
		List<GradStudentSpecialProgram> gradStudentSpecialProgramList = new ArrayList<>();
        for (GradStudentSpecialProgramEntity gradStudentSpecialProgramEntity : gradStudentSpecialProgramEntities) {
        	GradStudentSpecialProgram gradStudentSpecialProgram = modelMapper.map(gradStudentSpecialProgramEntity, GradStudentSpecialProgram.class);            
        	gradStudentSpecialProgram.setSpecialProgramCompletionDate(EducGradStatusApiUtils.parseDateFromString(gradStudentSpecialProgram.getSpecialProgramCompletionDate() != null ? gradStudentSpecialProgram.getSpecialProgramCompletionDate():null));
        	gradStudentSpecialProgramList.add(gradStudentSpecialProgram);
        }
        return gradStudentSpecialProgramList;
    }

    public GradStudentSpecialProgramEntity transformToEntity(GradStudentSpecialProgram gradStudentSpecialProgram) {
        GradStudentSpecialProgramEntity gradStudentSpecialProgramEntity = modelMapper.map(gradStudentSpecialProgram, GradStudentSpecialProgramEntity.class);
        gradStudentSpecialProgramEntity.setSpecialProgramCompletionDate(gradStudentSpecialProgram.getSpecialProgramCompletionDate() != null ?Date.valueOf(gradStudentSpecialProgram.getSpecialProgramCompletionDate()) : null);
        return gradStudentSpecialProgramEntity;
    }
}
