package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.service.CommonService;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentReportService;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class CommonControllerTest {

    @Mock
    private CommonService commonService;
    @Mock
    GradStudentReportService gradStudentReportService;

    @Mock ResponseHelper responseHelper;

    @Mock GradValidation validation;

    @InjectMocks
    private CommonController codeController;

    @Test
    public void testGetReportGradStudentData() {
        // ID
        UUID studentID = UUID.randomUUID();

        ReportGradStudentData reportGradStudentData = new ReportGradStudentData();
        reportGradStudentData.setGraduationStudentRecordId(studentID);
        reportGradStudentData.setFirstName("Jonh");
        reportGradStudentData.setMincode("005");

        Mockito.when(gradStudentReportService.getGradStudentDataByMincode(reportGradStudentData.getMincode())).thenReturn(List.of(reportGradStudentData));
        codeController.getStudentReportData(reportGradStudentData.getMincode());
        Mockito.verify(gradStudentReportService).getGradStudentDataByMincode(reportGradStudentData.getMincode());
    }

    @Test
    public void testGetStudentCareerProgram() {
        final String programCode = "2018-EN";
       Mockito.when(commonService.getStudentCareerProgram(programCode)).thenReturn(true);
       codeController.getStudentCareerProgram(programCode);
       Mockito.verify(commonService).getStudentCareerProgram(programCode);
    }

    @Test
    public void testGetAllStudentCareerProgramsList() {
        // UUID
        final UUID studentID = UUID.randomUUID();
        final String pen = "123456789";
        // Career Program
        final CareerProgram gradCareerProgram = new CareerProgram();
        gradCareerProgram.setCode("TEST");
        gradCareerProgram.setDescription("Test Code Name");

        // Student Career Program Data
        final List<StudentCareerProgram> gradStudentCareerProgramList = new ArrayList<>();
        final StudentCareerProgram studentCareerProgram1 = new StudentCareerProgram();
        studentCareerProgram1.setId(UUID.randomUUID());
        studentCareerProgram1.setStudentID(studentID);
        studentCareerProgram1.setCareerProgramCode(gradCareerProgram.getCode());
        gradStudentCareerProgramList.add(studentCareerProgram1);

        final StudentCareerProgram studentCareerProgram2 = new StudentCareerProgram();
        studentCareerProgram2.setId(UUID.randomUUID());
        studentCareerProgram2.setStudentID(studentID);
        studentCareerProgram2.setCareerProgramCode(gradCareerProgram.getCode());
        gradStudentCareerProgramList.add(studentCareerProgram2);


        Mockito.when(commonService.getAllGradStudentCareerProgramList(pen, "accessToken")).thenReturn(gradStudentCareerProgramList);
        codeController.getAllStudentCareerProgramsList(pen, "accessToken");
        Mockito.verify(commonService).getAllGradStudentCareerProgramList(pen, "accessToken");
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
        codeController.getAllStudentNotes(studentID.toString());
        Mockito.verify(commonService).getAllStudentNotes(studentID);
    }

    @Test
    public void testSaveStudentNotes() {
        // ID
        final UUID noteID = UUID.randomUUID();
        final UUID studentID = UUID.randomUUID();
        final StudentNote studentNote = new StudentNote();
        studentNote.setId(noteID);
        studentNote.setStudentID(studentID.toString());
        studentNote.setNote("Test Note Body");

        Mockito.when(commonService.saveStudentNote(studentNote)).thenReturn(studentNote);
        codeController.saveStudentNotes(studentNote);
        Mockito.verify(commonService).saveStudentNote(studentNote);
    }

    @Test
    public void testDeleteNotes() {
        // ID
        final UUID noteID = UUID.randomUUID();

        Mockito.when(commonService.deleteNote(noteID)).thenReturn(1);
        codeController.deleteNotes(noteID.toString());
        Mockito.verify(commonService).deleteNote(noteID);
    }
    
    @Test
	public void testGetAllStudentStatusCodeList() {
		List<StudentStatus> studentStatusList = new ArrayList<>();
		StudentStatus obj = new StudentStatus();
		obj.setCode("DC");
		obj.setDescription("Data Correction by School");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(new Date(System.currentTimeMillis()));
		obj.setUpdateDate(new Date(System.currentTimeMillis()));
		studentStatusList.add(obj);
		obj = new StudentStatus();
		obj.setCode("CC");
		obj.setDescription("Courses not complete");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(new Date(System.currentTimeMillis()));
		obj.setUpdateDate(new Date(System.currentTimeMillis()));
		studentStatusList.add(obj);
		Mockito.when(commonService.getAllStudentStatusCodeList()).thenReturn(studentStatusList);
		codeController.getAllStudentStatusCodeList();
		Mockito.verify(commonService).getAllStudentStatusCodeList();
	}
	
	@Test
	public void testGetSpecificStudentStatusCode() {
		String requirementType = "DC";
		StudentStatus obj = new StudentStatus();
		obj.setCode("DC");
		obj.setDescription("Data Correction by School");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(new Date(System.currentTimeMillis()));
		obj.setUpdateDate(new Date(System.currentTimeMillis()));
		Mockito.when(commonService.getSpecificStudentStatusCode(requirementType)).thenReturn(obj);
		codeController.getSpecificStudentStatusCode(requirementType);
		Mockito.verify(commonService).getSpecificStudentStatusCode(requirementType);
	}
	
	@Test
	public void testGetSpecificStudentStatusCode_noContent() {
		String requirementType = "AB";	
		Mockito.when(commonService.getSpecificStudentStatusCode(requirementType)).thenReturn(null);
		codeController.getSpecificStudentStatusCode(requirementType);
		Mockito.verify(commonService).getSpecificStudentStatusCode(requirementType);
	}
	
	@Test
	public void testCreateStudentStatus() {
		StudentStatus obj = new StudentStatus();
		obj.setCode("DC");
		obj.setDescription("Data Correction by School");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(new Date(System.currentTimeMillis()));
		obj.setUpdateDate(new Date(System.currentTimeMillis()));
		Mockito.when(commonService.createStudentStatus(obj)).thenReturn(obj);
		codeController.createStudentStatus(obj);
		Mockito.verify(commonService).createStudentStatus(obj);
	}
	
	@Test
	public void testUpdateStudentStatus() {
		StudentStatus obj = new StudentStatus();
		obj.setCode("DC");
		obj.setDescription("Data Correction by School");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(new Date(System.currentTimeMillis()));
		obj.setUpdateDate(new Date(System.currentTimeMillis()));
		Mockito.when(commonService.updateStudentStatus(obj)).thenReturn(obj);
		codeController.updateStudentStatusCode(obj);
		Mockito.verify(commonService).updateStudentStatus(obj);
	}
	
	@Test
	public void testDeleteStudentStatus() {
		String statusCode = "DC";
		Mockito.when(commonService.deleteStudentStatus(statusCode)).thenReturn(1);
		codeController.deleteStudentStatusCodes(statusCode);
		Mockito.verify(commonService).deleteStudentStatus(statusCode);
	}

    @Test
    public void testgetStudentGradStatusForAlgorithm() {

        String studentID = new UUID(1,1).toString();
        String accessToken = "accessToken";

        GradSearchStudent gs = new GradSearchStudent();
        gs.setStudentID(studentID);
        gs.setStudentStatus("A");

        GraduationStudentRecord gsr = new GraduationStudentRecord();
        gsr.setStudentID(UUID.fromString(studentID));
        gsr.setStudentStatus("CUR");

        GradStudentAlgorithmData data = new GradStudentAlgorithmData();
        data.setGradStudent(gs);
        data.setGraduationStudentRecord(gsr);

        Mockito.when(commonService.getGradStudentAlgorithmData(studentID,"accessToken")).thenReturn(data);
        codeController.getStudentGradStatusForAlgorithm(studentID, "accessToken");
    }
}
