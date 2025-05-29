package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CourseCacheServiceTest extends BaseIntegrationTest {

    @Autowired
    CourseCacheService courseCacheService;

    @MockBean
    @Qualifier("studentApiClient")
    WebClient studentApiClient;

    @MockBean
    RESTService restService;

    @Autowired
    EducGradStudentApiConstants constants;

    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.ResponseSpec responseMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @MockBean ExamSpecialCaseCodeService examSpecialCaseCodeService;
    @MockBean EquivalentOrChallengeCodeService equivalentOrChallengeCodeService;

    @Before
    public void setUp(){
        when(this.studentApiClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(any(String.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.onStatus(any(), any())).thenReturn(this.responseMock);
    }

    @Test
    public void testGetLetterGrades() {

        final ParameterizedTypeReference<List<LetterGrade>> responseType = new ParameterizedTypeReference<>() {};

        when(this.requestHeadersUriMock.uri(eq(this.constants.getLetterGradesUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);

        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(getLetterGrades()));
        var result = courseCacheService.getLetterGradesFromCache();
        assertNotNull(result);
    }

    @Test
    public void testGetLetterGrades_NoError() {

        final ParameterizedTypeReference<List<LetterGrade>> responseType = new ParameterizedTypeReference<>() {};

        when(this.requestHeadersUriMock.uri(eq(this.constants.getLetterGradesUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);

        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(getLetterGrades()));
        Assertions.assertDoesNotThrow(() -> { courseCacheService.loadLetterGrades(); });
    }

    @Test
    public void testGetExaminableCourses() {
        final ParameterizedTypeReference<List<ExaminableCourse>> responseType = new ParameterizedTypeReference<>() {};

        when(this.requestHeadersUriMock.uri(eq(this.constants.getExaminableCourseDetailUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);

        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(getExaminableCourses()));
        var result = courseCacheService.getExaminableCoursesFromCache() ;
        assertNotNull(result);
    }

    @Test
    public void testGetExaminableCourses_NoError() {
        final ParameterizedTypeReference<List<ExaminableCourse>> responseType = new ParameterizedTypeReference<>() {};

        when(this.requestHeadersUriMock.uri(eq(this.constants.getExaminableCourseDetailUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);

        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(getExaminableCourses()));
        Assertions.assertDoesNotThrow(() -> { courseCacheService.getExaminableCoursesFromCache(); });
    }

    @Test
    public void testGetExamSpecialCaseCodes_NoError() {
        when(examSpecialCaseCodeService.findAll()).thenReturn(getExamSpecialCaseCodes());
        Assertions.assertDoesNotThrow(() -> { courseCacheService.loadExaminableCourses(); });
    }

    @Test
    public void testGetExamSpecialCaseCodesFromCache_NoError() {
        when(examSpecialCaseCodeService.findAll()).thenReturn(getExamSpecialCaseCodes());
        var result = courseCacheService.getExamSpecialCaseCodesFromCache() ;
        assertNotNull(result);
    }

    @Test
    public void testGetEquivalentOrChallengeCodes_NoError() {
        when(equivalentOrChallengeCodeService.findAll()).thenReturn(getEquivalentOrChallengeCodes());
        Assertions.assertDoesNotThrow(() -> { courseCacheService.getEquivalentOrChallengeCodesFromCache(); });
    }
    @Test
    public void testGetEquivalentOrChallengeCodesFromCache_NoError() {
        when(equivalentOrChallengeCodeService.findAll()).thenReturn(getEquivalentOrChallengeCodes());
        var result = courseCacheService.getEquivalentOrChallengeCodesFromCache() ;
        assertNotNull(result);
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
        List<ExaminableCourse> examinableCourses = new ArrayList<>();
        examinableCourses.add(ExaminableCourse.builder().courseCode("A").courseLevel("10").examinableStart("1994-01").examinableEnd(LocalDate.now().plusYears(2).getYear()+"-"+String.format("%02d",LocalDate.now().getMonthValue())).build());
        return examinableCourses;
    }

    private List<ExamSpecialCaseCode> getExamSpecialCaseCodes() {
        List<ExamSpecialCaseCode> examSpecialCaseCodes = new ArrayList<>();
        examSpecialCaseCodes.add(ExamSpecialCaseCode.builder().examSpecialCaseCode("C").build());
        return examSpecialCaseCodes;
    }

    private List<EquivalentOrChallengeCode> getEquivalentOrChallengeCodes() {
        List<EquivalentOrChallengeCode> equivalentOrChallengeCodes = new ArrayList<>();
        equivalentOrChallengeCodes.add(EquivalentOrChallengeCode.builder().equivalentOrChallengeCode("C").build());
        return equivalentOrChallengeCodes;
    }

}
