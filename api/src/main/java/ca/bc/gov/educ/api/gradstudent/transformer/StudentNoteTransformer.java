package ca.bc.gov.educ.api.gradstudent.transformer;

import ca.bc.gov.educ.api.gradstudent.dto.StudentNote;
import ca.bc.gov.educ.api.gradstudent.entity.StudentNoteEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class StudentNoteTransformer {

    @Autowired
    ModelMapper modelMapper;

    public StudentNote transformToDTO (StudentNoteEntity studentNoteEntity) {
    	StudentNote studentNote = modelMapper.map(studentNoteEntity, StudentNote.class);
        return studentNote;
    }

    public StudentNote transformToDTO ( Optional<StudentNoteEntity> studentNoteEntity ) {
    	StudentNoteEntity cae = new StudentNoteEntity();
        if (studentNoteEntity.isPresent())
            cae = studentNoteEntity.get();

        StudentNote studentNote = modelMapper.map(cae, StudentNote.class);
        return studentNote;
    }

	public List<StudentNote> transformToDTO (List<StudentNoteEntity> studentNoteEntities ) {
		List<StudentNote> studentNoteList = new ArrayList<StudentNote>();
        for (StudentNoteEntity studentNoteEntity : studentNoteEntities) {
        	StudentNote studentNote = new StudentNote();
        	studentNote = modelMapper.map(studentNoteEntity, StudentNote.class);            
        	studentNoteList.add(studentNote);
        }
        return studentNoteList;
    }

    public StudentNoteEntity transformToEntity(StudentNote studentNote) {
        StudentNoteEntity studentNoteEntity = modelMapper.map(studentNote, StudentNoteEntity.class);
        return studentNoteEntity;
    }
}
