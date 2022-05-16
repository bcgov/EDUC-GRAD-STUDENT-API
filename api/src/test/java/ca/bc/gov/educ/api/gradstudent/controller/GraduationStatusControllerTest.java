package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.service.HistoryService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.ResponseHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class GraduationStatusControllerTest {
    @Mock
    private GraduationStatusService graduationStatusService;

    @Mock
    private HistoryService historyService;

    @Mock
    ResponseHelper responseHelper;

    @Mock
    GradValidation validation;

    @Mock
    Publisher publisher;

    @InjectMocks
    private GraduationStatusController graduationStatusController;

    @Test
    public void testGetStudentGradStatus() {
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


        Mockito.when(graduationStatusService.getGraduationStatus(studentID, "accessToken")).thenReturn(graduationStatus);
        graduationStatusController.getStudentGradStatus(studentID.toString(), "accessToken");
        Mockito.verify(graduationStatusService).getGraduationStatus(studentID, "accessToken");
    }

    @Test
    public void testGetStudentGradStatus_whenNotExists_thenReturnNoContentHttpStatus() {
        // ID
        UUID studentID = UUID.randomUUID();

        Mockito.when(graduationStatusService.getGraduationStatus(studentID, "accessToken")).thenReturn(null);
        Mockito.when(responseHelper.NO_CONTENT()).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        var result = graduationStatusController.getStudentGradStatus(studentID.toString(), "accessToken");
        Mockito.verify(graduationStatusService).getGraduationStatus(studentID, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testGetStudentGradStatusForAlgorithm() {
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

        Mockito.when(graduationStatusService.getGraduationStatusForAlgorithm(studentID)).thenReturn(graduationStatus);
        graduationStatusController.getStudentGradStatusForAlgorithm(studentID.toString());
        Mockito.verify(graduationStatusService).getGraduationStatusForAlgorithm(studentID);
    }

    @Test
    public void testSaveStudentGradStatus() throws JsonProcessingException {
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

        Mockito.when(graduationStatusService.saveGraduationStatus(studentID, graduationStatus,null, "accessToken")).thenReturn(Pair.of(graduationStatus, null));
        Mockito.when(responseHelper.GET(graduationStatus)).thenReturn(ResponseEntity.ok().body(graduationStatus));
        var result = graduationStatusController
                .saveStudentGradStatus(studentID.toString(), graduationStatus,null, "accessToken");
        Mockito.verify(graduationStatusService).saveGraduationStatus(studentID, graduationStatus,null, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testUpdateStudentGradStatus() throws JsonProcessingException {
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

        Mockito.when(validation.hasErrors()).thenReturn(false);
        Mockito.when(graduationStatusService.updateGraduationStatus(studentID, graduationStatus, "accessToken")).thenReturn(Pair.of(graduationStatus, null));
        Mockito.when(responseHelper.GET(graduationStatus)).thenReturn(ResponseEntity.ok().body(graduationStatus));
        var result = graduationStatusController.updateStudentGradStatus(studentID.toString(), graduationStatus, "accessToken");
        Mockito.verify(graduationStatusService).updateGraduationStatus(studentID, graduationStatus, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testUpdateStudentGradStatus_whenValidationHasErrors_thenReturnBadRequestHttpStatus() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();

        GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);

        Mockito.when(validation.hasErrors()).thenReturn(true);
        var result = graduationStatusController.updateStudentGradStatus(studentID.toString(), graduationStatus, null);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testGetStudentGradOptionalPrograms() {
        // ID
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();
        String pen = "123456789";

        StudentOptionalProgram gradStudentOptionalProgram = new StudentOptionalProgram();
        gradStudentOptionalProgram.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgram.setStudentID(studentID);
        gradStudentOptionalProgram.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgram.setPen(pen);
        gradStudentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM" ));

        Mockito.when(graduationStatusService.getStudentGradOptionalProgram(studentID, "accessToken")).thenReturn(List.of(gradStudentOptionalProgram));
        graduationStatusController.getStudentGradOptionalPrograms(studentID.toString(), "accessToken");
        Mockito.verify(graduationStatusService).getStudentGradOptionalProgram(studentID, "accessToken");
    }

    @Test
    public void testGetStudentGradOptionalPrograms_whenNotExists_thenReturnNoContentHttpStatus() {
        // ID
        UUID studentID = UUID.randomUUID();

        Mockito.when(graduationStatusService.getStudentGradOptionalProgram(studentID, "accessToken")).thenReturn(new ArrayList<>());
        Mockito.when(responseHelper.NO_CONTENT()).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        var result = graduationStatusController.getStudentGradOptionalPrograms(studentID.toString(), "accessToken");
        Mockito.verify(graduationStatusService).getStudentGradOptionalProgram(studentID, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testGetStudentGradOptionalProgram() {
        // ID
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();
        String pen = "123456789";

        StudentOptionalProgram gradStudentOptionalProgram = new StudentOptionalProgram();
        gradStudentOptionalProgram.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgram.setStudentID(studentID);
        gradStudentOptionalProgram.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgram.setPen(pen);
        gradStudentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM" ));

        Mockito.when(graduationStatusService.getStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode(studentID, optionalProgramID.toString(), "accessToken")).thenReturn(gradStudentOptionalProgram);
        graduationStatusController.getStudentGradOptionalProgram(studentID.toString(), optionalProgramID.toString(), "accessToken");
        Mockito.verify(graduationStatusService).getStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode(studentID, optionalProgramID.toString(),"accessToken");
    }

    @Test
    public void testGetStudentGradOptionalProgram_whenNotExists_thenReturnNoContentHttpStatus() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        Mockito.when(graduationStatusService.getStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode(studentID, optionalProgramID.toString(), "accessToken")).thenReturn(null);
        Mockito.when(responseHelper.NO_CONTENT()).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        var result = graduationStatusController.getStudentGradOptionalProgram(studentID.toString(), optionalProgramID.toString(), "accessToken");
        Mockito.verify(graduationStatusService).getStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode(studentID, optionalProgramID.toString(),"accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testSaveStudentGradOptionalProgram() {
        // ID
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();
        String pen = "123456789";

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        studentOptionalProgram.setId(gradStudentOptionalProgramID);
        studentOptionalProgram.setStudentID(studentID);
        studentOptionalProgram.setOptionalProgramID(optionalProgramID);
        studentOptionalProgram.setPen(pen);
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd" ));

        Mockito.when(graduationStatusService.saveStudentGradOptionalProgram(studentOptionalProgram)).thenReturn(studentOptionalProgram);
        Mockito.when(responseHelper.GET(studentOptionalProgram)).thenReturn(ResponseEntity.ok().body(studentOptionalProgram));
        var result = graduationStatusController.saveStudentGradOptionalProgram(studentOptionalProgram);
        Mockito.verify(graduationStatusService).saveStudentGradOptionalProgram(studentOptionalProgram);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testUpdateStudentGradOptionalProgram() {
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

        Mockito.when(graduationStatusService.updateStudentGradOptionalProgram(gradStudentOptionalProgramReq, "accessToken")).thenReturn(studentOptionalProgram);
        Mockito.when(responseHelper.GET(studentOptionalProgram)).thenReturn(ResponseEntity.ok().body(studentOptionalProgram));
        var result = graduationStatusController.updateStudentGradOptionalProgram(gradStudentOptionalProgramReq, "accessToken");
        Mockito.verify(graduationStatusService).updateStudentGradOptionalProgram(gradStudentOptionalProgramReq, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testSearchGraduationStudentRecords() {

        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen("126785500");
        graduationStatus.setStudentStatus("A");
        graduationStatus.setRecalculateGradStatus("Y");
        graduationStatus.setProgram("1950");
        graduationStatus.setSchoolOfRecord(mincode);
        graduationStatus.setSchoolAtGrad(mincode);
        graduationStatus.setGpa("4");

        GraduationStudentRecordSearchResult searchResult = new GraduationStudentRecordSearchResult();
        searchResult.setGraduationStudentRecords(List.of(graduationStatus));

        List<String> pens = new ArrayList<>();

        List<String> schoolOfRecords = new ArrayList<>();
        schoolOfRecords.add("06299164");
        schoolOfRecords.add("03838000");

        List<String> districts = new ArrayList<>();
        districts.add("044");

        List<String> programs = new ArrayList<>();
        programs.add("1950");

        StudentSearchRequest searchRequest = StudentSearchRequest.builder()
                .pens(pens)
                .schoolOfRecords(schoolOfRecords)
                .districts(districts)
                .programs(programs)
                .build();

        Mockito.when(graduationStatusService.searchGraduationStudentRecords(searchRequest, "accessToken")).thenReturn(searchResult);
        graduationStatusController.searchGraduationStudentRecords(searchRequest, "accessToken");
        Mockito.verify(graduationStatusService).searchGraduationStudentRecords(searchRequest, "accessToken");
    }

    @Test
    public void testGetStudentsForGraduation() {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";

        GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen(pen);
        graduationStatus.setStudentStatus("A");
        graduationStatus.setSchoolOfRecord("12345678");
        graduationStatus.setRecalculateGradStatus("Y");

        Mockito.when(graduationStatusService.getStudentsForGraduation()).thenReturn(List.of(graduationStatus));
        graduationStatusController.getStudentsForGraduation();
        Mockito.verify(graduationStatusService).getStudentsForGraduation();
    }

    @Test
    public void testGetStudentsForProjectedGraduation() {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";

        GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen(pen);
        graduationStatus.setStudentStatus("CUR");
        graduationStatus.setSchoolOfRecord("12345678");
        graduationStatus.setRecalculateGradStatus("Y");

        Mockito.when(graduationStatusService.getStudentsForProjectedGraduation()).thenReturn(List.of(graduationStatus));
        graduationStatusController.getStudentsForProjectedGraduation();
        Mockito.verify(graduationStatusService).getStudentsForProjectedGraduation();
    }

    @Test
    public void testGetStudentStatus() {
        Mockito.when(graduationStatusService.getStudentStatus("A")).thenReturn(true);
        graduationStatusController.getStudentStatus("A");
        Mockito.verify(graduationStatusService).getStudentStatus("A");
    }

    @Test
    public void testUgradStudent() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String ungradReasonCode = "NM";
        String ungradReasonDesc = "FDSS";

        GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen(pen);
        graduationStatus.setStudentStatus("A");
        graduationStatus.setSchoolOfRecord("12345678");
        graduationStatus.setRecalculateGradStatus("Y");

        StudentUndoCompletionReason responseStudentUngradReasons = new StudentUndoCompletionReason();
        responseStudentUngradReasons.setGraduationStudentRecordID(studentID);
        responseStudentUngradReasons.setUndoCompletionReasonCode(ungradReasonCode);

        Mockito.when(validation.hasErrors()).thenReturn(false);
        Mockito.when(graduationStatusService.undoCompletionStudent(studentID, ungradReasonCode,ungradReasonDesc, "accessToken")).thenReturn(Pair.of(graduationStatus, null));
        Mockito.when(responseHelper.GET(graduationStatus)).thenReturn(ResponseEntity.ok().body(graduationStatus));
        var result = graduationStatusController.ungradStudent(studentID.toString(), ungradReasonCode,ungradReasonDesc, "accessToken");
        Mockito.verify(graduationStatusService).undoCompletionStudent(studentID, ungradReasonCode,ungradReasonDesc, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void testreturnToOriginalState() {
        // ID
        UUID studentID = UUID.randomUUID();
        Mockito.when(graduationStatusService.restoreGradStudentRecord(studentID,true)).thenReturn(true);
        graduationStatusController.returnToOriginalState(studentID.toString(),true);
        Mockito.verify(graduationStatusService).restoreGradStudentRecord(studentID,true);
    }

    @Test
    public void testSaveStudentRecord_ProjectedGradRun() {
        // ID
        UUID studentID = UUID.randomUUID();

        GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setStudentStatus("A");
        graduationStatus.setSchoolOfRecord("12345678");
        graduationStatus.setRecalculateGradStatus("Y");

        Mockito.when(graduationStatusService.saveStudentRecordProjectedTVRRun(studentID,null)).thenReturn(graduationStatus);
        graduationStatusController.saveStudentGradStatusProjectedRun(studentID.toString(),null);
        Mockito.verify(graduationStatusService).saveStudentRecordProjectedTVRRun(studentID,null);
    }
    
    @Test
    public void testreturnToOriginalState_returnsfalse() {
        // ID
        UUID studentID = UUID.randomUUID();
        Mockito.when(graduationStatusService.restoreGradStudentRecord(studentID,false)).thenReturn(false);
        graduationStatusController.returnToOriginalState(studentID.toString(),false);
        Mockito.verify(graduationStatusService).restoreGradStudentRecord(studentID,false);
    }

    @Test
    public void testGetStudentHistory() {
        // ID
        String studentID = UUID.randomUUID().toString();
        List<GraduationStudentRecordHistory> histList = new ArrayList<>();
        GraduationStudentRecordHistory graduationStatusEntity = new GraduationStudentRecordHistory();
        graduationStatusEntity.setStudentID(UUID.fromString(studentID));
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-en");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setHistoryID(new UUID(1,1));
        graduationStatusEntity.setActivityCode("GRADALG");
        histList.add(graduationStatusEntity);
        Mockito.when(historyService.getStudentEditHistory(UUID.fromString(studentID))).thenReturn(histList);
        graduationStatusController.getStudentHistory(studentID);
        Mockito.verify(historyService).getStudentEditHistory(UUID.fromString(studentID));
    }

    @Test
    public void testGetOptionalProgramStudentHistory() {
        // ID
        String studentID = UUID.randomUUID().toString();
        List<StudentOptionalProgramHistory> histList = new ArrayList<>();
        StudentOptionalProgramHistory gradStudentOptionalProgramEntity = new StudentOptionalProgramHistory();
        gradStudentOptionalProgramEntity.setStudentOptionalProgramId(new UUID(1,1));
        gradStudentOptionalProgramEntity.setStudentID(UUID.fromString(studentID));
        gradStudentOptionalProgramEntity.setOptionalProgramID(new UUID(2,2));
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()).toString());
        gradStudentOptionalProgramEntity.setHistoryId(new UUID(3,3));
        gradStudentOptionalProgramEntity.setActivityCode("GRADALG");
        histList.add(gradStudentOptionalProgramEntity);
        Mockito.when(historyService.getStudentOptionalProgramEditHistory(UUID.fromString(studentID),"accessToken")).thenReturn(histList);
        graduationStatusController.getStudentOptionalProgramHistory(studentID, "accessToken");
        Mockito.verify(historyService).getStudentOptionalProgramEditHistory(UUID.fromString(studentID),"accessToken");
    }

    @Test
    public void testGetStudentHistoryByID() {
        // ID
        String historyID = UUID.randomUUID().toString();
        String studentID = UUID.randomUUID().toString();
        GraduationStudentRecordHistory graduationStatusEntity = new GraduationStudentRecordHistory();
        graduationStatusEntity.setStudentID(UUID.fromString(studentID));
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-en");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setHistoryID(new UUID(1,1));
        graduationStatusEntity.setActivityCode("GRADALG");
        Mockito.when(historyService.getStudentHistoryByID(UUID.fromString(historyID))).thenReturn(graduationStatusEntity);
        graduationStatusController.getStudentHistoryByID(historyID);
        Mockito.verify(historyService).getStudentHistoryByID(UUID.fromString(historyID));
    }

    @Test
    public void testGetOptionalProgramStudentHistoryByID() {
        // ID
        String historyID = UUID.randomUUID().toString();
        String studentID = UUID.randomUUID().toString();
        StudentOptionalProgramHistory gradStudentOptionalProgramEntity = new StudentOptionalProgramHistory();
        gradStudentOptionalProgramEntity.setStudentOptionalProgramId(new UUID(1,1));
        gradStudentOptionalProgramEntity.setStudentID(UUID.fromString(studentID));
        gradStudentOptionalProgramEntity.setOptionalProgramID(new UUID(2,2));
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()).toString());
        gradStudentOptionalProgramEntity.setHistoryId(new UUID(3,3));
        gradStudentOptionalProgramEntity.setActivityCode("GRADALG");
        Mockito.when(historyService.getStudentOptionalProgramHistoryByID(UUID.fromString(historyID),"accessToken")).thenReturn(gradStudentOptionalProgramEntity);
        graduationStatusController.getStudentOptionalProgramHistoryByID(historyID, "accessToken");
        Mockito.verify(historyService).getStudentOptionalProgramHistoryByID(UUID.fromString(historyID),"accessToken");
    }

    @Test
    public void testGetStudentHistoryByBatchID() {
        // ID
        String historyID = UUID.randomUUID().toString();
        UUID studentID = UUID.randomUUID();
        List<GraduationStudentRecordHistory> histList = new ArrayList<>();
        GraduationStudentRecordHistory graduationStatusEntity = new GraduationStudentRecordHistory();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-en");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setHistoryID(new UUID(1,1));
        graduationStatusEntity.setActivityCode("GRADALG");
        graduationStatusEntity.setBatchId(4000L);
        histList.add(graduationStatusEntity);
        Page<GraduationStudentRecordHistoryEntity> hPage = new PageImpl(histList);
        Mockito.when(historyService.getStudentHistoryByBatchID(4000L, 0, 10,"accessToken")).thenReturn(hPage);
        graduationStatusController.getStudentHistoryByBatchID(4000L,0,10, "accessToken");
        Mockito.verify(historyService).getStudentHistoryByBatchID(4000L, 0,10,"accessToken");
    }

    @Test
    public void testGetStudentsForYearlyRun() {
        List<UUID> histList = new ArrayList<>();
        histList.add(new UUID(1,1));
        Mockito.when(graduationStatusService.getStudentsForYearlyDistribution()).thenReturn(histList);
        graduationStatusController.getStudentsForYearlyRun();
        Mockito.verify(graduationStatusService).getStudentsForYearlyDistribution();
    }
}
