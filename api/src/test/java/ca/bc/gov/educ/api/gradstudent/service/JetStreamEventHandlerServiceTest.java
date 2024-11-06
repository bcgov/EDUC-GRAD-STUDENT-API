package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.gradstudent.constant.EventStatus.MESSAGE_PUBLISHED;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class JetStreamEventHandlerServiceTest {

    @Autowired
    EducGradStudentApiConstants constants;

    @Autowired
    JetStreamEventHandlerService jetStreamEventHandlerService;

    @MockBean CommonService commonService;

    @MockBean
    GradStatusEventRepository gradStatusEventRepository;

    @Autowired
    WebClient webClient;

    @Autowired
    FetchGradStatusSubscriber fetchGradStatusSubscriber;


    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testUpdateEventStatus() {
        UUID eventId = UUID.randomUUID();

        ChoreographedEvent choreographedEvent = new ChoreographedEvent();
        choreographedEvent.setEventID(eventId.toString());

        GradStatusEvent gradStatusEvent = new GradStatusEvent();
        gradStatusEvent.setEventId(eventId);
        gradStatusEvent.setEventStatus(DB_COMMITTED.toString());
        gradStatusEvent.setEventType("UPDATE_GRAD_STATUS");

        GradStatusEvent responseGradStatusEvent = new GradStatusEvent();
        responseGradStatusEvent.setEventId(eventId);
        responseGradStatusEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
        responseGradStatusEvent.setEventType("UPDATE_GRAD_STATUS");

        Mockito.when(gradStatusEventRepository.findById(eventId)).thenReturn(Optional.of(gradStatusEvent));
        Mockito.when(gradStatusEventRepository.save(gradStatusEvent)).thenReturn(responseGradStatusEvent);

        jetStreamEventHandlerService.updateEventStatus(choreographedEvent);
    }

}
