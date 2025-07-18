package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseActivityType;
import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStudentCoursesSubscriber;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseExamEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCourseRepository;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@SpringBootTest
@RunWith(SpringRunner.class)
public class StudentCourseServiceTest  extends BaseIntegrationTest {

    @MockBean StudentCourseRepository studentCourseRepository;

    @MockBean GraduationStudentRecordRepository graduationStatusRepository;

    @Autowired StudentCourseService studentCourseService;
    @SpyBean GraduationStatusService graduationStatusService;
    @MockBean CourseService courseService;
    @MockBean CourseCacheService courseCacheService;
    @MockBean HistoryService historyService;

    @MockBean(name = "studentApiClient")
    @Qualifier("studentApiClient")
    WebClient webClient;

    @MockBean
    FetchGradStudentCoursesSubscriber fetchGradStudentCoursesSubscriber;

    // NATS
    @MockBean
    NatsConnection natsConnection;
    @MockBean
    Publisher publisher;
    @MockBean
    Subscriber subscriber;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @Test
    public void testGetStudentCourses() {
        UUID studentID = UUID.randomUUID();
        StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID,"1","202404");
        studentCourseEntity.setRelatedCourseId(new BigInteger("2"));
        Mockito.when(studentCourseRepository.findByStudentID(studentID)).thenReturn(Arrays.asList(studentCourseEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
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
    public void testWithMockedAuthorities() {
        setSecurityContext();
        boolean isSystemCoordinator = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("GRAD_SYSTEM_COORDINATOR"));
        boolean isNotSystemCoordinator = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("GRAD_TEST"));
        assertTrue(isSystemCoordinator);
        assertFalse(isNotSystemCoordinator);
    }

    @Test
    public void testCreateStudentCourses_CUR() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getEquivalentOrChallengeCodesFromCache()).thenReturn(getEquivalentOrChallengeCodes());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertThat(result).isNotEmpty().hasSize(2);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());

    }

    @Test
    public void testUpdateStudentCourses_CUR() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getEquivalentOrChallengeCodesFromCache()).thenReturn(getEquivalentOrChallengeCodes());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertThat(result).isNotEmpty().hasSize(2);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());

    }

    @Test
    public void testUpdateStudentCourses_CUR_ExaminableToNonExaminable_Error() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setCourseExam(createStudentCourseExamEntity(createStudentCourseExam(80,80,80,"A")));
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getEquivalentOrChallengeCodesFromCache()).thenReturn(getEquivalentOrChallengeCodes());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertThat(result).isNotEmpty().hasSize(2);

        Map<String, List<String>> expectedValidationMessages = new HashMap<>();
        expectedValidationMessages.put(
                "studentCourse1", List.of(
                        StudentCourseValidationIssueTypeCode.STUDENT_COURSE_UPDATE_NOT_ALLOWED.getMessage()
                ));
        expectedValidationMessages.put(
                "studentCourse2", List.of(
                        StudentCourseValidationIssueTypeCode.STUDENT_COURSE_UPDATE_NOT_ALLOWED.getMessage()
                ));
    }

    @Test
    public void testCreateStudentCourses_CUR_Duplicate() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();

        List<StudentCourseEntity> existingStudentCourseEntities = new ArrayList<>();
        existingStudentCourseEntities.add(createStudentCourseEntity(studentID, studentCourses.get("studentCourse1").getCourseID(), studentCourses.get("studentCourse1").getCourseSession()));

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "");
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(studentCourseRepository.findByStudentID(any(UUID.class))).thenReturn(existingStudentCourseEntities);
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getEquivalentOrChallengeCodesFromCache()).thenReturn(getEquivalentOrChallengeCodes());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DUPLICATE.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());
    }

    @Test
    public void testCreateStudentCourses_MER() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("MER", "");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

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
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("TER", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCacheByProgramYear("1996")).thenReturn(getExaminableCourses());
        when(courseCacheService.getEquivalentOrChallengeCodesFromCache()).thenReturn(getEquivalentOrChallengeCodes());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);

        Map<String, List<Pair<String,Boolean>>> expectedValidationMessages = new HashMap<>();
        expectedValidationMessages.put("studentCourse1", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_TER.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage(), true)
        ));
        expectedValidationMessages.put("studentCourse2", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage(), true)
        ));
        expectedValidationMessages.put("studentCourse3", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage(), true)
        ));
        expectedValidationMessages.put("studentCourse4", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse5", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse6", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse7", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_CA_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse8", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse9", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse10", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse11", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse12", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse13", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_RELATED_COURSE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse14", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EQUIVALENCY_CHALLENGE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse15", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SPECIAL_CASE_AEGROTAT_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse16", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SCHOOL_PERCENT_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_SCHOOL_PERCENT_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_PERCENT_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse17", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_MANDATORY_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse19", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse20", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INVALID_DATA.getMessage(), true)));
        expectedValidationMessages.put("studentCourse21", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_MONTH_VALID.getMessage(), true)));

        assertValidationMessage(studentCourses, result, expectedValidationMessages);
    }

    @Test
    public void testCreateStudentCourses_ARC_1996() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("ARC", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCacheByProgramYear("1996")).thenReturn(getExaminableCourses());
        when(courseCacheService.getEquivalentOrChallengeCodesFromCache()).thenReturn(getEquivalentOrChallengeCodes());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);

        Map<String, List<Pair<String,Boolean>>> expectedValidationMessages = new HashMap<>();
        expectedValidationMessages.put("studentCourse1", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_ARC.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage(), true)
        ));
        expectedValidationMessages.put("studentCourse2", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage(), true)
        ));
        expectedValidationMessages.put("studentCourse3", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage(), true)
        ));
        expectedValidationMessages.put("studentCourse4", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse5", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse6", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse7", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_CA_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse8", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse9", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse10", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse11", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse12", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse13", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_RELATED_COURSE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse14", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EQUIVALENCY_CHALLENGE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse15", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SPECIAL_CASE_AEGROTAT_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse16", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SCHOOL_PERCENT_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_SCHOOL_PERCENT_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_PERCENT_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse17", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_MANDATORY_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse19", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse20", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INVALID_DATA.getMessage(), true)));
        expectedValidationMessages.put("studentCourse21", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_MONTH_VALID.getMessage(), true)));

        assertValidationMessage(studentCourses, result, expectedValidationMessages);

    }

    @Test
    public void testCreateStudentCourses_DEC_1996() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("DEC", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCacheByProgramYear("1996")).thenReturn(getExaminableCourses());
        when(courseCacheService.getEquivalentOrChallengeCodesFromCache()).thenReturn(getEquivalentOrChallengeCodes());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);

        Map<String, List<Pair<String,Boolean>>> expectedValidationMessages = new HashMap<>();
        expectedValidationMessages.put("studentCourse1", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_DEC.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage(), true)
        ));
        expectedValidationMessages.put("studentCourse2", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage(), true)
        ));
        expectedValidationMessages.put("studentCourse3", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage(), true)
        ));
        expectedValidationMessages.put("studentCourse4", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse5", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse6", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse7", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_CA_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse8", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse9", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse10", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse11", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse12", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse13", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_RELATED_COURSE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse14", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EQUIVALENCY_CHALLENGE_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse15", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SPECIAL_CASE_AEGROTAT_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse16", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SCHOOL_PERCENT_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_SCHOOL_PERCENT_VALID.getMessage(), true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_PERCENT_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse17", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_MANDATORY_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse19", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_VALID.getMessage(), true)));
        expectedValidationMessages.put("studentCourse20", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INVALID_DATA.getMessage(), true)));
        expectedValidationMessages.put("studentCourse21", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_MONTH_VALID.getMessage(), true)));

        assertValidationMessage(studentCourses, result, expectedValidationMessages);
    }

    @Test
    public void testCreateStudentCourses_TER_2004() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("TER", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_TER.getMessage())).findFirst().isPresent());


    }

    @Test
    public void testCreateStudentCourses_ARC_2004() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("ARC", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_ARC.getMessage())).findFirst().isPresent());


    }

    @Test
    public void testCreateStudentCourses_DEC_2004() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();

        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("DEC", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), false);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_DEC.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testUpdateStudentCourses_MER() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            if(studentCourse.getCourseExam() != null) {
                studentCourseEntity.setCourseExam(createStudentCourseExamEntity(studentCourse.getCourseExam()));
            }
            studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("MER", "");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

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
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            if(studentCourse.getCourseExam() != null) {
                studentCourseEntity.setCourseExam(createStudentCourseExamEntity(studentCourse.getCourseExam()));
            }
            if(StringUtils.isNotBlank(studentCourse.getCourseID()) && StringUtils.isNotBlank(studentCourse.getCourseSession()))
                studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("TER", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCacheByProgramYear("1996")).thenReturn(getExaminableCourses());
        when(courseCacheService.getEquivalentOrChallengeCodesFromCache()).thenReturn(getEquivalentOrChallengeCodes());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);

       Map<String, List<Pair<String,Boolean>>> expectedValidationMessages = new HashMap<>();
        expectedValidationMessages.put("studentCourse1", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_TER.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse2", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse3", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse4", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse5", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse6", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse7", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_CA_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse8", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse9", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse10", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse11", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse12", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse13", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_RELATED_COURSE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse14", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EQUIVALENCY_CHALLENGE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse15", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SPECIAL_CASE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse16", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SCHOOL_PERCENT_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_SCHOOL_PERCENT_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_PERCENT_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse17", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_MANDATORY_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse19", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse20", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INVALID_DATA.getMessage(),false)));
        expectedValidationMessages.put("studentCourse21", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_MONTH_VALID.getMessage(),true)));

        assertValidationMessage(studentCourses, result, expectedValidationMessages);
    }

    @Test
    public void testUpdateStudentCourses_ARC_1996() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            if(studentCourse.getCourseExam() != null) {
                studentCourseEntity.setCourseExam(createStudentCourseExamEntity(studentCourse.getCourseExam()));
            }
            if(StringUtils.isNotBlank(studentCourse.getCourseID()) && StringUtils.isNotBlank(studentCourse.getCourseSession()))
                studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("ARC", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCacheByProgramYear("1996")).thenReturn(getExaminableCourses());
        when(courseCacheService.getEquivalentOrChallengeCodesFromCache()).thenReturn(getEquivalentOrChallengeCodes());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);

        Map<String, List<Pair<String,Boolean>>> expectedValidationMessages = new HashMap<>();
        expectedValidationMessages.put("studentCourse1", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_ARC.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse2", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse3", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse4", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse5", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse6", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse7", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_CA_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse8", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse9", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse10", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse11", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse12", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse13", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_RELATED_COURSE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse14", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EQUIVALENCY_CHALLENGE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse15", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SPECIAL_CASE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse16", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SCHOOL_PERCENT_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_SCHOOL_PERCENT_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_PERCENT_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse17", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_MANDATORY_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse19", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse20", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INVALID_DATA.getMessage(),false)));
        expectedValidationMessages.put("studentCourse21", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_MONTH_VALID.getMessage(),true)));

        assertValidationMessage(studentCourses, result, expectedValidationMessages);

    }

    @Test
    public void testUpdateStudentCourses_DEC_1996() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            if(studentCourse.getCourseExam() != null) {
                studentCourseEntity.setCourseExam(createStudentCourseExamEntity(studentCourse.getCourseExam()));
            }
            if(StringUtils.isNotBlank(studentCourse.getCourseID()) && StringUtils.isNotBlank(studentCourse.getCourseSession()))
                studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("DEC", "1996-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCacheByProgramYear("1996")).thenReturn(getExaminableCourses());
        when(courseCacheService.getEquivalentOrChallengeCodesFromCache()).thenReturn(getEquivalentOrChallengeCodes());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);

        Map<String, List<Pair<String,Boolean>>> expectedValidationMessages = new HashMap<>();
        expectedValidationMessages.put("studentCourse1", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_DEC.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse2", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse3", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse4", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse5", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse6", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse7", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_CA_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse8", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse9", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse10", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse11", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse12", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse13", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_RELATED_COURSE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse14", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EQUIVALENCY_CHALLENGE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse15", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SPECIAL_CASE_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse16", List.of(
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SCHOOL_PERCENT_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_SCHOOL_PERCENT_VALID.getMessage(),true),
                Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_PERCENT_VALID.getMessage(),true)
        ));
        expectedValidationMessages.put("studentCourse17", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_MANDATORY_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse19", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_VALID.getMessage(),true)));
        expectedValidationMessages.put("studentCourse20", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INVALID_DATA.getMessage(),false)));
        expectedValidationMessages.put("studentCourse21", List.of(Pair.of(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_MONTH_VALID.getMessage(),true)));

        assertValidationMessage(studentCourses, result, expectedValidationMessages);

    }

    @Test
    public void testUpdateStudentCourses_TER_2004() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            if(studentCourse.getCourseExam() != null) {
                studentCourseEntity.setCourseExam(createStudentCourseExamEntity(studentCourse.getCourseExam()));
            }
            if(StringUtils.isNotBlank(studentCourse.getCourseID()) && StringUtils.isNotBlank(studentCourse.getCourseSession()))
                studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("TER", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_TER.getMessage())).findFirst().isPresent());


    }

    @Test
    public void testUpdateStudentCourses_ARC_2004() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            if(studentCourse.getCourseExam() != null) {
                studentCourseEntity.setCourseExam(createStudentCourseExamEntity(studentCourse.getCourseExam()));
            }
            if(StringUtils.isNotBlank(studentCourse.getCourseID()) && StringUtils.isNotBlank(studentCourse.getCourseSession()))
                studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("ARC", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_ARC.getMessage())).findFirst().isPresent());


    }

    @Test
    public void testUpdateStudentCourses_DEC_2004() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_WithValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            if(studentCourse.getCourseExam() != null) {
                studentCourseEntity.setCourseExam(createStudentCourseExamEntity(studentCourse.getCourseExam()));
            }
            if(StringUtils.isNotBlank(studentCourse.getCourseID()) && StringUtils.isNotBlank(studentCourse.getCourseSession()))
                studentCourseEntities.add(studentCourseEntity);
        }
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("DEC", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findByStudentID(studentID)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.saveStudentCourses(studentID, studentCourses.values().stream().toList(), true);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_STATUS_DEC.getMessage())).findFirst().isPresent());
    }

    @Test
    public void testDeleteStudentCourses_WithNoWarnings() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setId(studentCourseID);
            if(studentCourse.getCourseExam() != null) {
                studentCourseEntity.setCourseExam(createStudentCourseExamEntity(studentCourse.getCourseExam()));
            }
            studentCourseEntities.add(studentCourseEntity);
        }
        List<UUID> tobeDeleted = studentCourseEntities.stream().map(StudentCourseEntity::getId).toList();
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "2004-EN");
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setProgramCompletionDate(null);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findAllById(tobeDeleted)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        List<StudentCourseValidationIssue> result = studentCourseService.deleteStudentCourses(studentID, tobeDeleted);
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get().getValidationIssues().isEmpty());

    }

    @SneakyThrows
    @Test
    public void testDeleteStudentCourses_WithWarnings() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
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
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.deleteStudentCourses(studentID, tobeDeleted);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_GRADUATION_VALID.getMessage())).findFirst().isPresent());

        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_GRADUATION_VALID.getMessage())).findFirst().isPresent());

    }

    @SneakyThrows
    @Test
    public void testDeleteStudentCourses_WithError() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseEntity> studentCourseEntities = new ArrayList<>();
        for(StudentCourse studentCourse: studentCourses.values()) {
            UUID studentCourseID = UUID.randomUUID();
            studentCourse.setId(studentCourseID.toString());
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(studentID, studentCourse.getCourseID(), studentCourse.getCourseSession());
            studentCourseEntity.setCourseExam(createStudentCourseExamEntity(createStudentCourseExam(80,80,80,"A")));
            studentCourseEntity.setId(studentCourseID);
            studentCourseEntities.add(studentCourseEntity);
        }
        List<UUID> tobeDeleted = studentCourseEntities.stream().map(StudentCourseEntity::getId).toList();
        GraduationStudentRecordEntity graduationStatusEntity = getGraduationStudentRecordEntity("CUR", "");
        graduationStatusEntity.setStudentID(studentID);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(studentCourseRepository.findAllById(tobeDeleted)).thenReturn(studentCourseEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        when(courseCacheService.getLetterGradesFromCache()).thenReturn(getLetterGrades());
        when(courseCacheService.getExaminableCoursesFromCache()).thenReturn(getExaminableCourses());
        when(courseCacheService.getFineArtsAppliedSkillsCodesFromCache()).thenReturn(getFineArtsAppliedSkillsCodes());

        List<StudentCourseValidationIssue> result = studentCourseService.deleteStudentCourses(studentID, tobeDeleted);
        assertNotNull(result);
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse1").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse1").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_EXAM_VALID.getMessage())).findFirst().isPresent());
        assertTrue(result.stream().filter(x -> x.getCourseID().equals(studentCourses.get("studentCourse2").getCourseID()) && x.getCourseSession().equals(studentCourses.get("studentCourse2").getCourseSession())).findFirst().get()
                .getValidationIssues().stream().filter(y -> y.getValidationIssueMessage().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_EXAM_VALID.getMessage())).findFirst().isPresent());

    }

    @Test
    public void testGetStudentCourseHistory() {
        setSecurityContext();
        UUID studentID = UUID.randomUUID();
        Map<String, StudentCourse> studentCourses = getStudentCoursesTestData_NoValidationIssues();
        List<StudentCourseHistory> historyEntities = new ArrayList();
        for(StudentCourse studentCourse : studentCourses.values()) {
            StudentCourseHistory history = new StudentCourseHistory();
            history.setCourseID(studentCourse.getCourseID());
            history.setRelatedCourseId(studentCourse.getRelatedCourseId());
            history.setCourseSession(studentCourse.getCourseSession());
            history.setActivityCode(StudentCourseActivityType.USERCOURSEADD.name());
            historyEntities.add(history);
        }
        when(historyService.getStudentCourseHistory(any())).thenReturn(historyEntities);
        when(courseService.getCourses(anyList())).thenReturn(getCourses());
        List<StudentCourseHistory> result = studentCourseService.getStudentCourseHistory(studentID);
        assertEquals(historyEntities, result);
    }

    private void assertValidationMessage(Map<String, StudentCourse> studentCourses, List<StudentCourseValidationIssue> result, Map<String, List<Pair<String, Boolean>>> expectedValidationMessages) {
        for (Map.Entry<String, List<Pair<String, Boolean>>> entry : expectedValidationMessages.entrySet()) {
            String courseKey = entry.getKey();
            String courseId = studentCourses.get(courseKey).getCourseID();
            String courseSession = studentCourses.get(courseKey).getCourseSession();

            Optional<StudentCourseValidationIssue> matchedCourse = result.stream()
                    .filter(x -> x.getCourseID().equals(courseId) && x.getCourseSession().equals(courseSession)).findFirst();

            assertTrue("Course not found for key: " + courseKey, matchedCourse.isPresent());

            List<String> actualValidationMessages = matchedCourse.get().getValidationIssues().stream()
                    .map(ValidationIssue::getValidationIssueMessage).toList();

            for (Pair<String,Boolean> validationEntry : entry.getValue()) {
                String expectedMessage = validationEntry.getFirst();
                Boolean isRequired = validationEntry.getSecond();
                if(isRequired) {
                    assertTrue("Missing validation message for " + courseKey + ": " + expectedMessage,
                            actualValidationMessages.contains(expectedMessage));
                } else {
                    assertFalse("Unexpected validation message for " + courseKey + ": " + expectedMessage,
                            actualValidationMessages.contains(expectedMessage));
                }
            }
        }
    }

    private Map<String, StudentCourse> getStudentCoursesTestData_WithValidationIssues() {
        Map<String, StudentCourse> studentCourses = new HashMap<>();
        //STUDENT_COURSE_INTERIM_PERCENT_VALID, STUDENT_COURSE_FINAL_PERCENT_VALID, STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_CA_VALID
        StudentCourse studentCourse1 = createStudentCourse("1","202404", 200, null,-200, null, null, null, null);
        //STUDENT_COURSE_INTERIM_GRADE_VALID, STUDENT_COURSE_FINAL_GRADE_VALID
        StudentCourse studentCourse2 = createStudentCourse("2","200004", 40,"C", 100, "C-", null, null, null);
        //STUDENT_COURSE_Q_VALID, STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID
        StudentCourse studentCourse3 = createStudentCourse("3",LocalDate.now().getYear()+""+String.format("%02d", LocalDate.now().minusMonths(1).getMonthValue()), null, null,null,null, null, null, null);
        //STUDENT_COURSE_SESSION_START_VALID
        StudentCourse studentCourse4 = createStudentCourse("4",LocalDate.now().minusYears(2).getYear()+"04", null, null,null,null, null, null, null);
        //STUDENT_COURSE_SESSION_END_VALID
        StudentCourse studentCourse5 = createStudentCourse("4",LocalDate.now().plusYears(2).getYear()+"04", null, null,null,null, null, null, null);
        //STUDENT_COURSE_SESSION_VALID
        StudentCourse studentCourse6 = createStudentCourse("6","196004", null, null,null,null, null, null, null);
        //STUDENT_COURSE_CREDITS_BA_VALID
        StudentCourse studentCourse7 = createStudentCourse("7","202404", null, null,89,"A", 3, "B", null);
        //STUDENT_COURSE_CREDITS_A_F_VALID
        StudentCourse studentCourse8 = createStudentCourse("8","202404", null, null,89,"A", 1, "B", null);
        //STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_CA_VALID
        StudentCourse studentCourse9 = createStudentCourse("9","202404", null, null,89,"A", 1, "A", null);
        //STUDENT_COURSE_CREDITS_VALID
        StudentCourse studentCourse10 = createStudentCourse("10","202404", null, null,89,"A", 5, "B", null);
        //STUDENT_COURSE_VALID
        StudentCourse studentCourse11 = createStudentCourse("400","202404", null, null,89,"A", 5, "B", null);
        //STUDENT_COURSE_EXAMINABLE_VALID
        StudentCourse studentCourse12 = createStudentCourse("12","202404", null, null,89,"A", 4, "B", null);
        //STUDENT_RELATED_COURSE_VALID
        StudentCourse studentCourse13 = createStudentCourse("13","202404", null, null,89,"A", 4, "B", "XYZ");
        //STUDENT_COURSE_EQUIVALENCY_CHALLENGE_VALID
        StudentCourse studentCourse14 = createStudentCourse("14","202404", null, null,89,"A", 4, null, null);
        studentCourse14.setEquivOrChallenge("X");
        //STUDENT_COURSE_EXAM_SPECIAL_CASE_AEGROTAT_VALID
        StudentCourse studentCourse15 = createStudentCourse("15","202404", null, null,89,"A", 4, null, null);
        studentCourse15.setCourseExam(createStudentCourseExam(89, 88, 88, "X"   ));
        //STUDENT_COURSE_EXAM_SCHOOL_PERCENT_VALID, STUDENT_COURSE_EXAM_BEST_SCHOOL_PERCENT_VALID, STUDENT_COURSE_EXAM_BEST_PERCENT_VALID
        StudentCourse studentCourse16 = createStudentCourse("16","202404", null, null,89,"A", 4, null, null);
        studentCourse16.setCourseExam(createStudentCourseExam(-89, -88, -88,null   ));
        //STUDENT_COURSE_EXAM_MANDATORY_VALID
        StudentCourse studentCourse17 = createStudentCourse("17","202404", null, null,89,"A", 4, null, null);
        studentCourse17.setCourseExam(createStudentCourseExam(89, 88, 88,null   ));
        //STUDENT_COURSE_EXAM_OPTIONAL_VALID
        StudentCourse studentCourse18 = createStudentCourse("18","202404", null, null,89,"A", 4, null, null);
        studentCourse18.setCourseExam(createStudentCourseExam(89, 88, 88,null   ));
        //STUDENT_COURSE_EXAM_OPTIONAL_VALID
        StudentCourse studentCourse19 = createStudentCourse("19","202404", null, null,89,"A", 4, "X", null);
        studentCourse19.setCourseExam(createStudentCourseExam(89, 88, 88,null   ));
        //STUDENT_COURSE_INVALID_DATA
        StudentCourse studentCourse20 = createStudentCourse("","", null, null,89,"A", 4, "X", null);
        studentCourse20.setCourseExam(createStudentCourseExam(89, 88, 88,null   ));
        //STUDENT_COURSE_SESSION_MONTH_VALID
        StudentCourse studentCourse21 = createStudentCourse("21","202423", null, null,89,"A", 4, "X", null);
        studentCourse21.setCourseExam(createStudentCourseExam(89, 88, 88,null   ));


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
        studentCourses.put("studentCourse13", studentCourse13);
        studentCourses.put("studentCourse14", studentCourse14);
        studentCourses.put("studentCourse15", studentCourse15);
        studentCourses.put("studentCourse16", studentCourse16);
        studentCourses.put("studentCourse17", studentCourse17);
        studentCourses.put("studentCourse18", studentCourse18);
        studentCourses.put("studentCourse19", studentCourse19);
        studentCourses.put("studentCourse20", studentCourse20);
        studentCourses.put("studentCourse21", studentCourse21);
        return studentCourses;
    }

    private StudentCourseExam createStudentCourseExam(Integer schoolPercentage, Integer bestSchoolPercentage, Integer bestExamPercentage, String specialCase) {
        StudentCourseExam studentCourseExam = new StudentCourseExam();
        studentCourseExam.setSchoolPercentage(schoolPercentage);
        studentCourseExam.setBestSchoolPercentage(bestSchoolPercentage);
        studentCourseExam.setExamPercentage(bestExamPercentage);
        studentCourseExam.setBestExamPercentage(bestExamPercentage);
        studentCourseExam.setSpecialCase(specialCase);
        return studentCourseExam;
    }

    private Map<String, StudentCourse> getStudentCoursesTestData_NoValidationIssues() {
        Map<String, StudentCourse> studentCourses = new HashMap<>();
        //NO WARNING MINIMAL DATA
        StudentCourse studentCourse1 = createStudentCourse("5","202504", null, null,87,"A", 4, "B", "7");
        //NO WARNING ALL DATA
        StudentCourse studentCourse2= createStudentCourse("6","202504", 62, "C",85,"B", 4, "", null);
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
        graduationStatusEntity.setGpa("3.97");
        if(StringUtils.isNotBlank(program)) {
            graduationStatusEntity.setProgram(program);
        }
        return graduationStatusEntity;
    }

    private StudentCourseEntity createStudentCourseEntity(UUID studentID, String courseId, String courseSession) {
        StudentCourseEntity studentCourseEntity = new StudentCourseEntity();
        studentCourseEntity.setStudentID(studentID);
        studentCourseEntity.setCourseID(StringUtils.isNotBlank(courseId) ? new BigInteger(courseId) : null);
        studentCourseEntity.setCourseSession(courseSession);
        return studentCourseEntity;
    }

    private StudentCourseExamEntity createStudentCourseExamEntity(StudentCourseExam courseExam) {
        StudentCourseExamEntity studentCourseExamEntity = new StudentCourseExamEntity();
        studentCourseExamEntity.setId(UUID.randomUUID());
        studentCourseExamEntity.setSchoolPercentage(courseExam.getSchoolPercentage().doubleValue());
        studentCourseExamEntity.setBestSchoolPercentage(courseExam.getBestSchoolPercentage().doubleValue());
        if(courseExam.getExamPercentage() != null)
            studentCourseExamEntity.setExamPercentage(courseExam.getExamPercentage().doubleValue());
        return studentCourseExamEntity;
    }

    private StudentCourse createStudentCourse(String courseId, String courseSession, Integer interimPercent, String interimGrade, Integer finalPercent, String finalGrade, Integer credits, String fineArtsAppliedSkills, String relatedCourseId) {
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setCourseID(courseId);
        studentCourse.setCourseSession(courseSession);
        studentCourse.setInterimPercent(interimPercent);
        studentCourse.setInterimLetterGrade(interimGrade);
        studentCourse.setFinalPercent(finalPercent);
        studentCourse.setFinalLetterGrade(finalGrade);
        studentCourse.setCredits(credits);
        studentCourse.setFineArtsAppliedSkills(fineArtsAppliedSkills);
        studentCourse.setRelatedCourseId(relatedCourseId);
        return studentCourse;
    }

    private List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();
        CourseCharacteristics characteristicsBA = CourseCharacteristics.builder().type("BA").code("BA").build();
        CourseCharacteristics characteristicsLA = CourseCharacteristics.builder().type("LA").code("LD").build();
        CourseCharacteristics characteristicsOT = CourseCharacteristics.builder().type("CC").code("MI").build();
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
        courses.add(Course.builder().courseID("8").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsBA).courseAllowableCredit(allowableCredits).courseLevel("11").build());
        courses.add(Course.builder().courseID("9").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsBA).courseAllowableCredit(allowableCredits).courseLevel("11").build());
        courses.add(Course.builder().courseID("10").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsBA).courseAllowableCredit(allowableCredits).courseLevel("11").build());
        courses.add(Course.builder().courseID("11").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).courseLevel("11").build());
        courses.add(Course.builder().courseID("12").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseLevel("10").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("13").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseLevel("10").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("14").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseLevel("10").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("15").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseLevel("10").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("16").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("A").courseLevel("10").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("17").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("B").courseLevel("10").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("18").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("C").courseLevel("10").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("19").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("C").courseLevel("11").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());
        courses.add(Course.builder().courseID("21").startDate(LocalDate.now().minusYears(1)).completionEndDate(LocalDate.now().plusYears(1)).courseCode("C").courseLevel("11").courseCategory(characteristicsLA).courseAllowableCredit(allowableCredits).build());

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

    private void setSecurityContext() {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("GRAD_SYSTEM_COORDINATOR")
        );
        Authentication auth = mock(Authentication.class);
        doReturn(authorities).when(auth).getAuthorities();
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private List<ExaminableCourse> getExaminableCourses() {
        List<ExaminableCourse> examinableCourses = new ArrayList<>();
        examinableCourses.add(ExaminableCourse.builder().courseCode("A").courseLevel("10").examinableStart("1994-01").examinableEnd(LocalDate.now().plusYears(2).getYear()+"-"+String.format("%02d",LocalDate.now().getMonthValue())).programYear("1996").build());
        examinableCourses.add(ExaminableCourse.builder().courseCode("B").courseLevel("10").examinableStart("1994-01").examinableEnd(LocalDate.now().minusYears(2).getYear()+"-"+String.format("%02d",LocalDate.now().getMonthValue())).build());
        examinableCourses.add(ExaminableCourse.builder().courseCode("C").courseLevel("10").examinableStart("1994-01").examinableEnd(LocalDate.now().plusYears(2).getYear()+"-"+String.format("%02d",LocalDate.now().getMonthValue())).build());
        return examinableCourses;
    }

    private List<EquivalentOrChallengeCode> getEquivalentOrChallengeCodes() {
        List<EquivalentOrChallengeCode> equivalentOrChallengeCodes = new ArrayList<>();
        equivalentOrChallengeCodes.add(EquivalentOrChallengeCode.builder().equivalentOrChallengeCode("C").build());
        return equivalentOrChallengeCodes;
    }

    private List<FineArtsAppliedSkillsCode> getFineArtsAppliedSkillsCodes() {
        List<FineArtsAppliedSkillsCode> fineArtsAppliedSkillsCodes = new ArrayList<>();
        fineArtsAppliedSkillsCodes.add(FineArtsAppliedSkillsCode.builder().fineArtsAppliedSkillsCode("B").build());
        fineArtsAppliedSkillsCodes.add(FineArtsAppliedSkillsCode.builder().fineArtsAppliedSkillsCode("A").build());
        fineArtsAppliedSkillsCodes.add(FineArtsAppliedSkillsCode.builder().fineArtsAppliedSkillsCode("F").build());

        return fineArtsAppliedSkillsCodes;
    }

    @Test
    public void testTransferStudentCourse_SuccessfulTransfer() {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        StudentCourseEntity course = createStudentCourseEntity(sourceId, "123", "202201");
        course.setId(courseId);

        StudentCoursesTransferReq request = new StudentCoursesTransferReq();
        request.setSourceStudentId(sourceId);
        request.setTargetStudentId(targetId);
        request.setStudentCourseIdsToMove(List.of(courseId));

        Mockito.when(studentCourseRepository.findAllById(List.of(courseId))).thenReturn(List.of(course));
        Mockito.when(studentCourseRepository.findByStudentID(targetId)).thenReturn(Collections.emptyList());
        Mockito.when(studentCourseRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        GraduationStudentRecord dummyGradStatus = new GraduationStudentRecord();
        dummyGradStatus.setGpa("3.97");
        Mockito.doReturn(dummyGradStatus).when(graduationStatusService).getGraduationStatus(sourceId);
        Mockito.doReturn(dummyGradStatus).when(graduationStatusService).getGraduationStatus(targetId);

        List<ValidationIssue> result = studentCourseService.transferStudentCourse(request);

        assertThat(result).isEmpty();

        Mockito.verify(studentCourseRepository).saveAll(argThat(iterable -> {
            List<StudentCourseEntity> list = StreamSupport
                .stream(iterable.spliterator(), false)
                .toList();
            return list.size() == 1 && list.get(0).getStudentID().equals(targetId);
        }));

        Mockito.verify(historyService).createStudentCourseHistory(
            List.of(course), StudentCourseActivityType.USERCOURSEADD);
        Mockito.verify(historyService).createStudentCourseHistory(
            anyList(), eq(StudentCourseActivityType.USERCOURSEDEL));

        Mockito.verify(graduationStatusService).updateBatchFlagsForStudentCourses(targetId);
        Mockito.verify(graduationStatusService).updateBatchFlagsForStudentCourses(sourceId);
    }

    @Test
    public void testTransferStudentCourse_CourseNotFound() {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID missingCourseId = UUID.randomUUID();

        StudentCoursesTransferReq request = new StudentCoursesTransferReq();
        request.setSourceStudentId(sourceId);
        request.setTargetStudentId(targetId);
        request.setStudentCourseIdsToMove(List.of(missingCourseId));

        Mockito.when(studentCourseRepository.findById(missingCourseId)).thenReturn(Optional.empty());
        Mockito.when(studentCourseRepository.findByStudentID(targetId)).thenReturn(Collections.emptyList());

        GraduationStudentRecord dummyGradStatus = new GraduationStudentRecord();
        Mockito.doReturn(dummyGradStatus).when(graduationStatusService).getGraduationStatus(sourceId);
        Mockito.doReturn(dummyGradStatus).when(graduationStatusService).getGraduationStatus(targetId);

        List<ValidationIssue> result = studentCourseService.transferStudentCourse(request);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getValidationFieldName()).isEqualTo(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_NOT_FOUND.getCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferStudentCourse_SourceStudentNotFound() {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        StudentCoursesTransferReq request = new StudentCoursesTransferReq();
        request.setSourceStudentId(sourceId);
        request.setTargetStudentId(targetId);
        request.setStudentCourseIdsToMove(List.of(UUID.randomUUID()));

        Mockito.doThrow(new EntityNotFoundException("Not found"))
            .when(graduationStatusService)
            .getGraduationStatus(sourceId);

        studentCourseService.transferStudentCourse(request);
    }

    @Test
    public void testTransferStudentCourse_DuplicateCourseInTarget() {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        StudentCourseEntity courseToMove = createStudentCourseEntity(sourceId, "123", "2021S1");
        courseToMove.setId(courseId);

        StudentCourseEntity existingCourse = createStudentCourseEntity(targetId, "123", "2021S1");

        StudentCoursesTransferReq request = new StudentCoursesTransferReq();
        request.setSourceStudentId(sourceId);
        request.setTargetStudentId(targetId);
        request.setStudentCourseIdsToMove(List.of(courseId));

        Mockito.doReturn(new GraduationStudentRecord())
            .when(graduationStatusService)
            .getGraduationStatus(sourceId);
        Mockito.doReturn(new GraduationStudentRecord())
            .when(graduationStatusService)
            .getGraduationStatus(targetId);
        Mockito.when(studentCourseRepository.findById(courseId)).thenReturn(Optional.of(courseToMove));
        Mockito.when(studentCourseRepository.findByStudentID(targetId)).thenReturn(List.of(existingCourse));

        List<ValidationIssue> result = studentCourseService.transferStudentCourse(request);

        assertThat(result).isNotEmpty();
        assertThat(result.stream().anyMatch(issue ->
            issue.getValidationFieldName().equals(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_TRANSFER_COURSE_DUPLICATE.getCode())
        )).isTrue();
    }
}
