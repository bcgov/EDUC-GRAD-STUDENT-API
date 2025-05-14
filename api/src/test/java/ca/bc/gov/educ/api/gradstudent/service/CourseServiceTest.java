package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CourseServiceTest extends BaseIntegrationTest {

    @Autowired
    CourseService courseService;

    @MockBean
    WebClient webClient;

    @Autowired
    EducGradStudentApiConstants constants;

    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.ResponseSpec responseMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;

    @Before
    public void setUp(){
        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
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
        var result = courseService.getLetterGrades("abc") ;
        assertNotNull(result);
    }

    @Test
    public void testGetLetterGrades_Empty() {

        final ParameterizedTypeReference<List<LetterGrade>> responseType = new ParameterizedTypeReference<>() {};

        when(this.requestHeadersUriMock.uri(eq(this.constants.getLetterGradesUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);

        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(Collections.emptyList()));
        var result = courseService.getLetterGrades("abc") ;
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetCourses() {
        CourseSearchRequest courseSearchRequest = new CourseSearchRequest();
        courseSearchRequest.setCourseIds(List.of("1"));
        when(webClient.post()).thenReturn(requestBodyUriMock);
        when(requestBodyUriMock.uri(constants.getCourseDetailSearchUrl())).thenReturn(requestBodyMock);
        when(requestBodyMock.bodyValue(courseSearchRequest)).thenReturn(requestHeadersMock);
        when(requestHeadersMock.headers(any())).thenReturn(requestHeadersMock);
        when(requestHeadersMock.retrieve()).thenReturn(responseMock);

        ParameterizedTypeReference<List<Course>> typeRef = new ParameterizedTypeReference<>() {};
        when(responseMock.bodyToMono(typeRef)).thenReturn(Mono.just(getCourses()));
        List<Course> result = courseService.getCourses(List.of("1"), "ABC");
        assertNotNull(result);
    }

    @Test
    public void testGetCourses_Empty() {
        final ParameterizedTypeReference<List<Course>> responseType = new ParameterizedTypeReference<>() {};

        when(this.requestHeadersUriMock.uri(eq(this.constants.getCourseDetailSearchUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);

        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(Collections.emptyList()));
        var result = courseService.getCourses(Collections.emptyList(),"ABC") ;
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetExaminableCourses() {
        final ParameterizedTypeReference<List<ExaminableCourse>> responseType = new ParameterizedTypeReference<>() {};

        when(this.requestHeadersUriMock.uri(eq(this.constants.getCourseExaminableSearchUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);

        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(getExaminableCourses()));
        var result = courseService.getExaminableCourses(List.of("1"),"ABC") ;
        assertNotNull(result);
    }

    @Test
    public void testGetExaminableCourses_Empty() {
        final ParameterizedTypeReference<List<LetterGrade>> responseType = new ParameterizedTypeReference<>() {};

        when(this.requestHeadersUriMock.uri(eq(this.constants.getCourseExaminableSearchUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);

        when(this.responseMock.bodyToMono(responseType)).thenReturn(Mono.just(Collections.emptyList()));
        var result = courseService.getExaminableCourses(Collections.emptyList(),"ABC") ;
        assertTrue(result.isEmpty());
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
