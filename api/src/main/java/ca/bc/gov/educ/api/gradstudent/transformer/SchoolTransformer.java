package ca.bc.gov.educ.api.gradstudent.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.gradstudent.model.SchoolEntity;
import ca.bc.gov.educ.api.gradstudent.struct.School;

@Component
public class SchoolTransformer {

    @Autowired
    ModelMapper modelMapper;

    public School transformToDTO (SchoolEntity schoolEntity) {
        return modelMapper.map(schoolEntity, School.class);
    }

    public School transformToDTO ( Optional<SchoolEntity> schoolEntity ) {
        SchoolEntity se = new SchoolEntity();

        if (schoolEntity.isPresent())
            se = schoolEntity.get();

        return modelMapper.map(se, School.class);
    }

	public List<School> transformToDTO (Iterable<SchoolEntity> schoolEntities ) {

        List<School> schoolList = new ArrayList<School>();

        for (SchoolEntity schoolEntity : schoolEntities) {
            School school = new School();
            school = modelMapper.map(schoolEntity, School.class);            
            schoolList.add(school);
        }

        return schoolList;
    }

    public SchoolEntity transformToEntity(School school) {
        return modelMapper.map(school, SchoolEntity.class);
    }
}
