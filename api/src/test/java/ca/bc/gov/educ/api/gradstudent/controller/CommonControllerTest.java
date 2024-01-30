package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.service.CommonService;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentReportService;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
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
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class CommonControllerTest {

    @Mock
    private CommonService commonService;
    @Mock
    GradStudentReportService gradStudentReportService;
    @Mock
    GraduationStatusService graduationStatusService;

    @Mock ResponseHelper responseHelper;

    @Mock GradValidation validation;

    @InjectMocks
    private CommonController commonController;

    @Test
    public void testGetReportGradStudentDataByMincode() {
        // ID
        UUID studentID = UUID.randomUUID();

        ReportGradStudentData reportGradStudentData = new ReportGradStudentData();
        reportGradStudentData.setGraduationStudentRecordId(studentID);
        reportGradStudentData.setFirstName("Jonh");
        reportGradStudentData.setMincode("03939000");

        Mockito.when(gradStudentReportService.getGradStudentDataByMincode(reportGradStudentData.getMincode())).thenReturn(List.of(reportGradStudentData));
        commonController.getStudentReportDataByMincode(reportGradStudentData.getMincode());
        Mockito.verify(gradStudentReportService).getGradStudentDataByMincode(reportGradStudentData.getMincode());

        Mockito.when(gradStudentReportService.getGradSchoolsForNonGradYearEndReport()).thenReturn(List.of(reportGradStudentData.getMincode()));
        commonController.getSchoolReportDataForYearEndNonGrad();
        Mockito.verify(gradStudentReportService).getGradSchoolsForNonGradYearEndReport();
    }

    @Test
    public void testGetReportGradStudentDataByDistcode() {
        // ID
        UUID studentID = UUID.randomUUID();

        ReportGradStudentData reportGradStudentData = new ReportGradStudentData();
        reportGradStudentData.setGraduationStudentRecordId(studentID);
        reportGradStudentData.setFirstName("Jonh");
        reportGradStudentData.setMincode("005");

        Mockito.when(gradStudentReportService.getGradStudentDataByMincode(reportGradStudentData.getMincode())).thenReturn(List.of(reportGradStudentData));
        commonController.getStudentReportDataByMincode(reportGradStudentData.getMincode());
        Mockito.verify(gradStudentReportService).getGradStudentDataByMincode(reportGradStudentData.getMincode());

        Mockito.when(gradStudentReportService.getGradDistrictsForNonGradYearEndReport()).thenReturn(List.of(reportGradStudentData.getMincode()));
        commonController.getDistrictReportDataForYearEndNonGrad();
        Mockito.verify(gradStudentReportService).getGradDistrictsForNonGradYearEndReport();
    }


    @Test
    public void testGetReportGradStudentData() {
        // ID
        UUID studentID = UUID.randomUUID();

        ReportGradStudentData reportGradStudentData = new ReportGradStudentData();
        reportGradStudentData.setGraduationStudentRecordId(studentID);
        reportGradStudentData.setFirstName("Jonh");
        reportGradStudentData.setMincode("005");

        Mockito.when(gradStudentReportService.getGradStudentDataByStudentGuids(List.of(reportGradStudentData.getGraduationStudentRecordId()))).thenReturn(List.of(reportGradStudentData));
        commonController.getStudentReportData(List.of(reportGradStudentData.getGraduationStudentRecordId()));
        Mockito.verify(gradStudentReportService).getGradStudentDataByStudentGuids(List.of(reportGradStudentData.getGraduationStudentRecordId()));

        Mockito.when(gradStudentReportService.getGradStudentDataForNonGradYearEndReport(reportGradStudentData.getMincode())).thenReturn(List.of(reportGradStudentData));
        commonController.getStudentReportDataForYearEndNonGrad(reportGradStudentData.getMincode());
        Mockito.verify(gradStudentReportService).getGradStudentDataForNonGradYearEndReport(reportGradStudentData.getMincode());

        Mockito.when(gradStudentReportService.getGradStudentDataForNonGradYearEndReport()).thenReturn(List.of(reportGradStudentData));
        commonController.getStudentReportDataForYearEndNonGrad();
        Mockito.verify(gradStudentReportService).getGradStudentDataForNonGradYearEndReport();
    }

    @Test
    public void testGetSpecificHistoryActivityCode() {
        HistoryActivity activity = new HistoryActivity();
        activity.setCode("01");
        Mockito.when(commonService.getSpecificHistoryActivityCode(activity.getCode())).thenReturn(activity);
        commonController.getSpecificHistoryActivityCode(activity.getCode());
        Mockito.verify(commonService).getSpecificHistoryActivityCode(activity.getCode());
    }

    @Test
    public void testGetAllHistoryActivityCode() {
        HistoryActivity activity = new HistoryActivity();
        activity.setCode("01");
        Mockito.when(commonService.getAllHistoryActivityCodeList()).thenReturn(List.of(activity));
        commonController.getAllHistoryActivityCodeList();
        Mockito.verify(commonService).getAllHistoryActivityCodeList();
    }

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
        commonController.getAllStudentCareerProgramsList(pen, "accessToken");
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
        note1.setUpdateDate(LocalDateTime.now());
        allNotesList.add(note1);

        final StudentNote note2 = new StudentNote();
        note2.setId(UUID.randomUUID());
        note2.setStudentID(studentID.toString());
        note2.setNote("Test2 Comments");
        note2.setUpdateDate(LocalDateTime.now(Clock.offset(Clock.systemDefaultZone(), Duration.ofHours(3))));
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
    
    @Test
    public void testGetAllStudentStatusCodeList() {
        List<StudentStatus> studentStatusList = new ArrayList<>();
        StudentStatus obj = new StudentStatus();
        obj.setCode("DC");
        obj.setDescription("Data Correction by School");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        studentStatusList.add(obj);
        obj = new StudentStatus();
        obj.setCode("CC");
        obj.setDescription("Courses not complete");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        studentStatusList.add(obj);
        Mockito.when(commonService.getAllStudentStatusCodeList()).thenReturn(studentStatusList);
        commonController.getAllStudentStatusCodeList();
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
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        Mockito.when(commonService.getSpecificStudentStatusCode(requirementType)).thenReturn(obj);
        commonController.getSpecificStudentStatusCode(requirementType);
        Mockito.verify(commonService).getSpecificStudentStatusCode(requirementType);
    }

    @Test
    public void testGetSpecificStudentStatusCode_noContent() {
        String requirementType = "AB";
        Mockito.when(commonService.getSpecificStudentStatusCode(requirementType)).thenReturn(null);
        commonController.getSpecificStudentStatusCode(requirementType);
        Mockito.verify(commonService).getSpecificStudentStatusCode(requirementType);
    }

    @Test
    public void testCreateStudentStatus() {
        StudentStatus obj = new StudentStatus();
        obj.setCode("DC");
        obj.setDescription("Data Correction by School");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        Mockito.when(commonService.createStudentStatus(obj)).thenReturn(obj);
        commonController.createStudentStatus(obj);
        Mockito.verify(commonService).createStudentStatus(obj);
    }

    @Test
    public void testUpdateStudentStatus() {
        StudentStatus obj = new StudentStatus();
        obj.setCode("DC");
        obj.setDescription("Data Correction by School");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        Mockito.when(commonService.updateStudentStatus(obj)).thenReturn(obj);
        commonController.updateStudentStatusCode(obj);
        Mockito.verify(commonService).updateStudentStatus(obj);
    }

    @Test
    public void testDeleteStudentStatus() {
        String statusCode = "DC";
        Mockito.when(commonService.deleteStudentStatus(statusCode)).thenReturn(1);
        commonController.deleteStudentStatusCodes(statusCode);
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
        commonController.getStudentGradStatusForAlgorithm(studentID, "accessToken");
    }

    @Test
    public void testGetDeceasedStudentIDs() {
        UUID studentID1 = UUID.randomUUID();
        UUID studentID2 = UUID.randomUUID();

        Mockito.when(commonService.getDeceasedStudentIDs(Arrays.asList(studentID1, studentID2))).thenReturn(Arrays.asList(studentID1, studentID2));
        commonController.getDeceasedStudentIDs(Arrays.asList(studentID1, studentID2));
        Mockito.verify(commonService).getDeceasedStudentIDs(Arrays.asList(studentID1, studentID2));
    }

    @Test
    public void testCreateStudentGradOptionalProgram() {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
        graduationStudentRecord.setStudentID(studentID);
        graduationStudentRecord.setStudentStatus("CUR");
        graduationStudentRecord.setPen("123456789");
        graduationStudentRecord.setStudentStatus("A");
        graduationStudentRecord.setRecalculateGradStatus("Y");
        graduationStudentRecord.setProgram("2018-en");
        graduationStudentRecord.setSchoolOfRecord(mincode);
        graduationStudentRecord.setSchoolAtGrad(mincode);
        graduationStudentRecord.setGpa("4");
        graduationStudentRecord.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM"));

        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        Mockito.when(graduationStatusService.createStudentGradOptionalProgram(studentID, studentOptionalProgram, "CP")).thenReturn(studentOptionalProgram);
        Mockito.when(graduationStatusService.getGraduationStatus(studentID, "accessToken")).thenReturn(graduationStudentRecord);

        Mockito.when(responseHelper.GET(graduationStudentRecord)).thenReturn(ResponseEntity.ok().body(graduationStudentRecord));

        var result = commonController.createStudentGradOptionalProgram(studentID, studentOptionalProgram, "CP", "accessToken");
        Mockito.verify(graduationStatusService).createStudentGradOptionalProgram(studentID, studentOptionalProgram, "CP");
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testUpdateStudentGradOptionalProgram() {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
        graduationStudentRecord.setStudentID(studentID);
        graduationStudentRecord.setStudentStatus("CUR");
        graduationStudentRecord.setPen("123456789");
        graduationStudentRecord.setStudentStatus("A");
        graduationStudentRecord.setRecalculateGradStatus("Y");
        graduationStudentRecord.setProgram("2018-en");
        graduationStudentRecord.setSchoolOfRecord(mincode);
        graduationStudentRecord.setSchoolAtGrad(mincode);
        graduationStudentRecord.setGpa("4");
        graduationStudentRecord.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM"));

        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        Mockito.when(graduationStatusService.updateStudentGradOptionalProgram(studentID, optionalProgramID, studentOptionalProgram)).thenReturn(studentOptionalProgram);
        Mockito.when(graduationStatusService.getGraduationStatus(studentID, "accessToken")).thenReturn(graduationStudentRecord);

        Mockito.when(responseHelper.GET(graduationStudentRecord)).thenReturn(ResponseEntity.ok().body(graduationStudentRecord));

        var result = commonController.updateStudentGradOptionalProgram(studentID, optionalProgramID, studentOptionalProgram,"accessToken");
        Mockito.verify(graduationStatusService).updateStudentGradOptionalProgram(studentID, optionalProgramID, studentOptionalProgram);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testDeleteStudentGradOptionalProgram() {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
        graduationStudentRecord.setStudentID(studentID);
        graduationStudentRecord.setStudentStatus("CUR");
        graduationStudentRecord.setPen("123456789");
        graduationStudentRecord.setStudentStatus("A");
        graduationStudentRecord.setRecalculateGradStatus("Y");
        graduationStudentRecord.setProgram("2018-en");
        graduationStudentRecord.setSchoolOfRecord(mincode);
        graduationStudentRecord.setSchoolAtGrad(mincode);
        graduationStudentRecord.setGpa("4");
        graduationStudentRecord.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM"));

        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        Mockito.when(graduationStatusService.getGraduationStatus(studentID, "accessToken")).thenReturn(graduationStudentRecord);
        doNothing().when(graduationStatusService).deleteStudentGradOptionalProgram(studentID, optionalProgramID, null);
        Mockito.when(responseHelper.GET(graduationStudentRecord)).thenReturn(ResponseEntity.ok().body(graduationStudentRecord));

        var result = commonController.deleteStudentGradOptionalProgram(studentID, optionalProgramID, null, "accessToken");
        Mockito.verify(graduationStatusService).deleteStudentGradOptionalProgram(studentID, optionalProgramID, null);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testDeleteStudentGradOptionalProgramWithCareerProgram() {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
        graduationStudentRecord.setStudentID(studentID);
        graduationStudentRecord.setStudentStatus("CUR");
        graduationStudentRecord.setPen("123456789");
        graduationStudentRecord.setStudentStatus("A");
        graduationStudentRecord.setRecalculateGradStatus("Y");
        graduationStudentRecord.setProgram("2018-en");
        graduationStudentRecord.setSchoolOfRecord(mincode);
        graduationStudentRecord.setSchoolAtGrad(mincode);
        graduationStudentRecord.setGpa("4");
        graduationStudentRecord.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM"));

        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        String careerProgramID = UUID.randomUUID().toString();

        Mockito.when(graduationStatusService.getGraduationStatus(studentID, "accessToken")).thenReturn(graduationStudentRecord);
        doNothing().when(graduationStatusService).deleteStudentGradOptionalProgram(studentID, optionalProgramID, careerProgramID);
        Mockito.when(responseHelper.GET(graduationStudentRecord)).thenReturn(ResponseEntity.ok().body(graduationStudentRecord));

        var result = commonController.deleteStudentGradOptionalProgram(studentID, optionalProgramID, careerProgramID, "accessToken");
        Mockito.verify(graduationStatusService).deleteStudentGradOptionalProgram(studentID, optionalProgramID, careerProgramID);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
