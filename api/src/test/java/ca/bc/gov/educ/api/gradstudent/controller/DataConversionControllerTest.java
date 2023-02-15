package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCareerProgram;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentOptionalProgramReq;
import ca.bc.gov.educ.api.gradstudent.service.DataConversionService;
import ca.bc.gov.educ.api.gradstudent.service.HistoryService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class DataConversionControllerTest {

    @Mock
    private DataConversionService dataConversionService;

    @Mock
    private HistoryService historyService;

    @Mock
    ResponseHelper responseHelper;

    @Mock
    GradValidation validation;

    @Mock
    Publisher publisher;

    @InjectMocks
    private DataConversionController dataConversionController;

    @Test
    public void testSaveStudentGradStatus() {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen("123456789");
        graduationStatus.setStudentStatus("A");
        graduationStatus.setRecalculateGradStatus("Y");
        graduationStatus.setProgram("2018-en");
        graduationStatus.setSchoolOfRecord(mincode);
        graduationStatus.setSchoolAtGrad(mincode);
        graduationStatus.setGpa("4");
        graduationStatus.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM"));

        Mockito.when(dataConversionService.saveGraduationStudentRecord(studentID, graduationStatus,false)).thenReturn(graduationStatus);
        Mockito.when(responseHelper.GET(graduationStatus)).thenReturn(ResponseEntity.ok().body(graduationStatus));
        var result = dataConversionController
                .saveStudentGradStatus(studentID.toString(), false, graduationStatus);
        Mockito.verify(dataConversionService).saveGraduationStudentRecord(studentID, graduationStatus,false);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testSaveStudentOptionalProgram() {
        // ID
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();
        String pen = "123456789";

        StudentOptionalProgramReq gradStudentOptionalProgramReq = new StudentOptionalProgramReq();
        gradStudentOptionalProgramReq.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramReq.setStudentID(studentID);
        gradStudentOptionalProgramReq.setPen(pen);
        gradStudentOptionalProgramReq.setMainProgramCode("2018-en");
        gradStudentOptionalProgramReq.setOptionalProgramCode("FI");
        gradStudentOptionalProgramReq.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd" ));

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        studentOptionalProgram.setId(gradStudentOptionalProgramID);
        studentOptionalProgram.setStudentID(studentID);
        studentOptionalProgram.setOptionalProgramID(optionalProgramID);
        studentOptionalProgram.setPen(pen);
        studentOptionalProgram.setOptionalProgramCompletionDate(gradStudentOptionalProgramReq.getOptionalProgramCompletionDate());

        Mockito.when(dataConversionService.saveStudentOptionalProgram(gradStudentOptionalProgramReq, "accessToken")).thenReturn(studentOptionalProgram);
        Mockito.when(responseHelper.GET(studentOptionalProgram)).thenReturn(ResponseEntity.ok().body(studentOptionalProgram));
        var result = dataConversionController.saveStudentOptionalProgram(gradStudentOptionalProgramReq, "accessToken");
        Mockito.verify(dataConversionService).saveStudentOptionalProgram(gradStudentOptionalProgramReq, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testSaveStudentCareerProgram() {
        // ID
        UUID gradStudentCareerProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        String careerProgramCode = "Test";
        String pen = "123456789";

        StudentCareerProgram studentCareerProgram = new StudentCareerProgram();
        studentCareerProgram.setId(gradStudentCareerProgramID);
        studentCareerProgram.setStudentID(studentID);
        studentCareerProgram.setCareerProgramCode(careerProgramCode);
        studentCareerProgram.setCareerProgramName("Test Career Course");

        Mockito.when(dataConversionService.saveStudentCareerProgram(studentCareerProgram)).thenReturn(studentCareerProgram);
        Mockito.when(responseHelper.GET(studentCareerProgram)).thenReturn(ResponseEntity.ok().body(studentCareerProgram));
        var result = dataConversionController.saveStudentCareerProgram(studentCareerProgram);
        Mockito.verify(dataConversionService).saveStudentCareerProgram(studentCareerProgram);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testDeleteStudentOptionalProgram() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        Mockito.doNothing().when(dataConversionService).deleteStudentOptionalProgram(optionalProgramID, studentID);
        Mockito.when(responseHelper.DELETE(1)).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        var result = dataConversionController.deleteStudentOptionalProgram(optionalProgramID.toString(), studentID.toString());
        Mockito.verify(dataConversionService).deleteStudentOptionalProgram(optionalProgramID, studentID);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testDeleteStudentCareerProgram() {
        // ID
        UUID studentID = UUID.randomUUID();
        String careerProgramCode = "Test";

        Mockito.doNothing().when(dataConversionService).deleteStudentCareerProgram(careerProgramCode, studentID);
        Mockito.when(responseHelper.DELETE(1)).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        var result = dataConversionController.deleteStudentCareerProgram(careerProgramCode.toString(), studentID.toString());
        Mockito.verify(dataConversionService).deleteStudentCareerProgram(careerProgramCode, studentID);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
