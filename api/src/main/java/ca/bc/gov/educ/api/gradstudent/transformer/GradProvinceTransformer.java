package ca.bc.gov.educ.api.gradstudent.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.gradstudent.model.GradProvinceEntity;
import ca.bc.gov.educ.api.gradstudent.struct.GradProvince;


@Component
public class GradProvinceTransformer {

    @Autowired
    ModelMapper modelMapper;

    public GradProvince transformToDTO (GradProvinceEntity gradProgramEntity) {
    	GradProvince gradProvince = modelMapper.map(gradProgramEntity, GradProvince.class);
        return gradProvince;
    }

    public GradProvince transformToDTO ( Optional<GradProvinceEntity> gradProgramEntity ) {
    	GradProvinceEntity cae = new GradProvinceEntity();
        if (gradProgramEntity.isPresent())
            cae = gradProgramEntity.get();

        GradProvince gradProvince = modelMapper.map(cae, GradProvince.class);
        return gradProvince;
    }

	public List<GradProvince> transformToDTO (Iterable<GradProvinceEntity> gradProvinceEntities ) {
		List<GradProvince> gradProvinceList = new ArrayList<GradProvince>();
        for (GradProvinceEntity gradProvinceEntity : gradProvinceEntities) {
        	GradProvince gradProvince = new GradProvince();
        	gradProvince = modelMapper.map(gradProvinceEntity, GradProvince.class);            
        	gradProvinceList.add(gradProvince);
        }
        return gradProvinceList;
    }

    public GradProvinceEntity transformToEntity(GradProvince gradProvince) {
        GradProvinceEntity gradProvinceEntity = modelMapper.map(gradProvince, GradProvinceEntity.class);
        return gradProvinceEntity;
    }
}
