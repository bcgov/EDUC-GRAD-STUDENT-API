package ca.bc.gov.educ.api.gradstudent.service.event;

import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.DemographicStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.student.v1.StudentUpdate;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordHistoryRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@ActiveProfiles("integration-test")
@SpringBootTest
class GraduationStudentRecordServiceTest {

    @Autowired
    private GraduationStudentRecordRepository graduationStudentRecordRepository;

    @Autowired
    private GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository;

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
                .statusCode("A")
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

    @ParameterizedTest
    @MethodSource("provideStatusMappingTestCases")
    @Transactional
    void testMapStudentStatusForUpdate_ReportedT_ShouldReturnExpectedStatus(
            String currentStatus, UUID schoolOfRecordId, UUID reportingSchoolId, String expectedStatus, String description) {
        // Given
        UUID studentID = UUID.randomUUID();
        
        DemographicStudent demStudent = createMockDemographicStudent("N", "CSF", "T", reportingSchoolId.toString());
        Student studentFromApi = createMockStudent(studentID.toString());
        GraduationStudentRecordEntity existingEntity = createMockGraduationStudentRecordEntity(studentID, schoolOfRecordId, currentStatus);
        
        // When
        var result = graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, existingEntity);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedStatus, result.getRight().getStudentStatus(), description);
    }

    private static Stream<Arguments> provideStatusMappingTestCases() {
        UUID schoolID = UUID.randomUUID();
        UUID reportingSchoolID = UUID.randomUUID();
        UUID differentSchoolID = UUID.randomUUID();
        
        return Stream.of(
            Arguments.of("TER", schoolID, schoolID, "TER", "Reported 'T' and GRAD Status = 'TER' → Store as TER"),
            Arguments.of("ARC", schoolID, schoolID, "ARC", "Reported 'T' and GRAD Status = 'ARC' → No update, keep status as ARC"),
            Arguments.of("CUR", schoolID, schoolID, "TER", "Reported 'T' and GRAD Status = 'CUR' and SoR = Reporting School → Store as TER"),
            Arguments.of("CUR", differentSchoolID, reportingSchoolID, "CUR", "Reported 'T' and GRAD Status = 'CUR' and SoR not = Reporting School → No update, keep status as CUR")
        );
    }

    @ParameterizedTest
    @MethodSource("provideUnexpectedStatusTestCases")
    @Transactional
    void testMapStudentStatusForUpdate_ReportedT_UnexpectedStatus_ShouldThrowException(String unexpectedStatus, String description) {
        // Given: Reported "T" with unexpected GRAD status → Should throw IllegalArgumentException
        UUID studentID = UUID.randomUUID();
        UUID schoolID = UUID.randomUUID();
        
        DemographicStudent demStudent = createMockDemographicStudent("N", "CSF", "T", schoolID.toString());
        Student studentFromApi = createMockStudent(studentID.toString());
        GraduationStudentRecordEntity existingEntity = createMockGraduationStudentRecordEntity(studentID, schoolID, unexpectedStatus);
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
            () -> graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, existingEntity),
            description);
    }

    private static Stream<Arguments> provideUnexpectedStatusTestCases() {
        return Stream.of(
            Arguments.of("MER", "Reported 'T' with unexpected GRAD status (MER) → Should throw IllegalArgumentException"),
            Arguments.of("DEC", "Reported 'T' with unexpected GRAD status (DEC) → Should throw IllegalArgumentException")
        );
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
    
    @ParameterizedTest
    @MethodSource("provideUpdateGradStatusTestCases")
    @Transactional
    void testUpdateGradStatus_IncomingA_WithActiveStatus_ShouldNotChangeStatus(
            String existingStatus, String description) {
        // Given: Incoming status "A" with existing active status (CUR, TER, ARC) → Should not change
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("A")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), existingStatus);
        String originalStatus = entity.getStudentStatus();
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then
        assertEquals(originalStatus, entity.getStudentStatus(), description);
    }

    private static Stream<Arguments> provideUpdateGradStatusTestCases() {
        return Stream.of(
            Arguments.of("CUR", "Incoming 'A' with existing 'CUR' (active) → Should not change status"),
            Arguments.of("TER", "Incoming 'A' with existing 'TER' (active) → Should not change status"),
            Arguments.of("ARC", "Incoming 'A' with existing 'ARC' (active) → Should not change status")
        );
    }

    @Test
    @Transactional
    void testUpdateGradStatus_IncomingA_WithNonActiveStatusMER_ShouldFindPreviousActiveStatus() {
        // Given: Incoming status "A" with existing non-active status "MER" → Should find previous active status from history
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("A")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "MER");
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // Create history records: MER (current), then TER (active status to restore)
        List<GraduationStudentRecordHistoryEntity> historyRecords = new ArrayList<>();
        GraduationStudentRecordHistoryEntity history1 = createHistoryEntity(studentID, "MER", LocalDateTime.now().minusDays(1));
        GraduationStudentRecordHistoryEntity history2 = createHistoryEntity(studentID, "TER", LocalDateTime.now().minusDays(2));
        historyRecords.add(history1);
        historyRecords.add(history2);
        
        // Save history records
        graduationStudentRecordHistoryRepository.saveAll(historyRecords);
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then: Should restore to TER (previous active status)
        assertEquals("TER", entity.getStudentStatus(), "Incoming 'A' with existing 'MER' → Should restore to previous active status 'TER'");
    }

    @Test
    @Transactional
    void testUpdateGradStatus_IncomingA_WithNonActiveStatusDEC_ShouldFindPreviousActiveStatus() {
        // Given: Incoming status "A" with existing non-active status "DEC" → Should find previous active status from history
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("A")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "DEC");
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // Create history records: DEC (current), then CUR (active status to restore)
        List<GraduationStudentRecordHistoryEntity> historyRecords = new ArrayList<>();
        GraduationStudentRecordHistoryEntity history1 = createHistoryEntity(studentID, "DEC", LocalDateTime.now().minusDays(1));
        GraduationStudentRecordHistoryEntity history2 = createHistoryEntity(studentID, "CUR", LocalDateTime.now().minusDays(2));
        historyRecords.add(history1);
        historyRecords.add(history2);
        
        // Save history records
        graduationStudentRecordHistoryRepository.saveAll(historyRecords);
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then: Should restore to CUR (previous active status)
        assertEquals("CUR", entity.getStudentStatus(), "Incoming 'A' with existing 'DEC' → Should restore to previous active status 'CUR'");
    }

    @Test
    @Transactional
    void testUpdateGradStatus_IncomingA_WithNonActiveStatus_NoPreviousActiveStatus_ShouldReturnCUR() {
        // Given: Incoming status "A" with existing non-active status, but no previous active status in history → Should return CUR
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("A")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "MER");
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // Create history records: MER (current), but no active status before it
        List<GraduationStudentRecordHistoryEntity> historyRecords = new ArrayList<>();
        GraduationStudentRecordHistoryEntity history1 = createHistoryEntity(studentID, "MER", LocalDateTime.now().minusDays(1));
        historyRecords.add(history1);
        
        // Save history records
        graduationStudentRecordHistoryRepository.saveAll(historyRecords);
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then: Should default to CUR
        assertEquals("CUR", entity.getStudentStatus(), "Incoming 'A' with existing 'MER' and no previous active status → Should default to 'CUR'");
    }

    @Test
    @Transactional
    void testUpdateGradStatus_IncomingA_WithNonActiveStatus_EmptyHistory_ShouldReturnCUR() {
        // Given: Incoming status "A" with existing non-active status, but empty history → Should return CUR
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("A")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "DEC");
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // No history records
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then: Should default to CUR
        assertEquals("CUR", entity.getStudentStatus(), "Incoming 'A' with existing 'DEC' and empty history → Should default to 'CUR'");
    }

    @Test
    @Transactional
    void testUpdateGradStatus_IncomingT_ShouldSetStatusDirectly() {
        // Given: Incoming status "T" (not "A") → Should set status directly
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("T")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "CUR");
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then: Should set to incoming status
        assertEquals("TER", entity.getStudentStatus(), "Incoming 'T' → Should set status to 'TER' directly");
    }

    @Test
    @Transactional
    void testUpdateGradStatus_IncomingD_ShouldSetStatusDirectly() {
        // Given: Incoming status "D" (not "A") → Should set status directly
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("D")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "CUR");
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then: Should set to incoming status
        assertEquals("DEC", entity.getStudentStatus(), "Incoming 'D' → Should set status to 'DEC' directly");
    }

    // Unit tests for findPreviousActiveStatus method (lines 129-145)

    @Test
    @Transactional
    void testFindPreviousActiveStatus_HistoryContainsCurrentStatus_WithActiveStatusAfter_ShouldReturnActiveStatus() {
        // Given: History contains currentStatus and has active status after it → Should return that active status
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("A")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "MER");
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // Create history records in descending order (newest first): MER, TER (active), CUR (active)
        List<GraduationStudentRecordHistoryEntity> historyRecords = new ArrayList<>();
        GraduationStudentRecordHistoryEntity history1 = createHistoryEntity(studentID, "MER", LocalDateTime.now().minusDays(1));
        GraduationStudentRecordHistoryEntity history2 = createHistoryEntity(studentID, "TER", LocalDateTime.now().minusDays(2));
        GraduationStudentRecordHistoryEntity history3 = createHistoryEntity(studentID, "CUR", LocalDateTime.now().minusDays(3));
        historyRecords.add(history1);
        historyRecords.add(history2);
        historyRecords.add(history3);
        
        // Save history records
        graduationStudentRecordHistoryRepository.saveAll(historyRecords);
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then: Should return TER (first active status found after MER)
        assertEquals("TER", entity.getStudentStatus(), "History contains MER, then TER (active) → Should return 'TER'");
    }

    @Test
    @Transactional
    void testFindPreviousActiveStatus_HistoryContainsCurrentStatus_NoActiveStatusAfter_ShouldReturnCUR() {
        // Given: History contains currentStatus but no active status after it → Should return CURRENT
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("A")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "MER");
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // Create history records: MER, DEC (non-active), MER (non-active)
        List<GraduationStudentRecordHistoryEntity> historyRecords = new ArrayList<>();
        GraduationStudentRecordHistoryEntity history1 = createHistoryEntity(studentID, "MER", LocalDateTime.now().minusDays(1));
        GraduationStudentRecordHistoryEntity history2 = createHistoryEntity(studentID, "DEC", LocalDateTime.now().minusDays(2));
        GraduationStudentRecordHistoryEntity history3 = createHistoryEntity(studentID, "MER", LocalDateTime.now().minusDays(3));
        historyRecords.add(history1);
        historyRecords.add(history2);
        historyRecords.add(history3);
        
        // Save history records
        graduationStudentRecordHistoryRepository.saveAll(historyRecords);
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then: Should return CUR (default)
        assertEquals("CUR", entity.getStudentStatus(), "History contains MER but no active status after → Should return 'CUR'");
    }

    @Test
    @Transactional
    void testFindPreviousActiveStatus_HistoryDoesNotContainCurrentStatus_ShouldReturnCUR() {
        // Given: History doesn't contain currentStatus → Should return CURRENT
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("A")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "MER");
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // Create history records that don't contain MER
        List<GraduationStudentRecordHistoryEntity> historyRecords = new ArrayList<>();
        GraduationStudentRecordHistoryEntity history1 = createHistoryEntity(studentID, "CUR", LocalDateTime.now().minusDays(1));
        GraduationStudentRecordHistoryEntity history2 = createHistoryEntity(studentID, "TER", LocalDateTime.now().minusDays(2));
        historyRecords.add(history1);
        historyRecords.add(history2);
        
        // Save history records
        graduationStudentRecordHistoryRepository.saveAll(historyRecords);
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then: Should return CUR (default)
        assertEquals("CUR", entity.getStudentStatus(), "History doesn't contain currentStatus 'MER' → Should return 'CUR'");
    }

    @Test
    @Transactional
    void testFindPreviousActiveStatus_EmptyHistory_ShouldReturnCUR() {
        // Given: Empty history → Should return CURRENT
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("A")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "DEC");
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // No history records
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then: Should return CUR (default)
        assertEquals("CUR", entity.getStudentStatus(), "Empty history → Should return 'CUR'");
    }

    @Test
    @Transactional
    void testFindPreviousActiveStatus_MultipleActiveStatuses_ShouldReturnFirstActiveAfterCurrent() {
        // Given: History with multiple active statuses → Should return first active status found after currentStatus
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("123456789")
                .statusCode("A")
                .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "MER");
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        
        // Create history records: MER, TER (active - should be returned), ARC (active - should not be returned)
        List<GraduationStudentRecordHistoryEntity> historyRecords = new ArrayList<>();
        GraduationStudentRecordHistoryEntity history1 = createHistoryEntity(studentID, "MER", LocalDateTime.now().minusDays(1));
        GraduationStudentRecordHistoryEntity history2 = createHistoryEntity(studentID, "TER", LocalDateTime.now().minusDays(2));
        GraduationStudentRecordHistoryEntity history3 = createHistoryEntity(studentID, "ARC", LocalDateTime.now().minusDays(3));
        historyRecords.add(history1);
        historyRecords.add(history2);
        historyRecords.add(history3);
        
        // Save history records
        graduationStudentRecordHistoryRepository.saveAll(historyRecords);
        
        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);
        
        // Then: Should return TER (first active status found after MER)
        assertEquals("TER", entity.getStudentStatus(), "History contains MER, then TER (active), then ARC (active) → Should return first active 'TER'");
    }
    @Test
    @Transactional
    void testFindPreviousActiveStatus_CaseInsensitiveStatusMatch_ShouldWork() {
        // Given: History contains currentStatus with different case → Should still match (case-insensitive)
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
            .pen("123456789")
            .statusCode("A")
            .build();
        GraduationStudentRecordEntity entity = createMockGraduationStudentRecordEntity(studentID, UUID.randomUUID(), "mer"); // lowercase
        GradStatusEvent event = GradStatusEvent.builder()
            .updateUser("Test")
            .build();

        // Create history records: MER (uppercase), then TER (active)
        List<GraduationStudentRecordHistoryEntity> historyRecords = new ArrayList<>();
        GraduationStudentRecordHistoryEntity history1 = createHistoryEntity(studentID, "MER", LocalDateTime.now().minusDays(1));
        GraduationStudentRecordHistoryEntity history2 = createHistoryEntity(studentID, "TER", LocalDateTime.now().minusDays(2));
        historyRecords.add(history1);
        historyRecords.add(history2);

        // Save history records
        graduationStudentRecordHistoryRepository.saveAll(historyRecords);

        // When
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, entity, event);

        // Then: Should match case-insensitively and return TER
        assertEquals("TER", entity.getStudentStatus(), "Case-insensitive match: 'mer' matches 'MER' in history → Should return 'TER'");
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

    private GraduationStudentRecordHistoryEntity createHistoryEntity(UUID studentID, String studentStatus, LocalDateTime updateDate) {
        GraduationStudentRecordHistoryEntity history = new GraduationStudentRecordHistoryEntity();
        history.setStudentID(studentID);
        history.setStudentStatus(studentStatus);
        history.setUpdateDate(updateDate);
        history.setCreateDate(updateDate);
        history.setCreateUser("Test");
        history.setUpdateUser("Test");
        return history;
    }
}