package ca.bc.gov.educ.api.gradstudent.messaging.jetstream.v2;

import ca.bc.gov.educ.api.gradstudent.constant.Topics;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GraduationStudentRecordGradStatus;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.v2.GraduationStudentGradStatusRequest;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.service.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.JsonTransformer;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.nats.client.Connection;
import io.nats.client.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FetchGradStatusSubscriberTest extends BaseIntegrationTest {

    @Mock
    private Message mockMessage;

    @InjectMocks
    FetchGradStatusSubscriber fetchGradStatusSubscriberv2;

    @Autowired
    EducGradStudentApiConstants constants;
    @Autowired GraduationStatusService graduationStatusService;
    @MockBean
    GradStudentService gradStudentService;
    @MockBean
    HistoryService historyService;
    @Autowired
    GradStudentReportService gradStudentReportService;
    @Autowired
    JsonTransformer jsonTransformer;
    @MockBean GraduationStudentRecordRepository graduationStatusRepository;
    @MockBean
    StudentOptionalProgramRepository gradStudentOptionalProgramRepository;
    @MockBean
    StudentCareerProgramRepository gradStudentCareerProgramRepository;
    @MockBean
    ReportGradStudentDataRepository reportGradStudentDataRepository;
    @MockBean
    StudentNonGradReasonRepository studentNonGradReasonRepository;
    @MockBean GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository;
    @MockBean
    CommonService commonService;
    @MockBean
    GradValidation validation;
    @MockBean
    @Qualifier("studentApiClient")
    WebClient webClient;

    @MockBean
    ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber fetchGradStatusSubscriber;
    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;
    // NATS
    @MockBean Connection connection;
    @MockBean NatsConnection natsConnection;
    @MockBean Publisher publisher;
    @MockBean Subscriber subscriber;

    @MockBean
    GraduationStudentRecordSearchRepository graduationStudentRecordSearchRepository;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        openMocks(this);
        when(natsConnection.connection()).thenReturn(connection);
        Field field1 = FetchGradStatusSubscriber.class.getDeclaredField("graduationStatusService");
        field1.setAccessible(true);
        field1.set(fetchGradStatusSubscriberv2, graduationStatusService);
        Field field2 = FetchGradStatusSubscriber.class.getDeclaredField("natsConnection");
        field2.setAccessible(true);
        field2.set(fetchGradStatusSubscriberv2, natsConnection.connection());
    }


    @Test
    public void test_FetchGradStatusSubscriber_Graduated_Success() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        final Event event = prepareEventMessage(studentID, LocalDate.now());
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setProgramCompletionDate(new java.util.Date());
        GraduationStudentRecordGradStatus graduationStudentRecordGradStatus = new GraduationStudentRecordGradStatus(studentID, "2018-EN", new java.util.Date());
        when(graduationStatusRepository.findByStudentID(studentID, GraduationStudentRecordGradStatus.class)).thenReturn(graduationStudentRecordGradStatus);
        when(graduationStatusService.getGraduationStatusProjection(studentID)).thenReturn(graduationStudentRecordGradStatus);
        assertDoesNotThrow(() -> { fetchGradStatusSubscriberv2.onMessage(mockMessage); });
    }

    @Test
    public void test_FetchGradStatusSubscriber_NotGraduated_Success() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        final Event event = prepareEventMessage(studentID, LocalDate.now().minusDays(1));
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setProgramCompletionDate(new java.util.Date());
        GraduationStudentRecordGradStatus graduationStudentRecordGradStatus = new GraduationStudentRecordGradStatus(studentID, "2018-EN", new java.util.Date());
        when(graduationStatusRepository.findByStudentID(studentID, GraduationStudentRecordGradStatus.class)).thenReturn(graduationStudentRecordGradStatus);
        when(graduationStatusService.getGraduationStatusProjection(studentID)).thenReturn(graduationStudentRecordGradStatus);
        assertDoesNotThrow(() -> { fetchGradStatusSubscriberv2.onMessage(mockMessage); });
    }

    @Test
    public void test_FetchGradStatusSubscriber_NoCompletionDate_Success() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        final Event event = prepareEventMessage(studentID, LocalDate.now().minusDays(1));
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setProgramCompletionDate(new java.util.Date());
        GraduationStudentRecordGradStatus graduationStudentRecordGradStatus = new GraduationStudentRecordGradStatus(studentID, "2018-EN", null);
        when(graduationStatusRepository.findByStudentID(studentID, GraduationStudentRecordGradStatus.class)).thenReturn(graduationStudentRecordGradStatus);
        when(graduationStatusService.getGraduationStatusProjection(studentID)).thenReturn(graduationStudentRecordGradStatus);
        assertDoesNotThrow(() -> { fetchGradStatusSubscriberv2.onMessage(mockMessage); });
    }

    @Test
    public void test_FetchGradStatusSubscriber_404() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        final Event event = prepareEventMessage(studentID, LocalDate.now().minusDays(1));
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setProgramCompletionDate(new java.util.Date());
        GraduationStudentRecordGradStatus graduationStudentRecordGradStatus = new GraduationStudentRecordGradStatus(UUID.randomUUID(), "2018-EN", new java.util.Date());
        when(graduationStatusRepository.findByStudentID(studentID, GraduationStudentRecordGradStatus.class)).thenReturn(graduationStudentRecordGradStatus);
        when(graduationStatusService.getGraduationStatusProjection(studentID)).thenReturn(null);
        assertDoesNotThrow(() -> { fetchGradStatusSubscriberv2.onMessage(mockMessage); });
    }

    @Test
    public void test_FetchGradStatusSubscriber_400() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        final Event event = prepareEventMessage(studentID, null);
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setProgramCompletionDate(new java.util.Date());
        GraduationStudentRecordGradStatus graduationStudentRecordGradStatus = new GraduationStudentRecordGradStatus(studentID, "2018-EN", new java.util.Date());
        when(graduationStatusRepository.findByStudentID(studentID, GraduationStudentRecordGradStatus.class)).thenReturn(graduationStudentRecordGradStatus);
        when(graduationStatusService.getGraduationStatusProjection(studentID)).thenReturn(null);
        assertDoesNotThrow(() -> { fetchGradStatusSubscriberv2.onMessage(mockMessage); });
    }

    @Test
    public void testHandleEvent_TopicName_verification() {
        assertThat(fetchGradStatusSubscriberv2.getTopic()).isEqualTo(Topics.GRAD_STUDENT_API_FETCH_GRAD_STATUS_TOPIC_V2.toString());
    }

    Event prepareEventMessage(UUID studentID, LocalDate graduationDate) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        GraduationStudentGradStatusRequest graduationStudentGradStatusRequest = new GraduationStudentGradStatusRequest();
        graduationStudentGradStatusRequest.setDate(graduationDate);
        graduationStudentGradStatusRequest.setStudentID(studentID);

        return Event.builder().eventPayload(objectMapper.writeValueAsString(graduationStudentGradStatusRequest)).build();
    }
}

