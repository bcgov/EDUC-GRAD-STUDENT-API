package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

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

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.getGraduationStatus(studentID, null)).thenReturn(graduationStatus);
        graduationStatusController.getStudentGradStatus(studentID.toString());
        Mockito.verify(graduationStatusService).getGraduationStatus(studentID, null);
    }

    @Test
    public void testGetStudentGradStatus_whenNotExists_thenReturnNoContentHttpStatus() {
        // ID
        UUID studentID = UUID.randomUUID();

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.getGraduationStatus(studentID, null)).thenReturn(null);
        Mockito.when(responseHelper.NO_CONTENT()).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        var result = graduationStatusController.getStudentGradStatus(studentID.toString());
        Mockito.verify(graduationStatusService).getGraduationStatus(studentID, null);
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

        Mockito.when(graduationStatusService.saveGraduationStatus(studentID, graduationStatus,null, null)).thenReturn(Pair.of(graduationStatus, null));
        Mockito.when(responseHelper.GET(graduationStatus)).thenReturn(ResponseEntity.ok().body(graduationStatus));
        var result = graduationStatusController.saveStudentGradStatus(studentID.toString(), graduationStatus,null);
        Mockito.verify(graduationStatusService).saveGraduationStatus(studentID, graduationStatus,null, null);
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

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(validation.hasErrors()).thenReturn(false);
        Mockito.when(graduationStatusService.updateGraduationStatus(studentID, graduationStatus, null)).thenReturn(Pair.of(graduationStatus, null));
        Mockito.when(responseHelper.GET(graduationStatus)).thenReturn(ResponseEntity.ok().body(graduationStatus));
        var result = graduationStatusController.updateStudentGradStatus(studentID.toString(), graduationStatus);
        Mockito.verify(graduationStatusService).updateGraduationStatus(studentID, graduationStatus, null);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testUpdateStudentGradStatus_whenValidationHasErrors_thenReturnBadRequestHttpStatus() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();

        GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(validation.hasErrors()).thenReturn(true);
        var result = graduationStatusController.updateStudentGradStatus(studentID.toString(), graduationStatus);
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

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.getStudentGradOptionalProgram(studentID, null)).thenReturn(List.of(gradStudentOptionalProgram));
        graduationStatusController.getStudentGradOptionalPrograms(studentID.toString());
        Mockito.verify(graduationStatusService).getStudentGradOptionalProgram(studentID, null);
    }

    @Test
    public void testGetStudentGradOptionalPrograms_whenNotExists_thenReturnNoContentHttpStatus() {
        // ID
        UUID studentID = UUID.randomUUID();

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.getStudentGradOptionalProgram(studentID, null)).thenReturn(new ArrayList<>());
        Mockito.when(responseHelper.NO_CONTENT()).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        var result = graduationStatusController.getStudentGradOptionalPrograms(studentID.toString());
        Mockito.verify(graduationStatusService).getStudentGradOptionalProgram(studentID, null);
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

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.getStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode(studentID, optionalProgramID.toString(), null)).thenReturn(gradStudentOptionalProgram);
        graduationStatusController.getStudentGradOptionalProgram(studentID.toString(), optionalProgramID.toString());
        Mockito.verify(graduationStatusService).getStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode(studentID, optionalProgramID.toString(),null);
    }

    @Test
    public void testGetStudentGradOptionalProgram_whenNotExists_thenReturnNoContentHttpStatus() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.getStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode(studentID, optionalProgramID.toString(), null)).thenReturn(null);
        Mockito.when(responseHelper.NO_CONTENT()).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        var result = graduationStatusController.getStudentGradOptionalProgram(studentID.toString(), optionalProgramID.toString());
        Mockito.verify(graduationStatusService).getStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode(studentID, optionalProgramID.toString(),null);
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

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.updateStudentGradOptionalProgram(gradStudentOptionalProgramReq, null)).thenReturn(studentOptionalProgram);
        Mockito.when(responseHelper.GET(studentOptionalProgram)).thenReturn(ResponseEntity.ok().body(studentOptionalProgram));
        var result = graduationStatusController.updateStudentGradOptionalProgram(gradStudentOptionalProgramReq);
        Mockito.verify(graduationStatusService).updateStudentGradOptionalProgram(gradStudentOptionalProgramReq, null);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
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

        GradStudentUngradReasons responseStudentUngradReasons = new GradStudentUngradReasons();
        responseStudentUngradReasons.setStudentID(studentID);
        responseStudentUngradReasons.setPen(pen);
        responseStudentUngradReasons.setUngradReasonCode(ungradReasonCode);

        Mockito.when(validation.hasErrors()).thenReturn(false);
        Mockito.when(graduationStatusService.ungradStudent(studentID, ungradReasonCode,ungradReasonDesc, null)).thenReturn(Pair.of(graduationStatus, null));
        Mockito.when(responseHelper.GET(graduationStatus)).thenReturn(ResponseEntity.ok().body(graduationStatus));
        var result = graduationStatusController.ungradStudent(studentID.toString(), ungradReasonCode,ungradReasonDesc);
        Mockito.verify(graduationStatusService).ungradStudent(studentID, ungradReasonCode,ungradReasonDesc, null);
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

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

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
        Mockito.when(historyService.getStudentOptionalProgramEditHistory(UUID.fromString(studentID),null)).thenReturn(histList);
        graduationStatusController.getStudentOptionalProgramHistory(studentID);
        Mockito.verify(historyService).getStudentOptionalProgramEditHistory(UUID.fromString(studentID),null);
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
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);
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
        Mockito.when(historyService.getStudentOptionalProgramHistoryByID(UUID.fromString(historyID),null)).thenReturn(gradStudentOptionalProgramEntity);
        graduationStatusController.getStudentOptionalProgramHistoryByID(historyID);
        Mockito.verify(historyService).getStudentOptionalProgramHistoryByID(UUID.fromString(historyID),null);
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
        Mockito.when(historyService.getStudentHistoryByBatchID(4000L, 0, 10)).thenReturn(hPage);
        graduationStatusController.getStudentHistoryByBatchID(4000L,0,10);
        Mockito.verify(historyService).getStudentHistoryByBatchID(4000L, 0,10);
    }
}
