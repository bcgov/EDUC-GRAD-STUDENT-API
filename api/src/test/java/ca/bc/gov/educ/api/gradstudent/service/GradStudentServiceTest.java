package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GradStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordView;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GradStudentServiceTest extends BaseIntegrationTest {

    @Autowired
    EducGradStudentApiConstants constants;

    @Autowired
    GradStudentService gradStudentService;

    @Autowired
    GraduationStatusService graduationStatusService;

    @MockBean
    CommonService commonService;

    @MockBean
    WebClient webClient;

    @MockBean
    FetchGradStatusSubscriber fetchGradStatusSubscriber;

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
        final UUID schoolId = UUID.randomUUID();
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
        graduationStatus.setSchoolOfRecordId(schoolId);

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setSchoolOfRecordId(schoolId);

        when(this.graduationStatusRepository.findByStudentID(studentID)).thenReturn(graduationStatusEntity);
        when(this.graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(graduationStatusEntity)).thenReturn(graduationStatus);

        // SchoolClob
        final SchoolClob schoolClob = new SchoolClob();
        schoolClob.setSchoolId(schoolId.toString());
        schoolClob.setMinCode(mincode);
        schoolClob.setSchoolName(schoolName);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(SchoolClob.class)).thenReturn(Mono.just(schoolClob));

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
        final UUID schoolId = UUID.randomUUID();
        final String schoolName = "Test School";

        // SchoolClob
        final SchoolClob schoolClob = new SchoolClob();
        schoolClob.setSchoolId(schoolId.toString());
        schoolClob.setMinCode(mincode);
        schoolClob.setSchoolName(schoolName);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(SchoolClob.class)).thenReturn(Mono.just(schoolClob));

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
        graduationStatus.setSchoolOfRecordId(schoolId);

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setSchoolOfRecordId(schoolId);

        when(this.graduationStatusRepository.findByStudentID(studentID)).thenReturn(graduationStatusEntity);
        when(this.graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(graduationStatusEntity)).thenReturn(graduationStatus);

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
                .mincode(mincode).schoolId(schoolId).gradProgram(program).build();
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
        assertThat(responseStudent.getSchoolOfRecordId()).isEqualTo(schoolId.toString());
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
        final UUID schoolId = UUID.randomUUID();
        final String schoolName = "Test School";

        // SchoolClob
        final SchoolClob schoolClob = new SchoolClob();
        schoolClob.setSchoolId(schoolId.toString());
        schoolClob.setMinCode(mincode);
        schoolClob.setSchoolName(schoolName);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(SchoolClob.class)).thenReturn(Mono.just(schoolClob));

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
        graduationStatus.setSchoolOfRecordId(schoolId);

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setSchoolOfRecordId(schoolId);

        final GraduationStudentRecordView graduationStatusView = new GraduationStudentRecordView() {
            @Override
            public String getProgram() {
                return program;
            }

            @Override
            public Date getProgramCompletionDate() {
                return null;
            }

            @Override
            public String getGpa() {
                return null;
            }

            @Override
            public String getHonoursStanding() {
                return null;
            }

            @Override
            public String getRecalculateGradStatus() {
                return null;
            }

            @Override
            public String getStudentGrade() {
                return stdGrade;
            }

            @Override
            public String getStudentStatus() {
                return gradStatus;
            }

            @Override
            public UUID getStudentID() {
                return studentID;
            }

            @Override
            public String getRecalculateProjectedGrad() {
                return null;
            }

            @Override
            public Long getBatchId() {
                return null;
            }

            @Override
            public String getConsumerEducationRequirementMet() {
                return null;
            }

            @Override
            public String getStudentCitizenship() {
                return null;
            }

            @Override
            public Date getAdultStartDate() {
                return null;
            }

            @Override
            public String getStudentProjectedGradData() {
                return null;
            }

            @Override
            public UUID getSchoolOfRecordId() {
                return schoolId;
            }

            @Override
            public UUID getSchoolAtGradId() {
                return null;
            }

            @Override
            public LocalDateTime getCreateDate() {
                return null;
            }

            @Override
            public LocalDateTime getUpdateDate() {
                return null;
            }
        };

        List<UUID> studentSubList = new ArrayList<>();
        studentSubList.add(graduationStatusView.getStudentID());

        when(this.graduationStatusRepository.findByStudentID(studentID)).thenReturn(graduationStatusEntity);
        when(this.graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(graduationStatusEntity)).thenReturn(graduationStatus);
        when(this.graduationStatusRepository.findByStudentIDIn(studentSubList)).thenReturn(List.of(graduationStatusView));
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
        assertThat(responseStudent.getSchoolOfRecordId()).isEqualTo(schoolId.toString());
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
    public void testStudentDemographics() {
        testGetStudentByPenFromStudentAPI();

        final UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336df2204b");
        final String pen = "123456789";
        final String program = "2018-EN";
        final String gradStatus = "A";
        final String stdGrade = "12";
        final UUID schoolId = UUID.randomUUID();
        final String schoolName = "Test School";
        final UUID schoolAtGradID = UUID.randomUUID();

        String graduationData = readFile("json/studentGradData.json");

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setProgramCompletionDate(new Date());
        graduationStatusEntity.setSchoolOfRecordId(schoolId);
        graduationStatusEntity.setSchoolAtGradId(schoolAtGradID);
        graduationStatusEntity.setStudentGradData(graduationData);

        when(this.graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));

        SchoolClob schoolClob = new SchoolClob();
        schoolClob.setSchoolId(schoolId.toString());
        schoolClob.setMinCode("12345678");
        schoolClob.setSchoolName(schoolName);
        schoolClob.setSchoolCategoryLegacyCode("02");
        schoolClob.setSchoolCategoryCode("INDEPEND");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolId))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(SchoolClob.class)).thenReturn(Mono.just(schoolClob));

        GradStudentCertificates certificate = new GradStudentCertificates();
        certificate.setStudentID(studentID);
        certificate.setPen(pen);
        certificate.setGradCertificateTypeCode("EI");
        certificate.setDistributionDate(new Date());

        final ParameterizedTypeReference<List<GradStudentCertificates>> certificatesType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getStudentCertificates(), studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(certificatesType)).thenReturn(Mono.just(List.of(certificate)));

        School school = new School();
        school.setSchoolId(schoolId.toString());
        school.setMincode("07171040");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolBySchoolIdUrl(), graduationStatusEntity.getSchoolAtGradId(), "accessToken"))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        var studentDemog = graduationStatusService.getStudentDemographics(pen, "accessToken");
        assertThat(studentDemog).isNotNull();

    }

    @Test
    public void testStudentDemographics_whenCertificateType_isE() {
        testGetStudentByPenFromStudentAPI();

        final UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336df2204b");
        final String pen = "123456789";
        final String firstName = "FirstName";
        final String lastName = "LastName";
        final String program = "2018-EN";
        final String gradStatus = "A";
        final String stdGrade = "12";
        final UUID schoolId = UUID.randomUUID();
        final String schoolName = "Test School";

        String graduationData = readFile("json/studentGradData.json");

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setProgramCompletionDate(new Date());
        graduationStatusEntity.setSchoolOfRecordId(schoolId);
        graduationStatusEntity.setStudentGradData(graduationData);

        when(this.graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));

        SchoolClob schoolClob = new SchoolClob();
        schoolClob.setSchoolId(schoolId.toString());
        schoolClob.setMinCode("12345678");
        schoolClob.setSchoolName(schoolName);
        schoolClob.setSchoolCategoryLegacyCode("02");
        schoolClob.setSchoolCategoryCode("INDEPEND");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolId))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(SchoolClob.class)).thenReturn(Mono.just(schoolClob));

        GradStudentCertificates certificate = new GradStudentCertificates();
        certificate.setStudentID(studentID);
        certificate.setPen(pen);
        certificate.setGradCertificateTypeCode("E");
        certificate.setDistributionDate(new Date());

        final ParameterizedTypeReference<List<GradStudentCertificates>> certificatesType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getStudentCertificates(), studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(certificatesType)).thenReturn(Mono.just(List.of(certificate)));

        School school = new School();
        school.setSchoolId(schoolId.toString());
        school.setMincode("07171040");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolBySchoolIdUrl(), graduationStatusEntity.getSchoolAtGradId(), "accessToken"))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        var studentDemog = graduationStatusService.getStudentDemographics(pen, "accessToken");
        assertThat(studentDemog).isNotNull();

    }

    @Test
    public void testStudentDemographics_whenCertificateType_isF() {
        testGetStudentByPenFromStudentAPI();

        final UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336df2204b");
        final String pen = "123456789";
        final String firstName = "FirstName";
        final String lastName = "LastName";
        final String program = "2018-EN";
        final String gradStatus = "A";
        final String stdGrade = "12";
        final UUID schoolId = UUID.randomUUID();
        final String schoolName = "Test School";

        String graduationData = readFile("json/studentGradData.json");

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setProgramCompletionDate(new Date());
        graduationStatusEntity.setSchoolOfRecordId(schoolId);
        graduationStatusEntity.setStudentGradData(graduationData);

        when(this.graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));

        SchoolClob schoolClob = new SchoolClob();
        schoolClob.setSchoolId(schoolId.toString());
        schoolClob.setMinCode("12345678");
        schoolClob.setSchoolName(schoolName);
        schoolClob.setSchoolCategoryLegacyCode("02");
        schoolClob.setSchoolCategoryCode("INDEPEND");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolId))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(SchoolClob.class)).thenReturn(Mono.just(schoolClob));

        GradStudentCertificates certificate = new GradStudentCertificates();
        certificate.setStudentID(studentID);
        certificate.setPen(pen);
        certificate.setGradCertificateTypeCode("F");
        certificate.setDistributionDate(new Date());

        final ParameterizedTypeReference<List<GradStudentCertificates>> certificatesType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getStudentCertificates(), studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(certificatesType)).thenReturn(Mono.just(List.of(certificate)));

        School school = new School();
        school.setSchoolId(schoolId.toString());
        school.setMincode("07171040");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolBySchoolIdUrl(), graduationStatusEntity.getSchoolAtGradId(), "accessToken"))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        var studentDemog = graduationStatusService.getStudentDemographics(pen, "accessToken");
        assertThat(studentDemog).isNotNull();

    }

    @Test
    public void testGetStudentByPenFromStudentAPI() {
        // ID
        final UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336df2204b");
        final String pen = "123456789";
        final String firstName = "FirstName";
        final String lastName = "LastName";
        final String program = "2018-EN";
        final String gradStatus = "A";
        final String stdGrade = "12";
        final String mincode = "12345678";
        final UUID schoolId = UUID.randomUUID();
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
        graduationStatus.setSchoolOfRecordId(schoolId);

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setSchoolOfRecordId(schoolId);

        when(this.graduationStatusRepository.findByStudentID(studentID)).thenReturn(graduationStatusEntity);
        when(this.graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(graduationStatusEntity)).thenReturn(graduationStatus);

        // SchoolClob
        final SchoolClob schoolClob = new SchoolClob();
        schoolClob.setSchoolId(schoolId.toString());
        schoolClob.setMinCode(mincode);
        schoolClob.setSchoolName(schoolName);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolClobBySchoolIdUrl(),schoolId))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(SchoolClob.class)).thenReturn(Mono.just(schoolClob));

        var result = gradStudentService.getStudentByPenFromStudentAPI(pen, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);

        GradSearchStudent responseStd = result.get(0);
        assertThat(responseStd.getStudentID()).isEqualTo(studentID.toString());
        assertThat(responseStd.getProgram()).isEqualTo(program);
        assertThat(responseStd.getStudentGrade()).isEqualTo(stdGrade);
        assertThat(responseStd.getStudentStatus()).isEqualTo(gradStatus);
        assertThat(responseStd.getSchoolOfRecordId()).isEqualTo(schoolId.toString());
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
        final UUID schoolId = UUID.randomUUID();
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
        graduationStatus.setSchoolOfRecordId(schoolId);

        // Graduation Status Entity
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(gradStatus);
        graduationStatusEntity.setStudentGrade(stdGrade);
        graduationStatusEntity.setProgram(program);
        graduationStatusEntity.setSchoolOfRecordId(schoolId);

        when(this.graduationStatusRepository.findByStudentID(studentID)).thenReturn(graduationStatusEntity);
        when(this.graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(graduationStatusEntity)).thenReturn(graduationStatus);

        // SchoolClob
        final SchoolClob schoolClob = new SchoolClob();
        schoolClob.setSchoolId(schoolId.toString());
        schoolClob.setMinCode(mincode);
        schoolClob.setSchoolName(schoolName);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(SchoolClob.class)).thenReturn(Mono.just(schoolClob));

        var result = gradStudentService.getStudentByStudentIDFromStudentAPI(studentID.toString(), "accessToken");

        assertThat(result).isNotNull();

        assertThat(result.getStudentID()).isEqualTo(studentID.toString());
        assertThat(result.getProgram()).isEqualTo(program);
        assertThat(result.getStudentGrade()).isEqualTo(stdGrade);
        assertThat(result.getStudentStatus()).isEqualTo(gradStatus);
        assertThat(result.getSchoolOfRecord()).isEqualTo(mincode);
        assertThat(result.getSchoolOfRecordId()).isEqualTo(schoolId.toString());
        assertThat(result.getSchoolOfRecordName()).isEqualTo(schoolName);
        assertThat(result.getMincode()).isEqualTo(student.getMincode());
        // extra
        assertThat(result.getUsualFirstName()).isEqualTo(student.getUsualFirstName());
        assertThat(result.getUsualLastName()).isEqualTo(student.getUsualLastName());
        assertThat(result.getPen()).isEqualTo(student.getPen());
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
        final UUID schoolId = UUID.randomUUID();

        // Grad Student
        GraduationStudentRecordEntity rec = new GraduationStudentRecordEntity();
        rec.setStudentID(studentID);
        rec.setProgram("2018-EN");
        rec.setSchoolOfRecordId(schoolId);
        GraduationStudentRecordDistribution rec2 = new GraduationStudentRecordDistribution();
        rec2.setStudentID(studentID);
        rec2.setProgram("2018-EN");
        rec2.setSchoolOfRecordId(schoolId);


        Mockito.when(graduationStatusRepository.findByStudentID(UUID.fromString(studentID.toString()))).thenReturn(rec);
        when(this.graduationStatusTransformer.tToDForDistribution(rec)).thenReturn(rec2);
        var result = gradStudentService.getStudentByStudentIDFromGrad(studentID.toString());
        assertThat(result).isNotNull();
        assertThat(result.getProgram()).isEqualTo("2018-EN");
    }

    @Test
    public void testGetStudentIDsByStatusCode_whenStatusCode_is_notProvided() {
        // ID
        final UUID studentID = UUID.randomUUID();
        var result = gradStudentService.getStudentIDsByStatusCode(Arrays.asList(studentID), "");
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetStudentIDsByStatusCode_whenStudentIDs_are_notProvided() {
        var result = gradStudentService.getStudentIDsByStatusCode(new ArrayList<>(), "DEC");
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetStudentIDsByStatusCode() {
        // ID 1
        final UUID studentID1 = UUID.randomUUID();
        // ID 2
        final UUID studentID2 = UUID.randomUUID();

        Mockito.when(graduationStatusRepository.filterGivenStudentsByStatusCode(Arrays.asList(studentID1, studentID2), "DEC")).thenReturn(Arrays.asList(studentID1));

        var result = gradStudentService.getStudentIDsByStatusCode(Arrays.asList(studentID1, studentID2), "DEC");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).isEqualTo(studentID1);
    }

    @Test
    public void testGetStudentIDsBySearchCriterias() {
        UUID schoolId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        List<UUID> result = List.of(studentId);

        StudentSearchRequest searchRequest = StudentSearchRequest.builder()
                .schoolIds(List.of(schoolId))
                .pens(List.of("12345678"))
                .studentIDs(result)
                .build();

        when(graduationStatusRepository.findBySchoolOfRecordIdIn(searchRequest.getSchoolIds())).thenReturn(result);
        when(graduationStatusRepository.findStudentIDsByPenIn(searchRequest.getPens())).thenReturn(result);
        when(graduationStatusRepository.findAllStudentGuids()).thenReturn(result);

        List<UUID> results = gradStudentService.getStudentIDsBySearchCriteriaOrAll(searchRequest);
        assertThat(results).isNotEmpty();
    }

    @Test
    public void testGetGraduationStudentRecord_GivenValidProgramCompletionDate_ExpectTrue() throws EntityNotFoundException {
        UUID studentID = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setProgramCompletionDate(new java.util.Date());
        when(graduationStatusRepository.findByStudentID(studentID, GradStudentRecord.class)).thenReturn(new GradStudentRecord(studentID, "2018-EN", new java.util.Date(), "schoolOfRecord", UUID.randomUUID(), "studentStatusCode", "{\"nonGradReasons\":null,\"graduated\":true}"));
        GradStudentRecord result = gradStudentService.getGraduationStudentRecord(studentID);
        assertNotNull(result);
    }

    @Test
    public void testGetGraduationStudentRecord_givenNotFound_ExpectEntityNotFoundExcetpion() {
        UUID studentID = UUID.randomUUID();
        when(graduationStatusRepository.findByStudentID(studentID, GradStudentRecord.class)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> {
            gradStudentService.getGraduationStudentRecord(studentID);
        });
    }

    @Test
    public void testGetGraduationStudentRecord_GivenRecordNotFound_ExpectEntityNotFoundException() {
        UUID studentID = UUID.randomUUID();
        when(graduationStatusRepository.findByStudentID(studentID, GradStudentRecord.class)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> gradStudentService.getGraduationStudentRecord(studentID));
    }

    @Test
    public void testParseGraduationStatus_GivenNullInput_ExpectFalse() {
        String studentProjectedGradData = null;
        Boolean result = gradStudentService.parseGraduationStatus(studentProjectedGradData);
        assertFalse("Expected false for null input", result);
    }

    @Test
    public void testParseGraduationStatus_GivenEmptyInput_ExpectFalse() {
        String studentProjectedGradData = "";
        Boolean result = gradStudentService.parseGraduationStatus(studentProjectedGradData);
        assertFalse("Expected false for empty input", result);
    }

    @Test
    public void testParseGraduationStatus_GivenMalformedJson_ExpectFalse() {
        String malformedJson = "{invalid-json}";
        Boolean result = gradStudentService.parseGraduationStatus(malformedJson);
        assertFalse("Expected false for malformed JSON", result);
    }

    @SneakyThrows
    protected Object createDataObjectFromJson(String jsonPath, Class<?> clazz) {
        String json = readFile(jsonPath);
        return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).readValue(json, clazz);
    }

    @SneakyThrows
    protected String readFile(String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(path);
        return readInputStream(inputStream);
    }

    private String readInputStream(InputStream is) throws Exception {
        StringBuffer sb = new StringBuffer();
        InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
