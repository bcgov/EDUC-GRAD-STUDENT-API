package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCourseRepository;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import ca.bc.gov.educ.api.gradstudent.service.event.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.gradstudent.service.event.EventHandlerService;
import io.nats.client.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class EventHandlerDelegatorServiceTest extends BaseIntegrationTest {
    @Autowired
    EventHandlerService eventHandlerService;
    @Autowired
    RestUtils restUtils;
    @MockBean
    @Qualifier("studentApiClient")
    WebClient webClient;
    @Autowired
    GraduationStudentRecordRepository graduationStudentRecordRepository;
    @Autowired
    StudentCourseRepository studentCourseRepository;
    @Autowired
    GradStatusEventRepository gradStatusEventRepository;
    @MockBean
    GraduationStatusService graduationStatusService;
    @SpyBean
    private EventHandlerDelegatorService eventHandlerDelegatorService;
    @MockBean
    private Message message;
    @MockBean
    public ClientRegistrationRepository clientRegistrationRepository;
    @MockBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;
    @MockBean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private GradStatusEvent gradStatusEvent;
    private ChoreographedEvent choreographedEvent;

    private void setUpDefaultEvent() {
        UUID eventID = UUID.randomUUID();
        choreographedEvent = new ChoreographedEvent();
        choreographedEvent.setEventType(EventType.ASSESSMENT_STUDENT_UPDATE);
        choreographedEvent.setEventID(eventID.toString());
        choreographedEvent.setEventOutcome(EventOutcome.ASSESSMENT_STUDENT_UPDATED);
        choreographedEvent.setEventPayload("{\"studentId\":\"12345\",\"assessmentId\":\"67890\"}");
        gradStatusEvent = new GradStatusEvent();
        gradStatusEvent.setEventId(eventID);
        gradStatusEvent.setEventPayload(choreographedEvent.getEventPayload());
        gradStatusEvent.setEventType(EventType.ASSESSMENT_STUDENT_UPDATE.toString());
    }

    @Test
    void whenHandleChoreographyEvent_thenVerified() throws IOException {
        setUpDefaultEvent();
        when(graduationStatusService.eventExistsInDB(any())).thenReturn(Optional.empty());
        when(graduationStatusService.persistEventToDB(any())).thenReturn(gradStatusEvent);
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        verify(eventHandlerDelegatorService, times(1)).handleChoreographyEvent(this.choreographedEvent, this.message);
    }

    @Test
    void whenHandleChoreographyEvent_duplicate() throws IOException {
        setUpDefaultEvent();
        when(graduationStatusService.eventExistsInDB(any())).thenReturn(Optional.of(gradStatusEvent));
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        verify(eventHandlerDelegatorService, times(1)).handleChoreographyEvent(this.choreographedEvent, this.message);
    }



}