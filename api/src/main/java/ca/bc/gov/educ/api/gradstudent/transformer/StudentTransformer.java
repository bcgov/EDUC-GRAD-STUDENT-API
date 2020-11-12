package ca.bc.gov.educ.api.gradstudent.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;
import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;

@Component
public class StudentTransformer {

	@Autowired
	ModelMapper modelMapper; 

    public GradStudent transformToDTO ( Optional<GradStudentEntity> gradStudentEntity ) {
    	GradStudentEntity gse = new GradStudentEntity();
        if (gradStudentEntity.isPresent())
            gse = gradStudentEntity.get();
        return modelMapper.map(gse, GradStudent.class);
    }
    
    public List<GradStudent> transformToDTO (List<GradStudentEntity> gradStudentEntities ) {

        List<GradStudent> gradStudentList = new ArrayList<GradStudent>();

        for (GradStudentEntity gradStudentEntity : gradStudentEntities) {
        	GradStudent gradStudent = new GradStudent();
        	gradStudent = modelMapper.map(gradStudentEntity, GradStudent.class);            
        	gradStudentList.add(gradStudent);
        }

        return gradStudentList;
    }

	public GradStudent transformToDTO (GradStudentEntity gradStudentEntity ) {
        GradStudent gradStudent = new GradStudent();
        gradStudent = modelMapper.map(gradStudentEntity, GradStudent.class);
        return gradStudent;
    }

    public GradStudentEntity transformToEntity(GradStudent gradStudent) {
    	GradStudentEntity gradStudentEntity = modelMapper.map(gradStudent, GradStudentEntity.class);
        return gradStudentEntity;
    }
}
