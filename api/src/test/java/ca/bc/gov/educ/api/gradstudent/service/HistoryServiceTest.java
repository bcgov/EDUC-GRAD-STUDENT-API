package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordHistoryRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramHistoryRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class HistoryServiceTest {

    @Autowired
    EducGradStudentApiConstants constants;

    @Autowired
    private HistoryService historyService;

    @MockBean
    private GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository;

    @MockBean
    private StudentOptionalProgramHistoryRepository studentOptionalProgramHistoryRepository;

    @MockBean
    GradValidation validation;

    @MockBean
    private WebClient webClient;

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

    // NATS
    @MockBean
    private NatsConnection natsConnection;

    @MockBean
    private Publisher publisher;

    @MockBean
    private Subscriber subscriber;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetStudentEditHistory() {
        // ID
        UUID studentID = UUID.randomUUID();

        List<GraduationStudentRecordHistoryEntity> histList = new ArrayList<>();
        GraduationStudentRecordHistoryEntity graduationStatusEntity = new GraduationStudentRecordHistoryEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-en");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setHistoryID(new UUID(1,1));
        graduationStatusEntity.setActivityCode("GRADALG");
        histList.add(graduationStatusEntity);
        when(graduationStudentRecordHistoryRepository.findByStudentID(studentID)).thenReturn(histList);
        var result = historyService.getStudentEditHistory(studentID);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public  void testGetStudentOptionalProgramHistory() {
        UUID studentID = UUID.randomUUID();
        List<StudentOptionalProgramHistoryEntity> histList = new ArrayList<>();
        StudentOptionalProgramHistoryEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramHistoryEntity();
        gradStudentOptionalProgramEntity.setStudentOptionalProgramID(new UUID(1,1));
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(new UUID(2,2));
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));
        gradStudentOptionalProgramEntity.setHistoryId(new UUID(3,3));
        gradStudentOptionalProgramEntity.setActivityCode("GRADALG");
        histList.add(gradStudentOptionalProgramEntity);

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(gradStudentOptionalProgramEntity.getOptionalProgramID());
        optionalProgram.setGraduationProgramCode("2018-en");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramNameUrl(),gradStudentOptionalProgramEntity.getOptionalProgramID()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        when(studentOptionalProgramHistoryRepository.findByStudentID(studentID)).thenReturn(histList);
        var result = historyService.getStudentOptionalProgramEditHistory(studentID,"accessToken");
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);

    }

    @Test
    public void testGetStudentHistoryByHistoryID() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID historyID = UUID.randomUUID();
        GraduationStudentRecordHistoryEntity graduationStatusEntity = new GraduationStudentRecordHistoryEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-en");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setHistoryID(new UUID(1,1));
        graduationStatusEntity.setActivityCode("GRADALG");
        when(graduationStudentRecordHistoryRepository.findById(historyID)).thenReturn(Optional.of(graduationStatusEntity));
        var result = historyService.getStudentHistoryByID(historyID);
        assertThat(result).isNotNull();
    }

    @Test
    public  void testGetStudentOptionalProgramHistoryByID() {
        UUID studentID = UUID.randomUUID();
        UUID historyID = UUID.randomUUID();
        StudentOptionalProgramHistoryEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramHistoryEntity();
        gradStudentOptionalProgramEntity.setStudentOptionalProgramID(new UUID(1,1));
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(new UUID(2,2));
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));
        gradStudentOptionalProgramEntity.setHistoryId(new UUID(3,3));
        gradStudentOptionalProgramEntity.setActivityCode("GRADALG");

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(gradStudentOptionalProgramEntity.getOptionalProgramID());
        optionalProgram.setGraduationProgramCode("2018-en");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramNameUrl(),gradStudentOptionalProgramEntity.getOptionalProgramID()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        when(studentOptionalProgramHistoryRepository.findById(historyID)).thenReturn(Optional.of(gradStudentOptionalProgramEntity));



        var result = historyService.getStudentOptionalProgramHistoryByID(historyID,"accessToken");
        assertThat(result).isNotNull();
    }
}
