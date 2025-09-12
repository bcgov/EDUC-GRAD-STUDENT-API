package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseActivityType;
import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseExamEntity;
import ca.bc.gov.educ.api.gradstudent.model.mapper.StudentCourseMapper;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCourseRepository;
import ca.bc.gov.educ.api.gradstudent.validator.rules.UpsertStudentCourseRulesProcessor;
import ca.bc.gov.educ.api.gradstudent.validator.rules.DeleteStudentCourseRulesProcessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.math.BigInteger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class StudentCourseService {

    private static final StudentCourseMapper mapper = StudentCourseMapper.mapper;
    private final StudentCourseRepository studentCourseRepository;
    private final UpsertStudentCourseRulesProcessor upsertStudentCourseRulesProcessor;
    private final DeleteStudentCourseRulesProcessor deleteStudentCourseRulesProcessor;
    private final GraduationStatusService graduationStatusService;
    private final CourseService courseService;
    private final HistoryService historyService;

    public List<StudentCourse> getStudentCourses(UUID studentID) {
        if (studentID != null) {
            List<StudentCourseEntity> studentCourseEntities = studentCourseRepository.findByStudentID(studentID);
            return studentCourseEntities.stream().map(mapper::toStructure).toList();
        }
        return Collections.emptyList();
    }

    public List<StudentCourseHistory> getStudentCourseHistory(UUID studentID) {
        if (studentID != null) {
            return historyService.getStudentCourseHistory(studentID);
        }
        return Collections.emptyList();
    }

    public List<StudentCourseValidationIssue> saveStudentCourses(UUID studentID, List<StudentCourse> studentCourses, boolean isUpdate) {
        List<StudentCourseValidationIssue> studentCourseResponse = new ArrayList<>();
        Map<String, StudentCourseValidationIssue> courseValidationIssues = new HashMap<>();
        List<StudentCourseEntity> tobePersisted = new ArrayList<>();

        List<StudentCourse> existingStudentCourses = getStudentCourses(studentID);
        GraduationStudentRecord graduationStudentRecord = graduationStatusService.getGraduationStatus(studentID);
        //Performance consideration: Consolidate and fetch all courses in a single call.
        log.info("Retrieving {} student courses from DB", existingStudentCourses.size());
        List<Course> courses = courseService.getCourses(studentCourses.stream()
                .flatMap(sc -> Stream.of(sc.getCourseID(), sc.getRelatedCourseId()))
                .filter(StringUtils::isNotBlank)
                .toList());
        log.info("Retrieved {} student courses", courses.size());
        StudentCourseActivityType activityCode = isUpdate ? StudentCourseActivityType.USERCOURSEMOD : StudentCourseActivityType.USERCOURSEADD;

        studentCourses.forEach(studentCourse -> {
            Course course = courses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID())).findFirst().orElse(null);
            Course relatedCourse = courses.stream().filter(x -> x.getCourseID().equals(studentCourse.getRelatedCourseId())).findFirst().orElse(null);
            StudentCourse existingStudentCourse = getExistingCourse(studentCourse, existingStudentCourses, isUpdate);
            //Check for duplicate course in the list of student courses
            boolean isUpsertAllowed = isUpsertAllowed(studentCourses, existingStudentCourses, studentCourse, isUpdate);
            if (!isUpsertAllowed) {
                studentCourseResponse.add(prepareInvalidCourseValidationIssue(studentCourse, existingStudentCourse, course, isUpdate));
            } else {
                //Perform validation checks
                StudentCourseRuleData studentCourseRuleData = prepareStudentCourseRuleData(studentCourse, graduationStudentRecord, course, relatedCourse, activityCode);
                List<ValidationIssue> validationIssues = upsertStudentCourseRulesProcessor.processRules(studentCourseRuleData);
                boolean hasError = validationIssues.stream().anyMatch(issue -> "ERROR".equals(issue.getValidationIssueSeverityCode()));
                if (!hasError) {
                    StudentCourseEntity studentCourseEntity = mapper.toEntity(studentCourse);
                    if (isUpdate && studentCourse.getId() != null && existingStudentCourse != null) {
                        studentCourseEntity.setCreateDate(existingStudentCourse.getCreateDate());
                        studentCourseEntity.setCreateUser(existingStudentCourse.getCreateUser());
                        if (studentCourse.getCourseExam() != null && existingStudentCourse.getCourseExam() != null) {
                            BeanUtils.copyProperties(existingStudentCourse.getCourseExam(), studentCourseEntity.getCourseExam(), "schoolPercentage", "bestSchoolPercentage", "bestExamPercentage", "specialCase", "updateUser", "updateDate");
                        }
                    }
                    tobePersisted.add(studentCourseEntity);
                }
                courseValidationIssues.put(studentCourse.getCourseID().concat(studentCourse.getCourseSession()), createCourseValidationIssue(studentCourse, course, validationIssues));
            }
        });
        //Persist the student courses if there are no validation issues
        persistAndCreateHistory(tobePersisted, studentID, isUpdate, courseValidationIssues);
        studentCourseResponse.addAll(courseValidationIssues.values().stream().toList());
        return studentCourseResponse;
    }

    private boolean isUpsertAllowed(List<StudentCourse> studentCourses, List<StudentCourse> existingStudentCourses, StudentCourse studentCourse, boolean isUpdate) {
        //Check for invalid course in the list of student courses
        Long dupeCount = studentCourses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID()) && x.getCourseSession().equals(studentCourse.getCourseSession())).count();
        if (dupeCount != 1) {
            return false; // More than one match or none — invalid for upsert
        }
        StudentCourse courseCodeLevelMatch = getExistingCourse(studentCourse, existingStudentCourses, false);
        if (isUpdate) {
            StudentCourse exactMatch = getExistingCourse(studentCourse, existingStudentCourses, true);
            return exactMatch != null && (exactMatch.getCourseExam() == null || studentCourse.getCourseExam() != null) &&
                    (courseCodeLevelMatch == null || courseCodeLevelMatch.getId().equals(exactMatch.getId())) ;
        }
        return courseCodeLevelMatch == null;
    }

    private StudentCourseValidationIssue prepareInvalidCourseValidationIssue(StudentCourse studentCourse, StudentCourse existingStudentCourse, Course course, boolean isUpdate) {
        StudentCourseValidationIssueTypeCode invalidTypeCode = determineInvalidTypeCode(studentCourse, existingStudentCourse, isUpdate);
        return createCourseValidationIssue(studentCourse, course, List.of(ValidationIssue.builder().validationIssueMessage(invalidTypeCode.getMessage()).validationFieldName(invalidTypeCode.getCode()).validationIssueSeverityCode(invalidTypeCode.getSeverityCode().getCode()).build()));
    }

    private StudentCourseValidationIssueTypeCode determineInvalidTypeCode(StudentCourse studentCourse, StudentCourse existingStudentCourse, boolean isUpdate) {
        if (isUpdate && existingStudentCourse == null) {
            return StudentCourseValidationIssueTypeCode.STUDENT_COURSE_UPDATE_NOT_FOUND;
        }
        if (existingStudentCourse != null &&
                existingStudentCourse.getCourseExam() != null &&
                studentCourse.getCourseExam() == null) {
            return StudentCourseValidationIssueTypeCode.STUDENT_COURSE_UPDATE_NOT_ALLOWED;
        }
        return StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DUPLICATE;
    }

    private Map<String, StudentCourseValidationIssue> persistAndCreateHistory(List<StudentCourseEntity> tobePersisted, UUID studentID, boolean isUpdate, Map<String, StudentCourseValidationIssue> courseValidationIssues) {
        if (!tobePersisted.isEmpty()) {
            StudentCourseActivityType activityCode = isUpdate ? StudentCourseActivityType.USERCOURSEMOD : StudentCourseActivityType.USERCOURSEADD;
            List<StudentCourseEntity> savedEntities = studentCourseRepository.saveAll(tobePersisted.stream().map(entity -> {
                entity.setStudentID(studentID);
                return entity;
            }).toList());
            createStudentCourseHistory(studentID, savedEntities, activityCode);
            savedEntities.forEach(entity -> {
                StudentCourseValidationIssue courseValidationIssue = courseValidationIssues.get(entity.getCourseID().toString().concat(entity.getCourseSession()));
                if (courseValidationIssue != null) {
                    courseValidationIssue.setId(entity.getId());
                    courseValidationIssue.setHasPersisted(true);
                }
            });
        }
        return courseValidationIssues;
    }

    private StudentCourse getExistingCourse(StudentCourse studentCourse, List<StudentCourse> existingStudentCourses, boolean isUpdate) {
        if (isUpdate) {
            return existingStudentCourses.stream().filter(x -> x.getId().equals(studentCourse.getId())).findFirst().orElse(null);
        }
        return existingStudentCourses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID()) && x.getCourseSession().equals(studentCourse.getCourseSession())).findFirst().orElse(null);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<StudentCourseValidationIssue> deleteStudentCourses(UUID studentID, List<UUID> studentCourseIDs) {
        if (CollectionUtils.isEmpty(studentCourseIDs)) return Collections.emptyList();
        Map<UUID, StudentCourseValidationIssue> courseValidationIssueMap = new HashMap<>();
        List<StudentCourseEntity> tobeDeleted = new ArrayList<>();
        List<StudentCourseEntity> existingStudentCourses = studentCourseRepository.findAllById(studentCourseIDs);
        if (existingStudentCourses.isEmpty()) {
            log.warn("No student courses found for deletion with IDs: {}", studentCourseIDs);
            throw new IllegalArgumentException("Invalid Student Course Ids: " + studentCourseIDs);
        }
        GraduationStudentRecord graduationStudentRecord = graduationStatusService.getGraduationStatus(studentID);
        StudentCourseRuleData studentCourseRuleData = new StudentCourseRuleData();
        studentCourseRuleData.setGraduationStudentRecord(graduationStudentRecord);
        List<ValidationIssue> ruleValidationIssues = deleteStudentCourseRulesProcessor.processRules(studentCourseRuleData);
        List<Course> courses = courseService.getCourses(existingStudentCourses.stream().map(StudentCourseEntity::getCourseID).map(BigInteger::toString).toList());
        existingStudentCourses.forEach(studentCourse -> {
            Course course = courses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID().toString())).findFirst().orElse(null);
            StudentCourseValidationIssue studentCourseValidationIssue = createCourseValidationIssue(
                    studentCourse.getId().toString(),
                    studentCourse.getCourseID().toString(),
                    studentCourse.getCourseSession(),
                    course,
                    ruleValidationIssues
            );
            if (isCourseExamDeleteRestricted(studentCourse)) {
                studentCourseValidationIssue.getValidationIssues().add(ValidationIssue.builder().validationIssueMessage(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_EXAM_VALID.getMessage()).validationIssueSeverityCode(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_EXAM_VALID.getSeverityCode().getCode()).validationFieldName(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_EXAM_VALID.getCode()).build());
            }
            courseValidationIssueMap.putIfAbsent(studentCourse.getId(), studentCourseValidationIssue);
            boolean hasError = studentCourseValidationIssue.getValidationIssues().stream().anyMatch(issue -> "ERROR".equals(issue.getValidationIssueSeverityCode()));
            if (!hasError) {
                tobeDeleted.add(studentCourse);
            }
        });
        deleteAndCreateHistory(tobeDeleted, studentID, courseValidationIssueMap);
        return courseValidationIssueMap.values().stream().toList();
    }

    @Transactional
    public List<ValidationIssue> transferStudentCourse(StudentCoursesTransferReq request) {
        List<ValidationIssue> validationIssues = new ArrayList<>();
        List<StudentCourseEntity> validEntities = new ArrayList<>();
        List<StudentCourseEntity> originalEntitiesForHistory = new ArrayList<>();

        List<StudentCourseEntity> existingStudentCourses = studentCourseRepository.findByStudentID(request.getTargetStudentId());
        assertStudentExists(request.getSourceStudentId(), "Source");
        assertStudentExists(request.getTargetStudentId(), "Target");

        List<UUID> courseIdsToMove = request.getStudentCourseIdsToMove();
        Map<UUID, StudentCourseEntity> studentCourseEntityMap = studentCourseRepository.findAllById(courseIdsToMove)
            .stream().collect(Collectors.toMap(StudentCourseEntity::getId, c -> c));

        for (UUID courseId : courseIdsToMove) {
            StudentCourseEntity studentCourse = studentCourseEntityMap.get(courseId);
            if (studentCourse == null) {
                validationIssues.add(buildValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_NOT_FOUND));
                continue;
            }
            List<ValidationIssue> courseIssues = validateCourseForTransfer(request, studentCourse, existingStudentCourses);
            if (courseIssues.isEmpty()) {
                StudentCourseEntity originalCopy = new StudentCourseEntity();
                BeanUtils.copyProperties(studentCourse, originalCopy);
                originalEntitiesForHistory.add(originalCopy);

                studentCourse.setStudentID(request.getTargetStudentId());
                validEntities.add(studentCourse);
            } else {
                validationIssues.addAll(courseIssues);
            }
        }
        if(!validEntities.isEmpty()) {
            studentCourseRepository.saveAll(validEntities);
            createStudentCourseHistory(request.getTargetStudentId(), validEntities, StudentCourseActivityType.USERCOURSEADD);
            createStudentCourseHistory(request.getSourceStudentId(), originalEntitiesForHistory, StudentCourseActivityType.USERCOURSEDEL);
        }

        return validationIssues;
    }

    private List<ValidationIssue> validateCourseForTransfer(
        StudentCoursesTransferReq request,
        StudentCourseEntity course,
        List<StudentCourseEntity> existingCourses
    ) {
        List<ValidationIssue> issues = new ArrayList<>();
        if (request.getSourceStudentId().equals(request.getTargetStudentId())) {
            issues.add(buildValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_TRANSFER_SAME_STUDENT));
        }
        if (!course.getStudentID().equals(request.getSourceStudentId())) {
            issues.add(buildValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_TRANSFER_STUDENT_COURSE_MISMATCH));
        }
        boolean courseExistsForTarget = existingCourses.stream().anyMatch(existing ->
            existing.getCourseID().equals(course.getCourseID()) &&
                existing.getCourseSession().equals(course.getCourseSession())
        );
        if (courseExistsForTarget) {
            issues.add(buildValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_TRANSFER_COURSE_DUPLICATE));
        }
        if (isCourseExamDeleteRestricted(course)) {
            issues.add(buildValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_EXAM_VALID));
        }
        return issues;
    }

    private void assertStudentExists(UUID studentId, String role) {
        try {
            graduationStatusService.getGraduationStatus(studentId);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException(role + " student not found: " + studentId);
        }
    }

    private Map<UUID, StudentCourseValidationIssue> deleteAndCreateHistory(List<StudentCourseEntity> tobeDeleted, UUID studentID, Map<UUID, StudentCourseValidationIssue> courseValidationIssues) {
        if (!tobeDeleted.isEmpty()) {
            studentCourseRepository.deleteAll(tobeDeleted);
            createStudentCourseHistory(studentID, tobeDeleted, StudentCourseActivityType.USERCOURSEDEL);
            tobeDeleted.forEach(studentCourseEntity -> {
                StudentCourseValidationIssue courseValidationIssue = courseValidationIssues.get(studentCourseEntity.getId());
                if (courseValidationIssue != null) {
                    courseValidationIssue.setHasPersisted(true);
                }
            });
        }
        return courseValidationIssues;
    }

    private void createStudentCourseHistory(UUID studentID, List<StudentCourseEntity> studentCourseEntities, StudentCourseActivityType historyActivityCode) {
        historyService.createStudentCourseHistory(studentCourseEntities, historyActivityCode);
        graduationStatusService.updateBatchFlagsForStudentByStatus(studentID);
    }

    private boolean isCourseExamDeleteRestricted(StudentCourseEntity studentCourseEntity) {
        if (studentCourseEntity.getCourseExam() != null) {
            StudentCourseExamEntity studentCourseExamEntity = studentCourseEntity.getCourseExam();
            if (studentCourseExamEntity.getExamPercentage() != null || (StringUtils.isNotBlank(studentCourseExamEntity.getSpecialCase()) && !"N".equals(studentCourseExamEntity.getSpecialCase()))) {
                return true;
            }
        }
        return false;
    }

    private StudentCourseRuleData prepareStudentCourseRuleData(StudentCourse studentCourse, GraduationStudentRecord graduationStudentRecord, Course course, Course relatedCourse, StudentCourseActivityType activityCode) {
        StudentCourseRuleData studentCourseRuleData = new StudentCourseRuleData();
        studentCourseRuleData.setGraduationStudentRecord(graduationStudentRecord);
        studentCourseRuleData.setStudentCourse(studentCourse);
        studentCourseRuleData.setCourse(course);
        studentCourseRuleData.setRelatedCourse(relatedCourse);
        studentCourseRuleData.setActivityType(activityCode);
        studentCourseRuleData.setIsSystemCoordinator(isSystemCoordinator());
        return studentCourseRuleData;
    }

    private boolean isSystemCoordinator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = auth.getAuthorities().stream().map(r -> r.getAuthority()).collect(Collectors.toSet());
        return roles.contains("GRAD_SYSTEM_COORDINATOR");
    }

    private StudentCourseValidationIssue createCourseValidationIssue(StudentCourse studentCourse, Course course, List<ValidationIssue> validationIssues) {
        return createCourseValidationIssue(studentCourse.getId() , studentCourse.getCourseID(), studentCourse.getCourseSession(), course, validationIssues);
    }

    private StudentCourseValidationIssue createCourseValidationIssue(String id, String courseId, String courseSession, Course course, List<ValidationIssue> validationIssues) {
        StudentCourseValidationIssue studentCourseValidationIssue = new StudentCourseValidationIssue();
        studentCourseValidationIssue.setId(StringUtils.isNotBlank(id) ? UUID.fromString(id) : null);
        studentCourseValidationIssue.setCourseID(courseId);
        studentCourseValidationIssue.setCourseSession(courseSession);
        if (course != null) {
            studentCourseValidationIssue.setCourseCode(course.getCourseCode());
            studentCourseValidationIssue.setCourseLevel(course.getCourseLevel());
        }
        studentCourseValidationIssue.setValidationIssues(new ArrayList<>(validationIssues));
        return studentCourseValidationIssue;
    }

    private ValidationIssue buildValidationIssue(StudentCourseValidationIssueTypeCode issueTypeCode) {
        return ValidationIssue.builder()
            .validationIssueMessage(issueTypeCode.getMessage())
            .validationFieldName(issueTypeCode.getCode())
            .validationIssueSeverityCode(issueTypeCode.getSeverityCode().getCode())
            .build();
    }

}
