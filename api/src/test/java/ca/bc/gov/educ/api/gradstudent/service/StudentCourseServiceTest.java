package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseActivityType;
import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCourseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class StudentCourseServiceTest  extends BaseIntegrationTest {

    @MockBean StudentCourseRepository studentCourseRepository;

    @MockBean GraduationStudentRecordRepository graduationStatusRepository;

    @Autowired StudentCourseService studentCourseService;
    @Autowired GraduationStatusService graduationStatusService;
    @MockBean CourseService courseService;
    @MockBean HistoryService historyService;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @Test
    public void testGetStudentCourses() {
        UUID studentID = UUID.randomUUID();
        StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID,"1","202404");
        Mockito.when(studentCourseRepository.findByStudentID(studentID)).thenReturn(Arrays.asList(studentCourseEntity));
        var result = studentCourseService.getStudentCourses(studentID) ;
        assertNotNull(result);
        assertThat(result).isNotEmpty().hasSize(1);
    }

    @Test
    public void testGetStudentCourses_NoData() {
        UUID studentID = UUID.randomUUID();
        StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID,"1","202404");
        Mockito.when(studentCourseRepository.findByStudentID(studentID)).thenReturn(Arrays.asList(studentCourseEntity));
        var result = studentCourseService.getStudentCourses(UUID.randomUUID()) ;
        assertNotNull(result);
        assertThat(result).isEmpty();
    }

    @Test
    public void testCreateStudentCourses_CUR() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertThat(result).isNotEmpty().hasSize(2);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());

    }

    @Test
    public void testUpdateStudentCourses_CUR() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID);
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertThat(result).isNotEmpty().hasSize(2);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());

    }

    @Test
    public void testCreateStudentCourses_CUR_Duplicate() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();

        List<StudentCourseEntity> existingStudentCourseEntities = new ArrayList<>();
        existingStudentCourseEntities.add(createStudentCourseEntity(studentID, studentCourses.get("studentCourse1").getCourseID(), studentCourses.get("studentCourse1").getCourseSession()));

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "");
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(studentCourseRepository.findByStudentID(any(UUID.class))).thenReturn(existingStudentCourseEntities);
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DUPLICATE.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());
    }

    @Test
    public void testCreateStudentCourses_MER() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("MER", "");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertThat(result).isNotEmpty().hasSize(2);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_MER.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_MER.getMessage())).findFirst().isPresent());
    }

    @Test
    public void testCreateStudentCourses_TER_1996() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("TER", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_TER.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse4").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse4").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse5").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse5").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse6").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse6").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse7").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse7").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse8").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse8").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse10").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse10").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse11").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse11").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse12").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse12").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testCreateStudentCourses_ARC_1996() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("ARC", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_ARC.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse4").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse4").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse5").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse5").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse6").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse6").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse7").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse7").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse8").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse8").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse10").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse10").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse11").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse11").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse12").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse12").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testCreateStudentCourses_DEC_1996() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("DEC", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_DEC.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse4").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse4").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse5").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse5").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse6").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse6").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse7").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse7").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse8").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse8").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse10").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse10").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse11").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse11").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse12").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse12").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testCreateStudentCourses_TER_2004() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("TER", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_TER.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testCreateStudentCourses_ARC_2004() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("ARC", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_ARC.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testCreateStudentCourses_DEC_2004() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("DEC", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_DEC.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testUpdateStudentCourses_MER() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID);
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("MER", "");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertThat(result).isNotEmpty().hasSize(2);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_MER.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_MER.getMessage())).findFirst().isPresent());
    }

    @Test
    public void testUpdateStudentCourses_TER_1996() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID);
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("TER", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_TER.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse4").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse4").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse5").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse5").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse6").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse6").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse7").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse7").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse8").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse8").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse10").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse10").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse11").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse11").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse12").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse12").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testUpdateStudentCourses_ARC_1996() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID);
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("ARC", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_ARC.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse4").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse4").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse5").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse5").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse6").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse6").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse7").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse7").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse8").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse8").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse10").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse10").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse11").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse11").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse12").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse12").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testUpdateStudentCourses_DEC_1996() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID);
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("DEC", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_DEC.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse3").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse3").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse4").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse4").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse5").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse5").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse6").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse6").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse7").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse7").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse8").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse8").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse10").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse10").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse11").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse11").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse12").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse12").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testUpdateStudentCourses_TER_2004() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID);
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("TER", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_TER.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testUpdateStudentCourses_ARC_2004() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID);
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("ARC", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_ARC.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testUpdateStudentCourses_DEC_2004() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID);
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("DEC", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_DEC.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse9").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse9").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testDeleteStudentCourses_WithNoWarnings() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID);
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }
        List<UUID> tobeDeleted = studentCourseEntities.stream().map(StudentCourseEntity::getId).toList();
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findAllById(tobeDeleted)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.deleteStudentCourses(studentID, tobeDeleted);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());

    }

    @SneakyThrows
    @Test
    public void testDeleteStudentCourses_WithWarnings() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        List<OptionalStudentCourse> optionalStudentCourses = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID);
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
            OptionalStudentCourse optionalStudentCourse = new OptionalStudentCourse();
            Course course = getCourses().stream().filter(x -> x.getCourseID().equals(studentCourse.getCourseID())).findFirst().orElse(null);
            optionalStudentCourse.setCourseCode(course.getCourseCode());
            optionalStudentCourse.setCourseLevel(course.getCourseLevel());
            optionalStudentCourse.setUsed(true);
            optionalStudentCourses.add(optionalStudentCourse);
        }
        List<UUID> tobeDeleted = studentCourseEntities.stream().map(StudentCourseEntity::getId).toList();
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "2004-EN");
        GraduationDataOptionalDetails graduationDataOptionalDetails = new GraduationDataOptionalDetails();
        GradStudentOptionalStudentProgram gradStudentOptionalStudentProgram1 = new GradStudentOptionalStudentProgram();
        gradStudentOptionalStudentProgram1.setStudentID(studentID);
        OptionalStudentCourses optionalStudentCoursesList = new OptionalStudentCourses();
        optionalStudentCoursesList.setStudentCourseList(optionalStudentCourses);
        gradStudentOptionalStudentProgram1.setOptionalStudentCourses(optionalStudentCoursesList);
        graduationDataOptionalDetails.setOptionalGradStatus(List.of(gradStudentOptionalStudentProgram1));
        graduationStatusEntity.setStudentGradData(objectMapper.writeValueAsString(graduationDataOptionalDetails));
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findAllById(tobeDeleted)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseService.getLetterGrades()).thenReturn(getLetterGrades());
        when(courseService.getExaminableCourses(anyList())).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.deleteStudentCourses(studentID, tobeDeleted);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testGetStudentCourseHistory() {
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseHistory> historyEntities = new ArrayList();
        for(StudentCourse studentCourse : studentCourses.values()) {
            StudentCourseHistory history = new StudentCourseHistory();
            history.setCourseID(studentCourse.getCourseID());
            history.setCourseSession(studentCourse.getCourseSession());
            history.setActivityCode(StudentCourseActivityType.USERCOURSEADD.name());
            historyEntities.add(history);
        }
        when(historyService.getStudentCourseHistory(any())).thenReturn(historyEntities);
        List<StudentCourseHistory> result = studentCourseService.getStudentCourseHistory(studentID);
        assertEquals(historyEntities, result);
    }

    private Map<String, StudentCourse> getStudentCoursesTestData_WithValidationIssues() {
        Map<String, StudentCourse> studentCourses = new HashMap<>();
        //STUDENT_COURSE_INTERIM_PERCENT_VALID, STUDENT_COURSE_FINAL_PERCENT_VALID, STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_VALID
        StudentCourse studentCourse1 = createStudentCourse("1","202404", 200, null,-200, null, null, null);
        //STUDENT_COURSE_INTERIM_GRADE_VALID, STUDENT_COURSE_FINAL_GRADE_VALID
        StudentCourse studentCourse2 = createStudentCourse("2","200004", 40,"C", 100, "C-", null, null);
        //STUDENT_COURSE_Q_VALID, STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID
        StudentCourse studentCourse3 = createStudentCourse("3",LocalDate.now().getYear()+""+String.format("%02d", LocalDate.now().minusMonths(1).getMonthValue()), null, null,null,null, null, null);
        //STUDENT_COURSE_SESSION_START_VALID
        StudentCourse studentCourse4 = createStudentCourse("4",LocalDate.now().minusYears(2).getYear()+"04", null, null,null,null, null, null);
        //STUDENT_COURSE_SESSION_END_VALID
        StudentCourse studentCourse5 = createStudentCourse("4",LocalDate.now().plusYears(2).getYear()+"04", null, null,null,null, null, null);
        //STUDENT_COURSE_SESSION_VALID
        StudentCourse studentCourse6 = createStudentCourse("6","196004", null, null,null,null, null, null);
        //STUDENT_COURSE_CREDITS_BA_VALID
        StudentCourse studentCourse7 = createStudentCourse("7","202404", null, null,89,"A", 3, "B");
        //STUDENT_COURSE_CREDITS_A_F_VALID
        StudentCourse studentCourse8 = createStudentCourse("8","202404", null, null,89,"A", 1, "B");
        //STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_VALID
        StudentCourse studentCourse9 = createStudentCourse("9","202404", null, null,89,"A", 1, "A");
        //STUDENT_COURSE_CREDITS_VALID
        StudentCourse studentCourse10 = createStudentCourse("10","202404", null, null,89,"A", 5, "B");
        //STUDENT_COURSE_VALID
        StudentCourse studentCourse11 = createStudentCourse("400","202404", null, null,89,"A", 5, "B");
        //STUDENT_COURSE_EXAMINABLE_VALID
        StudentCourse studentCourse12 = createStudentCourse("12","202404", null, null,89,"A", 4, "B");

        studentCourses.put("studentCourse1", studentCourse1);
        studentCourses.put("studentCourse2", studentCourse2);
        studentCourses.put("studentCourse3", studentCourse3);
        studentCourses.put("studentCourse4", studentCourse4);
        studentCourses.put("studentCourse5", studentCourse5);
        studentCourses.put("studentCourse6", studentCourse6);
        studentCourses.put("studentCourse7", studentCourse7);
        studentCourses.put("studentCourse8", studentCourse8);
        studentCourses.put("studentCourse9", studentCourse9);
        studentCourses.put("studentCourse10", studentCourse10);
        studentCourses.put("studentCourse11", studentCourse11);
        studentCourses.put("studentCourse12", studentCourse12);
        return studentCourses;
    }

    private Map<String, StudentCourse> getStudentCoursesTestData_NoValidationIssues() {
        Map<String, StudentCourse> studentCourses = new HashMap<>();
        //NO WARNING MINIMAL DATA
        StudentCourse studentCourse1 = createStudentCourse("5","202504", null, null,87,"A", 4, "B");
        //NO WARNING ALL DATA
        StudentCourse studentCourse2= createStudentCourse("6","202504", 62, "C",85,"B", 4, "B");
        studentCourse2.setCustomizedCourseName("CUSTOM");
        studentCourse2.setEquivOrChallenge("C");
        studentCourse2.setRelatedCourseId("4");
        studentCourses.put("studentCourse1", studentCourse1);
        studentCourses.put("studentCourse2", studentCourse2);

        return studentCourses;
    }


    private GraduationStudentRecordEntity getGraduationStudentRecordEntity(String studentStatus, String program) {
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setProgramCompletionDate(new java.util.Date());
        graduationStatusEntity.setStudentStatus(studentStatus);
        if(StringUtils.isNotBlank(program)) {
            graduationStatusEntity.setProgram(program);
        }
        return graduationStatusEntity;
    }

    private StudentCourseEntity createStudentCourseEntity(UUID studentID, String courseId, String courseSession) {
        StudentCourseEntity studentCourseEntity = new StudentCourseEntity();
        studentCourseEntity.setStudentID(studentID);
        studentCourseEntity.setCourseID(new BigInteger(courseId));
        studentCourseEntity.setCourseSession(courseSession);
        return studentCourseEntity;
    }

    private StudentCourse createStudentCourse(String courseId, String courseSession, Integer interimPercent, String interimGrade, Integer finalPercent, String finalGrade, Integer credits, String fineArtsAppliedSkills) {
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setCourseID(courseId);
        studentCourse.setCourseSession(courseSession);
        studentCourse.setInterimPercent(interimPercent);
        studentCourse.setInterimLetterGrade(interimGrade);
        studentCourse.setFinalPercent(finalPercent);
        studentCourse.setFinalLetterGrade(finalGrade);
        studentCourse.setCredits(credits);
        studentCourse.setFineArtsAppliedSkills(fineArtsAppliedSkills);
        return studentCourse;
    }

    private List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();
        CourseCharacteristics characteristicsBA = CourseCharacteristics.builder().type("BA").code("EC").build();
        CourseCharacteristics characteristicsLA = CourseCharacteristics.builder().type("LA").code("EC").build();
        CourseCharacteristics characteristicsOT = CourseCharacteristics.builder().type("CC").code("EC").build();
        List<CourseAllowableCredits> allowableCredits = new ArrayList<>();
        allowableCredits.add(CourseAllowableCredits.builder().creditValue("1").build());
        allowableCredits.add(CourseAllowableCredits.builder().creditValue("2").build());
        allowableCredits.add(CourseAllowableCredits.builder().creditValue("3").build());
        allowableCredits.add(CourseAllowableCredits.builder().creditValue("4").build());

        courses.add(Course.builder().courseID("1").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsOT).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("2").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsOT).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("3").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now()).courseCode("Q").courseCategory(characteristicsOT).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("4").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsBA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("5").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("6").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());

        courses.add(Course.builder().courseID("7").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsBA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("8").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsBA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("9").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsBA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("10").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsBA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("11").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("12").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());

        return courses;
    }

    @SneakyThrows
    private List<LetterGrade> getLetterGrades() {
        List<LetterGrade> letterGrades = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        letterGrades.add(LetterGrade.builder().grade("A").percentRangeLow(86).percentRangeHigh(100).effectiveDate(sdf.parse("1940-01-01")).expiryDate(null).build());
        letterGrades.add(LetterGrade.builder().grade("B").percentRangeLow(73).percentRangeHigh(85).effectiveDate(sdf.parse("1940-01-01")).expiryDate(null).build());
        letterGrades.add(LetterGrade.builder().grade("C").percentRangeLow(60).percentRangeHigh(66).effectiveDate(sdf.parse("1940-01-01")).expiryDate(null).build());
        letterGrades.add(LetterGrade.builder().grade("C-").percentRangeLow(50).percentRangeHigh(59).effectiveDate(sdf.parse("1940-01-01")).expiryDate(null).build());
        letterGrades.add(LetterGrade.builder().grade("RM").percentRangeLow(0).percentRangeHigh(0).effectiveDate(sdf.parse("2007-09-01")).expiryDate(sdf.parse("2021-07-01")).build());
        return letterGrades;
    }

    private List<ExaminableCourse> getExaminableCourses() {
        ZoneId zoneId = ZoneId.of("America/Vancouver");
        List<ExaminableCourse> examinableCourses = new ArrayList<>();
        examinableCourses.add(ExaminableCourse.builder().courseID("12").examinableStart(Date.from(LocalDate.now().minusYears(2).atStartOfDay(zoneId).toInstant())).examinableEnd(Date.from(LocalDate.now().plusYears(2).atStartOfDay(zoneId).toInstant())).build());
        return examinableCourses;
    }

}
