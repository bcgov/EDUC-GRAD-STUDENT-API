package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.dto.*;
import ca.bc.gov.educ.api.gradstudent.entity.GraduationStatusEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStatusRepository;
import ca.bc.gov.educ.api.gradstudent.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradStudentServiceTest {

    @Autowired
    EducGradStudentApiConstants constants;

    @Autowired
    GradStudentService gradStudentService;

    @MockBean
    WebClient webClient;

    @MockBean
    GraduationStatusRepository graduationStatusRepository;

    @MockBean
    private GraduationStatusTransformer graduationStatusTransformer;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock
    private WebClient.RequestBodySpec requestBodyMock;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock
    private WebClient.ResponseSpec responseMock;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetStudentFromStudentAPI() {
        // ID
        final UUID studentID = UUID.randomUUID();
        final String legalFirstName = "Legal";
        final String legalLastName = "Test";
        final String pen = "123456789";
        final String program = "2018-EN";
        final String gradStatus = "A";
        final String stdGrade = "12";
        final String mincode = "12345678";
        final String schoolName = "Test School";

        final Student student = new Student();
        student.setStudentID(studentID.toString());
        student.setPen(pen);
        student.setMincode(mincode);
        student.setLegalFirstName(legalFirstName);
        student.setLegalLastName(legalLastName);
        student.setGradeCode(stdGrade);
        student.setGradeYear("2020");
        student.setDemogCode("A");
        student.setPostalCode("V6S 1Z5");
        student.setLocalID("01234567");
        student.setMemo("test memo text");

        RestResponsePage<Student> response = new RestResponsePage<Student>(Arrays.asList(student));

        final ParameterizedTypeReference<RestResponsePage<Student>> studentResponseType = new ParameterizedTypeReference<RestResponsePage<Student>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(eq(this.constants.getPenStudentApiUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(studentResponseType)).thenReturn(Mono.just(response));

        // Graduation Status
        final GraduationStatus graduationStatus = new GraduationStatus();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen(pen);
        graduationStatus.setStudentStatus(gradStatus);
        graduationStatus.setStudentGrade(stdGrade);
        graduationStatus.setProgram(program);
        graduationStatus.setSchoolOfRecord(mincode);

        // Graduation Status Entity
        final GraduationStatusEntity graduationStatusEntity = new GraduationStatusEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setSchoolOfRecord(mincode);

        when(this.graduationStatusRepository.findByStudentID(studentID)).thenReturn(graduationStatusEntity);
        when(this.graduationStatusTransformer.transformToDTO(graduationStatusEntity)).thenReturn(graduationStatus);

        // School
        final School school = new School();
        school.setMinCode(mincode);
        school.setSchoolName(schoolName);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeUrl(),mincode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        var result = gradStudentService.getStudentFromStudentAPI(legalFirstName, legalLastName, null, null, null, null, null,
                mincode, null, null, null, 1, 10, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getGradSearchStudents().isEmpty()).isFalse();
        assertThat(result.getGradSearchStudents().size()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getNumberOfElements()).isEqualTo(1);

        GradSearchStudent responseStudent = result.getGradSearchStudents().get(0);
        assertThat(responseStudent.getStudentID()).isEqualTo(studentID.toString());
        assertThat(responseStudent.getLegalFirstName()).isEqualTo(legalFirstName);
        assertThat(responseStudent.getLegalLastName()).isEqualTo(legalLastName);
        assertThat(responseStudent.getProgram()).isEqualTo(program);
        assertThat(responseStudent.getStudentStatus()).isEqualTo(gradStatus);
        assertThat(responseStudent.getStudentGrade()).isEqualTo(stdGrade);
        assertThat(responseStudent.getSchoolOfRecordName()).isEqualTo(schoolName);
        // extra
        assertThat(responseStudent.getGradeCode()).isEqualTo(student.getGradeCode());
        assertThat(responseStudent.getGradeYear()).isEqualTo(student.getGradeYear());
        assertThat(responseStudent.getDemogCode()).isEqualTo(student.getDemogCode());
        assertThat(responseStudent.getPostalCode()).isEqualTo(student.getPostalCode());
        assertThat(responseStudent.getLocalID()).isEqualTo(student.getLocalID());
        assertThat(responseStudent.getMemo()).isEqualTo(student.getMemo());
    }

    @Test
    public void testGetStudentByPenFromStudentAPI() {
        // ID
        final UUID studentID = UUID.randomUUID();
        final String pen = "123456789";
        final String firstName = "FirstName";
        final String lastName = "LastName";
        final String program = "2018-EN";
        final String gradStatus = "A";
        final String stdGrade = "12";
        final String mincode = "12345678";
        final String schoolName = "Test School";

        // Grad Student
        final Student student = new Student();
        student.setStudentID(studentID.toString());
        student.setPen(pen);
        student.setLegalLastName(lastName);
        student.setLegalFirstName(firstName);
        student.setMincode(mincode);
        student.setSexCode("M");
        student.setGenderCode("M");
        student.setUsualFirstName("Usual First");
        student.setUsualLastName("Usual Last");
        student.setEmail("junit@test.com");
        student.setEmailVerified("Y");
        student.setStatusCode("A");
        student.setDob("1990-01-01");

        final ParameterizedTypeReference<List<Student>> studentResponseType = new ParameterizedTypeReference<List<Student>>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getPenStudentApiByPenUrl(),pen))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(studentResponseType)).thenReturn(Mono.just(Arrays.asList(student)));

        // Graduation Status
        final GraduationStatus graduationStatus = new GraduationStatus();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen(pen);
        graduationStatus.setStudentStatus(gradStatus);
        graduationStatus.setStudentGrade(stdGrade);
        graduationStatus.setProgram(program);
        graduationStatus.setSchoolOfRecord(mincode);

        // Graduation Status Entity
        final GraduationStatusEntity graduationStatusEntity = new GraduationStatusEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setSchoolOfRecord(mincode);

        when(this.graduationStatusRepository.findByStudentID(studentID)).thenReturn(graduationStatusEntity);
        when(this.graduationStatusTransformer.transformToDTO(graduationStatusEntity)).thenReturn(graduationStatus);

        // School
        final School school = new School();
        school.setMinCode(mincode);
        school.setSchoolName(schoolName);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeUrl(),mincode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        var result = gradStudentService.getStudentByPenFromStudentAPI(pen, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);

        GradSearchStudent responseStd = result.get(0);
        assertThat(responseStd.getStudentID()).isEqualTo(studentID.toString());
        assertThat(responseStd.getProgram()).isEqualTo(program);
        assertThat(responseStd.getStudentGrade()).isEqualTo(stdGrade);
        assertThat(responseStd.getStudentStatus()).isEqualTo(gradStatus);
        assertThat(responseStd.getSchoolOfRecord()).isEqualTo(mincode);
        assertThat(responseStd.getSchoolOfRecordName()).isEqualTo(schoolName);
        // extra
        assertThat(responseStd.getUsualFirstName()).isEqualTo(student.getUsualFirstName());
        assertThat(responseStd.getUsualLastName()).isEqualTo(student.getUsualLastName());
        assertThat(responseStd.getPen()).isEqualTo(student.getPen());
        assertThat(responseStd.getMincode()).isEqualTo(student.getMincode());
        assertThat(responseStd.getDob()).isEqualTo(student.getDob());
        assertThat(responseStd.getSexCode()).isEqualTo(student.getSexCode());
        assertThat(responseStd.getGenderCode()).isEqualTo(student.getGenderCode());
        assertThat(responseStd.getStatusCode()).isEqualTo(student.getStatusCode());
        assertThat(responseStd.getEmail()).isEqualTo(student.getEmail());
        assertThat(responseStd.getEmailVerified()).isEqualTo(student.getEmailVerified());
    }
}
