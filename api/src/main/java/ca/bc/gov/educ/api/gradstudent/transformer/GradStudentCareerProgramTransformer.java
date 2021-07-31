package ca.bc.gov.educ.api.gradstudent.transformer;

import ca.bc.gov.educ.api.gradstudent.dto.GradStudentCareerProgram;
import ca.bc.gov.educ.api.gradstudent.entity.StudentCareerProgramEntity;
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

    public GradStudentCareerProgram transformToDTO (StudentCareerProgramEntity gradProgramEntity) {
    	GradStudentCareerProgram gradCertificateTypes = modelMapper.map(gradProgramEntity, GradStudentCareerProgram.class);
        return gradCertificateTypes;
    }

    public GradStudentCareerProgram transformToDTO ( Optional<StudentCareerProgramEntity> gradProgramEntity ) {
    	StudentCareerProgramEntity cae = new StudentCareerProgramEntity();
        if (gradProgramEntity.isPresent())
            cae = gradProgramEntity.get();

        GradStudentCareerProgram gradCertificateTypes = modelMapper.map(cae, GradStudentCareerProgram.class);
        return gradCertificateTypes;
    }

	public List<GradStudentCareerProgram> transformToDTO (Iterable<StudentCareerProgramEntity> gradCertificateTypesEntities ) {
		List<GradStudentCareerProgram> gradCertificateTypesList = new ArrayList<GradStudentCareerProgram>();
        for (StudentCareerProgramEntity gradCertificateTypesEntity : gradCertificateTypesEntities) {
        	GradStudentCareerProgram gradCertificateTypes = new GradStudentCareerProgram();
        	gradCertificateTypes = modelMapper.map(gradCertificateTypesEntity, GradStudentCareerProgram.class);            
        	gradCertificateTypesList.add(gradCertificateTypes);
        }
        return gradCertificateTypesList;
    }

    public StudentCareerProgramEntity transformToEntity(GradStudentCareerProgram gradCertificateTypes) {
        StudentCareerProgramEntity gradCertificateTypesEntity = modelMapper.map(gradCertificateTypes, StudentCareerProgramEntity.class);
        return gradCertificateTypesEntity;
    }
}
