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
    	StudentCareerProgram gradCertificateTypes = modelMapper.map(gradProgramEntity, StudentCareerProgram.class);
        return gradCertificateTypes;
    }

    public StudentCareerProgram transformToDTO ( Optional<StudentCareerProgramEntity> gradProgramEntity ) {
    	StudentCareerProgramEntity cae = new StudentCareerProgramEntity();
        if (gradProgramEntity.isPresent())
            cae = gradProgramEntity.get();

        StudentCareerProgram gradCertificateTypes = modelMapper.map(cae, StudentCareerProgram.class);
        return gradCertificateTypes;
    }

	public List<StudentCareerProgram> transformToDTO (Iterable<StudentCareerProgramEntity> gradCertificateTypesEntities ) {
		List<StudentCareerProgram> gradCertificateTypesList = new ArrayList<StudentCareerProgram>();
        for (StudentCareerProgramEntity gradCertificateTypesEntity : gradCertificateTypesEntities) {
        	StudentCareerProgram gradCertificateTypes = new StudentCareerProgram();
        	gradCertificateTypes = modelMapper.map(gradCertificateTypesEntity, StudentCareerProgram.class);            
        	gradCertificateTypesList.add(gradCertificateTypes);
        }
        return gradCertificateTypesList;
    }

    public StudentCareerProgramEntity transformToEntity(StudentCareerProgram gradCertificateTypes) {
        StudentCareerProgramEntity gradCertificateTypesEntity = modelMapper.map(gradCertificateTypes, StudentCareerProgramEntity.class);
        return gradCertificateTypesEntity;
    }
}
