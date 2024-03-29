package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentNote;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentRecordNoteEntity;
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

    public StudentNote transformToDTO (StudentRecordNoteEntity studentNoteEntity) {
    	return modelMapper.map(studentNoteEntity, StudentNote.class);
    }

    public StudentNote transformToDTO ( Optional<StudentRecordNoteEntity> studentNoteEntity ) {
    	StudentRecordNoteEntity cae = new StudentRecordNoteEntity();
        if (studentNoteEntity.isPresent())
            cae = studentNoteEntity.get();

        return modelMapper.map(cae, StudentNote.class);
    }

	public List<StudentNote> transformToDTO (List<StudentRecordNoteEntity> studentNoteEntities ) {
		List<StudentNote> studentNoteList = new ArrayList<>();
        for (StudentRecordNoteEntity studentNoteEntity : studentNoteEntities) {
            StudentNote studentNote = modelMapper.map(studentNoteEntity, StudentNote.class);
        	studentNoteList.add(studentNote);
        }
        return studentNoteList;
    }

    public StudentRecordNoteEntity transformToEntity(StudentNote studentNote) {
        return modelMapper.map(studentNote, StudentRecordNoteEntity.class);
    }
}
