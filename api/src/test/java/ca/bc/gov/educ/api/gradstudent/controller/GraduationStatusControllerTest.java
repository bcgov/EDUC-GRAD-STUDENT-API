package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.gradstudent.dto.GradStudentSpecialProgramReq;
import ca.bc.gov.educ.api.gradstudent.dto.GradStudentUngradReasons;
import ca.bc.gov.educ.api.gradstudent.dto.GraduationStatus;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStatusApiUtils;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class GraduationStatusControllerTest {
    @Mock
    private GraduationStatusService graduationStatusService;

    @Mock
    ResponseHelper responseHelper;

    @Mock
    GradValidation validation;

    @InjectMocks
    private GraduationStatusController graduationStatusController;

    @Test
    public void testGetStudentGradStatus() {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStatus graduationStatus = new GraduationStatus();
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

        GraduationStatus graduationStatus = new GraduationStatus();
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
    public void testSaveStudentGradStatus() {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStatus graduationStatus = new GraduationStatus();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen("123456789");
        graduationStatus.setStudentStatus("A");
        graduationStatus.setRecalculateGradStatus("Y");
        graduationStatus.setProgram("2018-en");
        graduationStatus.setSchoolOfRecord(mincode);
        graduationStatus.setSchoolAtGrad(mincode);
        graduationStatus.setGpa("4");
        graduationStatus.setProgramCompletionDate(EducGradStatusApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM"));

        Mockito.when(graduationStatusService.saveGraduationStatus(studentID, graduationStatus)).thenReturn(graduationStatus);
        Mockito.when(responseHelper.GET(graduationStatus)).thenReturn(ResponseEntity.ok().body(graduationStatus));
        var result = graduationStatusController.saveStudentGradStatus(studentID.toString(), graduationStatus);
        Mockito.verify(graduationStatusService).saveGraduationStatus(studentID, graduationStatus);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testUpdateStudentGradStatus() {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStatus graduationStatus = new GraduationStatus();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen("123456789");
        graduationStatus.setStudentStatus("A");
        graduationStatus.setRecalculateGradStatus("Y");
        graduationStatus.setProgram("2018-en");
        graduationStatus.setSchoolOfRecord(mincode);
        graduationStatus.setSchoolAtGrad(mincode);
        graduationStatus.setGpa("4");
        graduationStatus.setProgramCompletionDate(EducGradStatusApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM"));

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(validation.hasErrors()).thenReturn(false);
        Mockito.when(graduationStatusService.updateGraduationStatus(studentID, graduationStatus, null)).thenReturn(graduationStatus);
        Mockito.when(responseHelper.GET(graduationStatus)).thenReturn(ResponseEntity.ok().body(graduationStatus));
        var result = graduationStatusController.updateStudentGradStatus(studentID.toString(), graduationStatus);
        Mockito.verify(graduationStatusService).updateGraduationStatus(studentID, graduationStatus, null);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testUpdateStudentGradStatus_whenValidationHasErrors_thenReturnBadRequestHttpStatus() {
        // ID
        UUID studentID = UUID.randomUUID();

        GraduationStatus graduationStatus = new GraduationStatus();
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
    public void testGetStudentGradSpecialPrograms() {
        // ID
        UUID gradStudentSpecialProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID specialProgramID = UUID.randomUUID();
        String pen = "123456789";

        GradStudentSpecialProgram gradStudentSpecialProgram = new GradStudentSpecialProgram();
        gradStudentSpecialProgram.setId(gradStudentSpecialProgramID);
        gradStudentSpecialProgram.setStudentID(studentID);
        gradStudentSpecialProgram.setSpecialProgramID(specialProgramID);
        gradStudentSpecialProgram.setPen(pen);
        gradStudentSpecialProgram.setSpecialProgramCompletionDate(EducGradStatusApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM" ));

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.getStudentGradSpecialProgram(studentID, null)).thenReturn(Arrays.asList(gradStudentSpecialProgram));
        graduationStatusController.getStudentGradSpecialPrograms(studentID.toString());
        Mockito.verify(graduationStatusService).getStudentGradSpecialProgram(studentID, null);
    }

    @Test
    public void testGetStudentGradSpecialPrograms_whenNotExists_thenReturnNoContentHttpStatus() {
        // ID
        UUID studentID = UUID.randomUUID();

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.getStudentGradSpecialProgram(studentID, null)).thenReturn(new ArrayList<>());
        Mockito.when(responseHelper.NO_CONTENT()).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        var result = graduationStatusController.getStudentGradSpecialPrograms(studentID.toString());
        Mockito.verify(graduationStatusService).getStudentGradSpecialProgram(studentID, null);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testGetStudentGradSpecialProgram() {
        // ID
        UUID gradStudentSpecialProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID specialProgramID = UUID.randomUUID();
        String pen = "123456789";

        GradStudentSpecialProgram gradStudentSpecialProgram = new GradStudentSpecialProgram();
        gradStudentSpecialProgram.setId(gradStudentSpecialProgramID);
        gradStudentSpecialProgram.setStudentID(studentID);
        gradStudentSpecialProgram.setSpecialProgramID(specialProgramID);
        gradStudentSpecialProgram.setPen(pen);
        gradStudentSpecialProgram.setSpecialProgramCompletionDate(EducGradStatusApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM" ));

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.getStudentGradSpecialProgramByProgramCodeAndSpecialProgramCode(studentID, specialProgramID.toString(), null)).thenReturn(gradStudentSpecialProgram);
        graduationStatusController.getStudentGradSpecialProgram(studentID.toString(), specialProgramID.toString());
        Mockito.verify(graduationStatusService).getStudentGradSpecialProgramByProgramCodeAndSpecialProgramCode(studentID, specialProgramID.toString(),null);
    }

    @Test
    public void testGetStudentGradSpecialProgram_whenNotExists_thenReturnNoContentHttpStatus() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID specialProgramID = UUID.randomUUID();

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.getStudentGradSpecialProgramByProgramCodeAndSpecialProgramCode(studentID, specialProgramID.toString(), null)).thenReturn(null);
        Mockito.when(responseHelper.NO_CONTENT()).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        var result = graduationStatusController.getStudentGradSpecialProgram(studentID.toString(), specialProgramID.toString());
        Mockito.verify(graduationStatusService).getStudentGradSpecialProgramByProgramCodeAndSpecialProgramCode(studentID, specialProgramID.toString(),null);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testSaveStudentGradSpecialProgram() {
        // ID
        UUID gradStudentSpecialProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID specialProgramID = UUID.randomUUID();
        String pen = "123456789";

        GradStudentSpecialProgram studentSpecialProgram = new GradStudentSpecialProgram();
        studentSpecialProgram.setId(gradStudentSpecialProgramID);
        studentSpecialProgram.setStudentID(studentID);
        studentSpecialProgram.setSpecialProgramID(specialProgramID);
        studentSpecialProgram.setPen(pen);
        studentSpecialProgram.setSpecialProgramCompletionDate(EducGradStatusApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd" ));

        Mockito.when(graduationStatusService.saveStudentGradSpecialProgram(studentSpecialProgram)).thenReturn(studentSpecialProgram);
        Mockito.when(responseHelper.GET(studentSpecialProgram)).thenReturn(ResponseEntity.ok().body(studentSpecialProgram));
        var result = graduationStatusController.saveStudentGradSpecialProgram(studentSpecialProgram);
        Mockito.verify(graduationStatusService).saveStudentGradSpecialProgram(studentSpecialProgram);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testUpdateStudentGradSpecialProgram() {
        // ID
        UUID gradStudentSpecialProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID specialProgramID = UUID.randomUUID();
        String pen = "123456789";

        GradStudentSpecialProgramReq gradStudentSpecialProgramReq = new GradStudentSpecialProgramReq();
        gradStudentSpecialProgramReq.setId(gradStudentSpecialProgramID);
        gradStudentSpecialProgramReq.setStudentID(studentID);
        gradStudentSpecialProgramReq.setPen(pen);
        gradStudentSpecialProgramReq.setMainProgramCode("2018-en");
        gradStudentSpecialProgramReq.setSpecialProgramCode("FI");
        gradStudentSpecialProgramReq.setSpecialProgramCompletionDate(EducGradStatusApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd" ));

        GradStudentSpecialProgram studentSpecialProgram = new GradStudentSpecialProgram();
        studentSpecialProgram.setId(gradStudentSpecialProgramID);
        studentSpecialProgram.setStudentID(studentID);
        studentSpecialProgram.setSpecialProgramID(specialProgramID);
        studentSpecialProgram.setPen(pen);
        studentSpecialProgram.setSpecialProgramCompletionDate(gradStudentSpecialProgramReq.getSpecialProgramCompletionDate());

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(graduationStatusService.updateStudentGradSpecialProgram(gradStudentSpecialProgramReq, null)).thenReturn(studentSpecialProgram);
        Mockito.when(responseHelper.GET(studentSpecialProgram)).thenReturn(ResponseEntity.ok().body(studentSpecialProgram));
        var result = graduationStatusController.updateStudentGradSpecialProgram(gradStudentSpecialProgramReq);
        Mockito.verify(graduationStatusService).updateStudentGradSpecialProgram(gradStudentSpecialProgramReq, null);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetStudentsForGraduation() {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";

        GraduationStatus graduationStatus = new GraduationStatus();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen(pen);
        graduationStatus.setStudentStatus("A");
        graduationStatus.setSchoolOfRecord("12345678");
        graduationStatus.setRecalculateGradStatus("Y");

        Mockito.when(graduationStatusService.getStudentsForGraduation()).thenReturn(Arrays.asList(graduationStatus));
        graduationStatusController.getStudentsForGraduation();
        Mockito.verify(graduationStatusService).getStudentsForGraduation();
    }

    @Test
    public void testGetStudentStatus() {
        Mockito.when(graduationStatusService.getStudentStatus("A")).thenReturn(true);
        graduationStatusController.getStudentStatus("A");
        Mockito.verify(graduationStatusService).getStudentStatus("A");
    }

    @Test
    public void testUgradStudent() {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String ungradReasonCode = "NM";
        String ungradReasonDesc = "FDSS";

        GraduationStatus graduationStatus = new GraduationStatus();
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
        Mockito.when(graduationStatusService.ungradStudent(studentID, ungradReasonCode,ungradReasonDesc, null)).thenReturn(graduationStatus);
        Mockito.when(responseHelper.GET(graduationStatus)).thenReturn(ResponseEntity.ok().body(graduationStatus));
        var result = graduationStatusController.ungradStudent(studentID.toString(), ungradReasonCode,ungradReasonDesc);
        Mockito.verify(graduationStatusService).ungradStudent(studentID, ungradReasonCode,ungradReasonDesc, null);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

}
