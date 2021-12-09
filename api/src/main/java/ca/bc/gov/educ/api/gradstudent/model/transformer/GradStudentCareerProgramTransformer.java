package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCareerProgram;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCareerProgramEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class GradStudentCareerProgramTransformer {

    @Autowired
    ModelMapper modelMapper;

    public StudentCareerProgram transformToDTO (StudentCareerProgramEntity gradProgramEntity) {
    	return modelMapper.map(gradProgramEntity, StudentCareerProgram.class);
    }

    public StudentCareerProgram transformToDTO ( Optional<StudentCareerProgramEntity> gradProgramEntity ) {
    	StudentCareerProgramEntity cae = new StudentCareerProgramEntity();
        if (gradProgramEntity.isPresent())
            cae = gradProgramEntity.get();

        return modelMapper.map(cae, StudentCareerProgram.class);
    }

	public List<StudentCareerProgram> transformToDTO (Iterable<StudentCareerProgramEntity> gradCertificateTypesEntities ) {
		List<StudentCareerProgram> gradCertificateTypesList = new ArrayList<>();
        for (StudentCareerProgramEntity gradCertificateTypesEntity : gradCertificateTypesEntities) {
            StudentCareerProgram gradCertificateTypes = modelMapper.map(gradCertificateTypesEntity, StudentCareerProgram.class);
        	gradCertificateTypesList.add(gradCertificateTypes);
        }
        return gradCertificateTypesList;
    }

    public StudentCareerProgramEntity transformToEntity(StudentCareerProgram gradCertificateTypes) {
        return modelMapper.map(gradCertificateTypes, StudentCareerProgramEntity.class);
    }
}
