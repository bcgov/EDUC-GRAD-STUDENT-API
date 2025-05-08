package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.dto.Course;
import ca.bc.gov.educ.api.gradstudent.model.dto.LetterGrade;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseValidationIssue;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCourseRepository;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
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
        List<StudentCourse> studentCourses = new ArrayList<>();
        studentCourses.add(createStudentCourse("1","202404", null,null,null, null));
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "");
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList(), anyString())).thenReturn(getCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses, "abc", false);
        assertNotNull(result);
        assertThat(result).isNotEmpty().hasSize(1);
    }

    /**
     * This test raises listed StudentCourseValidationIssueTypeCode
     * STUDENT_COURSE_DUPLICATE, STUDENT_COURSE_VALID
     */
    @Test
    public void testCreateStudentCourses_CUR_Duplicate() {
        UUID studentID = UUID.randomUUID();
        List<StudentCourse> studentCourses = new ArrayList<>();
        StudentCourse studentCourse1 = createStudentCourse("1","202404", null,null,null, null);
        StudentCourse studentCourse2 = createStudentCourse("100","202404", null,null,null, null);
        studentCourses.add(studentCourse1);
        studentCourses.add(studentCourse2);
        List<StudentCourseEntity> existingStudentCourseEntities = new ArrayList<>();
        existingStudentCourseEntities.add(createStudentCourseEntity(studentID,"1","202404" ));

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "");
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList(), anyString())).thenReturn(getCourses());
        when(studentCourseRepository.findByStudentID(any(UUID.class))).thenReturn(existingStudentCourseEntities);
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses, "abc", false);
        assertNotNull(result);
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DUPLICATE.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage())));
    }

    /**
     * This test raises listed StudentCourseValidationIssueTypeCode
     * STUDENT_STATUS_MER, STUDENT_COURSE_SESSION_START_VALID, STUDENT_COURSE_SESSION_END_VALID
     * STUDENT_COURSE_INTERIM_PERCENT_VALID, STUDENT_COURSE_INTERIM_GRADE_VALID, STUDENT_COURSE_FINAL_PERCENT_VALID, STUDENT_COURSE_FINAL_GRADE_VALID
     */
    @Test
    public void testCreateStudentCourses_MER() {
        UUID studentID = UUID.randomUUID();
        List<StudentCourse> studentCourses = new ArrayList<>();
        StudentCourse studentCourse1 = createStudentCourse("1","202404", 200, null,-200, null);
        StudentCourse studentCourse2 = createStudentCourse("2","200004", null,null, 100, "C-");
        StudentCourse studentCourse3 = createStudentCourse("3","202404", null, null,null,null);
        StudentCourse studentCourse4 = createStudentCourse("3","202404", 100, "C-",null,null);
        studentCourses.add(studentCourse1);
        studentCourses.add(studentCourse2);
        studentCourses.add(studentCourse3);
        studentCourses.add(studentCourse4);
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("MER", "");
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList(), anyString())).thenReturn(getCourses());
        when(courseService.getLetterGrades(anyString())).thenReturn(getLetterGrades());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses, "abc", false);
        assertNotNull(result);
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_MER.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse2.getCourseID()) && x.getCourseSession().equals(studentCourse2.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse2.getCourseID()) && x.getCourseSession().equals(studentCourse2.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse3.getCourseID()) && x.getCourseSession().equals(studentCourse3.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse3.getCourseID()) && x.getCourseSession().equals(studentCourse3.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse4.getCourseID()) && x.getCourseSession().equals(studentCourse4.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage())));
    }

    /**
     * This test raises listed StudentCourseValidationIssueTypeCode
     * STUDENT_STATUS_ARC, STUDENT_COURSE_SESSION_START_VALID, STUDENT_COURSE_SESSION_END_VALID
     * STUDENT_COURSE_INTERIM_PERCENT_VALID, STUDENT_COURSE_FINAL_PERCENT_VALID, STUDENT_COURSE_FINAL_GRADE_VALID
     */
    @Test
    public void testCreateStudentCourses_ARC() {
        UUID studentID = UUID.randomUUID();
        List<StudentCourse> studentCourses = new ArrayList<>();
        StudentCourse studentCourse1 = createStudentCourse("1","202404", 200, null,-200, null);
        StudentCourse studentCourse2 = createStudentCourse("2","200004", null,null, 100, "C-");
        StudentCourse studentCourse3 = createStudentCourse("3","202404", null, null,null,null);
        studentCourses.add(studentCourse1);
        studentCourses.add(studentCourse2);
        studentCourses.add(studentCourse3);
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("ARC", "");
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList(), anyString())).thenReturn(getCourses());
        when(courseService.getLetterGrades(anyString())).thenReturn(getLetterGrades());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses, "abc", false);
        assertNotNull(result);
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_ARC.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse2.getCourseID()) && x.getCourseSession().equals(studentCourse2.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse2.getCourseID()) && x.getCourseSession().equals(studentCourse2.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse3.getCourseID()) && x.getCourseSession().equals(studentCourse3.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse3.getCourseID()) && x.getCourseSession().equals(studentCourse3.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage())));
    }

    /**
     * This test raises listed StudentCourseValidationIssueTypeCode
     * STUDENT_STATUS_DEC, STUDENT_COURSE_SESSION_START_VALID, STUDENT_COURSE_SESSION_END_VALID
     * STUDENT_COURSE_INTERIM_PERCENT_VALID, STUDENT_COURSE_FINAL_PERCENT_VALID, STUDENT_COURSE_FINAL_GRADE_VALID
     */
    @Test
    public void testCreateStudentCourses_DEC() {
        UUID studentID = UUID.randomUUID();
        List<StudentCourse> studentCourses = new ArrayList<>();
        StudentCourse studentCourse1 = createStudentCourse("1","202404", 200, null,-200, null);
        StudentCourse studentCourse2 = createStudentCourse("2","200004", null,null, 100, "C-");
        StudentCourse studentCourse3 = createStudentCourse("3","202404", null, null,null,null);
        studentCourses.add(studentCourse1);
        studentCourses.add(studentCourse2);
        studentCourses.add(studentCourse3);
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("DEC", "");
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList(), anyString())).thenReturn(getCourses());
        when(courseService.getLetterGrades(anyString())).thenReturn(getLetterGrades());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses, "abc", false);
        assertNotNull(result);
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_DEC.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse2.getCourseID()) && x.getCourseSession().equals(studentCourse2.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse2.getCourseID()) && x.getCourseSession().equals(studentCourse2.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse3.getCourseID()) && x.getCourseSession().equals(studentCourse3.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse3.getCourseID()) && x.getCourseSession().equals(studentCourse3.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage())));
    }


    /**
     * This test raises listed StudentCourseValidationIssueTypeCode
     * STUDENT_STATUS_TER, STUDENT_COURSE_SESSION_START_VALID, STUDENT_COURSE_SESSION_END_VALID
     * STUDENT_COURSE_INTERIM_PERCENT_VALID, STUDENT_COURSE_FINAL_PERCENT_VALID, STUDENT_COURSE_FINAL_GRADE_VALID
     */
    @Test
    public void testCreateStudentCourses_TER() {
        UUID studentID = UUID.randomUUID();
        List<StudentCourse> studentCourses = new ArrayList<>();
        StudentCourse studentCourse1 = createStudentCourse("1","202404", 200, null,-200, null);
        StudentCourse studentCourse2 = createStudentCourse("2","200004", null,null, 100, "C-");
        StudentCourse studentCourse3 = createStudentCourse("3","202404", null, null,null,null);
        studentCourses.add(studentCourse1);
        studentCourses.add(studentCourse2);
        studentCourses.add(studentCourse3);
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("TER", "");
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList(), anyString())).thenReturn(getCourses());
        when(courseService.getLetterGrades(anyString())).thenReturn(getLetterGrades());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses, "abc", false);
        assertNotNull(result);
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_TER.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse1.getCourseID()) && x.getCourseSession().equals(studentCourse1.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse2.getCourseID()) && x.getCourseSession().equals(studentCourse2.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse2.getCourseID()) && x.getCourseSession().equals(studentCourse2.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse3.getCourseID()) && x.getCourseSession().equals(studentCourse3.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage())));
        assertNotNull(result.stream().filter(x -> x.getCourseID().equals(studentCourse3.getCourseID()) && x.getCourseSession().equals(studentCourse3.getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage())));
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

    private StudentCourse createStudentCourse(String courseId, String courseSession, Integer interimPercent, String interimGrade, Integer finalPercent, String finalGrade) {
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setCourseID(courseId);
        studentCourse.setCourseSession(courseSession);
        studentCourse.setInterimPercent(interimPercent);
        studentCourse.setInterimLetterGrade(interimGrade);
        studentCourse.setFinalPercent(finalPercent);
        studentCourse.setFinalLetterGrade(finalGrade);
        return studentCourse;
    }

    private List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();
        courses.add(Course.builder().courseID("1").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").build());
        courses.add(Course.builder().courseID("2").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").build());
        courses.add(Course.builder().courseID("3").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("Q").build());
        return courses;
    }

    @SneakyThrows
    private List<LetterGrade> getLetterGrades() {
        List<LetterGrade> letterGrades = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        letterGrades.add(LetterGrade.builder().grade("A").percentRangeLow(89).percentRangeHigh(100).effectiveDate(sdf.parse("1940-01-01")).expiryDate(null).build());
        letterGrades.add(LetterGrade.builder().grade("C-").percentRangeLow(50).percentRangeHigh(59).effectiveDate(sdf.parse("1940-01-01")).expiryDate(null).build());
        letterGrades.add(LetterGrade.builder().grade("RM").percentRangeLow(0).percentRangeHigh(0).effectiveDate(sdf.parse("2007-09-01")).expiryDate(sdf.parse("2021-07-01")).build());
        return letterGrades;
    }

}
