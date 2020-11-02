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
        School school = modelMapper.map(schoolEntity, School.class);
        return school;
    }

    public School transformToDTO ( Optional<SchoolEntity> schoolEntity ) {
        SchoolEntity cae = new SchoolEntity();

        if (schoolEntity.isPresent())
            cae = schoolEntity.get();

        School school = modelMapper.map(cae, School.class);
        return school;
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
        SchoolEntity schoolEntity = modelMapper.map(school, SchoolEntity.class);
        return schoolEntity;
    }
}
