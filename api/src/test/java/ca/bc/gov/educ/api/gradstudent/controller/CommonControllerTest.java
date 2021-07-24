package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.dto.GradCareerProgram;
import ca.bc.gov.educ.api.gradstudent.dto.GradStudentCareerProgram;
import ca.bc.gov.educ.api.gradstudent.dto.StudentNote;
import ca.bc.gov.educ.api.gradstudent.service.CommonService;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.ResponseHelper;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class CommonControllerTest {

    @Mock
    private CommonService commonService;

    @Mock
    ResponseHelper responseHelper;

    @Mock
    GradValidation validation;

    @InjectMocks
    private CommonController commonController;

    @Test
    public void testGetStudentCareerProgram() {
        final String programCode = "2018-EN";

       Mockito.when(commonService.getStudentCareerProgram(programCode)).thenReturn(true);
       commonController.getStudentCareerProgram(programCode);
       Mockito.verify(commonService).getStudentCareerProgram(programCode);
    }

    @Test
    public void testGetAllStudentCareerProgramsList() {
        // UUID
        final UUID studentID = UUID.randomUUID();
        final String pen = "123456789";
        // Career Program
        final GradCareerProgram gradCareerProgram = new GradCareerProgram();
        gradCareerProgram.setCode("TEST");
        gradCareerProgram.setDescription("Test Code Name");

        // Student Career Program Data
        final List<GradStudentCareerProgram> gradStudentCareerProgramList = new ArrayList<>();
        final GradStudentCareerProgram studentCareerProgram1 = new GradStudentCareerProgram();
        studentCareerProgram1.setId(UUID.randomUUID());
        studentCareerProgram1.setPen(pen);
        studentCareerProgram1.setStudentID(studentID);
        studentCareerProgram1.setCareerProgramCode(gradCareerProgram.getCode());
        gradStudentCareerProgramList.add(studentCareerProgram1);

        final GradStudentCareerProgram studentCareerProgram2 = new GradStudentCareerProgram();
        studentCareerProgram2.setId(UUID.randomUUID());
        studentCareerProgram2.setPen(pen);
        studentCareerProgram2.setStudentID(studentID);
        studentCareerProgram2.setCareerProgramCode(gradCareerProgram.getCode());
        gradStudentCareerProgramList.add(studentCareerProgram2);

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(commonService.getAllGradStudentCareerProgramList(pen, null)).thenReturn(gradStudentCareerProgramList);
        commonController.getAllStudentCareerProgramsList(pen);
        Mockito.verify(commonService).getAllGradStudentCareerProgramList(pen, null);
    }

    @Test
    public void testGetAllStudentNotes() {
        // UUID
        final UUID studentID = UUID.randomUUID();

        final List<StudentNote> allNotesList = new ArrayList<>();

        final StudentNote note1 = new StudentNote();
        note1.setId(UUID.randomUUID());
        note1.setStudentID(studentID.toString());
        note1.setNote("Test1 Comments");
        note1.setUpdateDate(new Date(System.currentTimeMillis()));
        allNotesList.add(note1);

        final StudentNote note2 = new StudentNote();
        note2.setId(UUID.randomUUID());
        note2.setStudentID(studentID.toString());
        note2.setNote("Test2 Comments");
        note2.setUpdateDate(new Date(System.currentTimeMillis() + 100000L));
        allNotesList.add(note2);

        Mockito.when(commonService.getAllStudentNotes(studentID)).thenReturn(allNotesList);
        commonController.getAllStudentNotes(studentID.toString());
        Mockito.verify(commonService).getAllStudentNotes(studentID);
    }

    @Test
    public void testSaveStudentNotes() {
        // ID
        final UUID noteID = UUID.randomUUID();
        final UUID studentID = UUID.randomUUID();
        final String pen = "123456789";

        final StudentNote studentNote = new StudentNote();
        studentNote.setId(noteID);
        studentNote.setStudentID(studentID.toString());
        studentNote.setNote("Test Note Body");

        Mockito.when(commonService.saveStudentNote(studentNote)).thenReturn(studentNote);
        commonController.saveStudentNotes(studentNote);
        Mockito.verify(commonService).saveStudentNote(studentNote);
    }

    @Test
    public void testDeleteNotes() {
        // ID
        final UUID noteID = UUID.randomUUID();

        Mockito.when(commonService.deleteNote(noteID)).thenReturn(1);
        commonController.deleteNotes(noteID.toString());
        Mockito.verify(commonService).deleteNote(noteID);
    }


}
