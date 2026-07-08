package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.AssessmentCompletionCurrentStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.AssessmentCompletionCurrentStudentPage;
import ca.bc.gov.educ.api.gradstudent.model.dto.AssessmentCompletionCurrentStudentProjection;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentCompletionCurrentStudentService {
  private static final String CURRENT_STUDENT_STATUS = "CUR";
  private static final int DEFAULT_PAGE_SIZE = 2000;
  private static final int MAX_PAGE_SIZE = 5000;

  private final GraduationStudentRecordRepository graduationStudentRecordRepository;
  private final RestUtils restUtils;

  @Transactional(readOnly = true)
  public AssessmentCompletionCurrentStudentPage getCurrentStudents(
    final UUID schoolId,
    final UUID districtId,
    final String schoolCategoryCode,
    final Integer pageNumber,
    final Integer pageSize
  ) {
    validateScope(schoolId, districtId);

    final List<School> schools = restUtils.getSchoolList();
    final List<UUID> schoolIds = resolveSchoolIds(schools, schoolId, districtId, schoolCategoryCode);
    final int resolvedPageNumber = Math.max(pageNumber == null ? 0 : pageNumber, 0);
    final int resolvedPageSize = normalizePageSize(pageSize);

    if (schoolIds.isEmpty()) {
      return AssessmentCompletionCurrentStudentPage.builder()
        .content(Collections.emptyList())
        .pageNumber(resolvedPageNumber)
        .pageSize(resolvedPageSize)
        .numberOfElements(0)
        .hasNext(false)
        .build();
    }

    final Pageable pageable = PageRequest.of(
      resolvedPageNumber,
      resolvedPageSize,
      Sort.by(
        Sort.Order.asc("schoolOfRecordId"),
        Sort.Order.asc("legalLastName"),
        Sort.Order.asc("legalFirstName"),
        Sort.Order.asc("studentID")
      )
    );
    final Slice<AssessmentCompletionCurrentStudentProjection> slice =
      graduationStudentRecordRepository.findAssessmentCompletionCurrentStudentsBySchoolOfRecordIdInAndStudentStatus(
        schoolIds,
        CURRENT_STUDENT_STATUS,
        pageable
      );

    final Map<String, School> schoolsById = schools.stream()
      .filter(school -> StringUtils.isNotBlank(school.getSchoolId()))
      .collect(Collectors.toMap(School::getSchoolId, school -> school, (existing, replacement) -> existing, LinkedHashMap::new));

    final List<AssessmentCompletionCurrentStudent> content = slice.getContent().stream()
      .map(student -> toDto(student, schoolsById))
      .toList();

    return AssessmentCompletionCurrentStudentPage.builder()
      .content(content)
      .pageNumber(resolvedPageNumber)
      .pageSize(resolvedPageSize)
      .numberOfElements(content.size())
      .hasNext(slice.hasNext())
      .build();
  }

  private AssessmentCompletionCurrentStudent toDto(
    final AssessmentCompletionCurrentStudentProjection student,
    final Map<String, School> schoolsById
  ) {
    final UUID schoolOfRecordId = student.getSchoolOfRecordId();
    final School school = schoolOfRecordId == null
      ? null
      : schoolsById.get(schoolOfRecordId.toString());

    return AssessmentCompletionCurrentStudent.builder()
      .graduationStudentRecordId(student.getGraduationStudentRecordId())
      .schoolOfRecordId(schoolOfRecordId)
      .schoolOfRecordName(school != null ? school.getDisplayName() : null)
      .schoolOfRecordMincode(school != null ? school.getMincode() : null)
      .pen(student.getPen())
      .lastName(student.getLastName())
      .firstName(student.getFirstName())
      .middleName(student.getMiddleName())
      .dob(student.getDob())
      .studentGrade(student.getStudentGrade())
      .programCode(student.getProgramCode())
      .build();
  }

  private List<UUID> resolveSchoolIds(
    final List<School> schools,
    final UUID schoolId,
    final UUID districtId,
    final String schoolCategoryCode
  ) {
    if (schoolId != null) {
      return List.of(schoolId);
    }

    return schools.stream()
      .filter(school -> StringUtils.equalsIgnoreCase(school.getDistrictId(), districtId.toString()))
      .filter(school -> StringUtils.isBlank(schoolCategoryCode) || StringUtils.equalsIgnoreCase(schoolCategoryCode, school.getSchoolCategoryCode()))
      .map(School::getSchoolId)
      .filter(StringUtils::isNotBlank)
      .map(this::toUuid)
      .filter(Objects::nonNull)
      .distinct()
      .toList();
  }

  private void validateScope(final UUID schoolId, final UUID districtId) {
    if ((schoolId == null && districtId == null) || (schoolId != null && districtId != null)) {
      throw new IllegalArgumentException("Provide exactly one of schoolId or districtId.");
    }
  }

  private int normalizePageSize(final Integer pageSize) {
    if (pageSize == null || pageSize <= 0) {
      return DEFAULT_PAGE_SIZE;
    }
    return Math.min(pageSize, MAX_PAGE_SIZE);
  }

  private UUID toUuid(final String value) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
