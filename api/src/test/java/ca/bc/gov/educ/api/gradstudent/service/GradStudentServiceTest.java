package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStudentRecordSubscriber;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GradStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordView;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import static org.mockito.Mockito.*;
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
    @Qualifier("studentApiClient")
    WebClient webClient;

    @MockBean
    FetchGradStatusSubscriber fetchGradStatusSubscriber;

    @MockBean
    FetchGradStudentRecordSubscriber fetchGradStudentRecordSubscriber;

    @MockBean
    GraduationStudentRecordRepository graduationStatusRepository;
    @MockBean GraduationStatusTransformer graduationStatusTransformer;
    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;

    @MockBean
    RESTService restService;

    // NATS
    @MockBean NatsConnection natsConnection;
    @MockBean Publisher publisher;
    @MockBean Subscriber subscriber;

    private static final String FAKE_PEN = "123456789";

    @Before
    public void setUp() {
        openMocks(this);
    }


    private static class TestGraduationCountProjection implements GraduationCountProjection {
        private Long currentGraduates;
        private Long currentNonGraduates;
        private UUID schoolOfRecordId;

        public TestGraduationCountProjection(Long currentGraduates, Long currentNonGraduates, UUID schoolOfRecordId) {
            this.schoolOfRecordId = schoolOfRecordId;
            this.currentGraduates = currentGraduates;
            this.currentNonGraduates = currentNonGraduates;
        }

        @Override
        public Long getCurrentGraduates() {
            return currentGraduates;
        }

        @Override
        public Long getCurrentNonGraduates() {
            return currentNonGraduates;
        }

        @Override
        public UUID getSchoolOfRecordId() {
            return schoolOfRecordId;
        }
    }


    @Test
    public void testGetStudentFromStudentAPI() {
        // ID
        final UUID studentID = UUID.randomUUID();
        final String legalFirstName = "Legal";
        final String legalLastName = "Test";
        final String pen = FAKE_PEN;
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

        when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), SchoolClob.class, webClient)).thenReturn(schoolClob);

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
        final String pen = FAKE_PEN;
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

        when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), SchoolClob.class, webClient)).thenReturn(schoolClob);

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
        final String pen = FAKE_PEN;
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
        final String pen = FAKE_PEN;
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
        certificate.setGradCertificateTypeCode("EI");
        certificate.setDistributionDate(new Date());

        final ParameterizedTypeReference<List<GradStudentCertificates>> certificatesType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getStudentCertificates(), studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(certificatesType)).thenReturn(Mono.just(List.of(certificate)));

        var studentDemog = graduationStatusService.getStudentDemographics(pen, "accessToken");
        assertThat(studentDemog).isNotNull();

    }

    @Test
    public void testStudentDemographics_whenCertificateType_isE() {
        testGetStudentByPenFromStudentAPI();

        final UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336df2204b");
        final String pen = FAKE_PEN;
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

        var studentDemog = graduationStatusService.getStudentDemographics(pen, "accessToken");
        assertThat(studentDemog).isNotNull();

    }

    @Test
    public void testStudentDemographics_whenCertificateType_isF() {
        testGetStudentByPenFromStudentAPI();

        final UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336df2204b");
        final String pen = FAKE_PEN;
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

        var studentDemog = graduationStatusService.getStudentDemographics(pen, "accessToken");
        assertThat(studentDemog).isNotNull();

    }

    @Test
    public void testGetStudentByPenFromStudentAPI() {
        // ID
        final UUID studentID = UUID.fromString("ac339d70-7649-1a2e-8176-4a336df2204b");
        final String pen = FAKE_PEN;
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

        when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), SchoolClob.class, webClient)).thenReturn(schoolClob);

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
        final String pen = FAKE_PEN;
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

        when(this.restService.get(String.format(constants.getPenStudentApiByStudentIdUrl(), studentID), Student.class, webClient)).thenReturn(student);

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

        when(this.restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), schoolId), SchoolClob.class, webClient)).thenReturn(schoolClob);

        var result = gradStudentService.getStudentByStudentIDFromStudentAPI(studentID.toString());

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
        final String pen = FAKE_PEN;
        final String firstName = "FirstName";
        final String lastName = "LastName";
        final String mincode = "12345678";

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
        final String pen = FAKE_PEN;
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

        var gradStudent = gradStudentService.getStudentByStudentIDFromStudentAPI(studentID.toString());
        assertThat(gradStudent).isNotNull();

        GraduationStudentRecordDistribution rec2 = new GraduationStudentRecordDistribution();
        rec2.setStudentID(studentID);
        rec2.setProgram(program);
        rec2.setSchoolOfRecordId(schoolId);

        when(this.graduationStatusTransformer.tToDForDistribution(graduationStatusEntity, gradStudent)).thenReturn(rec2);
        var result = gradStudentService.getStudentByStudentIDFromGrad(studentID.toString(), "accessToken");
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
        Student student = new Student();
        student.setStudentID(studentId.toString());

        StudentSearchRequest searchRequest = StudentSearchRequest.builder()
                .schoolIds(List.of(schoolId))
                .pens(List.of(FAKE_PEN))
                .studentIDs(result)
                .build();

        when(graduationStatusRepository.findBySchoolOfRecordIdIn(searchRequest.getSchoolIds())).thenReturn(result);
        when(restService.get(this.constants.getPenStudentApiByPenUrl() + searchRequest.getPens().get(0), Student.class, null)).thenReturn(student);
        when(graduationStatusRepository.findAllStudentGuids()).thenReturn(result);

        List<UUID> results = gradStudentService.getStudentIDsBySearchCriteriaOrAll(searchRequest);
        assertThat(results.get(0)).isEqualTo(studentId);
    }

    @Test
    public void testGetStudentIDsByPens_Given_PEN_Should_Return_Student_ID() {
        List<String> pens = List.of(FAKE_PEN);
        Student student = new Student();
        student.setStudentID(UUID.randomUUID().toString());
        when(restService.get(String.format(this.constants.getPenStudentApiByPenUrl(), pens.get(0)), Student[].class, null)).thenReturn(new Student[]{student});
        List<UUID> result = gradStudentService.getStudentIDsByPens(pens);
        assertThat(result.get(0)).isEqualTo(UUID.fromString(student.getStudentID()));
    }

    @Test
    public void testGetStudentIDsByPens_Given_Empty_Array_Should_Return_Empty(){
        assertThat(gradStudentService.getStudentIDsByPens(List.of())).isEmpty();
    }

    @Test
    public void testGetStudentIDsByPens_Given_Null_Return_Should_Return_Empty(){
        List<String> pens = List.of(FAKE_PEN);
        when(restService.get(String.format(this.constants.getPenStudentApiByPenUrl(), pens.get(0)), Student.class, null)).thenReturn(null);
        assertThat(gradStudentService.getStudentIDsByPens(List.of())).isEmpty();
    }

    @Test
    public void testResolveStudentPENsToUUIDs_Given_Valid_Pens_Should_Return_Map() {
        List<String> pens = List.of(FAKE_PEN, "321654987", "987654321");
        Student student = new Student();
        student.setStudentID(UUID.randomUUID().toString());
        Student student2 = new Student();
        student2.setStudentID(UUID.randomUUID().toString());
        Student student3 = new Student();
        student3.setStudentID(UUID.randomUUID().toString());
        when(restService.get(String.format(this.constants.getPenStudentApiByPenUrl(), pens.get(0)), Student[].class, null)).thenReturn(new Student[]{student});
        when(restService.get(String.format(this.constants.getPenStudentApiByPenUrl(), pens.get(1)), Student[].class, null)).thenReturn(new Student[]{student2});
        when(restService.get(String.format(this.constants.getPenStudentApiByPenUrl(), pens.get(2)), Student[].class, null)).thenReturn(new Student[]{student3});
        Map<String, UUID> result = gradStudentService.resolveStudentPENsToUUIDs(pens);
        assertThat(result).size().isEqualTo(pens.size());
        assertThat(result.get(pens.get(0))).isEqualByComparingTo(UUID.fromString(student.getStudentID()));
    }

    @Test
    public void testGetGraduationStudentRecord_GivenValidProgramCompletionDate_ExpectTrue() throws EntityNotFoundException {
        UUID studentID = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setProgramCompletionDate(new java.util.Date());
        when(graduationStatusRepository.findByStudentID(studentID, GradStudentRecord.class)).thenReturn(new GradStudentRecord(studentID, "2018-EN", new java.util.Date(),  UUID.randomUUID(), UUID.randomUUID(),"studentStatusCode", "{\"nonGradReasons\":null,\"graduated\":true}", "10"));
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

    @Test
    public void testGetGraduationCountsBySchools_WithNullOrEmptyList_ShouldReturnEmptyList() {
        List<GraduationCountProjection> resultWithNull = gradStudentService.getGraduationCountsBySchools(null);
        assertThat(resultWithNull).isNotNull().isEmpty();

        List<GraduationCountProjection> resultWithEmptyList = gradStudentService.getGraduationCountsBySchools(Collections.emptyList());
        assertThat(resultWithEmptyList).isNotNull().isEmpty();

        verify(graduationStatusRepository, never()).countCurrentGraduatesAndNonGraduatesBySchoolOfRecordIn(anyList());
    }

    @Test
    public void testGetGraduationCountsBySchools_WithNonEmptyList_ShouldReturnCountsFromRepository() {
        final UUID schoolId1 = UUID.randomUUID();
        final UUID schoolId2 = UUID.randomUUID();
        final List<UUID> schoolIds = Arrays.asList(schoolId1, schoolId2);

        List<GraduationCountProjection> expectedCounts = Arrays.asList(new TestGraduationCountProjection(100L, 50L, schoolId1), new TestGraduationCountProjection(15L, 10L, schoolId2));

        when(graduationStatusRepository.countCurrentGraduatesAndNonGraduatesBySchoolOfRecordIn(schoolIds))
                .thenReturn(expectedCounts);

        List<GraduationCountProjection> actualCounts = gradStudentService.getGraduationCountsBySchools(schoolIds);

        assertThat(actualCounts).isNotNull().isNotEmpty().isEqualTo(expectedCounts);

        verify(graduationStatusRepository, times(1)).countCurrentGraduatesAndNonGraduatesBySchoolOfRecordIn(schoolIds);
    }

    @Test
    public void testGetStudentIDsBySearchCriteriaOrAll_WithMultipleCriteriaIncludingProgramGradeSchool_ShouldCombineResults() {
        // Given
        UUID schoolId = UUID.randomUUID();
        UUID studentId1 = UUID.randomUUID();
        UUID studentId2 = UUID.randomUUID();
        UUID studentId3 = UUID.randomUUID();

        List<String> programs = List.of("1950");
        List<String> grades = List.of("12");
        List<UUID> schoolIds = List.of(schoolId);
        List<UUID> providedStudentIds = List.of(studentId1);

        StudentSearchRequest searchRequest = StudentSearchRequest.builder()
                .studentIDs(providedStudentIds)
                .programs(programs)
                .grades(grades)
                .schoolIds(schoolIds)
                .build();

        // When
        when(graduationStatusRepository.findCurrentStudentUUIDsByProgramInAndSchoolOfRecordInAndGradeIn(programs, grades, schoolIds))
                .thenReturn(List.of(studentId2));
        when(graduationStatusRepository.findBySchoolOfRecordIdIn(schoolIds))
                .thenReturn(List.of(studentId3));

        List<UUID> results = gradStudentService.getStudentIDsBySearchCriteriaOrAll(searchRequest);

        // Then
        verify(graduationStatusRepository).findCurrentStudentUUIDsByProgramInAndSchoolOfRecordInAndGradeIn(programs, grades, schoolIds);
        verify(graduationStatusRepository).findBySchoolOfRecordIdIn(schoolIds);
        assertThat(results).hasSize(3);
        assertThat(results).containsExactlyInAnyOrder(studentId1, studentId2, studentId3);
    }

    @Test
    public void testGetStudentIDsBySearchCriteriaOrAll_WithEmptyLists_ShouldNotCallProgramGradeSchoolMethod() {
        // Given
        StudentSearchRequest searchRequest = StudentSearchRequest.builder()
                .programs(List.of())
                .grades(List.of())
                .schoolIds(List.of())
                .build();

        // When
        List<UUID> results = gradStudentService.getStudentIDsBySearchCriteriaOrAll(searchRequest);

        // Then
        verify(graduationStatusRepository, never()).findCurrentStudentUUIDsByProgramInAndSchoolOfRecordInAndGradeIn(any(), any(), any());
        assertThat(results).isEmpty();
    }

    @Test
    public void testGetStudentIDsBySearchCriteriaOrAll_WithProgramsAndGradesButNoSchoolIds_ShouldNotCallProgramGradeSchoolMethod() {
        // Given
        List<String> programs = List.of("1950", "SCCP");
        List<String> grades = List.of("12", "AD");

        StudentSearchRequest searchRequest = StudentSearchRequest.builder()
                .programs(programs)
                .grades(grades)
                .build();

        // When
        List<UUID> results = gradStudentService.getStudentIDsBySearchCriteriaOrAll(searchRequest);

        // Then
        verify(graduationStatusRepository, never()).findCurrentStudentUUIDsByProgramInAndSchoolOfRecordInAndGradeIn(any(), any(), any());
        assertThat(results).isEmpty();
    }

    @Test
    public void testGetStudentIDsBySearchCriteriaOrAll_WithProgramsGradesAndSchoolIds_ShouldCallCorrectRepositoryMethod() {
        // Given
        UUID schoolId1 = UUID.randomUUID();
        UUID schoolId2 = UUID.randomUUID();
        UUID studentId1 = UUID.randomUUID();
        UUID studentId2 = UUID.randomUUID();

        List<String> programs = List.of("1950", "SCCP", "2018");
        List<String> grades = List.of("12", "AD");
        List<UUID> schoolIds = List.of(schoolId1, schoolId2);
        List<UUID> expectedStudentIds = List.of(studentId1, studentId2);

        StudentSearchRequest searchRequest = StudentSearchRequest.builder()
                .programs(programs)
                .grades(grades)
                .schoolIds(schoolIds)
                .build();

        // When
        when(graduationStatusRepository.findCurrentStudentUUIDsByProgramInAndSchoolOfRecordInAndGradeIn(programs, grades, schoolIds))
                .thenReturn(expectedStudentIds);
        when(graduationStatusRepository.findBySchoolOfRecordIdIn(schoolIds))
                .thenReturn(List.of());

        List<UUID> results = gradStudentService.getStudentIDsBySearchCriteriaOrAll(searchRequest);

        // Then
        verify(graduationStatusRepository).findCurrentStudentUUIDsByProgramInAndSchoolOfRecordInAndGradeIn(programs, grades, schoolIds);
        verify(graduationStatusRepository).findBySchoolOfRecordIdIn(schoolIds);
        assertThat(results).hasSize(2);
        assertThat(results).containsExactlyInAnyOrder(studentId1, studentId2);
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
