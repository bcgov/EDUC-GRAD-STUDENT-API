package ca.bc.gov.educ.api.gradstudent.transformer;

import ca.bc.gov.educ.api.gradstudent.dto.GradSpecialProgram;
import ca.bc.gov.educ.api.gradstudent.entity.GradSpecialProgramEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class GradSpecialProgramTransformer {

    @Autowired
    ModelMapper modelMapper;

    public GradSpecialProgram transformToDTO (GradSpecialProgramEntity gradProgramEntity) {
        GradSpecialProgram gradCertificateTypes = modelMapper.map(gradProgramEntity, GradSpecialProgram.class);
        return gradCertificateTypes;
    }

	public List<GradSpecialProgram> transformToDTO (Iterable<GradSpecialProgramEntity> gradCertificateTypesEntities ) {
		List<GradSpecialProgram> gradCertificateTypesList = new ArrayList<GradSpecialProgram>();
        for (GradSpecialProgramEntity gradCertificateTypesEntity : gradCertificateTypesEntities) {
            GradSpecialProgram gradCertificateTypes = new GradSpecialProgram();
        	gradCertificateTypes = modelMapper.map(gradCertificateTypesEntity, GradSpecialProgram.class);
        	gradCertificateTypesList.add(gradCertificateTypes);
        }
        return gradCertificateTypesList;
    }

    public GradSpecialProgramEntity transformToEntity(GradSpecialProgram gradCertificateTypes) {
        GradSpecialProgramEntity gradCertificateTypesEntity = modelMapper.map(gradCertificateTypes, GradSpecialProgramEntity.class);
        return gradCertificateTypesEntity;
    }
}
