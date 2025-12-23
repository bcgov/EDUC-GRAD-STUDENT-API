package ca.bc.gov.educ.api.gradstudent.service.event;

import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.DemographicStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.student.v1.StudentUpdate;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@ActiveProfiles("integration-test")
@SpringBootTest
class GraduationStudentRecordServiceTest {

    @Autowired
    private GraduationStudentRecordRepository graduationStudentRecordRepository;

    @Autowired
    private GraduationStudentRecordService graduationStudentRecordService;

    @Test
    @Transactional
    void handleStudentUpdated() {
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("321654987")
                .genderCode("F")
                .legalFirstName("Danielle")
                .build();
        GraduationStudentRecordEntity graduationStudentRecordEntity = GraduationStudentRecordEntity.builder()
                .studentID(studentID)
                .studentStatus("CUR")
                .build();
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, graduationStudentRecordEntity, event);
        Optional<GraduationStudentRecordEntity> savedEntity = graduationStudentRecordRepository.findById(studentID);
        savedEntity.ifPresent(studentRecordEntity -> assertEquals(graduationStudentRecordEntity.getStudentID(), studentRecordEntity.getStudentID()));
    }

    @Test
    @Transactional
    void testMapStudentStatusForUpdate_ReportedT_CurrentStatusTER_ShouldReturnTER() {
        // Given: Reported "T" and GRAD Status = "TER" → Store as TER
        UUID studentID = UUID.randomUUID();
        UUID schoolID = UUID.randomUUID();
        
        DemographicStudent demStudent = createMockDemographicStudent("N", "CSF", "T", schoolID.toString());
        Student studentFromApi = createMockStudent(studentID.toString());
        GraduationStudentRecordEntity existingEntity = createMockGraduationStudentRecordEntity(studentID, schoolID, "TER");
        
        // When
        var result = graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, existingEntity);
        
        // Then
        assertNotNull(result);
        assertEquals("TER", result.getRight().getStudentStatus());
    }

    @Test
    @Transactional
    void testMapStudentStatusForUpdate_ReportedT_CurrentStatusARC_ShouldReturnARC() {
        // Given: Reported "T" and GRAD Status = "ARC" → No update, keep status as ARC
        UUID studentID = UUID.randomUUID();
        UUID schoolID = UUID.randomUUID();
        
        DemographicStudent demStudent = createMockDemographicStudent("N", "CSF", "T", schoolID.toString());
        Student studentFromApi = createMockStudent(studentID.toString());
        GraduationStudentRecordEntity existingEntity = createMockGraduationStudentRecordEntity(studentID, schoolID, "ARC");
        
        // When
        var result = graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, existingEntity);
        
        // Then
        assertNotNull(result);
        assertEquals("ARC", result.getRight().getStudentStatus());
    }

    @Test
    @Transactional
    void testMapStudentStatusForUpdate_ReportedT_CurrentStatusCUR_SchoolOfRecordMatches_ShouldReturnTER() {
        // Given: Reported "T" and GRAD Status = "CUR" and SoR = Reporting School → Store as TER
        UUID studentID = UUID.randomUUID();
        UUID schoolID = UUID.randomUUID();
        
        DemographicStudent demStudent = createMockDemographicStudent("N", "CSF", "T", schoolID.toString());
        Student studentFromApi = createMockStudent(studentID.toString());
        GraduationStudentRecordEntity existingEntity = createMockGraduationStudentRecordEntity(studentID, schoolID, "CUR");
        
        // When
        var result = graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, existingEntity);
        
        // Then
        assertNotNull(result);
        assertEquals("TER", result.getRight().getStudentStatus());
    }

    @Test
    @Transactional
    void testMapStudentStatusForUpdate_ReportedT_CurrentStatusCUR_SchoolOfRecordDoesNotMatch_ShouldReturnCUR() {
        // Given: Reported "T" and GRAD Status = "CUR" and SoR not = Reporting School → No update, keep status as CUR
        UUID studentID = UUID.randomUUID();
        UUID reportingSchoolID = UUID.randomUUID();
        UUID differentSchoolID = UUID.randomUUID();
        
        DemographicStudent demStudent = createMockDemographicStudent("N", "CSF", "T", reportingSchoolID.toString());
        Student studentFromApi = createMockStudent(studentID.toString());
        GraduationStudentRecordEntity existingEntity = createMockGraduationStudentRecordEntity(studentID, differentSchoolID, "CUR");
        
        // When
        var result = graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, existingEntity);
        
        // Then
        assertNotNull(result);
        assertEquals("CUR", result.getRight().getStudentStatus());
    }

    @Test
    @Transactional
    void testMapStudentStatusForUpdate_ReportedT_CurrentStatusMER_ShouldThrowException() {
        // Given: Reported "T" with unexpected GRAD status (MER) → Should throw IllegalArgumentException
        UUID studentID = UUID.randomUUID();
        UUID schoolID = UUID.randomUUID();
        
        DemographicStudent demStudent = createMockDemographicStudent("N", "CSF", "T", schoolID.toString());
        Student studentFromApi = createMockStudent(studentID.toString());
        GraduationStudentRecordEntity existingEntity = createMockGraduationStudentRecordEntity(studentID, schoolID, "MER");
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, existingEntity));
    }

    @Test
    @Transactional
    void testMapStudentStatusForUpdate_ReportedT_CurrentStatusDEC_ShouldThrowException() {
        // Given: Reported "T" with unexpected GRAD status (DEC) → Should throw IllegalArgumentException
        UUID studentID = UUID.randomUUID();
        UUID schoolID = UUID.randomUUID();
        
        DemographicStudent demStudent = createMockDemographicStudent("N", "CSF", "T", schoolID.toString());
        Student studentFromApi = createMockStudent(studentID.toString());
        GraduationStudentRecordEntity existingEntity = createMockGraduationStudentRecordEntity(studentID, schoolID, "DEC");
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, existingEntity));
    }

    @Test
    @Transactional
    void testMapStudentStatusForUpdate_ReportedT_CurrentStatusNull_ShouldThrowException() {
        // Given: Reported "T" with null GRAD status → Should throw NullPointerException (from equalsIgnoreCase on null)
        UUID studentID = UUID.randomUUID();
        UUID schoolID = UUID.randomUUID();
        
        DemographicStudent demStudent = createMockDemographicStudent("N", "CSF", "T", schoolID.toString());
        Student studentFromApi = createMockStudent(studentID.toString());
        GraduationStudentRecordEntity existingEntity = createMockGraduationStudentRecordEntity(studentID, schoolID, null);
        
        // When/Then
        assertThrows(NullPointerException.class, () -> graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, existingEntity));
    }

    @Test
    @Transactional
    void testMapStudentStatusForUpdate_InvalidStatus_ShouldThrowException() {
        // Given: Invalid reported status (not A, D, or T) → Should throw IllegalArgumentException
        UUID studentID = UUID.randomUUID();
        UUID schoolID = UUID.randomUUID();
        
        DemographicStudent demStudent = createMockDemographicStudent("N", "CSF", "X", schoolID.toString());
        Student studentFromApi = createMockStudent(studentID.toString());
        GraduationStudentRecordEntity existingEntity = createMockGraduationStudentRecordEntity(studentID, schoolID, "CUR");
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, existingEntity));
    }

    // Helper methods
    private DemographicStudent createMockDemographicStudent(String isSummerCollection, String schoolReportingRequirementCode, String studentStatus, String schoolID) {
        return DemographicStudent.builder()
                .pen("123456789")
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .createUser("ABC")
                .updateUser("ABC")
                .grade("08")
                .birthdate("19900101")
                .citizenship("C")
                .gradRequirementYear("2023")
                .programCode1("AA")
                .studentStatus(studentStatus)
                .mincode("03636018")
                .schoolID(schoolID)
                .isSummerCollection(isSummerCollection)
                .schoolCertificateCompletionDate("20230701")
                .schoolReportingRequirementCode(schoolReportingRequirementCode)
                .build();
    }

    private Student createMockStudent(String studentID) {
        return Student.builder()
                .studentID(studentID)
                .pen("123456789")
                .legalFirstName("Test")
                .legalLastName("Student")
                .dob("2000-01-01")
                .sexCode("M")
                .emailVerified("N")
                .build();
    }

    private GraduationStudentRecordEntity createMockGraduationStudentRecordEntity(UUID studentID, UUID schoolID, String studentStatus) {
        GraduationStudentRecordEntity entity = GraduationStudentRecordEntity.builder()
                .studentID(studentID)
                .pen("123456789")
                .studentGrade("12")
                .program("2023-EN")
                .schoolOfRecordId(schoolID)
                .build();
        entity.setStudentStatus(studentStatus);
        return entity;
    }
}