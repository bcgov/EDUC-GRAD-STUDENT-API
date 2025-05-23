package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseActivityType;
import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import ca.bc.gov.educ.api.gradstudent.model.mapper.StudentCourseMapper;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCourseRepository;
import ca.bc.gov.educ.api.gradstudent.util.JsonTransformer;
import ca.bc.gov.educ.api.gradstudent.validator.rules.StudentCourseRulesProcessor;
import com.nimbusds.jose.util.Pair;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.math.BigInteger;

import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class StudentCourseService {

    private static final StudentCourseMapper mapper = StudentCourseMapper.mapper;
    private final StudentCourseRepository studentCourseRepository;
    private final StudentCourseRulesProcessor studentCourseRulesProcessor;
    private final GraduationStatusService graduationStatusService;
    private final CourseService courseService;
    private final HistoryService historyService;
    private final JsonTransformer jsonTransformer;

    public List<StudentCourse> getStudentCourses(UUID studentID) {
        if(studentID != null) {
            return studentCourseRepository.findByStudentID(studentID).stream().map(mapper::toStructure).toList();
        }
        return Collections.emptyList();
    }

    public List<StudentCourseHistory> getStudentCourseHistory(UUID studentID) {
        if(studentID != null) {
            return historyService.getStudentCourseHistory(studentID);
        }
        return Collections.emptyList();
    }

    @Transactional
    public List<StudentCourseValidationIssue> saveStudentCourses(UUID studentID, List<StudentCourse> studentCourses, boolean isUpdate) {
        Map<String, StudentCourseValidationIssue> courseValidationIssues = new HashMap<>();
        List<StudentCourseValidationIssue> studentCourseResults = new ArrayList<>();
        List<StudentCourse> tobePersisted = new ArrayList<>();
        List<StudentCourse> existingStudentCourses = getStudentCourses(studentID);
        GraduationStudentRecord graduationStudentRecord = graduationStatusService.getGraduationStatus(studentID);
        List<Course> courses = courseService.getCourses(studentCourses.stream().map(StudentCourse::getCourseID).toList());
        List<ExaminableCourse> examinableCourses = courseService.getExaminableCourses(studentCourses.stream().map(StudentCourse::getCourseID).toList());
        List<LetterGrade> letterGrades = courseService.getLetterGrades();
        String activityCode = isUpdate ? StudentCourseActivityType.USERCOURSEMOD.name() : StudentCourseActivityType.USERCOURSEADD.name();
        studentCourses.forEach(studentCourse -> {
            StudentCourse existingStudentCourse = getExistingCourse(studentCourse, existingStudentCourses, isUpdate);
            Course course = courses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID())).findFirst().orElse(null);
            List<ExaminableCourse> coursesExaminable = examinableCourses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID())).toList();
            LetterGrade interimLetterGrade = letterGrades.stream().filter(x -> x.getGrade().equals(studentCourse.getInterimLetterGrade())).findFirst().orElse(null);
            LetterGrade finalLetterGrade = letterGrades.stream().filter(x -> x.getGrade().equals(studentCourse.getFinalLetterGrade())).findFirst().orElse(null);
            StudentCourseRuleData studentCourseRuleData = prepareStudentCourseRuleData(studentCourse, graduationStudentRecord, course, Pair.of(interimLetterGrade, finalLetterGrade), coursesExaminable);
            List<ValidationIssue> validationIssues = studentCourseRulesProcessor.processRules(studentCourseRuleData);
            boolean hasError = validationIssues.stream().anyMatch(issue -> "ERROR".equals(issue.getValidationIssueSeverityCode()));
            Long dupeCount = studentCourses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID()) && x.getCourseSession().equals(studentCourse.getCourseSession())).count();
            boolean allowSaveOrUpdate = dupeCount == 1 && ((existingStudentCourse != null && isUpdate) || (existingStudentCourse == null && !isUpdate));
            if(!hasError && allowSaveOrUpdate) {
                tobePersisted.add(studentCourse);
            }
            if(allowSaveOrUpdate) {
                courseValidationIssues.put(studentCourse.getCourseID().concat(studentCourse.getCourseSession()), createCourseValidationIssue(studentCourse, validationIssues));
            } else {
                //Duplicate in DB or request itself
                studentCourseResults.add(validateForInvalidData(studentCourse, existingStudentCourse, isUpdate));
            }
        });
        if(!tobePersisted.isEmpty()) {
                List<StudentCourseEntity> savedEntities = studentCourseRepository.saveAll(tobePersisted.stream().map(mapper::toEntity).map(entity -> {
                            entity.setStudentID(studentID);
                            return entity;
                        }).toList());
                createStudentCourseHistory(studentID, savedEntities, activityCode);
                savedEntities.forEach(entity -> {
                    StudentCourseValidationIssue courseValidationIssue = courseValidationIssues.get(entity.getCourseID().toString().concat(entity.getCourseSession()));
                    if (courseValidationIssue != null) {
                        courseValidationIssue.setHasPersisted(true);
                    }
                });
        }
        studentCourseResults.addAll(courseValidationIssues.values().stream().toList());
        return studentCourseResults;
    }

    private StudentCourse getExistingCourse(StudentCourse studentCourse, List<StudentCourse> existingStudentCourses, boolean isUpdate) {
        if(isUpdate) {
            return existingStudentCourses.stream().filter(x -> x.getId().equals(studentCourse.getId())).findFirst().orElse(null);
        }
        return existingStudentCourses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID()) && x.getCourseSession().equals(studentCourse.getCourseSession())).findFirst().orElse(null);
    }

    private  StudentCourseValidationIssue validateForInvalidData(StudentCourse studentCourse, StudentCourse existingStudentCourse, boolean isUpdate) {
        StudentCourseValidationIssueTypeCode invalidTypeCode = existingStudentCourse == null && isUpdate ? StudentCourseValidationIssueTypeCode.STUDENT_COURSE_UPDATE_NOT_FOUND : StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DUPLICATE;
        return createCourseValidationIssue(studentCourse, List.of(ValidationIssue.builder().validationIssueMessage(invalidTypeCode.getMessage()).validationFieldName(invalidTypeCode.getCode()).validationIssueSeverityCode(invalidTypeCode.getSeverityCode().getCode()).build()));
    }

    @Transactional
    public List<StudentCourseValidationIssue> deleteStudentCourses(UUID studentID, List<UUID> studentCourseIDs) {
        Map<UUID, StudentCourseValidationIssue> courseValidationIssues = new HashMap<>();
        if(CollectionUtils.isEmpty(studentCourseIDs)) return Collections.emptyList();
        GraduationStudentRecord graduationStudentRecord = graduationStatusService.getGraduationStatus(studentID);
        List<StudentCourseEntity> existingStudentCourses = studentCourseRepository.findAllById(studentCourseIDs);
        List<Course> courses = courseService.getCourses(existingStudentCourses.stream().map(StudentCourseEntity::getCourseID).map(BigInteger::toString).toList());
        if(StringUtils.isNotBlank(graduationStudentRecord.getProgramCompletionDate())) {
            GraduationDataOptionalDetails graduationDataOptionalDetails = getGraduationStatusWithOptionalDetails(graduationStudentRecord);
            for(StudentCourseEntity studentCourse: existingStudentCourses) {
                Course course = courses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID().toString())).findFirst().orElse(null);
                courseValidationIssues.put(studentCourse.getId(), createCourseValidationIssue(studentCourse.getCourseID().toString(), studentCourse.getCourseSession(), new ArrayList<>()));
                if(graduationDataOptionalDetails != null && course != null && isCourseUsedForGraduation(course, graduationDataOptionalDetails)) {
                    StudentCourseValidationIssueTypeCode invalidTypeCode = StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_VALID;
                    courseValidationIssues.put(studentCourse.getId(), createCourseValidationIssue(studentCourse.getCourseID().toString(), studentCourse.getCourseSession(), List.of(ValidationIssue.builder().validationIssueMessage(invalidTypeCode.getMessage()).validationFieldName(invalidTypeCode.getCode()).validationIssueSeverityCode(invalidTypeCode.getSeverityCode().getCode()).build())));
                }
            }
        }
        studentCourseRepository.deleteAllById(studentCourseIDs);
        createStudentCourseHistory(studentID, existingStudentCourses, StudentCourseActivityType.USERCOURSEDEL.name());
        courseValidationIssues.values().forEach(x -> x.setHasPersisted(true));
        return courseValidationIssues.values().stream().toList();
    }

    private void createStudentCourseHistory(UUID studentID, List<StudentCourseEntity> studentCourseEntities , String historyActivityCode) {
        historyService.createStudentCourseHistory(studentCourseEntities, historyActivityCode);
        graduationStatusService.updateBatchFlagsForStudentCourses(studentID);
    }

    private boolean isCourseUsedForGraduation(Course course, GraduationDataOptionalDetails graduationDataOptionalDetails) {
        for (GradStudentOptionalStudentProgram gradStudentOptionalStudentProgram : graduationDataOptionalDetails.getOptionalGradStatus()) {
            for (OptionalStudentCourse optionalStudentCourse : gradStudentOptionalStudentProgram.getOptionalStudentCourses().getStudentCourseList()) {
                String optionalExternalCode = StringUtils.isNotBlank(optionalStudentCourse.getCourseLevel()) ? optionalStudentCourse.getCourseCode().concat(" ").concat(optionalStudentCourse.getCourseLevel()) : optionalStudentCourse.getCourseCode();
                String courseExternalCode = StringUtils.isNotBlank(course.getCourseLevel()) ? course.getCourseCode().concat(" ").concat(course.getCourseLevel()) : course.getCourseCode();
                if (optionalExternalCode.equals(courseExternalCode) && optionalStudentCourse.isUsed()) {
                    return true;
                }
            }
        }
        return false;
    }

    private GraduationDataOptionalDetails getGraduationStatusWithOptionalDetails(GraduationStudentRecord graduationStudentRecord) {
        if(graduationStudentRecord.getStudentGradData() != null) {
            return (GraduationDataOptionalDetails) jsonTransformer.unmarshall(graduationStudentRecord.getStudentGradData(), GraduationDataOptionalDetails.class);
        }
        return null;
    }

    private StudentCourseRuleData prepareStudentCourseRuleData(StudentCourse studentCourse, GraduationStudentRecord graduationStudentRecord, Course course, Pair<LetterGrade,LetterGrade> letterGrade, List<ExaminableCourse> examinableCourses) {
        StudentCourseRuleData studentCourseRuleData= new StudentCourseRuleData();
        studentCourseRuleData.setGraduationStudentRecord(graduationStudentRecord);
        studentCourseRuleData.setStudentCourse(studentCourse);
        studentCourseRuleData.setCourse(course);
        studentCourseRuleData.setInterimLetterGrade(letterGrade.getLeft());
        studentCourseRuleData.setFinalLetterGrade(letterGrade.getRight());
        studentCourseRuleData.setExaminableCourses(examinableCourses);
        return studentCourseRuleData;
    }

    private StudentCourseValidationIssue  createCourseValidationIssue(StudentCourse studentCourse, List<ValidationIssue> validationIssues){
        return createCourseValidationIssue(studentCourse.getCourseID(), studentCourse.getCourseSession(), validationIssues);
    }

    private StudentCourseValidationIssue  createCourseValidationIssue(String courseID, String courseSession, List<ValidationIssue> validationIssues){
        StudentCourseValidationIssue studentCourseValidationIssue = new StudentCourseValidationIssue();
        studentCourseValidationIssue.setCourseID(courseID);
        studentCourseValidationIssue.setCourseSession(courseSession);
        studentCourseValidationIssue.setValidationIssues(validationIssues);
        return studentCourseValidationIssue;
    }

}
