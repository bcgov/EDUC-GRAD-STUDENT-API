package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import ca.bc.gov.educ.api.gradstudent.model.mapper.StudentCourseMapper;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCourseRepository;
import ca.bc.gov.educ.api.gradstudent.validator.rules.StudentCourseRulesProcessor;
import com.nimbusds.jose.util.Pair;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

    public List<StudentCourse> getStudentCourses(UUID studentID) {
        if(studentID != null) {
            return studentCourseRepository.findByStudentID(studentID).stream().map(mapper::toStructure).toList();
        }
        return Collections.emptyList();
    }

    @Transactional
    public List<StudentCourseValidationIssue> saveStudentCourses(UUID studentID, List<StudentCourse> studentCourses, String accessToken, boolean isUpdate) {
        Map<String, StudentCourseValidationIssue> courseValidationIssues = new HashMap<>();
        List<StudentCourseValidationIssue> studentCourseResults = new ArrayList<>();
        List<StudentCourse> tobePersisted = new ArrayList<>();
        List<StudentCourse> existingStudentCourses = getStudentCourses(studentID);
        GraduationStudentRecord graduationStudentRecord = graduationStatusService.getGraduationStatus(studentID);
        List<Course> courses = courseService.getCourses(studentCourses.stream().map(StudentCourse::getCourseID).toList(), accessToken);
        List<ExaminableCourse> examinableCourses = courseService.getExaminableCourses(studentCourses.stream().map(StudentCourse::getCourseID).toList(), accessToken);
        List<LetterGrade> letterGrades = courseService.getLetterGrades(accessToken);
        studentCourses.forEach(studentCourse -> {
            StudentCourse existingStudentCourse = getExistingCourse(studentCourse, existingStudentCourses, isUpdate);
            Course course = courses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID())).findFirst().orElse(null);
            List<ExaminableCourse> coursesExaminable = examinableCourses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID())).toList();
            LetterGrade interimLetterGrade = letterGrades.stream().filter(x -> x.getGrade().equals(studentCourse.getInterimLetterGrade())).findFirst().orElse(null);
            LetterGrade finalLetterGrade = letterGrades.stream().filter(x -> x.getGrade().equals(studentCourse.getFinalLetterGrade())).findFirst().orElse(null);
            StudentCourseRuleData studentCourseRuleData = prepareStudentCourseRuleData(studentCourse, graduationStudentRecord, course, Pair.of(interimLetterGrade, finalLetterGrade), coursesExaminable);
            List<ValidationIssue> validationIssues = studentCourseRulesProcessor.processRules(studentCourseRuleData);
            boolean hasError = validationIssues.stream().anyMatch(issue -> "ERROR".equals(issue.getValidationIssueSeverityCode()));
            Long repCount = studentCourses.stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID()) && x.getCourseSession().equals(studentCourse.getCourseSession())).count();
            boolean allowSaveOrUpdate = repCount == 1 && ((existingStudentCourse != null && isUpdate) || (existingStudentCourse == null && !isUpdate));
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
    public void deleteStudentCourses(UUID studentID, List<UUID> studentCourseIDs) {
        if(!CollectionUtils.isEmpty(studentCourseIDs)) {
            studentCourseRepository.deleteAllById(studentCourseIDs);
        }
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
        StudentCourseValidationIssue studentCourseValidationIssue = new StudentCourseValidationIssue();
        studentCourseValidationIssue.setCourseID(studentCourse.getCourseID());
        studentCourseValidationIssue.setCourseSession(studentCourse.getCourseSession());
        studentCourseValidationIssue.setValidationIssues(validationIssues);
        return studentCourseValidationIssue;
    }

}
