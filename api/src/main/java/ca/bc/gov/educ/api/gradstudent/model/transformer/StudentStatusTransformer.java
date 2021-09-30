package ca.bc.gov.educ.api.gradstudent.model.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentStatus;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentStatusEntity;


@Component
public class StudentStatusTransformer {

    @Autowired
    ModelMapper modelMapper;

    public StudentStatus transformToDTO (StudentStatusEntity studentStatusEntity) {
    	return modelMapper.map(studentStatusEntity, StudentStatus.class);
    }

    public StudentStatus transformToDTO ( Optional<StudentStatusEntity> studentStatusEntity ) {
    	StudentStatusEntity cae = new StudentStatusEntity();
        if (studentStatusEntity.isPresent())
            cae = studentStatusEntity.get();

        return modelMapper.map(cae, StudentStatus.class);
    }

	public List<StudentStatus> transformToDTO (Iterable<StudentStatusEntity> studentStatusEntities ) {
		List<StudentStatus> studentStatusList = new ArrayList<>();
        for (StudentStatusEntity StudentStatusEntity : studentStatusEntities) {
        	StudentStatus studentStatus = modelMapper.map(StudentStatusEntity, StudentStatus.class);            
        	studentStatusList.add(studentStatus);
        }
        return studentStatusList;
    }

    public StudentStatusEntity transformToEntity(StudentStatus studentStatus) {
        return modelMapper.map(studentStatus, StudentStatusEntity.class);
    }
}
