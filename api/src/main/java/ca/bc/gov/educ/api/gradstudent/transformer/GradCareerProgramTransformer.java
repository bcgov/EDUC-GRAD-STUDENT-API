package ca.bc.gov.educ.api.gradstudent.transformer;

import ca.bc.gov.educ.api.gradstudent.dto.GradCareerProgram;
import ca.bc.gov.educ.api.gradstudent.entity.GradCareerProgramEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class GradCareerProgramTransformer {

    @Autowired
    ModelMapper modelMapper;

    public GradCareerProgram transformToDTO (GradCareerProgramEntity gradProgramEntity) {
        GradCareerProgram gradCertificateTypes = modelMapper.map(gradProgramEntity, GradCareerProgram.class);
        return gradCertificateTypes;
    }

	public List<GradCareerProgram> transformToDTO (Iterable<GradCareerProgramEntity> gradCertificateTypesEntities ) {
		List<GradCareerProgram> gradCertificateTypesList = new ArrayList<GradCareerProgram>();
        for (GradCareerProgramEntity gradCertificateTypesEntity : gradCertificateTypesEntities) {
            GradCareerProgram gradCertificateTypes = new GradCareerProgram();
        	gradCertificateTypes = modelMapper.map(gradCertificateTypesEntity, GradCareerProgram.class);
        	gradCertificateTypesList.add(gradCertificateTypes);
        }
        return gradCertificateTypesList;
    }

    public GradCareerProgramEntity transformToEntity(GradCareerProgram gradCertificateTypes) {
        GradCareerProgramEntity gradCertificateTypesEntity = modelMapper.map(gradCertificateTypes, GradCareerProgramEntity.class);
        return gradCertificateTypesEntity;
    }
}
