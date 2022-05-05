package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
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
    CommonService commonService;

    @MockBean
    WebClient webClient;

    @MockBean
    GraduationStudentRecordRepository graduationStatusRepository;
    @MockBean GraduationStatusTransformer graduationStatusTransformer;
    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;

    // NATS
    @MockBean NatsConnection natsConnection;
    @MockBean Publisher publisher;
    @MockBean Subscriber subscriber;

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

        RestResponsePage<Student> response = new RestResponsePage<>(List.of(student));

        final ParameterizedTypeReference<RestResponsePage<Student>> studentResponseType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(eq(this.constants.getPenStudentApiSearchUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(studentResponseType)).thenReturn(Mono.just(response));

        // Graduation Status
        final GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen(pen);
        graduationStatus.setStudentStatus(gradStatus);
        graduationStatus.setStudentGrade(stdGrade);
        graduationStatus.setProgram(program);
        graduationStatus.setSchoolOfRecord(mincode);

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
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

        StudentSearchRequest studentSearchRequest = StudentSearchRequest.builder().legalFirstName(legalFirstName).legalLastName(legalFirstName).mincode(mincode).build();
        var result = gradStudentService.getStudentFromStudentAPI(studentSearchRequest, 1, 10, "accessToken");

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
    public void testGetGRADStudents() {
        // ID
        final UUID studentID = UUID.randomUUID();
        final String pen = "123456789";
        final String legalFirstName = "FirstName";
        final String legalLastName = "LastName";
        final String program = "2018-EN";
        final String gradStatus = "A";
        final String stdGrade = "12";
        final String mincode = "12345678";
        final String schoolName = "Test School";

        // School
        final School school = new School();
        school.setMinCode(mincode);
        school.setSchoolName(schoolName);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeUrl(),mincode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

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

        // Graduation Status
        final GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen(pen);
        graduationStatus.setStudentStatus(gradStatus);
        graduationStatus.setStudentGrade(stdGrade);
        graduationStatus.setProgram(program);
        graduationStatus.setSchoolOfRecord(mincode);

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setSchoolOfRecord(mincode);

        when(this.graduationStatusRepository.findByStudentID(studentID)).thenReturn(graduationStatusEntity);
        when(this.graduationStatusTransformer.transformToDTO(graduationStatusEntity)).thenReturn(graduationStatus);

        org.springframework.data.domain.Page<GraduationStudentRecordEntity> pagedResult = new PageImpl<>(List.of(graduationStatusEntity));
        when(this.graduationStatusRepository.findAll(any(), any(Pageable.class))).thenReturn(pagedResult);

        RestResponsePage<Student> response = new RestResponsePage<>(List.of(student));
        final ParameterizedTypeReference<RestResponsePage<Student>> studentResponseType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(eq(this.constants.getPenStudentApiSearchUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(studentResponseType)).thenReturn(Mono.just(response));

        StudentSearchRequest studentSearchRequest = StudentSearchRequest.builder().legalFirstName(legalFirstName).legalLastName(legalFirstName)
                .mincode(mincode).gradProgram(program).build();
        var result = gradStudentService.getGRADStudents(studentSearchRequest, 1, 5, "accessToken");

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
    public void testGetStudentFromStudentAPIGradOnly() {
        // ID
        final UUID studentID = UUID.randomUUID();
        final String pen = "123456789";
        final String legalFirstName = "FirstName";
        final String legalLastName = "LastName";
        final String program = "2018-EN";
        final String gradStatus = "A";
        final String stdGrade = "12";
        final String mincode = "12345678";
        final String schoolName = "Test School";

        // School
        final School school = new School();
        school.setMinCode(mincode);
        school.setSchoolName(schoolName);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeUrl(),mincode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

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

        // Graduation Status
        final GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen(pen);
        graduationStatus.setStudentStatus(gradStatus);
        graduationStatus.setStudentGrade(stdGrade);
        graduationStatus.setProgram(program);
        graduationStatus.setSchoolOfRecord(mincode);

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setSchoolOfRecord(mincode);
        List<UUID> studentSubList = new ArrayList<>();
        studentSubList.add(graduationStatusEntity.getStudentID());


        when(this.graduationStatusRepository.findByStudentID(studentID)).thenReturn(graduationStatusEntity);
        when(this.graduationStatusTransformer.transformToDTO(graduationStatusEntity)).thenReturn(graduationStatus);
        when(this.graduationStatusRepository.findByStudentIDIn(studentSubList)).thenReturn(List.of(graduationStatusEntity));
        RestResponsePage<Student> response = new RestResponsePage<>(List.of(student));
        final ParameterizedTypeReference<RestResponsePage<Student>> studentResponseType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(eq(this.constants.getPenStudentApiSearchUrl()), any(Function.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(studentResponseType)).thenReturn(Mono.just(response));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getPenStudentApiByStudentIdUrl(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Student.class)).thenReturn(Mono.just(student));

        StudentSearchRequest studentSearchRequest = StudentSearchRequest.builder().legalFirstName(legalFirstName).legalLastName(legalFirstName)
                .mincode(mincode).build();
        var result = gradStudentService.getStudentFromStudentAPIGradOnly(studentSearchRequest, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getGradSearchStudents().isEmpty()).isFalse();
        assertThat(result.getGradSearchStudents().size()).isEqualTo(1);

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

        final ParameterizedTypeReference<List<Student>> studentResponseType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getPenStudentApiByPenUrl(),pen))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(studentResponseType)).thenReturn(Mono.just(List.of(student)));

        // Graduation Status
        final GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen(pen);
        graduationStatus.setStudentStatus(gradStatus);
        graduationStatus.setStudentGrade(stdGrade);
        graduationStatus.setProgram(program);
        graduationStatus.setSchoolOfRecord(mincode);

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
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

    @Test
    public void testGetStudentByStudentIDFromStudentAPI() {
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

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getPenStudentApiByStudentIdUrl(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Student.class)).thenReturn(Mono.just(student));

        // Graduation Status
        final GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen(pen);
        graduationStatus.setStudentStatus(gradStatus);
        graduationStatus.setStudentGrade(stdGrade);
        graduationStatus.setProgram(program);
        graduationStatus.setSchoolOfRecord(mincode);

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
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

        var result = gradStudentService.getStudentByStudentIDFromStudentAPI(studentID.toString(), "accessToken");

        assertThat(result).isNotNull();

        assertThat(result.getStudentID()).isEqualTo(studentID.toString());
        assertThat(result.getProgram()).isEqualTo(program);
        assertThat(result.getStudentGrade()).isEqualTo(stdGrade);
        assertThat(result.getStudentStatus()).isEqualTo(gradStatus);
        assertThat(result.getSchoolOfRecord()).isEqualTo(mincode);
        assertThat(result.getSchoolOfRecordName()).isEqualTo(schoolName);
        // extra
        assertThat(result.getUsualFirstName()).isEqualTo(student.getUsualFirstName());
        assertThat(result.getUsualLastName()).isEqualTo(student.getUsualLastName());
        assertThat(result.getPen()).isEqualTo(student.getPen());
        assertThat(result.getMincode()).isEqualTo(student.getMincode());
        assertThat(result.getDob()).isEqualTo(student.getDob());
        assertThat(result.getSexCode()).isEqualTo(student.getSexCode());
        assertThat(result.getGenderCode()).isEqualTo(student.getGenderCode());
        assertThat(result.getStatusCode()).isEqualTo(student.getStatusCode());
        assertThat(result.getEmail()).isEqualTo(student.getEmail());
        assertThat(result.getEmailVerified()).isEqualTo(student.getEmailVerified());
    }

    @Test
    public void testAddNewPenFromStudentAPI() {
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
        final StudentCreate student = new StudentCreate();
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
        student.setHistoryActivityCode("USERNEW");

        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getPenStudentApiUrl())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Student.class)).thenReturn(Mono.just(student));

        var result = gradStudentService.addNewPenFromStudentAPI(student, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getPen()).isEqualTo(pen);
    }

    @Test
    public void testGetStudentInfoFromGRAD() {
        // ID
        final UUID studentID = UUID.randomUUID();

        // Grad Student
        GraduationStudentRecordEntity rec = new GraduationStudentRecordEntity();
        rec.setStudentID(studentID);
        rec.setProgram("2018-EN");
        rec.setSchoolOfRecord("31121121");
        GraduationStudentRecordDistribution rec2 = new GraduationStudentRecordDistribution();
        rec2.setStudentID(studentID);
        rec2.setProgram("2018-EN");
        rec2.setSchoolOfRecord("31121121");


        Mockito.when(graduationStatusRepository.findByStudentID(UUID.fromString(studentID.toString()))).thenReturn(rec);
        when(this.graduationStatusTransformer.tToDForDistribution(rec)).thenReturn(rec2);
        var result = gradStudentService.getStudentByStudentIDFromGrad(studentID.toString());
        assertThat(result).isNotNull();
        assertThat(result.getProgram()).isEqualTo("2018-EN");
    }
}
