package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.AssessmentCompletionCurrentStudentPage;
import ca.bc.gov.educ.api.gradstudent.model.dto.AssessmentCompletionCurrentStudentProjection;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentCompletionCurrentStudentServiceTest {
  private static final Sort DEFAULT_SORT = Sort.by(
    Sort.Order.asc("schoolOfRecordId"),
    Sort.Order.asc("legalLastName"),
    Sort.Order.asc("legalFirstName"),
    Sort.Order.asc("studentID")
  );

  @Mock
  private GraduationStudentRecordRepository graduationStudentRecordRepository;

  @Mock
  private RestUtils restUtils;

  @InjectMocks
  private AssessmentCompletionCurrentStudentService service;

  @Test
  void getCurrentStudents_forDistrict_filtersSchoolsAndMapsSchoolDetails() {
    UUID districtId = UUID.randomUUID();
    UUID schoolId = UUID.randomUUID();
    UUID graduationStudentRecordId = UUID.randomUUID();

    School publicSchool = School.builder()
      .schoolId(schoolId.toString())
      .districtId(districtId.toString())
      .schoolCategoryCode("PUBLIC")
      .displayName("Surrey Secondary")
      .mincode("12345678")
      .build();

    School independentSchool = School.builder()
      .schoolId(UUID.randomUUID().toString())
      .districtId(districtId.toString())
      .schoolCategoryCode("INDEPEND")
      .displayName("Other School")
      .mincode("87654321")
      .build();

    AssessmentCompletionCurrentStudentProjection projection = mock(AssessmentCompletionCurrentStudentProjection.class);
    when(projection.getGraduationStudentRecordId()).thenReturn(graduationStudentRecordId);
    when(projection.getSchoolOfRecordId()).thenReturn(schoolId);
    when(projection.getPen()).thenReturn("123456789");
    when(projection.getLastName()).thenReturn("DOE");
    when(projection.getFirstName()).thenReturn("JANE");
    when(projection.getMiddleName()).thenReturn("Q");
    when(projection.getDob()).thenReturn(LocalDateTime.of(2008, 1, 15, 0, 0));
    when(projection.getStudentGrade()).thenReturn("12");
    when(projection.getProgramCode()).thenReturn("2023-EN");

    when(restUtils.getSchoolList()).thenReturn(List.of(publicSchool, independentSchool));
    when(graduationStudentRecordRepository.findAssessmentCompletionCurrentStudentsBySchoolOfRecordIdInAndStudentStatus(
      eq(List.of(schoolId)),
      eq("CUR"),
      eq(PageRequest.of(0, 1000, DEFAULT_SORT))
    )).thenReturn(new SliceImpl<>(List.of(projection), PageRequest.of(0, 1000, DEFAULT_SORT), false));

    AssessmentCompletionCurrentStudentPage result = service.getCurrentStudents(null, districtId, "PUBLIC", 0, 1000);

    assertEquals(1, result.getContent().size());
    assertFalse(result.isHasNext());
    assertEquals(graduationStudentRecordId, result.getContent().get(0).getGraduationStudentRecordId());
    assertEquals(schoolId, result.getContent().get(0).getSchoolOfRecordId());
    assertEquals("Surrey Secondary", result.getContent().get(0).getSchoolOfRecordName());
    assertEquals("12345678", result.getContent().get(0).getSchoolOfRecordMincode());
    verify(graduationStudentRecordRepository).findAssessmentCompletionCurrentStudentsBySchoolOfRecordIdInAndStudentStatus(
      eq(List.of(schoolId)),
      eq("CUR"),
      eq(PageRequest.of(0, 1000, DEFAULT_SORT))
    );
  }

  @Test
  void getCurrentStudents_rejectsMissingOrConflictingScope() {
    assertThrows(IllegalArgumentException.class, () -> service.getCurrentStudents(null, null, null, 0, 1000));
    assertThrows(IllegalArgumentException.class, () -> service.getCurrentStudents(UUID.randomUUID(), UUID.randomUUID(), null, 0, 1000));
  }

  @Test
  void getCurrentStudents_returnsEmptyPageWhenDistrictHasNoMatchingSchools() {
    UUID districtId = UUID.randomUUID();
    School independentSchool = School.builder()
      .schoolId(UUID.randomUUID().toString())
      .districtId(districtId.toString())
      .schoolCategoryCode("INDEPEND")
      .build();

    when(restUtils.getSchoolList()).thenReturn(List.of(independentSchool));

    AssessmentCompletionCurrentStudentPage result = service.getCurrentStudents(null, districtId, "PUBLIC", 0, 1000);

    assertTrue(result.getContent().isEmpty());
    assertFalse(result.isHasNext());
  }
}
