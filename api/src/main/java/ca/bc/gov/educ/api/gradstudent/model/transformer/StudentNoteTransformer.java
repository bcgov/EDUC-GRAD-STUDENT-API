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
    	StudentNote studentNote = modelMapper.map(studentNoteEntity, StudentNote.class);
        return studentNote;
    }

    public StudentNote transformToDTO ( Optional<StudentRecordNoteEntity> studentNoteEntity ) {
    	StudentRecordNoteEntity cae = new StudentRecordNoteEntity();
        if (studentNoteEntity.isPresent())
            cae = studentNoteEntity.get();

        StudentNote studentNote = modelMapper.map(cae, StudentNote.class);
        return studentNote;
    }

	public List<StudentNote> transformToDTO (List<StudentRecordNoteEntity> studentNoteEntities ) {
		List<StudentNote> studentNoteList = new ArrayList<StudentNote>();
        for (StudentRecordNoteEntity studentNoteEntity : studentNoteEntities) {
        	StudentNote studentNote = new StudentNote();
        	studentNote = modelMapper.map(studentNoteEntity, StudentNote.class);            
        	studentNoteList.add(studentNote);
        }
        return studentNoteList;
    }

    public StudentRecordNoteEntity transformToEntity(StudentNote studentNote) {
        StudentRecordNoteEntity studentNoteEntity = modelMapper.map(studentNote, StudentRecordNoteEntity.class);
        return studentNoteEntity;
    }
}
