package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventType;
import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.DemographicStudent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import ca.bc.gov.educ.api.gradstudent.service.event.EventHandlerService;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.constant.Topics.GRAD_STUDENT_API_TOPIC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Slf4j
class EventHandlerServiceTest extends BaseIntegrationTest {
    @Autowired
    EventHandlerService eventHandlerService;
    @Autowired
    RestUtils restUtils;
    @MockBean
    @Qualifier("studentApiClient")
    WebClient webClient;
    @Autowired
    GraduationStudentRecordRepository graduationStudentRecordRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        graduationStudentRecordRepository.deleteAll();
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_DEM_DATA__whenNoStudentExist_shouldCreateStudentWithEventOutcome_DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var demStudent = createMockDemographicStudent("N", "REGULAR");
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(createmockStudent());
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_DEM_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(demStudent))
                .build();
        var response = eventHandlerService.handleProcessStudentDemDataEvent(event);
        assertThat(response).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_DEM_DATA__whenNoStudentExistAndIsSummer_shouldCreateStudentWithEventOutcome_DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var demStudent = createMockDemographicStudent("Y", "CSF");
        var studentFromApi = createmockStudent();
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentFromApi);
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_DEM_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(demStudent))
                .build();
        var response = eventHandlerService.handleProcessStudentDemDataEvent(event);
        assertThat(response).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);

        var student = graduationStudentRecordRepository.findOptionalByStudentID(UUID.fromString(studentFromApi.getStudentID()));
        assertThat(student).isPresent();
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_DEM_DATA__whenStudentExists_shouldUpdateStudentWithEventOutcome_DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var demStudent = createMockDemographicStudent("Y", "CSF");
        var studentFromApi = createmockStudent();
        graduationStudentRecordRepository.save(createMockGraduationStudentRecordEntity(UUID.fromString(studentFromApi.getStudentID()), UUID.fromString(demStudent.getSchoolID())));
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentFromApi);
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_DEM_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(demStudent))
                .build();
        var response = eventHandlerService.handleProcessStudentDemDataEvent(event);
        assertThat(response).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
    }

    private Student createmockStudent() {
        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setDob("1990-01-01");
        studentApiStudent.setStatusCode("A");
        return studentApiStudent;
    }

    private GraduationStudentRecordEntity createMockGraduationStudentRecordEntity(UUID studentID, UUID schoolID) {
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("123456789");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setStudentGrade("12");
        graduationStatusEntity.setProgram("2023-EN");
        graduationStatusEntity.setSchoolOfRecordId(schoolID);
        return graduationStatusEntity;
    }

    private DemographicStudent createMockDemographicStudent(String isSummerCollection, String schoolReportingRequirementCode) {
        return DemographicStudent.builder()
                .pen("123456789")
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .createUser("ABC")
                .updateUser("ABC")
                .grade("08")
                .birthdate("19900101")
                .citizenship("C")
                .gradRequirementYear("2023")
                .programCode1("AA")
                .programCode2("AB")
                .programCode3("AC")
                .programCode4("FR")
                .programCode5("DD")
                .studentStatus("A")
                .mincode("03636018")
                .schoolID(UUID.randomUUID().toString())
                .isSummerCollection(isSummerCollection)
                .schoolCertificateCompletionDate("2023-07-01")
                .schoolReportingRequirementCode(schoolReportingRequirementCode)
                .build();
    }

}
